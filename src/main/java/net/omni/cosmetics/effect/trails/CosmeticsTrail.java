package net.omni.cosmetics.effect.trails;

import net.omni.cosmetics.effect.Cosmetic;
import net.omni.cosmetics.effect.CosmeticCategory;
import net.omni.cosmetics.effect.CosmeticOperator;

import java.util.List;

public abstract class CosmeticsTrail extends Cosmetic {
    public CosmeticsTrail(String name, boolean enabled, CosmeticCategory category, String permission, int stars, String command, CosmeticOperator operator, String itemName, List<String> itemLore, String itemType) {
        super(name, enabled, category, permission, stars, command, operator, itemName, itemLore, itemType);
    }
}
