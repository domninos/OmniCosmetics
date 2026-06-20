package net.omni.cosmetics.effect.chat;

import net.omni.cosmetics.effect.Cosmetic;
import net.omni.cosmetics.effect.CosmeticCategory;
import net.omni.cosmetics.effect.CosmeticOperator;

import java.util.List;

public class CosmeticsTag extends Cosmetic {
    private final String tag;

    public CosmeticsTag(String name, boolean enabled, CosmeticCategory category, String permission, int stars, String command, CosmeticOperator operator, String itemName, List<String> itemLore, String itemType, String tag) {
        super(name, enabled, category, permission, stars, command, operator, itemName, itemLore, itemType);
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
