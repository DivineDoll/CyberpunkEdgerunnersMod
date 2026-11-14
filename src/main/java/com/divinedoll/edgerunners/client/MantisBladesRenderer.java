package com.divinedoll.edgerunners.client;

import com.divinedoll.edgerunners.EdgerunnersMod;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * Dibuja las hojas mantis con animación (Minecraft no admite ángulos intermedios en los
 * modelos JSON, así que las piezas se dibujan a mano):
 * - Despliegue / pliegue: la hoja gira desde la bisagra y sale como un resorte.
 * - Ataque: tajo rápido que abre un poco más la hoja.
 * - Brillo pulsante: los LED cian y las líneas rojas laten.
 * - El brazo blanco articulado se recalcula cada cuadro (cinemática inversa de 2 tramos).
 *
 * Hay dos formas de usarlo:
 * - render(): como ítem (tercera persona, inventario, suelo), con las transformaciones del modelo JSON.
 * - renderOnArm(): en primera persona, montado directamente sobre el antebrazo del jugador.
 */
public class MantisBladesRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
    private static final Identifier TEXTURE =
            new Identifier(EdgerunnersMod.MOD_ID, "textures/item/mantis_blade_pro.png");
    private static final float TEX = 64.0f;

    // Montaje sobre el antebrazo (unidades de pixel del modelo del brazo)
    private static final float MOUNT_SCALE = 0.65f;  // tamaño de las hojas respecto al modelo
    private static final float MOUNT_UP = 2.4f;      // altura del eje de la carcasa sobre el centro del brazo
    private static final float MOUNT_Y0 = 0.5f;      // posición de la bisagra a lo largo del brazo
    private static final float SQRT_HALF = 0.70710678f;

    // Baldosas de la textura usadas por el brazo articulado y las juntas
    private static final int[] UV_WHITE = uniform(0, 8, 8, 16);
    private static final int[] UV_CLAMP = uniform(40, 8, 48, 16);
    private static final int[] UV_CYAN = uniform(48, 8, 56, 16);

    // ------------------------------------------------------------------ como ítem

    @Override
    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices,
                       VertexConsumerProvider consumers, int light, int overlay) {
        MinecraftClient mc = MinecraftClient.getInstance();
        float delta = mc.getTickDelta();
        boolean firstPerson = mode.isFirstPerson();

        // En primera persona el progreso es suave; en el resto se ve el estado guardado en el ítem
        float deploy = firstPerson ? MantisAnimation.deploy(delta)
                : (MantisAnimation.isRetracted(stack) ? 0.0f : 1.0f);
        float swing = 0.0f;
        if (firstPerson && mc.player != null) {
            swing = mc.player.getHandSwingProgress(delta);
        }
        float time = (mc.world != null ? mc.world.getTime() : 0L) + delta;

        VertexConsumer vc = consumers.getBuffer(RenderLayer.getEntityCutoutNoCull(TEXTURE));

        matrices.push();
        matrices.scale(1.0f / 16.0f, 1.0f / 16.0f, 1.0f / 16.0f);
        // El modelo se diseñó con el pivote en (PIVOT_X, PIVOT_Y) y la carcasa a 22.5 grados
        matrices.translate(MantisBladesParts.PIVOT_X, MantisBladesParts.PIVOT_Y, 8.0f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MantisBladesParts.HOUSING_ANGLE));
        drawPivotSpace(matrices, vc, light, overlay, deploy, swing, time);
        matrices.pop();
    }

    // ------------------------------------------------------------------ sobre el antebrazo

    /**
     * Dibuja las hojas montadas sobre el antebrazo en primera persona.
     * Se llama con la pila de matrices ya colocada en el espacio del brazo (el mismo en el que
     * se dibuja el brazo con la skin): en ese espacio el eje Y apunta del codo a la mano.
     * El lado de arriba del brazo es -X para el derecho y +X para el izquierdo.
     */
    public static void renderOnArm(MatrixStack matrices, VertexConsumerProvider consumers, int light,
                                   int overlay, boolean right, boolean slim, float swing) {
        MinecraftClient mc = MinecraftClient.getInstance();
        float delta = mc.getTickDelta();
        float deploy = MantisAnimation.deploy(delta);
        float time = (mc.world != null ? mc.world.getTime() : 0L) + delta;
        VertexConsumer vc = consumers.getBuffer(RenderLayer.getEntityCutoutNoCull(TEXTURE));

        float side = right ? 1.0f : -1.0f;
        float centerX = -side * (slim ? 0.5f : 1.0f);   // centro del brazo respecto a su pivote
        float pivotX = -side * 5.0f;                    // pivote del hombro

        matrices.push();
        matrices.translate(pivotX / 16.0f, 2.0f / 16.0f, 0.0f);
        matrices.translate((centerX - side * MOUNT_UP) / 16.0f, MOUNT_Y0 / 16.0f, 0.0f);
        if (right) {
            // eje de la carcasa -> Y del brazo, arriba de la carcasa -> -X
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0f));
        } else {
            // eje de la carcasa -> Y del brazo, arriba de la carcasa -> +X (giro de 180 grados sobre (1,1,0))
            matrices.multiply(new Quaternionf().rotationAxis((float) Math.PI, SQRT_HALF, SQRT_HALF, 0.0f));
        }
        float s = MOUNT_SCALE / 16.0f;
        matrices.scale(s, s, s);
        drawPivotSpace(matrices, vc, light, overlay, deploy, swing, time);
        matrices.pop();
    }

    // ------------------------------------------------------------------ dibujo

    /**
     * Dibuja todo en el "espacio del pivote": el origen es la bisagra, la carcasa del antebrazo va
     * a lo largo de +X, +Y es "arriba" de la carcasa y Z queda centrada en 0. Las unidades son
     * pixeles del modelo (la pila de matrices ya debe estar escalada a 1/16).
     */
    private static void drawPivotSpace(MatrixStack matrices, VertexConsumer vc, int light, int overlay,
                                       float deploy, float swing, float time) {
        float eased = smooth(deploy);
        float slash = swing > 0.0f ? MathHelper.sin(MathHelper.sqrt(swing) * (float) Math.PI) : 0.0f;
        // Ángulo de la hoja respecto a la carcasa: 0 = plegada, 22.5 = desplegada
        float relAngle = (MantisBladesParts.BLADE_ANGLE - MantisBladesParts.HOUSING_ANGLE) * eased
                + 14.0f * slash * eased;
        float bladeScale = (0.12f + 0.88f * smooth(MathHelper.clamp((deploy - 0.15f) / 0.85f, 0.0f, 1.0f)))
                * (1.0f + 0.06f * slash);

        // Brillo: late siempre, más apagado plegada, destello al desplegar y al atacar
        float pulse = 0.65f + 0.35f * MathHelper.sin(time * 0.3f);
        float transition = deploy > 0.0f && deploy < 1.0f ? 1.0f - Math.abs(2.0f * deploy - 1.0f) : 0.0f;
        float bright = MathHelper.clamp(pulse * (0.35f + 0.65f * eased) + 0.5f * transition + 0.6f * slash, 0.0f, 1.0f);
        int glowRgb = Math.round(255.0f * bright);
        int fullBright = LightmapTextureManager.MAX_LIGHT_COORDINATE;

        // ---- Carcasa del antebrazo ----
        drawParts(matrices, vc, 0, false, light, fullBright, overlay, glowRgb);

        // ---- Hoja: la base no se escala, el resto sale como un resorte ----
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(relAngle));
        drawParts(matrices, vc, 1, false, light, fullBright, overlay, glowRgb);
        float o = MantisBladesParts.BLADE_SCALE_ORIGIN;
        matrices.translate(o, 0.0f, 0.0f);
        matrices.scale(bladeScale, 1.0f, 1.0f);
        matrices.translate(-o, 0.0f, 0.0f);
        drawParts(matrices, vc, 1, true, light, fullBright, overlay, glowRgb);
        matrices.pop();

        // ---- Brazo articulado de dos tramos (cinemática inversa) ----
        float h1x = MantisBladesParts.H1_LX;
        float h1y = MantisBladesParts.H1_LY;
        float bx = o + (MantisBladesParts.B1_LX - o) * bladeScale;
        float[] b1 = rotate(relAngle, bx, MantisBladesParts.B1_LY);
        float dx = b1[0] - h1x;
        float dy = b1[1] - h1y;
        float rawDist = (float) Math.sqrt(dx * dx + dy * dy);
        float a = MantisBladesParts.LEN_A;
        float b = MantisBladesParts.LEN_B;
        float dist = MathHelper.clamp(rawDist, Math.abs(a - b) + 0.05f, a + b - 0.05f);
        float ux = 1.0f;
        float uy = 0.0f;
        if (rawDist > 1.0e-4f) {
            ux = dx / rawDist;
            uy = dy / rawDist;
        }
        float endX = h1x + ux * dist;
        float endY = h1y + uy * dist;
        float along = (a * a - b * b + dist * dist) / (2.0f * dist);
        float height = (float) Math.sqrt(Math.max(0.0f, a * a - along * along)) * MantisBladesParts.ELBOW_SIGN;
        float jx = h1x + ux * along - uy * height;
        float jy = h1y + uy * along + ux * height;

        strut(matrices, vc, h1x, h1y, jx, jy, light, overlay);
        strut(matrices, vc, jx, jy, endX, endY, light, overlay);
        joint(matrices, vc, h1x, h1y, light, fullBright, overlay, glowRgb);
        joint(matrices, vc, jx, jy, light, fullBright, overlay, glowRgb);
        joint(matrices, vc, endX, endY, light, fullBright, overlay, glowRgb);
    }

    /** Dibuja las piezas de un marco. Para la hoja: scaled=false las de la base, true las que se escalan. */
    private static void drawParts(MatrixStack matrices, VertexConsumer vc, int frame, boolean scaled,
                                  int light, int fullBright, int overlay, int glowRgb) {
        for (int i = 0; i < MantisBladesParts.FRAME.length; i++) {
            if (MantisBladesParts.FRAME[i] != frame) {
                continue;
            }
            float[] box = MantisBladesParts.BOX[i];
            if (frame == 1 && (box[3] > MantisBladesParts.BLADE_SCALE_ORIGIN + 0.001f) != scaled) {
                continue;
            }
            boolean glow = MantisBladesParts.GLOW[i];
            drawBox(matrices, vc, box[0], box[1], box[2], box[3], box[4], box[5],
                    MantisBladesParts.UV[i], glow ? fullBright : light, overlay, glow ? glowRgb : 255);
        }
    }

    /** Un tramo del brazo: caja que va del punto (x1,y1) al (x2,y2), en dos listones paralelos. */
    private static void strut(MatrixStack matrices, VertexConsumer vc, float x1, float y1, float x2, float y2,
                              int light, int overlay) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        float angle = (float) Math.toDegrees(Math.atan2(dy, dx));
        matrices.push();
        matrices.translate(x1, y1, 0.0f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle));
        drawBox(matrices, vc, 0.0f, -0.5f, -1.25f, len, 0.5f, -0.65f, UV_WHITE, light, overlay, 255);
        drawBox(matrices, vc, 0.0f, -0.5f, 0.65f, len, 0.5f, 1.25f, UV_WHITE, light, overlay, 255);
        matrices.pop();
    }

    /** Junta: bloque oscuro con un núcleo cian que brilla. */
    private static void joint(MatrixStack matrices, VertexConsumer vc, float x, float y,
                              int light, int fullBright, int overlay, int glowRgb) {
        matrices.push();
        matrices.translate(x, y, 0.0f);
        drawBox(matrices, vc, -0.8f, -0.8f, -1.5f, 0.8f, 0.8f, 1.5f, UV_CLAMP, light, overlay, 255);
        drawBox(matrices, vc, -0.35f, -0.35f, -1.75f, 0.35f, 0.35f, 1.75f, UV_CYAN, fullBright, overlay, glowRgb);
        matrices.pop();
    }

    // ------------------------------------------------------------------ geometría

    /** Caja con UV por cara (norte, este, sur, oeste, arriba, abajo), como un modelo JSON de bloques. */
    private static void drawBox(MatrixStack matrices, VertexConsumer vc,
                                float x1, float y1, float z1, float x2, float y2, float z2,
                                int[] uv, int light, int overlay, int rgb) {
        MatrixStack.Entry entry = matrices.peek();
        Matrix4f pos = entry.getPositionMatrix();
        Matrix3f nor = entry.getNormalMatrix();
        // norte (-z)
        quad(vc, pos, nor, x2, y2, z1, x2, y1, z1, x1, y1, z1, x1, y2, z1, 0, 0, -1, uv, 0, light, overlay, rgb);
        // este (+x)
        quad(vc, pos, nor, x2, y2, z2, x2, y1, z2, x2, y1, z1, x2, y2, z1, 1, 0, 0, uv, 4, light, overlay, rgb);
        // sur (+z)
        quad(vc, pos, nor, x1, y2, z2, x1, y1, z2, x2, y1, z2, x2, y2, z2, 0, 0, 1, uv, 8, light, overlay, rgb);
        // oeste (-x)
        quad(vc, pos, nor, x1, y2, z1, x1, y1, z1, x1, y1, z2, x1, y2, z2, -1, 0, 0, uv, 12, light, overlay, rgb);
        // arriba (+y)
        quad(vc, pos, nor, x1, y2, z1, x1, y2, z2, x2, y2, z2, x2, y2, z1, 0, 1, 0, uv, 16, light, overlay, rgb);
        // abajo (-y)
        quad(vc, pos, nor, x1, y1, z2, x1, y1, z1, x2, y1, z1, x2, y1, z2, 0, -1, 0, uv, 20, light, overlay, rgb);
    }

    private static void quad(VertexConsumer vc, Matrix4f pos, Matrix3f nor,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz,
                             float nx, float ny, float nz, int[] uv, int o,
                             int light, int overlay, int rgb) {
        float u1 = uv[o] / TEX;
        float v1 = uv[o + 1] / TEX;
        float u2 = uv[o + 2] / TEX;
        float v2 = uv[o + 3] / TEX;
        vertex(vc, pos, nor, ax, ay, az, u1, v1, nx, ny, nz, light, overlay, rgb);
        vertex(vc, pos, nor, bx, by, bz, u1, v2, nx, ny, nz, light, overlay, rgb);
        vertex(vc, pos, nor, cx, cy, cz, u2, v2, nx, ny, nz, light, overlay, rgb);
        vertex(vc, pos, nor, dx, dy, dz, u2, v1, nx, ny, nz, light, overlay, rgb);
    }

    private static void vertex(VertexConsumer vc, Matrix4f pos, Matrix3f nor, float x, float y, float z,
                               float u, float v, float nx, float ny, float nz, int light, int overlay, int rgb) {
        vc.vertex(pos, x, y, z)
                .color(rgb, rgb, rgb, 255)
                .texture(u, v)
                .overlay(overlay)
                .light(light)
                .normal(nor, nx, ny, nz)
                .next();
    }

    // ------------------------------------------------------------------ utilidades

    private static float[] rotate(float degrees, float x, float y) {
        double a = Math.toRadians(degrees);
        float c = (float) Math.cos(a);
        float s = (float) Math.sin(a);
        return new float[]{x * c - y * s, x * s + y * c};
    }

    private static float smooth(float t) {
        t = MathHelper.clamp(t, 0.0f, 1.0f);
        return t * t * (3.0f - 2.0f * t);
    }

    private static int[] uniform(int u1, int v1, int u2, int v2) {
        int[] out = new int[24];
        for (int f = 0; f < 6; f++) {
            out[f * 4] = u1;
            out[f * 4 + 1] = v1;
            out[f * 4 + 2] = u2;
            out[f * 4 + 3] = v2;
        }
        return out;
    }
}
