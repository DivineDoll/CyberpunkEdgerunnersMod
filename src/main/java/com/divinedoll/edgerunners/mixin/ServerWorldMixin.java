package com.divinedoll.edgerunners.mixin;

import com.divinedoll.edgerunners.TimeSlow;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Salta el tick de las entidades cercanas a un jugador con Sandevistan activo.
 * Con require = 0, si el método cambiara no se aplica y el juego sigue funcionando
 * (quedaría solo el efecto de Lentitud del item).
 */
@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
    @Inject(method = "tickEntity(Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void edgerunners$slowTime(Entity entity, CallbackInfo ci) {
        if (TimeSlow.shouldSkip((ServerWorld) (Object) this, entity)) {
            ci.cancel();
        }
    }
}
