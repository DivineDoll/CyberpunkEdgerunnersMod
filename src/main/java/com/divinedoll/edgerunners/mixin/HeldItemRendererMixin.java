package com.divinedoll.edgerunners.mixin;

import com.divinedoll.edgerunners.item.ModItems;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Primera persona con las hojas mantis (una mano con el ítem y la otra vacía):
 * se dibujan los DOS brazos con la skin del jugador y una hoja en cada uno.
 * Todo ocurre en una sola inyección; con require = 0, si el método cambiara,
 * no se aplica y el juego usa el render normal.
 */
@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {
    @Inject(
            method = "renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void edgerunners$renderBladeArms(AbstractClientPlayerEntity player, float tickDelta, float pitch,
                                             Hand hand, float swingProgress, ItemStack item, float equipProgress,
                                             MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                             int light, CallbackInfo ci) {
        ItemStack main = player.getMainHandStack();
        ItemStack off = player.getOffHandStack();
        boolean dual = (main.isOf(ModItems.MANTIS_BLADES) && off.isEmpty())
                || (off.isOf(ModItems.MANTIS_BLADES) && main.isEmpty());
        if (!dual || player.isInvisible()) {
            return;
        }

        ItemStack blade = main.isOf(ModItems.MANTIS_BLADES) ? main : off;
        Arm arm = hand == Hand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        // Las dos hojas atacan a la vez
        float swing = player.getHandSwingProgress(tickDelta);

        edgerunners$drawArm(matrices, vertexConsumers, light, player, equipProgress, swing, arm);
        edgerunners$drawBlade(matrices, vertexConsumers, light, player, blade, equipProgress, swing, arm);
        ci.cancel();
    }

    /** Mismo posicionamiento que usa el juego para dibujar un brazo vacío en primera persona. */
    private void edgerunners$drawArm(MatrixStack matrices, VertexConsumerProvider consumers, int light,
                                     AbstractClientPlayerEntity player, float equip, float swing, Arm arm) {
        boolean right = arm == Arm.RIGHT;
        float f = right ? 1.0f : -1.0f;
        float g = MathHelper.sqrt(swing);
        float h = -0.3f * MathHelper.sin(g * (float) Math.PI);
        float i = 0.4f * MathHelper.sin(g * ((float) Math.PI * 2));
        float j = -0.4f * MathHelper.sin(swing * (float) Math.PI);

        matrices.push();
        matrices.translate(f * (h + 0.64000005f), i + -0.6f + equip * -0.6f, j + -0.71999997f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(f * 45.0f));
        float k = MathHelper.sin(swing * swing * (float) Math.PI);
        float l = MathHelper.sin(g * (float) Math.PI);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(f * l * 70.0f));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f * k * -20.0f));
        matrices.translate(f * -1.0f, 3.6f, 3.5f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f * 120.0f));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(200.0f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(f * -135.0f));
        matrices.translate(f * 5.6f, 0.0f, 0.0f);

        PlayerEntityRenderer renderer = (PlayerEntityRenderer) MinecraftClient.getInstance()
                .getEntityRenderDispatcher().getRenderer(player);
        if (right) {
            renderer.renderRightArm(matrices, consumers, light, player);
        } else {
            renderer.renderLeftArm(matrices, consumers, light, player);
        }
        matrices.pop();
    }

    /** Misma colocación y movimiento de ataque que un ítem sostenido en esa mano. */
    private void edgerunners$drawBlade(MatrixStack matrices, VertexConsumerProvider consumers, int light,
                                       AbstractClientPlayerEntity player, ItemStack blade, float equip,
                                       float swing, Arm arm) {
        boolean right = arm == Arm.RIGHT;
        int side = right ? 1 : -1;
        float sqrtSwing = MathHelper.sqrt(swing);

        matrices.push();
        matrices.translate(
                side * (-0.4f * MathHelper.sin(sqrtSwing * (float) Math.PI)),
                0.2f * MathHelper.sin(sqrtSwing * ((float) Math.PI * 2)),
                -0.2f * MathHelper.sin(swing * (float) Math.PI));
        matrices.translate(side * 0.56f, -0.52f + equip * -0.6f, -0.72f);
        float s = MathHelper.sin(swing * swing * (float) Math.PI);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side * (45.0f + s * -20.0f)));
        float g = MathHelper.sin(sqrtSwing * (float) Math.PI);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(side * g * -20.0f));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -80.0f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side * -45.0f));

        ((HeldItemRenderer) (Object) this).renderItem(player, blade,
                right ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND,
                !right, matrices, consumers, light);
        matrices.pop();
    }
}
