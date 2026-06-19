package net.omni.cosmetics.player;

import net.omni.cosmetics.effect.chat.CosmeticsTag;
import net.omni.cosmetics.effect.trails.BlockTrail;
import net.omni.cosmetics.effect.trails.ParticleTrail;

import java.util.UUID;

public class CosmeticsPlayer {

    private final UUID uuid;
    private CosmeticsTag activeTag;
    private BlockTrail activeBlockTrail;
    private ParticleTrail activePartileTrail;

    public CosmeticsPlayer(UUID uuid) {
        this.uuid = uuid;
    }

    public CosmeticsTag getActiveTag() {
        return activeTag;
    }

    public void setActiveTag(CosmeticsTag tag) {
        this.activeTag = tag;
    }

    public BlockTrail getActiveBlockTrail() {
        return activeBlockTrail;
    }

    public void setActiveBlockTrail(BlockTrail blockTrail) {
        this.activeBlockTrail = blockTrail;
    }

    public ParticleTrail getActivePartileTrail() {
        return activePartileTrail;
    }

    public void setActivePartileTrail(ParticleTrail particleTrail) {
        this.activePartileTrail = particleTrail;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void load(UUID uuid) {

    }
}
