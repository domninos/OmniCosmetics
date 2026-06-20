package net.omni.cosmetics.managers;

import net.omni.cosmetics.OmniCosmetics;
import net.omni.cosmetics.effect.Cosmetic;
import net.omni.cosmetics.effect.CosmeticCategory;
import net.omni.cosmetics.effect.chat.CosmeticsChatColor;
import net.omni.cosmetics.effect.chat.CosmeticsPin;
import net.omni.cosmetics.effect.chat.CosmeticsTag;
import net.omni.cosmetics.effect.trails.BlockTrail;
import net.omni.cosmetics.effect.trails.ParticleTrail;
import net.omni.cosmetics.player.CosmeticsPlayer;
import net.omni.cosmetics.util.ChatRenderer;
import net.omni.cosmetics.util.config.ConfigUtil;
import net.omni.cosmetics.util.config.MessageUtil;
import net.omni.cosmetics.util.config.Messages;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GUIManager {

    private final OmniCosmetics plugin;
    final Map<UUID, CosmeticCategory> openCategories = new HashMap<>();
    private final Set<UUID> openGuis = new HashSet<>();

    public GUIManager(OmniCosmetics plugin) {
        this.plugin = plugin;
    }

    public CosmeticCategory getOpenCategory(Player player) {
        return openCategories.get(player.getUniqueId());
    }

    public void setOpenCategory(Player player, CosmeticCategory category) {
        if (category == null) openCategories.remove(player.getUniqueId());
        else openCategories.put(player.getUniqueId(), category);
    }

    public boolean isInGui(Player player) {
        return openGuis.contains(player.getUniqueId());
    }

    public void removeFromGui(UUID uuid) {
        openGuis.remove(uuid);
    }

    public void openMenu(Player player) {
        setOpenCategory(player, null);
        ConfigUtil config = plugin.getConfigUtil();
        ChatRenderer renderer = MessageUtil.getRenderer();
        Inventory inv = renderer.createInventory(null, config.getGuiMainMenuSize(), config.getGuiMainMenuTitle());

        for (ConfigUtil.MainMenuItem item : config.getGuiMainMenuItems()) {
            inv.setItem(item.slot(), createCategoryItem(item.material(), item.name(), item.lore()));
        }

        player.openInventory(inv);
        openGuis.add(player.getUniqueId());
    }

    private ItemStack createCategoryItem(Material material, String name, List<String> lore) {
        ChatRenderer renderer = MessageUtil.getRenderer();
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        renderer.setDisplayName(meta, name);
        renderer.setLore(meta, lore);
        item.setItemMeta(meta);
        return item;
    }

    public void openCategory(Player player, CosmeticCategory category) {
        List<? extends Cosmetic> cosmetics = getFiltered(category, player);

        if (cosmetics.isEmpty()) {
            plugin.sendMessage(player, Messages.NO_COSMETICS.toString());
            return;
        }

        ChatRenderer renderer = MessageUtil.getRenderer();
        int size = ((cosmetics.size() / 9) + 1) * 9;
        if (size < 27) size = 27;

        String title = switch (category) {
            case PARTICLE_TRAIL -> "  <#AAFFAA>Particle Trails</#AAFFAA>";
            case BLOCK_TRAIL -> "  <#FFAA00>Block Trails</#FFAA00>";
            case TAG -> "  <#55FFFF>Tags</#55FFFF>";
            case PIN -> "  <#FF55FF>Pins</#FF55FF>";
            case CHAT_COLOR -> "  <#FF5555>Chat Colors</#FF5555>";
        };

        Inventory inv = renderer.createInventory(null, size, title);

        CosmeticsPlayer cp = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        for (int i = 0; i < cosmetics.size(); i++) {
            Cosmetic cosmetic = cosmetics.get(i);
            ItemStack item = new ItemStack(Material.valueOf(getItemType(cosmetic)));
            ItemMeta meta = item.getItemMeta();
            renderer.setDisplayName(meta, cosmetic.getItemName());
            List<String> lore = new ArrayList<>(cosmetic.getItemLore());
            lore.add("");
            if (isEquipped(cp, cosmetic)) {
                lore.add("<green>\u2713 Equipped</green>");
            }
            renderer.setLore(meta, lore);
            item.setItemMeta(meta);
            inv.setItem(i, item);
        }

        setOpenCategory(player, category);
        player.openInventory(inv);
        openGuis.add(player.getUniqueId());
    }

    private boolean isEquipped(CosmeticsPlayer cp, Cosmetic cosmetic) {
        if (cp == null) return false;
        return switch (cosmetic.getCategory()) {
            case PARTICLE_TRAIL -> cp.getActiveParticleTrail() != null && cp.getActiveParticleTrail().getName().equalsIgnoreCase(cosmetic.getName());
            case BLOCK_TRAIL -> cp.getActiveBlockTrail() != null && cp.getActiveBlockTrail().getName().equalsIgnoreCase(cosmetic.getName());
            case TAG -> cp.getActiveTag() != null && cp.getActiveTag().getName().equalsIgnoreCase(cosmetic.getName());
            case PIN -> cp.getActivePin() != null && cp.getActivePin().getName().equalsIgnoreCase(cosmetic.getName());
            case CHAT_COLOR -> cp.getActiveChatColor() != null && cp.getActiveChatColor().getName().equalsIgnoreCase(cosmetic.getName());
        };
    }

    private String getItemType(Cosmetic cosmetic) {
        if (cosmetic.getItemName() != null) {
            return switch (cosmetic.getCategory()) {
                case PARTICLE_TRAIL -> "FIREWORK_STAR";
                case BLOCK_TRAIL -> "GRASS_BLOCK";
                case TAG -> "NAME_TAG";
                case PIN -> "PLAYER_HEAD";
                case CHAT_COLOR -> "RED_DYE";
            };
        }
        return "BARRIER";
    }

    public List<? extends Cosmetic> getFiltered(CosmeticCategory category, Player player) {
        CosmeticsManager cm = plugin.getCosmeticsManager();
        return switch (category) {
            case PARTICLE_TRAIL -> cm.getParticleTrails().stream()
                    .filter(c -> player.hasPermission(c.getPermission()))
                    .toList();
            case BLOCK_TRAIL -> cm.getBlockTrails().stream()
                    .filter(c -> player.hasPermission(c.getPermission()))
                    .toList();
            case TAG -> cm.getTags().stream()
                    .filter(c -> player.hasPermission(c.getPermission()))
                    .toList();
            case PIN -> cm.getPins().stream()
                    .filter(c -> player.hasPermission(c.getPermission()))
                    .toList();
            case CHAT_COLOR -> cm.getChatColors().stream()
                    .filter(c -> player.hasPermission(c.getPermission()))
                    .toList();
        };
    }
}
