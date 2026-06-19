package net.omni.cosmetics.commands;

import net.omni.cosmetics.OmniCosmetics;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CosmeticsCommand implements CommandExecutor, TabCompleter {
    private final OmniCosmetics plugin;

    public CosmeticsCommand(OmniCosmetics plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }

    public void register() {
        PluginCommand cosmetics = Bukkit.getServer().getPluginCommand("cosmetics");

        if (cosmetics == null) {
            plugin.getLogger().warning("/cosmetics command not found!");
            return;
        }

        cosmetics.setTabCompleter(this);
        cosmetics.setExecutor(this);
    }
}
