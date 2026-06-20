package net.omni.cosmetics.effect.chat;

import net.omni.cosmetics.effect.Cosmetic;
import net.omni.cosmetics.effect.CosmeticCategory;
import net.omni.cosmetics.effect.CosmeticOperator;

import java.util.List;

public class CosmeticsPin extends Cosmetic {
    private final String pin;

    public CosmeticsPin(String name, boolean enabled, CosmeticCategory category, String permission, int stars, String command, CosmeticOperator operator, String itemName, List<String> itemLore, String pin) {
        super(name, enabled, category, permission, stars, command, operator, itemName, itemLore);
        this.pin = pin;
    }

    public String getPin() {
        return pin;
    }
}
