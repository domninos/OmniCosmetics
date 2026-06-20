package net.omni.cosmetics.listeners;

import net.omni.cosmetics.OmniCosmetics;
import net.omni.cosmetics.effect.Cosmetic;
import net.omni.cosmetics.effect.CosmeticCategory;
import net.omni.cosmetics.effect.chat.CosmeticsChatColor;
import net.omni.cosmetics.effect.chat.CosmeticsPin;
import net.omni.cosmetics.effect.chat.CosmeticsTag;
import net.omni.cosmetics.effect.trails.BlockTrail;
import net.omni.cosmetics.effect.trails.ParticleTrail;
import net.omni.cosmetics.managers.GUIManager;
import net.omni.cosmetics.player.CosmeticsPlayer;
import net.omni.cosmetics.util.config.Messages;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.List;

public class GUIListener implements Listener {

    private final OmniCosmetics plugin;

    public GUIListener(OmniCosmetics plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!plugin.getGuiManager().isInGui(player)) return;
        if (event.getCurrentItem() == null) return;

        event.setCancelled(true);
        int slot = event.getSlot();

        GUIManager guiManager = plugin.getGuiManager();
        CosmeticCategory category = guiManager.getOpenCategory(player);

        if (category == null) {
            CosmeticCategory cat = plugin.getConfigUtil().getCategoryBySlot(slot);
            if (cat != null)
                guiManager.openCategory(player, cat);
            return;
        }

        List<? extends Cosmetic> cosmetics = guiManager.getFiltered(category, player);
        if (slot < 0 || slot >= cosmetics.size()) return;

        Cosmetic selected = cosmetics.get(slot);
        CosmeticsPlayer cp = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        if (cp == null) return;

        switch (category) {
            case PARTICLE_TRAIL -> {
                if (cp.getActiveParticleTrail() != null && cp.getActiveParticleTrail().getName().equalsIgnoreCase(selected.getName()))
                    cp.setActiveParticleTrail(null);
                else
                    cp.setActiveParticleTrail((ParticleTrail) selected);
            }
            case BLOCK_TRAIL -> {
                if (cp.getActiveBlockTrail() != null && cp.getActiveBlockTrail().getName().equalsIgnoreCase(selected.getName()))
                    cp.setActiveBlockTrail(null);
                else
                    cp.setActiveBlockTrail((BlockTrail) selected);
            }
            case TAG -> {
                if (cp.getActiveTag() != null && cp.getActiveTag().getName().equalsIgnoreCase(selected.getName()))
                    cp.setActiveTag(null);
                else
                    cp.setActiveTag((CosmeticsTag) selected);
            }
            case PIN -> {
                if (cp.getActivePin() != null && cp.getActivePin().getName().equalsIgnoreCase(selected.getName()))
                    cp.setActivePin(null);
                else
                    cp.setActivePin((CosmeticsPin) selected);
            }
            case CHAT_COLOR -> {
                if (cp.getActiveChatColor() != null && cp.getActiveChatColor().getName().equalsIgnoreCase(selected.getName()))
                    cp.setActiveChatColor(null);
                else
                    cp.setActiveChatColor((CosmeticsChatColor) selected);
            }
        }

        plugin.getDatabaseManager().savePlayer(cp);
        player.closeInventory();
        plugin.sendMessage(player, Messages.COSMETIC_EQUIPPED.replace("name", selected.getName()));
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player)
            plugin.getGuiManager().removeFromGui(player.getUniqueId());
    }
}
