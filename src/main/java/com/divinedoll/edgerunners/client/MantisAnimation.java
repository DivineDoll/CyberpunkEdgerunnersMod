package com.divinedoll.edgerunners.client;

import com.divinedoll.edgerunners.item.ModItems;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;

/**
 * Estado de la animación de despliegue en primera persona.
 * Mientras llevas las hojas en la mano y no están plegadas, el progreso sube hasta 1;
 * si no las llevas, baja hasta 0, así que al equiparlas se vuelven a desplegar.
 */
public final class MantisAnimation {
    public static final String RETRACTED_KEY = "Retracted";

    private static float prev = 0.0f;
    private static float cur = 0.0f;

    private MantisAnimation() {
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(MantisAnimation::tick);
    }

    public static boolean isRetracted(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        return nbt != null && nbt.getBoolean(RETRACTED_KEY);
    }

    private static ItemStack heldBlades(ClientPlayerEntity player) {
        ItemStack main = player.getMainHandStack();
        if (main.isOf(ModItems.MANTIS_BLADES)) {
            return main;
        }
        ItemStack off = player.getOffHandStack();
        return off.isOf(ModItems.MANTIS_BLADES) ? off : null;
    }

    private static void tick(MinecraftClient client) {
        prev = cur;
        boolean deployed = false;
        if (client.player != null) {
            ItemStack blades = heldBlades(client.player);
            deployed = blades != null && !isRetracted(blades);
        }
        cur = MathHelper.clamp(cur + (deployed ? 0.09f : -0.14f), 0.0f, 1.0f);
    }

    /** Progreso de despliegue interpolado entre ticks: 0 = plegada, 1 = desplegada. */
    public static float deploy(float tickDelta) {
        return MathHelper.lerp(tickDelta, prev, cur);
    }
}
