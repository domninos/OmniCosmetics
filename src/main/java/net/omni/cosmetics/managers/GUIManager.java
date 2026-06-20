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

    public static final int[] CONTENT_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    private final OmniCosmetics plugin;
    final Map<UUID, CosmeticCategory> openCategories = new HashMap<>();
    private final Set<UUID> openGuis = new HashSet<>();
    private final Map<UUID, Integer> currentPages = new HashMap<>();

    public GUIManager(OmniCosmetics plugin) {
        this.plugin = plugin;
    }

    public CosmeticCategory getOpenCategory(Player player) {
        return openCategories.get(player.getUniqueId());
    }

    public void setOpenCategory(Player player, CosmeticCategory category) {
        if (category == null) {
            openCategories.remove(player.getUniqueId());
            currentPages.remove(player.getUniqueId());
        } else {
            openCategories.put(player.getUniqueId(), category);
        }
    }

    public boolean isInGui(Player player) {
        return openGuis.contains(player.getUniqueId());
    }

    public void removeFromGui(UUID uuid) {
        openGuis.remove(uuid);
    }

    public int getCurrentPage(Player player) {
        return currentPages.getOrDefault(player.getUniqueId(), 0);
    }

    public void setCurrentPage(Player player, int page) {
        if (page < 0) page = 0;
        currentPages.put(player.getUniqueId(), page);
    }

    public void openMenu(Player player) {
        setOpenCategory(player, null);
        ConfigUtil config = plugin.getConfigUtil();
        ChatRenderer renderer = MessageUtil.getRenderer();

        Inventory inv = renderer.createInventory(null, config.getGuiMainMenuSize(), config.getGuiMainMenuTitle());
        fillBorder(inv);

        ConfigUtil.GuiButton exit = config.getGuiMainMenuExit();
        inv.setItem(exit.slot(), createGuiItem(exit.material(), exit.name(), exit.lore()));

        for (ConfigUtil.MainMenuItem item : config.getGuiMainMenuItems()) {
            inv.setItem(item.slot(), createCategoryItem(item.material(), item.name(), item.lore()));
        }

        player.openInventory(inv);
        openGuis.add(player.getUniqueId());
    }

    public void openCategory(Player player, CosmeticCategory category) {
        openCategory(player, category, 0);
    }

    public void openCategory(Player player, CosmeticCategory category, int page) {
        List<? extends Cosmetic> cosmetics = getFiltered(category, player);

        if (cosmetics.isEmpty()) {
            plugin.sendMessage(player, Messages.NO_COSMETICS.toString());
            return;
        }

        ConfigUtil config = plugin.getConfigUtil();
        ChatRenderer renderer = MessageUtil.getRenderer();

        boolean paginate = cosmetics.size() > config.getPaginationThreshold();
        int totalPages = paginate ? Math.max(1, (int) Math.ceil((double) cosmetics.size() / CONTENT_SLOTS.length)) : 1;
        if (page >= totalPages) page = totalPages - 1;
        if (page < 0) page = 0;

        setOpenCategory(player, category);
        setCurrentPage(player, page);

        String title = config.getGuiCategoryTitle()
                .replace("%category%", categoryLabel(category))
                .replace("%page%", String.valueOf(page + 1))
                .replace("%max%", String.valueOf(totalPages));

        int size = config.getGuiCategorySize();
        Inventory inv = renderer.createInventory(null, size, title);
        fillBorder(inv);

        ConfigUtil.GuiButton back = config.getGuiCategoryBack();
        inv.setItem(back.slot(), createGuiItem(back.material(), back.name(), back.lore()));

        int start = page * CONTENT_SLOTS.length;
        int end = Math.min(start + CONTENT_SLOTS.length, cosmetics.size());
        CosmeticsPlayer cp = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        for (int i = start; i < end; i++) {
            Cosmetic cosmetic = cosmetics.get(i);
            Material mat;
            try {
                mat = Material.valueOf(getItemType(cosmetic));
            } catch (IllegalArgumentException e) {
                mat = Material.BARRIER;
            }
            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            renderer.setDisplayName(meta, cosmetic.getItemName());
            List<String> lore = new ArrayList<>(cosmetic.getItemLore());
            lore.add("");
            if (isEquipped(cp, cosmetic)) {
                lore.add("<green>\u2713 Equipped</green>");
            }
            renderer.setLore(meta, lore);
            item.setItemMeta(meta);
            inv.setItem(CONTENT_SLOTS[i - start], item);
        }

        for (int i = end - start; i < CONTENT_SLOTS.length; i++) {
            ConfigUtil.GuiButton placeholder = config.getGuiPlaceholder();
            inv.setItem(CONTENT_SLOTS[i], createGuiItem(placeholder.material(), placeholder.name(), placeholder.lore()));
        }

        if (paginate) {
            boolean isFirst = page == 0;
            boolean isLast = page >= totalPages - 1;

            ConfigUtil.NavButton prevCfg = config.getGuiCategoryPrevious();
            if (isFirst) {
                inv.setItem(prevCfg.slot(), createGuiItem(prevCfg.disabledMaterial(), prevCfg.disabledName(), prevCfg.disabledLore()));
            } else {
                inv.setItem(prevCfg.slot(), createGuiItem(prevCfg.material(), prevCfg.name(), prevCfg.lore()));
            }

            ConfigUtil.NavButton nextCfg = config.getGuiCategoryNext();
            if (isLast) {
                inv.setItem(nextCfg.slot(), createGuiItem(nextCfg.disabledMaterial(), nextCfg.disabledName(), nextCfg.disabledLore()));
            } else {
                inv.setItem(nextCfg.slot(), createGuiItem(nextCfg.material(), nextCfg.name(), nextCfg.lore()));
            }
        }

        player.openInventory(inv);
        openGuis.add(player.getUniqueId());
    }

    private void fillBorder(Inventory inv) {
        ConfigUtil config = plugin.getConfigUtil();
        ConfigUtil.GuiButton filler = config.getGuiFiller();
        ItemStack fillerItem = createGuiItem(filler.material(), filler.name(), filler.lore());

        Set<Integer> skip = new HashSet<>();
        skip.add(config.getGuiMainMenuExit().slot());
        skip.add(config.getGuiCategoryBack().slot());
        skip.add(config.getGuiCategoryPrevious().slot());
        skip.add(config.getGuiCategoryNext().slot());

        int rows = inv.getSize() / 9;
        for (int slot = 0; slot < inv.getSize(); slot++) {
            if (skip.contains(slot)) continue;
            if (isBorderSlot(slot, rows)) {
                inv.setItem(slot, fillerItem);
            }
        }
    }

    private boolean isBorderSlot(int slot, int rows) {
        int row = slot / 9;
        int col = slot % 9;
        return row == 0 || row == rows - 1 || col == 0 || col == 8;
    }

    private ItemStack createGuiItem(Material material, String name, List<String> lore) {
        ChatRenderer renderer = MessageUtil.getRenderer();
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        renderer.setDisplayName(meta, name);
        renderer.setLore(meta, lore);
        item.setItemMeta(meta);
        return item;
    }

    private String categoryLabel(CosmeticCategory category) {
        return switch (category) {
            case PARTICLE_TRAIL -> "Particle Trails";
            case BLOCK_TRAIL -> "Block Trails";
            case TAG -> "Tags";
            case PIN -> "Pins";
            case CHAT_COLOR -> "Chat Colors";
        };
    }

    private ItemStack createCategoryItem(Material material, String name, List<String> lore) {
        return createGuiItem(material, name, lore);
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
        String type = cosmetic.getItemType();
        if (type != null) return type;
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
