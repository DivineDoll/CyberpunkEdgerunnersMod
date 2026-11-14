package com.divinedoll.edgerunners.client;

import com.divinedoll.edgerunners.ModNetworking;
import com.divinedoll.edgerunners.item.ModItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EntityType;
import org.lwjgl.glfw.GLFW;

public class EdgerunnersClient implements ClientModInitializer {
    /** Tecla para activar el Sandevistan (se puede cambiar en Controles; por defecto V). */
    private static KeyBinding activateKey;

    @Override
    public void onInitializeClient() {
        SandevistanTrail.init();
        MantisAnimation.init();

        // Las hojas mantis se dibujan con un renderizador propio para poder animarlas
        BuiltinItemRendererRegistry.INSTANCE.register(ModItems.MANTIS_BLADES, new MantisBladesRenderer());

        // La columna del Sandevistan se dibuja en la espalda mientras lo llevas en el pecho
        ArmorRenderer.register(new SandevistanArmorRenderer(), ModItems.SANDEVISTAN);

        // Tecla de activación: avisa al servidor, que es quien aplica el efecto
        activateKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.edgerunners.sandevistan", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.categories.edgerunners"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (activateKey.wasPressed()) {
                if (client.player != null) {
                    ClientPlayNetworking.send(ModNetworking.ACTIVATE_SANDEVISTAN, PacketByteBufs.create());
                }
            }
        });

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
