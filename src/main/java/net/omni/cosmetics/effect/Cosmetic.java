package net.omni.cosmetics.effect;

import java.util.List;

public abstract class Cosmetic {

    private final String name;
    private final boolean enabled;
    private final CosmeticCategory category;
    private final String permission;
    private final int stars;
    private final String command;
    private final CosmeticOperator operator;
    private final String itemName;
    private final List<String> itemLore;
    private final String itemType;

    public Cosmetic(String name, boolean enabled, CosmeticCategory category, String permission, int stars, String command, CosmeticOperator operator, String itemName, List<String> itemLore, String itemType) {
        this.name = name;
        this.enabled = enabled;
        this.category = category;
        this.permission = permission;
        this.stars = stars;
        this.command = command;
        this.operator = operator;
        this.itemName = itemName;
        this.itemLore = itemLore;
        this.itemType = itemType;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public CosmeticCategory getCategory() {
        return category;
    }

    public String getPermission() {
        return permission;
    }

    public int getStars() {
        return stars;
    }

    public String getCommand() {
        return command;
    }

    public CosmeticOperator getOperator() {
        return operator;
    }

    public String getItemName() {
        return itemName;
    }

    public List<String> getItemLore() {
        return itemLore;
    }

    public String getItemType() {
        return itemType;
    }

    public void flush() {
        this.itemLore.clear();
    }

}
