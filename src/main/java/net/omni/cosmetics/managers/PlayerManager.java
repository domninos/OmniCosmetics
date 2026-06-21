package net.omni.cosmetics.managers;

import net.omni.cosmetics.OmniCosmetics;
import net.omni.cosmetics.player.CosmeticsPlayer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {

    private final OmniCosmetics plugin;
    private final Map<UUID, CosmeticsPlayer> players = new ConcurrentHashMap<>();

    public PlayerManager(OmniCosmetics plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<CosmeticsPlayer> loadPlayer(Player player) {
        return plugin.getDatabaseManager().loadPlayer(player)
                .thenApply(cosmeticsPlayer -> {
                    players.put(player.getUniqueId(), cosmeticsPlayer);
                    return cosmeticsPlayer;
                });
    }

    public void unloadPlayer(UUID uuid) {
        CosmeticsPlayer player = players.remove(uuid);

        if (player != null && player.isDirty()) {
            plugin.getDatabaseManager().savePlayer(player);
            player.markClean();
        }
    }

    public CosmeticsPlayer getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    public void saveAll() {
        plugin.getDatabaseManager().saveAllSync(players.values());
    }

    public Map<UUID, CosmeticsPlayer> getPlayers() {
        return players;
    }

    public void flush() {
        players.clear();
    }
}
