package net.omni.cosmetics.listeners;

import net.omni.cosmetics.OmniCosmetics;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {

    private final OmniCosmetics plugin;

    public BlockBreakListener(OmniCosmetics plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (plugin.getBlockTrailManager().isFakeBlock(event.getBlock()))
            event.setCancelled(true);
    }

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
}
