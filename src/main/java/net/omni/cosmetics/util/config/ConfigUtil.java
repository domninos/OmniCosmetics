package net.omni.cosmetics.util.config;

import net.omni.cosmetics.OmniCosmetics;
import net.omni.cosmetics.effect.CosmeticCategory;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

public class ConfigUtil {

    public record MainMenuItem(int slot, Material material, CosmeticCategory category, String name, List<String> lore) {
        public static MainMenuItem fromSection(ConfigurationSection section) {
            String catName = section.getString("category");
            if (catName == null) return null;
            CosmeticCategory category;
            try {
                category = CosmeticCategory.valueOf(catName.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
            int slot = section.getInt("slot");
            String matName = section.getString("material", "BARRIER");
            Material material;
            try {
                material = Material.valueOf(matName.toUpperCase());
            } catch (IllegalArgumentException e) {
                material = Material.BARRIER;
            }
            String name = section.getString("name", category.name());
            List<String> lore = section.getStringList("lore");
            if (lore == null) lore = List.of();
            return new MainMenuItem(slot, material, category, name, lore);
        }
    }

    private final OmniCosmetics plugin;

    private int trailInterval;
    private int trailRenderDistance;
    private boolean particleTrailsEnabled;
    private boolean blockTrailsEnabled;

    private String guiMainMenuTitle;
    private int guiMainMenuSize;
    private Map<Integer, MainMenuItem> guiMainMenuItems;

    public ConfigUtil(OmniCosmetics plugin) {
        this.plugin = plugin;
    }

    public void reloadConfig() {
        flush();
        plugin.reloadConfig();
        load();
    }

    public void flush() {
    }

    public void load() {
        AtomicInteger savedDefaults = new AtomicInteger();

        this.trailInterval = getAndDefaultInt("trail-interval", 5, savedDefaults::getAndAdd);
        this.trailRenderDistance = getAndDefaultInt("trail-render-distance", 32, savedDefaults::getAndAdd);
        this.particleTrailsEnabled = plugin.getConfig().getBoolean("particle-trails-enabled", true);
        this.blockTrailsEnabled = plugin.getConfig().getBoolean("block-trails-enabled", true);

        loadGuiMainMenu(savedDefaults);

        if (savedDefaults.get() > 0) {
            plugin.saveConfig();
            plugin.sendConsole("<green>Successfully loaded " + savedDefaults.get() + " default configuration(s)</green>");
        }

        plugin.sendConsole("<green>Successfully loaded config.yml</green>");
    }

    private void loadGuiMainMenu(AtomicInteger savedDefaults) {
        String base = "gui.main-menu";
        this.guiMainMenuTitle = getAndDefaultString(base + ".title", "  <#AAFFAA>Cosmetics</#AAFFAA>", savedDefaults::getAndAdd);
        this.guiMainMenuSize = getAndDefaultInt(base + ".size", 27, savedDefaults::getAndAdd);

        this.guiMainMenuItems = new HashMap<>();
        List<Map<?, ?>> itemsList = plugin.getConfig().getMapList(base + ".items");
        if (!itemsList.isEmpty()) {
            for (int i = 0; i < itemsList.size(); i++) {
                ConfigurationSection section = plugin.getConfig().getConfigurationSection(base + ".items." + i);
                if (section == null) continue;
                MainMenuItem item = MainMenuItem.fromSection(section);
                if (item != null) {
                    guiMainMenuItems.put(item.slot(), item);
                }
            }
        }

        if (guiMainMenuItems.isEmpty()) {
            guiMainMenuItems = defaultMainMenuItems();
        }
    }

    private Map<Integer, MainMenuItem> defaultMainMenuItems() {
        Map<Integer, MainMenuItem> items = new HashMap<>();
        items.put(10, new MainMenuItem(10, Material.FIREWORK_STAR, CosmeticCategory.PARTICLE_TRAIL,
                "<#AAFFAA>Particle Trails</#AAFFAA>",
                List.of("<gray>Particle effects that follow you</gray>")));
        items.put(12, new MainMenuItem(12, Material.GRASS_BLOCK, CosmeticCategory.BLOCK_TRAIL,
                "<#FFAA00>Block Trails</#FFAA00>",
                List.of("<gray>Block trails that follow you</gray>")));
        items.put(14, new MainMenuItem(14, Material.NAME_TAG, CosmeticCategory.TAG,
                "<#55FFFF>Tags</#55FFFF>",
                List.of("<gray>Name tags for chat</gray>")));
        items.put(16, new MainMenuItem(16, Material.PLAYER_HEAD, CosmeticCategory.PIN,
                "<#FF55FF>Pins</#FF55FF>",
                List.of("<gray>Emoji pins for chat</gray>")));
        items.put(22, new MainMenuItem(22, Material.RED_DYE, CosmeticCategory.CHAT_COLOR,
                "<#FF5555>Chat Colors</#FF5555>",
                List.of("<gray>Chat message colors</gray>")));
        return items;
    }

    private int getAndDefaultInt(String path, int defaultVal, IntConsumer consumer) {
        if (!plugin.getConfig().contains(path)) {
            plugin.getConfig().set(path, defaultVal);
            consumer.accept(1);
            return defaultVal;
        }
        return plugin.getConfig().getInt(path);
    }

    private String getAndDefaultString(String path, String defaultVal, IntConsumer consumer) {
        String temp = plugin.getConfig().getString(path);
        if (temp == null) {
            plugin.getConfig().set(path, defaultVal);
            consumer.accept(1);
            return defaultVal;
        }
        return temp;
    }

    public int getTrailInterval() {
        return trailInterval;
    }

    public int getTrailRenderDistance() {
        return trailRenderDistance;
    }

    public boolean isParticleTrailsEnabled() {
        return particleTrailsEnabled;
    }

    public boolean isBlockTrailsEnabled() {
        return blockTrailsEnabled;
    }

    public String getGuiMainMenuTitle() {
        return guiMainMenuTitle;
    }

    public int getGuiMainMenuSize() {
        return guiMainMenuSize;
    }

    public Collection<MainMenuItem> getGuiMainMenuItems() {
        return guiMainMenuItems.values();
    }

    public CosmeticCategory getCategoryBySlot(int slot) {
        MainMenuItem item = guiMainMenuItems.get(slot);
        return item != null ? item.category() : null;
    }
}