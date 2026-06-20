package net.omni.cosmetics;

import net.omni.cosmetics.effect.chat.CosmeticsChatColor;
import net.omni.cosmetics.effect.chat.CosmeticsPin;
import net.omni.cosmetics.effect.chat.CosmeticsTag;
import net.omni.cosmetics.effect.trails.BlockTrail;
import net.omni.cosmetics.effect.trails.ParticleTrail;
import net.omni.cosmetics.player.CosmeticsPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class CosmeticsAPI {

    private static OmniCosmetics instance;

    static void init(OmniCosmetics plugin) {
        instance = plugin;
    }

    @Nullable
    public static CosmeticsPlayer getPlayer(Player player) {
        return instance.getPlayerManager().getPlayer(player.getUniqueId());
    }

    @Nullable
    public static CosmeticsTag getActiveTag(Player player) {
        CosmeticsPlayer cp = getPlayer(player);
        return cp != null ? cp.getActiveTag() : null;
    }

    @Nullable
    public static CosmeticsPin getActivePin(Player player) {
        CosmeticsPlayer cp = getPlayer(player);
        return cp != null ? cp.getActivePin() : null;
    }

    @Nullable
    public static CosmeticsChatColor getActiveChatColor(Player player) {
        CosmeticsPlayer cp = getPlayer(player);
        return cp != null ? cp.getActiveChatColor() : null;
    }

    @Nullable
    public static ParticleTrail getActiveParticleTrail(Player player) {
        CosmeticsPlayer cp = getPlayer(player);
        return cp != null ? cp.getActiveParticleTrail() : null;
    }

    @Nullable
    public static BlockTrail getActiveBlockTrail(Player player) {
        CosmeticsPlayer cp = getPlayer(player);
        return cp != null ? cp.getActiveBlockTrail() : null;
    }
}
