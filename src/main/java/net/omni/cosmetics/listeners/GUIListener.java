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
import net.omni.cosmetics.util.config.ConfigUtil;
import net.omni.cosmetics.util.config.Messages;
import org.bukkit.Bukkit;
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
        if (!(event.getWhoClicked() instanceof Player player))
            return;

        if (!plugin.getGuiManager().isInGui(player))
            return;

        if (event.getCurrentItem() == null)
            return;

        event.setCancelled(true);
        int slot = event.getSlot();

        GUIManager guiManager = plugin.getGuiManager();
        ConfigUtil config = plugin.getConfigUtil();
        CosmeticCategory category = guiManager.getOpenCategory(player);

        if (category == null)
            handleMainMenuClick(player, guiManager, config, slot);
        else
            handleCategoryClick(player, guiManager, config, category, slot);
    }

    private void handleMainMenuClick(Player player, GUIManager guiManager, ConfigUtil config, int slot) {
        if (slot == config.getGuiMainMenuExit().slot()) {
            player.closeInventory();
            return;
        }

        CosmeticCategory cat = config.getCategoryBySlot(slot);
        if (cat != null)
            guiManager.openCategory(player, cat);
    }

    private void handleCategoryClick(Player player, GUIManager guiManager, ConfigUtil config, CosmeticCategory category, int slot) {
        int backSlot = config.getGuiCategoryBack().slot();
        int prevSlot = config.getGuiCategoryPrevious().slot();
        int nextSlot = config.getGuiCategoryNext().slot();

        if (slot == backSlot) {
            guiManager.openMenu(player);
            return;
        }

        if (slot == prevSlot) {
            int page = guiManager.getCurrentPage(player);

            if (page > 0)
                guiManager.openCategory(player, category, page - 1);

            return;
        }

        if (slot == nextSlot) {
            List<? extends Cosmetic> cosmetics = guiManager.getFiltered(category, player);

            int totalPages = Math.max(1, (int) Math.ceil((double) cosmetics.size() / GUIManager.CONTENT_SLOTS.length));
            int page = guiManager.getCurrentPage(player);

            if (page < totalPages - 1)
                guiManager.openCategory(player, category, page + 1);

            return;
        }

        int contentIndex = -1;
        for (int i = 0; i < GUIManager.CONTENT_SLOTS.length; i++) {
            if (GUIManager.CONTENT_SLOTS[i] == slot) {
                contentIndex = i;
                break;
            }
        }

        if (contentIndex < 0)
            return;

        int page = guiManager.getCurrentPage(player);
        List<? extends Cosmetic> cosmetics = guiManager.getFiltered(category, player);

        int cosmeticIndex = page * GUIManager.CONTENT_SLOTS.length + contentIndex;

        if (cosmeticIndex >= cosmetics.size())
            return;

        Cosmetic selected = cosmetics.get(cosmeticIndex);
        CosmeticsPlayer cp = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (cp == null)
            return;

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
        plugin.getGuiManager().removeFromGui(event.getPlayer().getUniqueId());
    }


    public void register() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
}
