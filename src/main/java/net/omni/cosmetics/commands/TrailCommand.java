package net.omni.cosmetics.commands;

import net.omni.cosmetics.OmniCosmetics;
import net.omni.cosmetics.effect.Cosmetic;
import net.omni.cosmetics.effect.trails.BlockTrail;
import net.omni.cosmetics.effect.trails.ParticleTrail;
import net.omni.cosmetics.player.CosmeticsPlayer;
import net.omni.cosmetics.util.config.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TrailCommand implements CommandExecutor, TabCompleter {
    private final OmniCosmetics plugin;

    public TrailCommand(OmniCosmetics plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!sender.hasPermission("omnicosmetics.trail.admin")) {
            plugin.sendMessage(sender, Messages.NO_PERMS.toString());
            return true;
        }

        if (args.length < 1) {
            plugin.sendMessage(sender, Messages.USAGE.replace("usage", "/trail <player> [trail_name]"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            plugin.sendMessage(sender, Messages.PLAYER_NOT_FOUND.replace("name", args[0]));
            return true;
        }

        CosmeticsPlayer cp = plugin.getPlayerManager().getPlayer(target.getUniqueId());
        if (cp == null) {
            plugin.sendMessage(sender, Messages.PLAYER_NOT_FOUND.replace("name", args[0]));
            return true;
        }

        if (args.length < 2 || args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("none")) {
            cp.setActiveParticleTrail(null);
            cp.setActiveBlockTrail(null);
            plugin.sendMessage(sender, Messages.TRAIL_REMOVED.replace("player", target.getName()));
            return true;
        }

        Cosmetic cosmetic = plugin.getCosmeticsManager().getByName(args[1]);
        if (!(cosmetic instanceof ParticleTrail || cosmetic instanceof BlockTrail)) {
            plugin.sendMessage(sender, Messages.TRAIL_NOT_FOUND.replace("name", args[1]));
            return true;
        }

        if (cosmetic instanceof ParticleTrail particleTrail) {
            cp.setActiveParticleTrail(particleTrail);
            cp.setActiveBlockTrail(null);
        } else {
            BlockTrail blockTrail = (BlockTrail) cosmetic;
            cp.setActiveBlockTrail(blockTrail);
            cp.setActiveParticleTrail(null);
        }

        plugin.sendMessage(sender, Messages.TRAIL_SET.replace("player", target.getName(), "name", cosmetic.getName()));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1)
            return null;

        if (args.length == 2) {
            List<String> names = new ArrayList<>();
            names.add("remove");

            for (ParticleTrail trail : plugin.getCosmeticsManager().getParticleTrails())
                names.add(trail.getName());

            for (BlockTrail trail : plugin.getCosmeticsManager().getBlockTrails())
                names.add(trail.getName());

            return names;
        }

        return List.of();
    }

    public void register() {
        PluginCommand cmd = Bukkit.getServer().getPluginCommand("trail");

        if (cmd == null) {
            plugin.getLogger().warning("/trail command not found!");
            return;
        }

        cmd.setTabCompleter(this);
        cmd.setExecutor(this);
    }
}
