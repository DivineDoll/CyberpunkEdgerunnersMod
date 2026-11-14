package com.divinedoll.edgerunners.client;

import com.divinedoll.edgerunners.EdgerunnersMod;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/** Dibuja la columna mecánica del Sandevistan en la espalda de quien lo lleva en el pecho. */
public class SandevistanArmorRenderer implements ArmorRenderer {
    private static final Identifier TEXTURE =
            new Identifier(EdgerunnersMod.MOD_ID, "textures/item/sandevistan_spine.png");
    /** Orden de caras en UV: norte, este, sur, oeste, arriba, abajo. */
    private static final float BACK_Z = 2.0f;
    private static final float TOP_Y = 0.6f;

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider consumers, ItemStack stack, LivingEntity entity,
                       EquipmentSlot slot, int light, BipedEntityModel<LivingEntity> model) {
        if (slot != EquipmentSlot.CHEST) {
            return;
        }
        matrices.push();
        model.body.rotate(matrices);
        VertexConsumer vc = consumers.getBuffer(RenderLayer.getEntityCutoutNoCull(TEXTURE));
        float pulse = 0.75f + 0.25f * (float) Math.sin((entity.age) * 0.2);
        int glowLight = LightmapTextureManager.MAX_LIGHT_COORDINATE;
        for (int i = 0; i < SandevistanSpineParts.BOX.length; i++) {
            float[] b = SandevistanSpineParts.BOX[i];
            matrices.push();
            // x lateral, y hacia abajo (t), z hacia atrás (w) en píxeles
            float cx = (b[0] + b[3]) / 2f, cy = TOP_Y + (b[1] + b[4]) / 2f, cz = BACK_Z + (b[2] + b[5]) / 2f;
            matrices.translate(cx / 16f, cy / 16f, cz / 16f);
            if (SandevistanSpineParts.ROT[i] != 0f) {
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(SandevistanSpineParts.ROT[i]));
            }
            float hx = (b[3] - b[0]) / 2f / 16f, hy = (b[4] - b[1]) / 2f / 16f, hz = (b[5] - b[2]) / 2f / 16f;
            boolean glow = SandevistanSpineParts.GLOW[i];
            drawBox(matrices, vc, hx, hy, hz, SandevistanSpineParts.UV[i],
                    glow ? glowLight : light, glow ? pulse : 1f);
            matrices.pop();
        }
        matrices.pop();
    }

    private static void drawBox(MatrixStack matrices, VertexConsumer vc, float hx, float hy, float hz,
                                int[] uv, int light, float bright) {
        Matrix4f pos = matrices.peek().getPositionMatrix();
        Matrix3f nor = matrices.peek().getNormalMatrix();
        // x: derecha del jugador es -x en el espacio del cuerpo visto desde atrás; basta con ser consistente.
        face(vc, pos, nor, uv, 0, light, bright, 0, 0, -1, -hx, -hy, -hz, hx, -hy, -hz, hx, hy, -hz, -hx, hy, -hz); // north (-z)
        face(vc, pos, nor, uv, 4, light, bright, 1, 0, 0, hx, -hy, -hz, hx, -hy, hz, hx, hy, hz, hx, hy, -hz);      // east
        face(vc, pos, nor, uv, 8, light, bright, 0, 0, 1, hx, -hy, hz, -hx, -hy, hz, -hx, hy, hz, hx, hy, hz);     // south (+z, visible)
        face(vc, pos, nor, uv, 12, light, bright, -1, 0, 0, -hx, -hy, hz, -hx, -hy, -hz, -hx, hy, -hz, -hx, hy, hz); // west
        face(vc, pos, nor, uv, 16, light, bright, 0, -1, 0, -hx, -hy, hz, hx, -hy, hz, hx, -hy, -hz, -hx, -hy, -hz); // up
        face(vc, pos, nor, uv, 20, light, bright, 0, 1, 0, -hx, hy, -hz, hx, hy, -hz, hx, hy, hz, -hx, hy, hz);    // down
    }

    private static void face(VertexConsumer vc, Matrix4f pos, Matrix3f nor, int[] uv, int o, int light, float bright,
                             float nx, float ny, float nz,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float x3, float y3, float z3, float x4, float y4, float z4) {
        float u1 = uv[o] / 64f, v1 = uv[o + 1] / 64f, u2 = uv[o + 2] / 64f, v2 = uv[o + 3] / 64f;
        v(vc, pos, nor, x1, y1, z1, u1, v1, light, bright, nx, ny, nz);
        v(vc, pos, nor, x2, y2, z2, u2, v1, light, bright, nx, ny, nz);
        v(vc, pos, nor, x3, y3, z3, u2, v2, light, bright, nx, ny, nz);
        v(vc, pos, nor, x4, y4, z4, u1, v2, light, bright, nx, ny, nz);
    }

    private static void v(VertexConsumer vc, Matrix4f pos, Matrix3f nor, float x, float y, float z,
                          float u, float vv, int light, float bright, float nx, float ny, float nz) {
        int c = Math.round(255 * Math.min(1f, bright));
        vc.vertex(pos, x, y, z).color(c, c, c, 255).texture(u, vv)
                .overlay(OverlayTexture.DEFAULT_UV).light(light).normal(nor, nx, ny, nz).next();
    }
}
