package com.divinedoll.edgerunners.effect;

import com.divinedoll.edgerunners.EdgerunnersMod;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEffects {
    public static final StatusEffect CYBERPSYCHOSIS = new CyberpsychosisEffect();

    public static void register() {
        Registry.register(Registries.STATUS_EFFECT,
                new Identifier(EdgerunnersMod.MOD_ID, "cyberpsychosis"), CYBERPSYCHOSIS);
    }
}
