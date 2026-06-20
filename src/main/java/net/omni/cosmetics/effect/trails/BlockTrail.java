package net.omni.cosmetics.effect.trails;

import net.omni.cosmetics.effect.CosmeticCategory;
import net.omni.cosmetics.effect.CosmeticOperator;

import java.util.List;

public class BlockTrail extends CosmeticsTrail {
    private final List<BlockConfig> blockConfigs;
    private final int radius;

    public BlockTrail(String name, boolean enabled, CosmeticCategory category, String permission, int stars, String command, CosmeticOperator operator, String itemName, List<String> itemLore, List<BlockConfig> blockConfigs, int radius) {
        super(name, enabled, category, permission, stars, command, operator, itemName, itemLore);
        this.blockConfigs = blockConfigs;
        this.radius = radius;
    }

    public List<BlockConfig> getBlockConfigs() {
        return blockConfigs;
    }

    public int getRadius() {
        return radius;
    }
}
