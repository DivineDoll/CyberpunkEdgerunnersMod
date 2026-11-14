package com.divinedoll.edgerunners;

import com.divinedoll.edgerunners.item.SandevistanItem;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

public final class ModNetworking {
    /** Paquete del cliente al servidor: el jugador pulsó la tecla del Sandevistan. */
    public static final Identifier ACTIVATE_SANDEVISTAN = new Identifier(EdgerunnersMod.MOD_ID, "activate_sandevistan");

    private ModNetworking() {
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ACTIVATE_SANDEVISTAN,
                (server, player, handler, buf, responseSender) ->
                        server.execute(() -> SandevistanItem.activate(player)));
    }
}
