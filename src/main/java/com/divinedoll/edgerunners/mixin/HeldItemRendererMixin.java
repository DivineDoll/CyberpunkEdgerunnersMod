package com.divinedoll.edgerunners.mixin;

import com.divinedoll.edgerunners.item.ModItems;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Primera persona: dibuja la segunda hoja mantis en la mano libre.
 * Si el método objetivo cambiara, require = 0 hace que simplemente no se aplique
 * (sin romper el juego): en ese caso solo se vería una hoja en primera persona.
 */
@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {
    @Inject(
            method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
            at = @At("TAIL"),
            require = 0
    )
    private void edgerunners$renderSecondBlade(float tickDelta, MatrixStack matrices,
                                               VertexConsumerProvider.Immediate vertexConsumers,
                                               ClientPlayerEntity player, int light, CallbackInfo ci) {
        ItemStack main = player.getMainHandStack();
        ItemStack off = player.getOffHandStack();

        ItemStack blade;
        Arm freeArm;
        if (main.isOf(ModItems.MANTIS_BLADES) && off.isEmpty()) {
            blade = main;
            freeArm = player.getMainArm().getOpposite();
        } else if (off.isOf(ModItems.MANTIS_BLADES) && main.isEmpty()) {
            blade = off;
            freeArm = player.getMainArm();
        } else {
            return;
        }

        boolean right = freeArm == Arm.RIGHT;
        int side = right ? 1 : -1;
        float swing = player.getHandSwingProgress(tickDelta);
        float sqrtSwing = MathHelper.sqrt(swing);

        matrices.push();
        // Mismo movimiento de ataque que la mano principal
        matrices.translate(
                side * (-0.4f * MathHelper.sin(sqrtSwing * (float) Math.PI)),
                0.2f * MathHelper.sin(sqrtSwing * ((float) Math.PI * 2)),
                -0.2f * MathHelper.sin(swing * (float) Math.PI));
        matrices.translate(side * 0.56f, -0.52f, -0.72f);
        float s = MathHelper.sin(swing * swing * (float) Math.PI);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side * (45.0f + s * -20.0f)));
        float g = MathHelper.sin(sqrtSwing * (float) Math.PI);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(side * g * -20.0f));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -80.0f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side * -45.0f));

        ((HeldItemRenderer) (Object) this).renderItem(player, blade,
                right ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND,
                !right, matrices, vertexConsumers, light);
        matrices.pop();
        vertexConsumers.draw();
    }
}
