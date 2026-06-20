package net.omni.cosmetics.managers;

import net.omni.cosmetics.OmniCosmetics;
import net.omni.cosmetics.effect.trails.BlockConfig;
import net.omni.cosmetics.effect.trails.BlockTrail;
import net.omni.cosmetics.player.CosmeticsPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BlockTrailManager {

    private static final int MAX_CASCADE = 5;
    private static final int MAX_FALL = 2;
    private static final long CLEANUP_MS = 2000L;
    private static final long TASK_INTERVAL = 5L;

    private static final Set<Material> CONTAINERS = Set.of(
            Material.CHEST, Material.TRAPPED_CHEST, Material.ENDER_CHEST,
            Material.HOPPER, Material.FURNACE, Material.BLAST_FURNACE,
            Material.SMOKER, Material.BARREL, Material.DROPPER,
            Material.DISPENSER, Material.SHULKER_BOX,
            Material.WHITE_SHULKER_BOX, Material.ORANGE_SHULKER_BOX,
            Material.MAGENTA_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX,
            Material.YELLOW_SHULKER_BOX, Material.LIME_SHULKER_BOX,
            Material.PINK_SHULKER_BOX, Material.GRAY_SHULKER_BOX,
            Material.LIGHT_GRAY_SHULKER_BOX, Material.CYAN_SHULKER_BOX,
            Material.PURPLE_SHULKER_BOX, Material.BLUE_SHULKER_BOX,
            Material.BROWN_SHULKER_BOX, Material.GREEN_SHULKER_BOX,
            Material.RED_SHULKER_BOX, Material.BLACK_SHULKER_BOX
    );
    private final OmniCosmetics plugin;
    private final Map<BlockKey, Long> placedBlocks = new ConcurrentHashMap<>();
    private int generatorTaskId = -1;
    private int cleanupTaskId = -1;
    private boolean worldGuardChecked;
    private boolean worldGuardPresent;

    public BlockTrailManager(OmniCosmetics plugin) {
        this.plugin = plugin;
    }

    public void restart() {
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
        for (Player target : Bukkit.getOnlinePlayers()) {
            CosmeticsPlayer cp = plugin.getPlayerManager().getPlayer(target.getUniqueId());
            if (cp == null) continue;

            BlockTrail trail = cp.getActiveBlockTrail();
            if (trail == null || !trail.isEnabled()) continue;

            if (!target.isOnGround()) continue;

            Location feetLoc = target.getLocation().subtract(0, 1, 0);
            UUID uuid = target.getUniqueId();

            placeLayer(target, uuid, trail, feetLoc);

            if (trail.getRadius() > 0) {
                Location cursor = feetLoc;
                for (int level = 0; level < MAX_CASCADE; level++) {
                    if (!isPillarLayer(cursor, trail)) break;
                    cursor = cursor.clone().add(0, -1, 0);
                    placeLayer(target, uuid, trail, cursor);
                }
            }
        }
    }

    private void placeLayer(Player target, UUID uuid, BlockTrail trail, Location center) {
        for (int dx = -trail.getRadius(); dx <= trail.getRadius(); dx++) {
            for (int dz = -trail.getRadius(); dz <= trail.getRadius(); dz++) {
                Location pos = dx == 0 && dz == 0 ? center : center.clone().add(dx, 0, dz);
                placeBlock(target, uuid, trail, pos);
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

    private void cleanup() {
        long now = System.currentTimeMillis();

        Iterator<Map.Entry<BlockKey, Long>> it = placedBlocks.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<BlockKey, Long> entry = it.next();

            if (now >= entry.getValue()) {
                BlockKey bk = entry.getKey();
                World world = Bukkit.getWorld(bk.worldName());

                if (world != null) {
                    Location loc = new Location(world, bk.x(), bk.y(), bk.z());
                    sendBlockChangeToAll(loc, world.getBlockAt(bk.x(), bk.y(), bk.z()).getBlockData());
                }

                it.remove();
            }
        }
    }

    private void placeBlock(Player target, UUID uuid, BlockTrail trail, Location loc) {
        Material type = loc.getBlock().getType();

        if (type.isAir()) {
            Location cursor = loc.clone();
            int fall = 0;
            while (fall < MAX_FALL) {
                cursor.add(0, -1, 0);
                fall++;
                if (!cursor.getBlock().getType().isAir()) {
                    loc = cursor.add(0, 1, 0);
                    break;
                }
            }
            if (fall == MAX_FALL) return;
        }

        type = loc.getBlock().getType();

        BlockData blockData = loc.getBlock().getBlockData();
        if (blockData instanceof Slab || blockData instanceof Stairs)
            return;

        if (CONTAINERS.contains(type))
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
        for (BlockConfig bc : configs) {
            r -= bc.chance();
            if (r <= 0) {
                placedBlocks.put(key, System.currentTimeMillis() + CLEANUP_MS);
                sendBlockChangeToAll(loc, bc.material().createBlockData());
                break;
            }
        }
    }

    private void sendBlockChangeToAll(Location loc, BlockData data) {
        int renderDistance = plugin.getConfigUtil().getTrailRenderDistance();
        int renderDistanceSq = renderDistance * renderDistance;
        World world = loc.getWorld();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().equals(world)) continue;
            if (player.getLocation().distanceSquared(loc) > renderDistanceSq) continue;
            player.sendBlockChange(loc, data);
        }
    }

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

    public boolean isFakeBlock(BlockKey key) {
        return placedBlocks.containsKey(key);
    }

    public void handleQuit(UUID playerId) {
        placedBlocks.keySet().removeIf(bk -> bk.playerId().equals(playerId));
    }

    public record BlockKey(UUID playerId, String worldName, int x, int y, int z) {
        public static BlockKey fromLocation(UUID playerId, Location loc) {
            return new BlockKey(playerId, loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }
    }
}
