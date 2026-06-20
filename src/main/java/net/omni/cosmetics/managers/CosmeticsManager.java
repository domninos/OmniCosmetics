package net.omni.cosmetics.managers;

import net.omni.cosmetics.OmniCosmetics;
import net.omni.cosmetics.effect.Cosmetic;
import net.omni.cosmetics.effect.CosmeticCategory;
import net.omni.cosmetics.effect.CosmeticOperator;
import net.omni.cosmetics.effect.chat.CosmeticsChatColor;
import net.omni.cosmetics.effect.chat.CosmeticsPin;
import net.omni.cosmetics.effect.chat.CosmeticsTag;
import net.omni.cosmetics.effect.trails.BlockConfig;
import net.omni.cosmetics.effect.trails.BlockTrail;
import net.omni.cosmetics.effect.trails.ParticleConfig;
import net.omni.cosmetics.effect.trails.ParticleTrail;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;

public class CosmeticsManager {

    private final OmniCosmetics plugin;
    private final Map<String, Cosmetic> byName = new HashMap<>();
    private final Map<String, ParticleTrail> particleTrails = new HashMap<>();
    private final Map<String, BlockTrail> blockTrails = new HashMap<>();
    private final Map<String, CosmeticsTag> tags = new HashMap<>();
    private final Map<String, CosmeticsPin> pins = new HashMap<>();
    private final Map<String, CosmeticsChatColor> chatColors = new HashMap<>();

    private File particleDir;
    private File blockDir;
    private File tagDir;
    private File pinDir;
    private File chatColorDir;

    public CosmeticsManager(OmniCosmetics plugin) {
        this.plugin = plugin;
    }

    public void initDirectories() {
        File trailsDir = new File(plugin.getDataFolder(), "trails");
        this.particleDir = new File(trailsDir, "particles");
        this.blockDir = new File(trailsDir, "blocks");
        this.tagDir = new File(plugin.getDataFolder(), "tags");
        this.pinDir = new File(plugin.getDataFolder(), "pins");
        this.chatColorDir = new File(plugin.getDataFolder(), "chatcolors");

        particleDir.mkdirs();
        blockDir.mkdirs();
        tagDir.mkdirs();
        pinDir.mkdirs();
        chatColorDir.mkdirs();

        saveExample("trails/particles/fairy.yml");
        saveExample("trails/particles/flame.yml");
        saveExample("trails/particles/snow.yml");
        saveExample("trails/particles/heart.yml");
        saveExample("trails/particles/cloud.yml");
        saveExample("trails/particles/portal.yml");
        saveExample("trails/particles/enchant.yml");
        saveExample("trails/particles/crit.yml");
        saveExample("trails/particles/smoke.yml");
        saveExample("trails/particles/soul.yml");
        saveExample("trails/particles/note.yml");
        saveExample("trails/particles/wax.yml");
        saveExample("trails/particles/drip.yml");
        saveExample("trails/particles/tear.yml");
        saveExample("trails/particles/spark.yml");
        saveExample("trails/particles/dragon.yml");
        saveExample("trails/particles/nautilus.yml");
        saveExample("trails/particles/glow.yml");
        saveExample("trails/particles/ink.yml");
        saveExample("trails/particles/damage.yml");
        saveExample("trails/particles/sweep.yml");
        saveExample("trails/particles/totem.yml");
        saveExample("trails/particles/dust.yml");
        saveExample("trails/particles/sculk.yml");
        saveExample("trails/blocks/tnt.yml");
        saveExample("trails/blocks/stone.yml");
        saveExample("trails/blocks/cobblestone.yml");
        saveExample("trails/blocks/brick.yml");
        saveExample("trails/blocks/nether_brick.yml");
        saveExample("trails/blocks/redstone.yml");
        saveExample("trails/blocks/diamond.yml");
        saveExample("trails/blocks/gold.yml");
        saveExample("trails/blocks/iron.yml");
        saveExample("trails/blocks/emerald.yml");
        saveExample("trails/blocks/lapis.yml");
        saveExample("trails/blocks/coal.yml");
        saveExample("trails/blocks/obsidian.yml");
        saveExample("trails/blocks/glowstone.yml");
        saveExample("trails/blocks/quartz.yml");
        saveExample("trails/blocks/purpur.yml");
        saveExample("tags/vip.yml");
        saveExample("pins/smile.yml");
        saveExample("chatcolors/red.yml");
    }

    private void saveExample(String path) {
        File file = new File(plugin.getDataFolder(), path);
        if (file.exists()) return;
        file.getParentFile().mkdirs();
        try (InputStream in = plugin.getResource("examples/" + path)) {
            if (in != null) {
                Files.copy(in, file.toPath());
                plugin.sendConsole("<green>Created example: " + path + "</green>");
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not save example: " + path, e);
        }
    }

    public void loadCosmetics() {
        loadParticleTrails();
        loadBlockTrails();
        loadTags();
        loadPins();
        loadChatColors();
        plugin.sendConsole("<green>Loaded " + byName.size() + " cosmetics.</green>");
    }

    public void reloadCosmetics() {
        flush();
        loadCosmetics();
    }

    public void flush() {
        byName.values().forEach(Cosmetic::flush);
        byName.clear();
        particleTrails.clear();
        blockTrails.clear();
        tags.clear();
        pins.clear();
        chatColors.clear();
    }

    private void loadParticleTrails() {
        File[] files = particleDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;
        for (File file : files) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                String name = config.getString("name", file.getName().replace(".yml", ""));
                boolean enabled = config.getBoolean("enabled", true);

                List<ParticleConfig> configs = new ArrayList<>();
                ConfigurationSection particles = config.getConfigurationSection("particles");
                if (particles != null) {
                    for (String key : particles.getKeys(false)) {
                        ConfigurationSection section = particles.getConfigurationSection(key);
                        if (section == null) continue;
                        ParticleConfig pc = ParticleConfig.fromSection(section);
                        if (pc != null) configs.add(pc);
                        else plugin.getLogger().warning("Invalid particle entry '" + key + "' in " + file.getName());
                    }
                }
                if (configs.isEmpty()) {
                    String particleName = config.getString("particle_type");
                    if (particleName == null) {
                        plugin.getLogger().warning("Missing particles or particle_type in " + file.getName());
                        continue;
                    }
                    try {
                        Particle particle = Particle.valueOf(particleName.toUpperCase());
                        configs.add(new ParticleConfig(particle, 1, 0.3, 0.3, 0.3, 0.0, null, 1.0f, null, null, null, null, 0f, 0));
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid particle_type '" + particleName + "' in " + file.getName());
                        continue;
                    }
                }

                if (configs.isEmpty()) {
                    plugin.getLogger().warning("No valid particles defined in " + file.getName());
                    continue;
                }

                String permission = config.getString("permission", "omniosmetics.trail." + name);
                int stars = config.getInt("stars", 1);
                String command = config.getString("command", "");
                String operatorStr = config.getString("operator", "console");
                CosmeticOperator operator = operatorStr.equalsIgnoreCase("player") ? CosmeticOperator.PLAYER : CosmeticOperator.CONSOLE;

                ConfigurationSection item = config.getConfigurationSection("item");
                String itemName = item != null ? item.getString("name", name) : name;
                String itemType = item != null ? item.getString("type", "BARRIER") : "BARRIER";
                List<String> itemLore = item != null ? item.getStringList("lore") : List.of();

                ParticleTrail trail = new ParticleTrail(name, enabled, CosmeticCategory.PARTICLE_TRAIL, permission, stars, command, operator, itemName, itemLore, itemType, configs);
                particleTrails.put(name.toLowerCase(), trail);
                byName.put(name.toLowerCase(), trail);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error loading particle trail: " + file.getName(), e);
            }
        }
    }

    private void loadBlockTrails() {
        File[] files = blockDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;
        for (File file : files) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                String name = config.getString("name", file.getName().replace(".yml", ""));
                boolean enabled = config.getBoolean("enabled", true);

                List<BlockConfig> configs = new ArrayList<>();
                ConfigurationSection blocks = config.getConfigurationSection("blocks");
                if (blocks != null) {
                    for (String key : blocks.getKeys(false)) {
                        ConfigurationSection section = blocks.getConfigurationSection(key);
                        if (section == null) continue;
                        BlockConfig bc = BlockConfig.fromSection(section, file.getName(), plugin.getLogger());
                        if (bc != null) configs.add(bc);
                    }
                }
                if (configs.isEmpty()) {
                    String typeName = config.getString("type");
                    if (typeName == null) {
                        plugin.getLogger().warning("Missing blocks or type in " + file.getName());
                        continue;
                    }
                    try {
                        Material material = Material.valueOf(typeName.toUpperCase());
                        if (!material.isBlock()) {
                            plugin.getLogger().warning("Legacy type '" + typeName + "' is not a block in " + file.getName());
                            continue;
                        }
                        double chance = config.getDouble("chance", 0.5);
                        if (chance < 0.0) chance = 0.0;
                        if (chance > 1.0) chance = 1.0;
                        configs.add(new BlockConfig(material, chance));
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid type '" + typeName + "' in " + file.getName());
                        continue;
                    }
                }

                if (configs.isEmpty()) {
                    plugin.getLogger().warning("No valid blocks defined in " + file.getName());
                    continue;
                }

                int radius = config.getInt("radius", 1);
                if (radius < 0) radius = 0;

                String permission = config.getString("permission", "omniosmetics.trail." + name);
                int stars = config.getInt("stars", 1);
                String command = config.getString("command", "");
                String operatorStr = config.getString("operator", "console");
                CosmeticOperator operator = operatorStr.equalsIgnoreCase("player") ? CosmeticOperator.PLAYER : CosmeticOperator.CONSOLE;

                ConfigurationSection item = config.getConfigurationSection("item");
                String itemName = item != null ? item.getString("name", name) : name;
                String itemType = item != null ? item.getString("type", "BARRIER") : "BARRIER";
                List<String> itemLore = item != null ? item.getStringList("lore") : List.of();

                BlockTrail trail = new BlockTrail(name, enabled, CosmeticCategory.BLOCK_TRAIL, permission, stars, command, operator, itemName, itemLore, itemType, configs, radius);
                blockTrails.put(name.toLowerCase(), trail);
                byName.put(name.toLowerCase(), trail);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error loading block trail: " + file.getName(), e);
            }
        }
    }

    private void loadTags() {
        File[] files = tagDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;
        for (File file : files) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                String name = config.getString("name", file.getName().replace(".yml", ""));
                boolean enabled = config.getBoolean("enabled", true);
                String tag = config.getString("tag", "[" + name + "]");
                String permission = config.getString("permission", "omniosmetics.tag." + name);
                int stars = config.getInt("stars", 1);
                String command = config.getString("command", "");
                String operatorStr = config.getString("operator", "console");
                CosmeticOperator operator = operatorStr.equalsIgnoreCase("player") ? CosmeticOperator.PLAYER : CosmeticOperator.CONSOLE;

                ConfigurationSection item = config.getConfigurationSection("item");
                String itemName = item != null ? item.getString("name", name) : name;
                String itemType = item != null ? item.getString("type", "NAME_TAG") : "NAME_TAG";
                List<String> itemLore = item != null ? item.getStringList("lore") : List.of();

                CosmeticsTag obj = new CosmeticsTag(name, enabled, CosmeticCategory.TAG, permission, stars, command, operator, itemName, itemLore, itemType, tag);
                tags.put(name.toLowerCase(), obj);
                byName.put(name.toLowerCase(), obj);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error loading tag: " + file.getName(), e);
            }
        }
    }

    private void loadPins() {
        File[] files = pinDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;
        for (File file : files) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                String name = config.getString("name", file.getName().replace(".yml", ""));
                boolean enabled = config.getBoolean("enabled", true);
                String pin = config.getString("pin", "");
                String permission = config.getString("permission", "omniosmetics.pin." + name);
                int stars = config.getInt("stars", 1);
                String command = config.getString("command", "");
                String operatorStr = config.getString("operator", "console");
                CosmeticOperator operator = operatorStr.equalsIgnoreCase("player") ? CosmeticOperator.PLAYER : CosmeticOperator.CONSOLE;

                ConfigurationSection item = config.getConfigurationSection("item");
                String itemName = item != null ? item.getString("name", name) : name;
                String itemType = item != null ? item.getString("type", "PLAYER_HEAD") : "PLAYER_HEAD";
                List<String> itemLore = item != null ? item.getStringList("lore") : List.of();

                CosmeticsPin obj = new CosmeticsPin(name, enabled, CosmeticCategory.PIN, permission, stars, command, operator, itemName, itemLore, itemType, pin);
                pins.put(name.toLowerCase(), obj);
                byName.put(name.toLowerCase(), obj);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error loading pin: " + file.getName(), e);
            }
        }
    }

    private void loadChatColors() {
        File[] files = chatColorDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;
        for (File file : files) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                String name = config.getString("name", file.getName().replace(".yml", ""));
                boolean enabled = config.getBoolean("enabled", true);
                String color = config.getString("color", "<white>");
                String permission = config.getString("permission", "omniosmetics.chatcolor." + name);
                int stars = config.getInt("stars", 1);
                String command = config.getString("command", "");
                String operatorStr = config.getString("operator", "console");
                CosmeticOperator operator = operatorStr.equalsIgnoreCase("player") ? CosmeticOperator.PLAYER : CosmeticOperator.CONSOLE;

                ConfigurationSection item = config.getConfigurationSection("item");
                String itemName = item != null ? item.getString("name", name) : name;
                String itemType = item != null ? item.getString("type", "BARRIER") : "BARRIER";
                List<String> itemLore = item != null ? item.getStringList("lore") : List.of();

                CosmeticsChatColor obj = new CosmeticsChatColor(name, enabled, CosmeticCategory.CHAT_COLOR, permission, stars, command, operator, itemName, itemLore, itemType, color);
                chatColors.put(name.toLowerCase(), obj);
                byName.put(name.toLowerCase(), obj);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error loading chat color: " + file.getName(), e);
            }
        }
    }

    public Cosmetic getByName(String name) {
        return byName.get(name.toLowerCase());
    }

    public ParticleTrail getParticleTrail(String name) {
        return particleTrails.get(name.toLowerCase());
    }

    public BlockTrail getBlockTrail(String name) {
        return blockTrails.get(name.toLowerCase());
    }

    public CosmeticsTag getTag(String name) {
        return tags.get(name.toLowerCase());
    }

    public CosmeticsPin getPin(String name) {
        return pins.get(name.toLowerCase());
    }

    public CosmeticsChatColor getChatColor(String name) {
        return chatColors.get(name.toLowerCase());
    }

    public Collection<ParticleTrail> getParticleTrails() {
        return particleTrails.values();
    }

    public Collection<BlockTrail> getBlockTrails() {
        return blockTrails.values();
    }

    public Collection<CosmeticsTag> getTags() {
        return tags.values();
    }

    public Collection<CosmeticsPin> getPins() {
        return pins.values();
    }

    public Collection<CosmeticsChatColor> getChatColors() {
        return chatColors.values();
    }

    public Collection<Cosmetic> getAll() {
        return byName.values();
    }
}
