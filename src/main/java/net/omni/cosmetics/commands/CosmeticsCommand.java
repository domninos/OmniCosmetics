package net.omni.cosmetics.commands;

import net.omni.cosmetics.OmniCosmetics;
import net.omni.cosmetics.util.config.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
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
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                plugin.sendMessage(sender, Messages.ONLY_PLAYERS.toString());
                return true;
            }

            if (!sender.hasPermission("omnicosmetics.menu")) {
                plugin.sendMessage(sender, Messages.NO_PERMS.toString());
                return true;
            }

            plugin.getGuiManager().openMenu((Player) sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("omnicosmetics.reload")) {
                plugin.sendMessage(sender, Messages.NO_PERMS.toString());
                return true;
            }

            plugin.reload();
            plugin.sendMessage(sender, Messages.RELOADED.toString());
            return true;
        }

        if (args[0].equalsIgnoreCase("benchmark")) {
            if (!sender.hasPermission("omnicosmetics.benchmark")) {
                plugin.sendMessage(sender, Messages.NO_PERMS.toString());
                return true;
            }
            if (!(sender instanceof Player player)) {
                plugin.sendMessage(sender, Messages.ONLY_PLAYERS.toString());
                return true;
            }

            if (plugin.getBenchmarkManager().isBenchmarking(player)) {
                plugin.getBenchmarkManager().stopBenchmark(player.getUniqueId());
                plugin.sendMessage(player, "<gray>Benchmark mode <red>disabled</red>.</gray>");
            } else {
                int radius = 1;
                if (args.length >= 2) {
                    try {
                        radius = Integer.parseInt(args[1]);
                    } catch (NumberFormatException ignored) {
                    }
                }
                plugin.getBenchmarkManager().startBenchmark(player, radius);
                plugin.sendMessage(player, "<gray>Benchmark mode <green>enabled</green> (radius=" + radius + ").</gray>");
            }
            return true;
        }

        plugin.sendMessage(sender, Messages.USAGE.replace("usage", "/cosmetics [reload|benchmark]"));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            List<String> options = new java.util.ArrayList<>();
            if (sender.hasPermission("omnicosmetics.reload"))
                options.add("reload");
            if (sender.hasPermission("omnicosmetics.benchmark"))
                options.add("benchmark");
            return options;
        }

        return List.of();
    }

    public void register() {
        PluginCommand cmd = Bukkit.getServer().getPluginCommand("cosmetics");

        if (cmd == null) {
            plugin.getLogger().warning("/cosmetics command not found!");
            return;
        }

        cmd.setTabCompleter(this);
        cmd.setExecutor(this);
    }
}
