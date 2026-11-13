package com.divinedoll.edgerunners.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/**
 * Efecto marcador: no hace nada por sí mismo. El cliente lo detecta para dibujar
 * la estela de imágenes fantasma y el tinte verde de pantalla.
 */
public class SandevistanEffect extends StatusEffect {
    public SandevistanEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0x19F0FF);
    }
}
