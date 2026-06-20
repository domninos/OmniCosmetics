package net.omni.cosmetics.effect.trails;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class ParticleConfig {

    private final Particle particle;
    private final int count;
    private final double offsetX, offsetY, offsetZ;
    private final double speed;
    private final Color color;
    private final float size;
    private final Color fromColor;
    private final Color toColor;
    private final Material material;
    private final Material blockMaterial;
    private final float roll;
    private final int delay;

    public ParticleConfig(Particle particle, int count, double offsetX, double offsetY, double offsetZ, double speed, Color color, float size, Color fromColor, Color toColor, Material material, Material blockMaterial, float roll, int delay) {
        this.particle = particle;
        this.count = count;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.speed = speed;
        this.color = color;
        this.size = size;
        this.fromColor = fromColor;
        this.toColor = toColor;
        this.material = material;
        this.blockMaterial = blockMaterial;
        this.roll = roll;
        this.delay = delay;
    }

    public static ParticleConfig fromSection(ConfigurationSection section) {
        String particleName = section.getString("particle");
        if (particleName == null) particleName = section.getName();

        Particle particle;
        try {
            particle = Particle.valueOf(particleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }

        int count = section.getInt("count", 1);
        double offsetX = section.getDouble("offset_x", 0.3);
        double offsetY = section.getDouble("offset_y", 0.3);
        double offsetZ = section.getDouble("offset_z", 0.3);
        double speed = section.getDouble("speed", 0.0);

        Color color = parseColor(section.getString("color"));
        float size = (float) section.getDouble("size", 1.0);
        Color fromColor = parseColor(section.getString("from_color"));
        Color toColor = parseColor(section.getString("to_color"));

        Material material = null;
        String matName = section.getString("material");
        if (matName != null) {
            try {
                material = Material.valueOf(matName.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        Material blockMaterial = null;
        String blockName = section.getString("block_material");
        if (blockName != null) {
            try {
                blockMaterial = Material.valueOf(blockName.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        float roll = (float) section.getDouble("roll", 0.0);
        int delay = section.getInt("delay", 0);

        return new ParticleConfig(particle, count, offsetX, offsetY, offsetZ, speed, color, size, fromColor, toColor, material, blockMaterial, roll, delay);
    }

    private static Color parseColor(String str) {
        if (str == null || str.isEmpty())
            return null;

        try {
            String[] parts = str.split(",");

            if (parts.length >= 3) {
                return Color.fromRGB(
                        Integer.parseInt(parts[0].trim()),
                        Integer.parseInt(parts[1].trim()),
                        Integer.parseInt(parts[2].trim())
                );
            }

        } catch (NumberFormatException ignored) {
        }

        return null;
    }

    public void spawn(World world, double x, double y, double z) {
        Object data = buildData();
        world.spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, speed, data);
    }

    private Object buildData() {
        if (particle == Particle.DUST)
            return new Particle.DustOptions(Objects.requireNonNullElse(color, Color.RED), size);

        if (particle == Particle.DUST_COLOR_TRANSITION) {
            if (fromColor != null && toColor != null)
                return new Particle.DustTransition(fromColor, toColor, size);

            return null;
        }
        if (particle == Particle.ITEM) {
            if (material != null)
                return new ItemStack(material);

            return null;
        }
        if (particle == Particle.BLOCK || particle == Particle.FALLING_DUST || particle == Particle.BLOCK_MARKER) {
            Material mat = blockMaterial != null ? blockMaterial : material;
            if (mat != null && mat.isBlock())
                return mat.createBlockData();

            return null;
        }
        if (particle.name().equals("SCULK_CHARGE"))
            return roll;

        if (particle.name().equals("SHRIEK"))
            return delay;

        if (particle == Particle.DRAGON_BREATH)
            return 0.0f;

        if (particle == Particle.INSTANT_EFFECT || particle.name().equals("EFFECT"))
            return new Particle.Spell(color != null ? color : Color.WHITE, 1.0f);

        if (particle == Particle.ENTITY_EFFECT)
            return color != null ? color : Color.WHITE;

        return null;
    }
}
