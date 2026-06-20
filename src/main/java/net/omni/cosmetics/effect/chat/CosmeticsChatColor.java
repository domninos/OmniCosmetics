package net.omni.cosmetics.effect.chat;

import net.omni.cosmetics.effect.Cosmetic;
import net.omni.cosmetics.effect.CosmeticCategory;
import net.omni.cosmetics.effect.CosmeticOperator;

import java.util.List;

public class CosmeticsChatColor extends Cosmetic {
    private final String color;

    public CosmeticsChatColor(String name, boolean enabled, CosmeticCategory category, String permission, int stars, String command, CosmeticOperator operator, String itemName, List<String> itemLore, String itemType, String color) {
        super(name, enabled, category, permission, stars, command, operator, itemName, itemLore, itemType);
        this.color = color;
    }

    public String getColor() {
        return color;
    }
}
