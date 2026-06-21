package net.omni.cosmetics.managers;

import net.omni.cosmetics.OmniCosmetics;
import net.omni.cosmetics.effect.trails.BlockConfig;
import net.omni.cosmetics.effect.trails.ParticleConfig;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class BenchmarkManager {

    private static final long REPORT_INTERVAL = 200;
    private static final int MAX_RADIUS = 5;

    private final OmniCosmetics plugin;
    private final Map<UUID, Integer> activePlayers = new ConcurrentHashMap<>();

    private List<ParticleConfig> pool = new ArrayList<>();
    private List<BlockConfig> blockPool = new ArrayList<>();

    private int tickCounter;
    private long windowTickTime;
    private int windowEntities;
    private int windowParticles;
    private int peakEntities;
    private int peakParticles;
    private int totalTicks;

    public BenchmarkManager(OmniCosmetics plugin) {
        this.plugin = plugin;
    }

    public boolean isBenchmarking(Player player) {
        return activePlayers.containsKey(player.getUniqueId());
    }

    public void flush() {
        activePlayers.clear();
        pool.clear();
        blockPool.clear();
    }

    public void startBenchmark(Player player, int radius) {
        if (radius < 1)
            radius = 1;

        if (radius > MAX_RADIUS)
            radius = MAX_RADIUS;

        activePlayers.put(player.getUniqueId(), radius);
        rebuildPool();

        plugin.sendConsole("<yellow>[Benchmark] Started for " + player.getName() + " (radius=" + radius + ")</yellow>");
    }

    public void rebuildPool() {
        pool = plugin.getCosmeticsManager().getParticleTrails().stream()
                .flatMap(t -> t.getParticleConfigs().stream())
                .collect(Collectors.toList());
        blockPool = plugin.getCosmeticsManager().getBlockTrails().stream()
                .flatMap(t -> t.getBlockConfigs().stream())
                .collect(Collectors.toList());

        if (pool.isEmpty())
            plugin.sendConsole("<red>[Benchmark] No particle configs found in loaded trails</red>");

        if (blockPool.isEmpty())
            plugin.sendConsole("<red>[Benchmark] No block configs found in loaded trails</red>");
    }

    public void stopBenchmark(UUID uuid) {
        if (activePlayers.remove(uuid) != null) {
            printFinalReport();
            resetMetrics();
        }
    }

    private void printFinalReport() {
        plugin.sendConsole("<gray>[Benchmark] Stopped. Peak ent/tick: <yellow>" + peakEntities
                + "</yellow> | Peak ptc/tick: <yellow>" + peakParticles
                + "</yellow> | Total ticks: <yellow>" + totalTicks + "</yellow></gray>");
    }

    private void resetMetrics() {
        tickCounter = 0;
        windowTickTime = 0;
        windowEntities = 0;
        windowParticles = 0;
        peakEntities = 0;
        peakParticles = 0;
        totalTicks = 0;
    }

    public void stopAll() {
        if (!activePlayers.isEmpty()) {
            activePlayers.clear();
            printFinalReport();
        }

        resetMetrics();
    }

    public void tick() {
        if (activePlayers.isEmpty())
            return;

        long start = System.nanoTime();
        int entitiesThisTick = 0;
        int particlesThisTick = 0;

        for (Map.Entry<UUID, Integer> entry : activePlayers.entrySet()) {
            UUID uuid = entry.getKey();
            int radius = entry.getValue();

            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                activePlayers.remove(uuid);
                continue;
            }

            World world = player.getWorld();
            int cx = player.getLocation().getBlockX() >> 4;
            int cz = player.getLocation().getBlockZ() >> 4;

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (!world.isChunkLoaded(cx + dx, cz + dz))
                        continue;

                    Chunk chunk = world.getChunkAt(cx + dx, cz + dz);

                    for (Entity entity : chunk.getEntities()) {
                        if (!(entity instanceof LivingEntity living))
                            continue;

                        if (living instanceof Player p && p.equals(player))
                            continue;

                        entitiesThisTick++;

                        if (!pool.isEmpty()) {
                            ParticleConfig pc = pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
                            pc.spawn(world, living.getX(), living.getY() + 1.0, living.getZ());
                            particlesThisTick++;
                        }

                        if (!blockPool.isEmpty()) {
                            BlockConfig bc = blockPool.get(ThreadLocalRandom.current().nextInt(blockPool.size()));

                            Location blockLoc = living.getLocation().clone().subtract(0, 1, 0);
                            plugin.getBlockTrailManager().placeBenchmarkBlock(uuid, bc, blockLoc);
                            particlesThisTick++;
                        }
                    }
                }
            }
        }

        windowTickTime += System.nanoTime() - start;
        windowEntities += entitiesThisTick;
        windowParticles += particlesThisTick;
        if (entitiesThisTick > peakEntities) peakEntities = entitiesThisTick;
        if (particlesThisTick > peakParticles) peakParticles = particlesThisTick;
        totalTicks++;
        tickCounter++;

        if (tickCounter >= REPORT_INTERVAL) {
            printReport();
            tickCounter = 0;
            windowEntities = 0;
            windowParticles = 0;
            windowTickTime = 0;
        }
    }

    private void printReport() {
        long avgEntities = windowEntities / REPORT_INTERVAL;
        long avgParticles = windowParticles / REPORT_INTERVAL;
        double avgMicros = windowTickTime / 1000.0 / REPORT_INTERVAL;
        double tps = Bukkit.getTPS()[0];

        plugin.sendConsole("<gray>[Benchmark]</gray> Ent/tick: <yellow>" + avgEntities
                + "</yellow> | Ptc/tick: <yellow>" + avgParticles
                + "</yellow> | Avg: <yellow>" + String.format("%.1f", avgMicros) + "µs</yellow> | TPS: <yellow>" + String.format("%.1f", tps) + "</yellow>");
    }
}
