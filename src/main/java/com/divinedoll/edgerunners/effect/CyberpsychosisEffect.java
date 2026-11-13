package com.divinedoll.edgerunners.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/**
 * Cyberpsicosis: más daño de ataque, pero te va desgastando.
 * El desgaste se detiene cuando te quedan 1 corazón o menos, así que no mata por sí solo.
 */
public class CyberpsychosisEffect extends StatusEffect {
    public CyberpsychosisEffect() {
        super(StatusEffectCategory.HARMFUL, 0xFF2A6D);
        this.addAttributeModifier(
                EntityAttributes.GENERIC_ATTACK_DAMAGE,
                "7f3c2a10-5b6e-4d1a-9c52-0e8d3a1b4c77",
                3.0,
                EntityAttributeModifier.Operation.ADDITION
        );
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration % 40 == 0;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity.getHealth() > 2.0f) {
            entity.damage(entity.getDamageSources().magic(), 1.0f);
        }
    }
}
