package net.omni.cosmetics.listeners;

import net.omni.cosmetics.OmniCosmetics;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final OmniCosmetics plugin;

    public PlayerListener(OmniCosmetics plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // TODO load db to cache
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // remove from cache, save to DB
    }

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

}
