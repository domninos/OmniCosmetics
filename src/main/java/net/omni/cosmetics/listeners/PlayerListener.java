package net.omni.cosmetics.listeners;

import net.omni.cosmetics.OmniCosmetics;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.logging.Level;

public class PlayerListener implements Listener {

    private final OmniCosmetics plugin;

    public PlayerListener(OmniCosmetics plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getPlayerManager().loadPlayer(event.getPlayer())
                .exceptionally(throwable -> {
                    plugin.getLogger().log(Level.WARNING, "Failed to load player data", throwable);
                    return null;
                });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        plugin.getPlayerManager().unloadPlayer(uuid);
        plugin.getBlockTrailManager().handleQuit(uuid);
        plugin.getGuiManager().removeFromGui(uuid);
        plugin.getBenchmarkManager().stopBenchmark(uuid);
    }

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
}
