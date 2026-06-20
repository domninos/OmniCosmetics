package net.omni.cosmetics.tasks;

import net.omni.cosmetics.OmniCosmetics;
import net.omni.cosmetics.effect.trails.ParticleConfig;
import net.omni.cosmetics.effect.trails.ParticleTrail;
import net.omni.cosmetics.player.CosmeticsPlayer;
import net.omni.cosmetics.util.config.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

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
        int renderDistance = config.getTrailRenderDistance();
        int renderDistanceSq = renderDistance * renderDistance;

        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();

        for (Player target : onlinePlayers) {
            CosmeticsPlayer cp = plugin.getPlayerManager().getPlayer(target.getUniqueId());
            if (cp == null) continue;

            if (!config.isParticleTrailsEnabled()) continue;

            ParticleTrail particleTrail = cp.getActiveParticleTrail();
            if (particleTrail == null || !particleTrail.isEnabled()) continue;

            Location targetLoc = target.getLocation();
            List<ParticleConfig> configs = particleTrail.getParticleConfigs();
            double px = targetLoc.getX();
            double py = targetLoc.getY() + 1.0;
            double pz = targetLoc.getZ();

            for (Player viewer : onlinePlayers) {
                if (!viewer.getWorld().equals(targetLoc.getWorld())) continue;
                if (viewer.getLocation().distanceSquared(targetLoc) > renderDistanceSq) continue;

                for (ParticleConfig pc : configs)
                    pc.spawn(viewer.getWorld(), px, py, pz);
            }
        }

        plugin.getBenchmarkManager().tick();
    }
}
