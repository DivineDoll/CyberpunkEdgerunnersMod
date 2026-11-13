package com.divinedoll.edgerunners.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.entity.EntityType;

public class EdgerunnersClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SandevistanTrail.init();

        // Hoja mantis extra en la mano libre (tercera persona)
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register(
                (entityType, entityRenderer, registrationHelper, context) -> {
                    if (entityType == EntityType.PLAYER && entityRenderer instanceof PlayerEntityRenderer playerRenderer) {
                        registrationHelper.register(
                                new MantisBladesFeatureRenderer(playerRenderer, context.getHeldItemRenderer()));
                    }
                });
    }
}
