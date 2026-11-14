package com.divinedoll.edgerunners.client;

import com.divinedoll.edgerunners.item.ModItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.entity.EntityType;

public class EdgerunnersClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SandevistanTrail.init();
        MantisAnimation.init();

        // Las hojas mantis se dibujan con un renderizador propio para poder animarlas
        BuiltinItemRendererRegistry.INSTANCE.register(ModItems.MANTIS_BLADES, new MantisBladesRenderer());

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
