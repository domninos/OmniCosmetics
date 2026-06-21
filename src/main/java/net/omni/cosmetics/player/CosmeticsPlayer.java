package net.omni.cosmetics.player;

import net.omni.cosmetics.effect.chat.CosmeticsChatColor;
import net.omni.cosmetics.effect.chat.CosmeticsPin;
import net.omni.cosmetics.effect.chat.CosmeticsTag;
import net.omni.cosmetics.effect.trails.BlockTrail;
import net.omni.cosmetics.effect.trails.ParticleTrail;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CosmeticsPlayer {

    private final UUID uuid;
    private final Player player;

    private boolean dirty;

    private CosmeticsTag activeTag;
    private CosmeticsPin activePin;
    private CosmeticsChatColor activeChatColor;

    private BlockTrail activeBlockTrail;
    private ParticleTrail activeParticleTrail;

    public CosmeticsPlayer(Player player) {
        this.player = player;
        this.uuid = player.getUniqueId();
    }

    public boolean isDirty() {
        return dirty;
    }

    public void markClean() {
        this.dirty = false;
    }

    public Player getPlayer() {
        return player;
    }

    public CosmeticsTag getActiveTag() {
        return activeTag;
    }

    public void setActiveTag(CosmeticsTag tag) {
        this.activeTag = tag;
        this.dirty = true;
    }

    public CosmeticsPin getActivePin() {
        return activePin;
    }

    public void setActivePin(CosmeticsPin pin) {
        this.activePin = pin;
        this.dirty = true;
    }

    public CosmeticsChatColor getActiveChatColor() {
        return activeChatColor;
    }

    public void setActiveChatColor(CosmeticsChatColor chatColor) {
        this.activeChatColor = chatColor;
        this.dirty = true;
    }

    public BlockTrail getActiveBlockTrail() {
        return activeBlockTrail;
    }

    public void setActiveBlockTrail(BlockTrail blockTrail) {
        this.activeBlockTrail = blockTrail;
        this.dirty = true;
    }

    public ParticleTrail getActiveParticleTrail() {
        return activeParticleTrail;
    }

    public void setActiveParticleTrail(ParticleTrail particleTrail) {
        this.activeParticleTrail = particleTrail;
        this.dirty = true;
    }

    public UUID getUUID() {
        return uuid;
    }
}
