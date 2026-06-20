package net.omni.cosmetics.listeners;

import net.omni.cosmetics.OmniCosmetics;
import net.omni.cosmetics.managers.BlockTrailManager;
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
        BlockTrailManager.BlockKey key = BlockTrailManager.BlockKey.fromLocation(
                event.getPlayer().getUniqueId(), event.getBlock().getLocation()
        );
        if (plugin.getBlockTrailManager().isFakeBlock(key))
            event.setCancelled(true);
    }
}
