package net.omni.cosmetics.effect.trails;

import net.omni.cosmetics.effect.CosmeticCategory;
import net.omni.cosmetics.effect.CosmeticOperator;

import java.util.List;

public class ParticleTrail extends CosmeticsTrail {
    private final List<ParticleConfig> particleConfigs;

    public ParticleTrail(String name, boolean enabled, CosmeticCategory category, String permission, int stars, String command, CosmeticOperator operator, String itemName, List<String> itemLore, String itemType, List<ParticleConfig> particleConfigs) {
        super(name, enabled, category, permission, stars, command, operator, itemName, itemLore, itemType);
        this.particleConfigs = particleConfigs;
    }

    public List<ParticleConfig> getParticleConfigs() {
        return particleConfigs;
    }
}
