package net.omni.cosmetics.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.omni.cosmetics.OmniCosmetics;
import net.omni.cosmetics.player.CosmeticsPlayer;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class DatabaseManager {

    private final OmniCosmetics plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(OmniCosmetics plugin) {
        this.plugin = plugin;
    }

    public void initDatabase() {
        File dbFile = new File(plugin.getDataFolder(), "cosmetics.db");

        if (!dbFile.exists()) {
            try {
                if (dbFile.createNewFile())
                    plugin.getLogger().log(Level.INFO, "Successfully created cosmetics.db!");
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create cosmetics.db!", e);
            }
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        config.setPoolName("OmniCosmeticsPool");
        config.setMaximumPoolSize(1);
        config.setConnectionTimeout(5000);
        config.setLeakDetectionThreshold(2000);

        this.dataSource = new HikariDataSource(config);

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS player_cosmetics (" +
                             "uuid TEXT PRIMARY KEY," +
                             "active_trail_name TEXT," +
                             "active_tag TEXT," +
                             "active_pin TEXT," +
                             "active_chat_color TEXT)")) {
            stmt.execute();
            try (PreparedStatement wal = conn.prepareStatement("PRAGMA journal_mode=WAL")) {
                wal.execute();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while creating the database!", e);
        }

        plugin.sendConsole("<green>Successfully initialized database.</green>");
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null)
            throw new SQLException("DataSource not initialized");
        return dataSource.getConnection();
    }

    public CompletableFuture<CosmeticsPlayer> loadPlayer(UUID uuid) {
        CompletableFuture<CosmeticsPlayer> future = new CompletableFuture<>();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String query = "SELECT active_trail_name, active_tag, active_pin, active_chat_color FROM player_cosmetics WHERE uuid = ?";

            try (Connection connection = getConnection();
                 PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());

                CosmeticsPlayer player = new CosmeticsPlayer(uuid);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String trailName = rs.getString("active_trail_name");
                        String tagName = rs.getString("active_tag");
                        String pinName = rs.getString("active_pin");
                        String colorName = rs.getString("active_chat_color");

                        if (trailName != null && !trailName.isEmpty()) {
                            player.setActiveParticleTrail(plugin.getCosmeticsManager().getParticleTrail(trailName));
                            player.setActiveBlockTrail(plugin.getCosmeticsManager().getBlockTrail(trailName));
                        }
                        if (tagName != null && !tagName.isEmpty())
                            player.setActiveTag(plugin.getCosmeticsManager().getTag(tagName));
                        if (pinName != null && !pinName.isEmpty())
                            player.setActivePin(plugin.getCosmeticsManager().getPin(pinName));
                        if (colorName != null && !colorName.isEmpty())
                            player.setActiveChatColor(plugin.getCosmeticsManager().getChatColor(colorName));
                    }
                }

                future.complete(player);
            } catch (SQLException e) {
                future.completeExceptionally(e);
                plugin.getLogger().log(Level.SEVERE, "Error loading player cosmetics from database!", e);
            }
        });

        return future;
    }

    public void savePlayer(CosmeticsPlayer player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> savePlayerSync(player));
    }

    public void savePlayerSync(CosmeticsPlayer player) {
        String query = "INSERT OR REPLACE INTO player_cosmetics (uuid, active_trail_name, active_tag, active_pin, active_chat_color) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, player.getUUID().toString());

            String trailName = null;
            if (player.getActiveParticleTrail() != null)
                trailName = player.getActiveParticleTrail().getName();
            else if (player.getActiveBlockTrail() != null)
                trailName = player.getActiveBlockTrail().getName();
            stmt.setString(2, trailName);

            stmt.setString(3, player.getActiveTag() != null ? player.getActiveTag().getName() : null);
            stmt.setString(4, player.getActivePin() != null ? player.getActivePin().getName() : null);
            stmt.setString(5, player.getActiveChatColor() != null ? player.getActiveChatColor().getName() : null);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error saving player cosmetics to database!", e);
        }
    }

    public void closePool() {
        if (dataSource != null && !dataSource.isClosed())
            dataSource.close();
    }
}
