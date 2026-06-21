package net.omni.cosmetics.util.config;

import net.omni.cosmetics.OmniCosmetics;
import net.omni.cosmetics.effect.CosmeticCategory;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

public class ConfigUtil {

    private final OmniCosmetics plugin;
    private int trailInterval;
    private int trailRenderDistance;
    private boolean particleTrailsEnabled;
    private boolean blockTrailsEnabled;
    private Set<String> blockBlacklist;
    private String guiMainMenuTitle;
    private int guiMainMenuSize;
    private Map<Integer, MainMenuItem> guiMainMenuItems;
    private GuiButton guiMainMenuExit;
    private String guiCategoryTitle;
    private int guiCategorySize;
    private GuiButton guiCategoryBack;
    private NavButton guiCategoryPrevious;
    private NavButton guiCategoryNext;
    private GuiButton guiFiller;
    private GuiButton guiPlaceholder;
    private int paginationThreshold;
    private int maxStars;

    public ConfigUtil(OmniCosmetics plugin) {
        this.plugin = plugin;
    }

    public void reloadConfig() {
        flush();
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        load();
    }

    public void flush() {
        blockBlacklist.clear();
        guiMainMenuItems.clear();
    }

    public void load() {
        AtomicInteger savedDefaults = new AtomicInteger();

        this.trailInterval = getAndDefaultInt("trail-interval", 5, savedDefaults::getAndAdd);
        this.trailRenderDistance = getAndDefaultInt("trail-render-distance", 32, savedDefaults::getAndAdd);

        this.particleTrailsEnabled = plugin.getConfig().getBoolean("particle-trails-enabled", true);
        this.blockTrailsEnabled = plugin.getConfig().getBoolean("block-trails-enabled", true);

        this.blockBlacklist = new HashSet<>(plugin.getConfig().getStringList("block-trail.blacklisted-blocks"));

        loadGuiConfig(savedDefaults);

        if (savedDefaults.get() > 0) {
            plugin.saveConfig();
            plugin.sendConsole("<green>Successfully loaded " + savedDefaults.get() + " default configuration(s)</green>");
        }

        plugin.sendConsole("<green>Successfully loaded config.yml</green>");
    }

    private int getAndDefaultInt(String path, int defaultVal, IntConsumer consumer) {
        if (!plugin.getConfig().contains(path)) {
            plugin.getConfig().set(path, defaultVal);
            consumer.accept(1);
            return defaultVal;
        }
        return plugin.getConfig().getInt(path);
    }

    private void loadGuiConfig(AtomicInteger savedDefaults) {
        loadMainMenu(savedDefaults);
        loadCategoryMenu(savedDefaults);
        loadFiller(savedDefaults);
        loadPlaceholder(savedDefaults);
        loadPagination(savedDefaults);
    }

    private void loadMainMenu(AtomicInteger savedDefaults) {
        String base = "gui.main-menu";
        this.guiMainMenuTitle = getAndDefaultString(base + ".title", "  <#AAFFAA>Cosmetics</#AAFFAA>", savedDefaults::getAndAdd);
        this.guiMainMenuSize = getAndDefaultInt(base + ".size", 27, savedDefaults::getAndAdd);

        this.guiMainMenuExit = loadGuiButton(base + ".exit", 4, "BARRIER",
                "<red>Exit</red>", List.of());

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

    private void loadCategoryMenu(AtomicInteger savedDefaults) {
        String base = "gui.category-menu";
        this.guiCategoryTitle = getAndDefaultString(base + ".title", "  <#AAFFAA>%category%</#AAFFAA>", savedDefaults::getAndAdd);
        this.guiCategorySize = getAndDefaultInt(base + ".size", 45, savedDefaults::getAndAdd);

        this.guiCategoryBack = loadGuiButton(base + ".back", 4, "BARRIER",
                "<red>Back</red>", List.of());
        this.guiCategoryPrevious = loadNavButton(base + ".previous", 18, "ARROW",
                "<yellow>Previous Page</yellow>", List.of());
        this.guiCategoryNext = loadNavButton(base + ".next", 26, "ARROW",
                "<yellow>Next Page</yellow>", List.of());
    }

    private void loadFiller(AtomicInteger savedDefaults) {
        String base = "gui.filler";
        this.guiFiller = loadGuiButton(base, -1, "GRAY_STAINED_GLASS_PANE", " ", List.of());
    }

    private void loadPlaceholder(AtomicInteger savedDefaults) {
        String base = "gui.placeholder";
        this.guiPlaceholder = loadGuiButton(base, -1, "STONE",
                "<gray>Coming Soon</gray>", List.of("<dark_gray>More cosmetics coming soon!</dark_gray>"));
    }

    private void loadPagination(AtomicInteger savedDefaults) {
        this.paginationThreshold = getAndDefaultInt("gui.pagination.threshold", 21, savedDefaults::getAndAdd);
        this.maxStars = getAndDefaultInt("gui.max-stars", 3, savedDefaults::getAndAdd);
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

    private GuiButton loadGuiButton(String path, int defaultSlot, String defaultMaterial, String defaultName, List<String> defaultLore) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(path);
        if (section != null) {
            return GuiButton.fromSection(section, defaultSlot, defaultMaterial, defaultName, defaultLore);
        }
        return new GuiButton(defaultSlot, Material.valueOf(defaultMaterial), defaultName, defaultLore);
    }

    private Map<Integer, MainMenuItem> defaultMainMenuItems() {
        Map<Integer, MainMenuItem> items = new HashMap<>();
        items.put(10, new MainMenuItem(10, Material.FIREWORK_STAR, CosmeticCategory.PARTICLE_TRAIL,
                "<#AAFFAA>Particle Trails</#AAFFAA>",
                List.of("<gray>Particle effects that follow you</gray>")));
        items.put(11, new MainMenuItem(11, Material.GRASS_BLOCK, CosmeticCategory.BLOCK_TRAIL,
                "<#FFAA00>Block Trails</#FFAA00>",
                List.of("<gray>Block trails that follow you</gray>")));
        items.put(12, new MainMenuItem(12, Material.NAME_TAG, CosmeticCategory.TAG,
                "<#55FFFF>Tags</#55FFFF>",
                List.of("<gray>Name tags for chat</gray>")));
        items.put(13, new MainMenuItem(13, Material.PLAYER_HEAD, CosmeticCategory.PIN,
                "<#FF55FF>Pins</#FF55FF>",
                List.of("<gray>Emoji pins for chat</gray>")));
        items.put(14, new MainMenuItem(14, Material.RED_DYE, CosmeticCategory.CHAT_COLOR,
                "<#FF5555>Chat Colors</#FF5555>",
                List.of("<gray>Chat message colors</gray>")));
        return items;
    }

    private NavButton loadNavButton(String path, int defaultSlot, String defaultMaterial, String defaultName, List<String> defaultLore) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(path);
        if (section != null) {
            return NavButton.fromSection(section, defaultSlot, defaultMaterial, defaultName, defaultLore);
        }
        return new NavButton(defaultSlot, Material.valueOf(defaultMaterial), defaultName, defaultLore,
                Material.valueOf(defaultMaterial), "<gray>" + defaultName + "</gray>", List.of());
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

    public GuiButton getGuiMainMenuExit() {
        return guiMainMenuExit;
    }

    public String getGuiCategoryTitle() {
        return guiCategoryTitle;
    }

    public GuiButton getGuiCategoryBack() {
        return guiCategoryBack;
    }

    public NavButton getGuiCategoryPrevious() {
        return guiCategoryPrevious;
    }

    public NavButton getGuiCategoryNext() {
        return guiCategoryNext;
    }

    public GuiButton getGuiFiller() {
        return guiFiller;
    }

    public GuiButton getGuiPlaceholder() {
        return guiPlaceholder;
    }

    public int getPaginationThreshold() {
        return paginationThreshold;
    }

    public int getGuiCategorySize() {
        return guiCategorySize;
    }

    public int getMaxStars() {
        return maxStars;
    }

    public Set<String> getBlockBlacklist() {
        return blockBlacklist;
    }

    public record MainMenuItem(int slot, Material material, CosmeticCategory category, String name, List<String> lore) {
        public static MainMenuItem fromSection(ConfigurationSection section) {
            String catName = section.getString("category");

            if (catName == null)
                return null;

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

            if (lore.isEmpty())
                lore = List.of();

            return new MainMenuItem(slot, material, category, name, lore);
        }
    }

    public record GuiButton(int slot, Material material, String name, List<String> lore) {
        public static GuiButton fromSection(ConfigurationSection section, int defaultSlot, String defaultMaterial, String defaultName, List<String> defaultLore) {
            int slot = section.getInt("slot", defaultSlot);
            String matName = section.getString("material", defaultMaterial);

            Material material;
            try {
                material = Material.valueOf(matName.toUpperCase());
            } catch (IllegalArgumentException e) {
                material = Material.valueOf(defaultMaterial);
            }

            String name = section.getString("name", defaultName);
            List<String> lore = section.getStringList("lore");

            if (lore.isEmpty())
                lore = defaultLore;

            return new GuiButton(slot, material, name, lore);
        }
    }

    public record NavButton(int slot, Material material, String name, List<String> lore, Material disabledMaterial,
                            String disabledName, List<String> disabledLore) {
        public static NavButton fromSection(ConfigurationSection section, int defaultSlot, String defaultMaterial, String defaultName, List<String> defaultLore) {
            int slot = section.getInt("slot", defaultSlot);
            String matName = section.getString("material", defaultMaterial);

            Material material;
            try {
                material = Material.valueOf(matName.toUpperCase());
            } catch (IllegalArgumentException e) {
                material = Material.valueOf(defaultMaterial);
            }

            String name = section.getString("name", defaultName);
            List<String> lore = section.getStringList("lore");

            if (lore.isEmpty())
                lore = defaultLore;

            String disMatName = section.getString("disabled-material", matName);
            Material disabledMaterial;
            try {
                disabledMaterial = Material.valueOf(disMatName.toUpperCase());
            } catch (IllegalArgumentException e) {
                disabledMaterial = Material.valueOf(defaultMaterial);
            }

            String disabledName = section.getString("disabled-name", "<gray>" + defaultName + "</gray>");
            List<String> disabledLore = section.getStringList("disabled-lore");

            if (disabledLore.isEmpty())
                disabledLore = List.of();

            return new NavButton(slot, material, name, lore, disabledMaterial, disabledName, disabledLore);
        }
    }
}
