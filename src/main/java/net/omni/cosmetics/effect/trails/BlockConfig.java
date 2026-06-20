package net.omni.cosmetics.effect.trails;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.logging.Logger;

public record BlockConfig(Material material, double chance) {

    public static BlockConfig fromSection(ConfigurationSection section, String file, Logger logger) {
        String blockName = section.getString("block");
        if (blockName == null) blockName = section.getName();

        Material material;
        try {
            material = Material.valueOf(blockName.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warning("Unknown material '" + blockName + "' in " + file);
            return null;
        }

        if (!material.isBlock()) {
            logger.warning("Material '" + blockName + "' is not a block in " + file);
            return null;
        }

        double chance = section.getDouble("chance", 0.5);
        if (chance < 0.0) chance = 0.0;
        if (chance > 1.0) chance = 1.0;

        return new BlockConfig(material, chance);
    }
}
