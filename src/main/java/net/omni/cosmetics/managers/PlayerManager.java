package net.omni.cosmetics.managers;

import net.omni.cosmetics.OmniCosmetics;
import net.omni.cosmetics.player.CosmeticsPlayer;

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

    public CompletableFuture<CosmeticsPlayer> loadPlayer(UUID uuid) {
        return plugin.getDatabaseManager().loadPlayer(uuid)
                .thenApply(player -> {
                    players.put(uuid, player);
                    return player;
                });
    }

    public void unloadPlayer(UUID uuid) {
        CosmeticsPlayer player = players.remove(uuid);
        if (player != null)
            plugin.getDatabaseManager().savePlayer(player);
    }

    public CosmeticsPlayer getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    public void saveAll() {
        for (CosmeticsPlayer player : players.values())
            plugin.getDatabaseManager().savePlayerSync(player);
    }

    public Map<UUID, CosmeticsPlayer> getPlayers() {
        return players;
    }
}
