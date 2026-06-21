package net.omni.cosmetics.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.omni.cosmetics.OmniCosmetics;
import net.omni.cosmetics.player.CosmeticsPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
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
                             "active_block_trail_name TEXT," +
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

    public CompletableFuture<CosmeticsPlayer> loadPlayer(Player player) {
        CompletableFuture<CosmeticsPlayer> future = new CompletableFuture<>();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String query = "SELECT active_trail_name, active_block_trail_name, active_tag, active_pin, active_chat_color FROM player_cosmetics WHERE uuid = ?";

            try (Connection connection = getConnection();
                 PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, player.getUniqueId().toString());

                CosmeticsPlayer cosmeticsPlayer = new CosmeticsPlayer(player);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String trailName = rs.getString("active_trail_name");
                        String blockTrailName = rs.getString("active_block_trail_name");
                        String tagName = rs.getString("active_tag");
                        String pinName = rs.getString("active_pin");
                        String colorName = rs.getString("active_chat_color");

                        if (trailName != null && !trailName.isEmpty())
                            cosmeticsPlayer.setActiveParticleTrail(plugin.getCosmeticsManager().getParticleTrail(trailName));
                        if (blockTrailName != null && !blockTrailName.isEmpty())
                            cosmeticsPlayer.setActiveBlockTrail(plugin.getCosmeticsManager().getBlockTrail(blockTrailName));
                        if (tagName != null && !tagName.isEmpty())
                            cosmeticsPlayer.setActiveTag(plugin.getCosmeticsManager().getTag(tagName));
                        if (pinName != null && !pinName.isEmpty())
                            cosmeticsPlayer.setActivePin(plugin.getCosmeticsManager().getPin(pinName));
                        if (colorName != null && !colorName.isEmpty())
                            cosmeticsPlayer.setActiveChatColor(plugin.getCosmeticsManager().getChatColor(colorName));
                    }
                }

                cosmeticsPlayer.markClean();
                future.complete(cosmeticsPlayer);
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
        String query = "INSERT OR REPLACE INTO player_cosmetics (uuid, active_trail_name, active_block_trail_name, active_tag, active_pin, active_chat_color) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, player.getUUID().toString());
            stmt.setString(2, player.getActiveParticleTrail() != null ? player.getActiveParticleTrail().getName() : null);
            stmt.setString(3, player.getActiveBlockTrail() != null ? player.getActiveBlockTrail().getName() : null);
            stmt.setString(4, player.getActiveTag() != null ? player.getActiveTag().getName() : null);
            stmt.setString(5, player.getActivePin() != null ? player.getActivePin().getName() : null);
            stmt.setString(6, player.getActiveChatColor() != null ? player.getActiveChatColor().getName() : null);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error saving player cosmetics to database!", e);
        }
    }

    public void saveAllSync(Collection<CosmeticsPlayer> players) {
        String query = "INSERT OR REPLACE INTO player_cosmetics (uuid, active_trail_name, active_block_trail_name, active_tag, active_pin, active_chat_color) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);

            for (CosmeticsPlayer player : players) {
                if (!player.isDirty())
                    continue;

                stmt.setString(1, player.getUUID().toString());
                stmt.setString(2, player.getActiveParticleTrail() != null ? player.getActiveParticleTrail().getName() : null);
                stmt.setString(3, player.getActiveBlockTrail() != null ? player.getActiveBlockTrail().getName() : null);
                stmt.setString(4, player.getActiveTag() != null ? player.getActiveTag().getName() : null);
                stmt.setString(5, player.getActivePin() != null ? player.getActivePin().getName() : null);
                stmt.setString(6, player.getActiveChatColor() != null ? player.getActiveChatColor().getName() : null);
                stmt.addBatch();

                player.markClean();
            }

            stmt.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error batch saving players!", e);
        }
    }

    public void closePool() {
        if (dataSource != null && !dataSource.isClosed())
            dataSource.close();
    }
}
