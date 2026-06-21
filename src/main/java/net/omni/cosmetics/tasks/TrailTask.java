package net.omni.cosmetics.tasks;

import net.omni.cosmetics.OmniCosmetics;
import net.omni.cosmetics.effect.trails.ParticleConfig;
import net.omni.cosmetics.effect.trails.ParticleTrail;
import net.omni.cosmetics.player.CosmeticsPlayer;
import net.omni.cosmetics.util.config.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TrailTask implements Runnable {

    private final OmniCosmetics plugin;
    private int taskId = -1;

    public TrailTask(OmniCosmetics plugin) {
        this.plugin = plugin;
    }

    public void restart() {
        stop();
        start();
    }

    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    public void start() {
        ConfigUtil config = plugin.getConfigUtil();
        taskId = Bukkit.getScheduler().runTaskTimer(plugin, this, 0L, config.getTrailInterval()).getTaskId();
    }

    @Override
    public void run() {
        ConfigUtil config = plugin.getConfigUtil();

        if (!config.isParticleTrailsEnabled()) {
            stop();
            return;
        }

        for (CosmeticsPlayer cp : plugin.getPlayerManager().getPlayers().values()) {
            if (cp == null)
                continue;

            ParticleTrail particleTrail = cp.getActiveParticleTrail();
            if (particleTrail == null || !particleTrail.isEnabled())
                continue;

            Player target = cp.getPlayer();

            if (target == null)
                continue;

            Location targetLoc = target.getLocation();
            double px = targetLoc.getX();
            double py = targetLoc.getY() + 1.0;
            double pz = targetLoc.getZ();

            for (ParticleConfig pc : particleTrail.getParticleConfigs())
                pc.spawn(target.getWorld(), px, py, pz);
        }

        plugin.getBenchmarkManager().tick();
    }
}
