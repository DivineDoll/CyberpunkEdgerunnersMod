package com.divinedoll.edgerunners.client;

import com.divinedoll.edgerunners.item.ModItems;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.RotationAxis;

/**
 * Tercera persona: si una mano lleva las hojas mantis y la otra está vacía,
 * dibuja otra hoja en la mano libre para que haya una en cada brazo.
 */
public class MantisBladesFeatureRenderer
        extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    private final HeldItemRenderer heldItemRenderer;

    public MantisBladesFeatureRenderer(
            FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context,
            HeldItemRenderer heldItemRenderer) {
        super(context);
        this.heldItemRenderer = heldItemRenderer;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                       AbstractClientPlayerEntity player, float limbAngle, float limbDistance, float tickDelta,
                       float animationProgress, float headYaw, float headPitch) {
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

        boolean left = freeArm == Arm.LEFT;
        matrices.push();
        this.getContextModel().setArmAngle(freeArm, matrices);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));
        matrices.translate((left ? -1 : 1) / 16.0f, 0.125f, -0.625f);
        this.heldItemRenderer.renderItem(player, blade,
                left ? ModelTransformationMode.THIRD_PERSON_LEFT_HAND : ModelTransformationMode.THIRD_PERSON_RIGHT_HAND,
                left, matrices, vertexConsumers, light);
        matrices.pop();
    }
}
