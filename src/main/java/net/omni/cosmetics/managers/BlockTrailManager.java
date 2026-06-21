package net.omni.cosmetics.managers;

import net.omni.cosmetics.OmniCosmetics;
import net.omni.cosmetics.effect.trails.BlockConfig;
import net.omni.cosmetics.effect.trails.BlockTrail;
import net.omni.cosmetics.player.CosmeticsPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BlockTrailManager {

    private static final int MAX_CASCADE = 5;
    private static final long CLEANUP_MS = 2000L;
    private static final long TASK_INTERVAL = 5L;

    private final OmniCosmetics plugin;
    private final Map<BlockKey, Long> placedBlocks = new ConcurrentHashMap<>();
    private final Map<UUID, BlockPos> lastPositions = new ConcurrentHashMap<>();
    private Set<Material> blacklist = Set.of();
    private int generatorTaskId = -1;
    private int cleanupTaskId = -1;
    private boolean worldGuardChecked;
    private boolean worldGuardPresent;

    public BlockTrailManager(OmniCosmetics plugin) {
        this.plugin = plugin;
        reloadBlacklist();
    }

    public void reloadBlacklist() {
        Set<String> names = plugin.getConfigUtil().getBlockBlacklist();
        Set<Material> mats = new HashSet<>();

        for (String name : names) {
            try {
                mats.add(Material.valueOf(name.toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }

        this.blacklist = mats.isEmpty() ? Set.of() : Collections.unmodifiableSet(mats);

        mats.clear();
    }

    public void restart() {
        reloadBlacklist();
        stop();
        start();
    }

    public void stop() {
        if (generatorTaskId != -1) {
            Bukkit.getScheduler().cancelTask(generatorTaskId);
            generatorTaskId = -1;
        }

        if (cleanupTaskId != -1) {
            Bukkit.getScheduler().cancelTask(cleanupTaskId);
            cleanupTaskId = -1;
        }
    }

    public void start() {
        generatorTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::generate, 0L, TASK_INTERVAL).getTaskId();
        cleanupTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::cleanup, CLEANUP_MS / 50L, TASK_INTERVAL).getTaskId();
    }

    private void generate() {
        for (CosmeticsPlayer cp : plugin.getPlayerManager().getPlayers().values()) {
            if (cp == null)
                continue;

            Player target = cp.getPlayer();

            if (target == null)
                continue;

            BlockTrail trail = cp.getActiveBlockTrail();
            if (trail == null || !trail.isEnabled())
                continue;

            if (!target.isOnGround())
                continue;

            UUID uuid = target.getUniqueId();
            int bx = target.getLocation().getBlockX();
            int bz = target.getLocation().getBlockZ();
            BlockPos lastPos = lastPositions.get(uuid);

            if (lastPos != null && lastPos.x() == bx && lastPos.z() == bz)
                continue;

            lastPositions.put(uuid, new BlockPos(bx, bz));

            Location feetLoc = target.getLocation().subtract(0, 1, 0);

            if (trail.getRadius() > 0 && isPillarLayer(feetLoc, trail)) {
                placeBlock(uuid, trail, feetLoc);
                Location cursor = feetLoc;

                for (int level = 0; level < MAX_CASCADE; level++) {
                    if (!isPillarLayer(cursor, trail))
                        break;

                    cursor = cursor.clone().add(0, -1, 0);
                    placeLayer(uuid, trail, cursor);
                }
            } else {
                placeLayer(uuid, trail, feetLoc);

                if (trail.getRadius() > 0) {
                    Location cursor = feetLoc;

                    for (int level = 0; level < MAX_CASCADE; level++) {
                        if (!isPillarLayer(cursor, trail))
                            break;

                        cursor = cursor.clone().add(0, -1, 0);
                        placeLayer(uuid, trail, cursor);
                    }
                }
            }
        }
    }

    private void cleanup() {
        long now = System.currentTimeMillis();

        Iterator<Map.Entry<BlockKey, Long>> it = placedBlocks.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<BlockKey, Long> entry = it.next();

            if (now >= entry.getValue()) {
                BlockKey bk = entry.getKey();

                sendBlockChangeToAll(bk.location(), bk.location().getBlock().getBlockData());
                it.remove();
            }
        }
    }

    private boolean isPillarLayer(Location center, BlockTrail trail) {
        for (int dx = -trail.getRadius(); dx <= trail.getRadius(); dx++) {
            for (int dz = -trail.getRadius(); dz <= trail.getRadius(); dz++) {
                if (dx == 0 && dz == 0) continue;
                if (center.clone().add(dx, 0, dz).getBlock().getType().isSolid())
                    return false;
            }
        }
        return true;
    }

    private void placeBlock(UUID uuid, BlockTrail trail, Location loc) {
        Material type = loc.getBlock().getType();

        if (type.isAir())
            return;

        BlockData blockData = loc.getBlock().getBlockData();
        if (blockData instanceof Slab || blockData instanceof Stairs)
            return;

        if (blacklist.contains(type))
            return;

        if (isWorldGuardProtected(loc))
            return;

        BlockKey key = BlockKey.fromLocation(uuid, loc);
        if (placedBlocks.containsKey(key))
            return;

        List<BlockConfig> configs = trail.getBlockConfigs();

        double total = 0;

        for (BlockConfig bc : configs)
            total += bc.chance();

        if (total <= 0)
            return;

        double r = Math.random() * total;

        // randomized block trail for benchmark
        for (BlockConfig bc : configs) {
            r -= bc.chance();

            if (r <= 0) {
                placedBlocks.put(key, System.currentTimeMillis() + CLEANUP_MS);
                sendBlockChangeToAll(loc, bc.material().createBlockData());
                break;
            }
        }
    }

    private void placeLayer(UUID uuid, BlockTrail trail, Location center) {
        for (int dx = -trail.getRadius(); dx <= trail.getRadius(); dx++) {
            for (int dz = -trail.getRadius(); dz <= trail.getRadius(); dz++) {
                Location pos = dx == 0 && dz == 0 ? center : center.clone().add(dx, 0, dz);
                placeBlock(uuid, trail, pos);
            }
        }
    }

    private void sendBlockChangeToAll(Location loc, BlockData data) {
        int renderDistance = plugin.getConfigUtil().getTrailRenderDistance();
        int renderDistanceSq = renderDistance * renderDistance;
        World world = loc.getWorld();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().equals(world))
                continue;

            if (player.getLocation().distanceSquared(loc) > renderDistanceSq)
                continue;

            player.sendBlockChange(loc, data);
        }
    }

    // TODO hook wg
    private boolean isWorldGuardProtected(Location location) {
        if (!worldGuardChecked) {
            try {
                Class.forName("com.sk89q.worldguard.WorldGuard");
                worldGuardPresent = true;
            } catch (ClassNotFoundException e) {
                worldGuardPresent = false;
            }
            worldGuardChecked = true;
        }

        if (!worldGuardPresent) return false;

        try {
            Object wg = Class.forName("com.sk89q.worldguard.WorldGuard")
                    .getMethod("getInstance").invoke(null);
            Object container = wg.getClass().getMethod("getRegionContainer").invoke(wg);
            Object query = container.getClass().getMethod("createQuery").invoke(container);
            Object wgLoc = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter")
                    .getMethod("adapt", Location.class).invoke(null, location);
            Object regions = query.getClass().getMethod("getApplicableRegions",
                            Class.forName("com.sk89q.worldedit.util.Location"))
                    .invoke(query, wgLoc);
            int count = (int) regions.getClass().getMethod("size").invoke(regions);
            return count > 0;
        } catch (Exception ignored) {
            return false;
        }
    }

    public void placeBenchmarkBlock(UUID playerId, BlockConfig bc, Location loc) {
        BlockKey key = BlockKey.fromLocation(playerId, loc);

        if (placedBlocks.containsKey(key))
            return;

        placedBlocks.put(key, System.currentTimeMillis() + CLEANUP_MS);
        sendBlockChangeToAll(loc, bc.material().createBlockData());
    }

    public boolean isFakeBlock(BlockKey key) {
        return placedBlocks.containsKey(key);
    }

    public boolean isFakeBlock(Block block) {
        return placedBlocks.keySet().stream().anyMatch(key -> key.x() == block.getX() && key.y() == block.getY() && key.z() == block.getZ());
    }

    public void handleQuit(UUID playerId) {
        placedBlocks.keySet().removeIf(bk -> bk.playerId().equals(playerId));
        lastPositions.remove(playerId);
    }

    public boolean hasEnabled(Player player) {
        return placedBlocks.keySet().stream().anyMatch(bk -> bk.playerId().equals(player.getUniqueId()));
    }

    private record BlockPos(int x, int z) {
    }

    public static class BlockKey {
        private final UUID uuid;
        private final Location location;

        public BlockKey(UUID playerId, Location location) {
            this.uuid = playerId;
            this.location = location;
        }

        public static BlockKey fromLocation(UUID playerId, Location location) {
            return new BlockKey(playerId, location);
        }

        public int x() {
            return location.getBlockX();
        }

        public int y() {
            return location.getBlockY();
        }

        public int z() {
            return location.getBlockZ();
        }

        public Location location() {
            return location;
        }

        public UUID playerId() {
            return uuid;
        }
    }
}
