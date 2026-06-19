package net.omni.cosmetics.effect.trails;

import net.omni.cosmetics.effect.Cosmetic;
import net.omni.cosmetics.effect.CosmeticCategory;
import net.omni.cosmetics.effect.CosmeticOperator;

import java.util.List;

public class BlockTrail extends Cosmetic {
    public BlockTrail(String name, boolean enabled, CosmeticCategory category, String permission, int stars, String command, CosmeticOperator operator, String itemName, List<String> itemLore) {
        super(name, enabled, category, permission, stars, command, operator, itemName, itemLore);
    }


}
