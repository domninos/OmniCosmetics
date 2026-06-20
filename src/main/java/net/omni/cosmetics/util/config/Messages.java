package net.omni.cosmetics.util.config;

import java.util.ArrayList;
import java.util.List;

public enum Messages {
    NO_PERMS("no_perms", "<red>You do not have permission to use this command.</red>"),
    ONLY_PLAYERS("only_players", "<red>Only players can use this command.</red>"),

    USAGE("usage", "<red>Invalid arguments. Usage: %usage%</red>"),
    UNKNOWN_COMMAND("unknown_cmd", "<red>Unknown command.</red>"),

    RELOADED("reloaded", "<green>Successfully reloaded configs and cosmetics.</green>"),

    TRAIL_NOT_FOUND("trail_not_found", "<red>Trail '%name%' not found.</red>"),
    TRAIL_SET("trail_set", "<green>Set %player%'s trail to %name%.</green>"),
    TRAIL_REMOVED("trail_removed", "<green>Removed %player%'s trail.</green>"),
    COSMETIC_EQUIPPED("cosmetic_equipped", "<green>Equipped %name%.</green>"),
    COSMETIC_REMOVED("cosmetic_removed", "<green>Removed %name%.</green>"),
    PLAYER_NOT_FOUND("player_not_found", "<red>Player '%name%' not found.</red>"),
    NO_COSMETICS("no_cosmetics", "<red>You don't have any cosmetics in this category.</red>");

    private final String path;
    private final Object defaultVal;
    private Object cachedVal;

    Messages(String path, Object defaultVal) {
        this.path = path;
        this.defaultVal = defaultVal;
    }

    public String getPath() {
        return path;
    }

    public Object getDefaultVal() {
        return defaultVal;
    }

    public void setCachedVal(Object val) {
        this.cachedVal = val;
    }

    public String replace(String... pairs) {
        String result = this.toString();

        return replace(result, pairs);
    }

    @Override
    public String toString() {
        if (cachedVal instanceof List<?>)
            return "";

        return cachedVal instanceof String ? (String) cachedVal : (String) defaultVal;
    }

    private String replace(String result, String... pairs) {
        if (result.isEmpty())
            return "";

        for (int i = 0; i < pairs.length - 1; i += 2) {
            String key = pairs[i];
            String val = pairs[i + 1];

            if (key != null && val != null) {
                result = result.replace("%" + key + "%", val);
            }
        }

        return result;
    }

    public String replaceList(String... pairs) {
        List<String> originalList = this.asList();

        if (originalList.isEmpty())
            return "";

        List<String> modifiedList = new ArrayList<>();

        for (String line : originalList) {
            if (line != null)
                modifiedList.add(replace(line, pairs));
        }

        return String.join("\n", modifiedList);
    }

    @SuppressWarnings("unchecked")
    public List<String> asList() {
        return cachedVal instanceof List<?> ? (List<String>) cachedVal : (List<String>) defaultVal;
    }

    public void flush() {
        if (cachedVal instanceof List<?> cachedList)
            cachedList.clear();

        if (defaultVal instanceof List<?> defaultList)
            defaultList.clear();

        this.cachedVal = null;
    }
}