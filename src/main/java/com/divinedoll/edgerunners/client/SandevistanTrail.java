package com.divinedoll.edgerunners.client;

import com.divinedoll.edgerunners.effect.ModEffects;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Estela del Sandevistan: mientras el jugador tiene el efecto y se mueve, se guardan
 * "fotos" de su pose cada pocos ticks. Cada foto se dibuja como una silueta translúcida
 * que pasa de cian a verde y se desvanece. Además, la pantalla se tiñe de verde.
 */
public final class SandevistanTrail {
    private static final int LIFETIME = 14;          // ticks que dura cada fantasma
    private static final int MAX_GHOSTS = 48;
    private static final double MIN_MOVE_SQ = 0.16;  // 0.4 bloques entre fantasmas

    private static final List<Ghost> GHOSTS = new ArrayList<>();
    private static final Map<UUID, Vec3d> LAST_POS = new HashMap<>();
    private static float tint = 0.0f;

    private static final class Ghost {
        final double x, y, z;
        final float bodyYaw, headYaw, pitch, limbAngle, limbDistance, ageTicks;
        final Identifier skin;
        int age = 0;

        Ghost(AbstractClientPlayerEntity p) {
            this.x = p.getX();
            this.y = p.getY();
            this.z = p.getZ();
            this.bodyYaw = p.bodyYaw;
            this.headYaw = p.headYaw;
            this.pitch = p.getPitch();
            this.limbAngle = p.limbAnimator.getPos();
            this.limbDistance = Math.min(p.limbAnimator.getSpeed(), 1.0f);
            this.ageTicks = p.age;
            this.skin = p.getSkinTexture();
        }
    }

    private SandevistanTrail() {
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(SandevistanTrail::onTick);
        WorldRenderEvents.AFTER_ENTITIES.register(SandevistanTrail::renderGhosts);
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (tint < 0.02f) {
                return;
            }
            int alpha = (int) (tint * 70.0f);
            int color = (alpha << 24) | 0x00FF8A;
            drawContext.fill(0, 0, drawContext.getScaledWindowWidth(), drawContext.getScaledWindowHeight(), color);
        });
    }

    private static void onTick(MinecraftClient client) {
        ClientWorld world = client.world;
        if (world == null) {
            GHOSTS.clear();
            LAST_POS.clear();
            tint = 0.0f;
            return;
        }
        if (client.isPaused()) {
            return;
        }

        Iterator<Ghost> it = GHOSTS.iterator();
        while (it.hasNext()) {
            Ghost ghost = it.next();
            ghost.age++;
            if (ghost.age > LIFETIME) {
                it.remove();
            }
        }

        for (AbstractClientPlayerEntity player : world.getPlayers()) {
            UUID id = player.getUuid();
            if (!player.hasStatusEffect(ModEffects.SANDEVISTAN)) {
                LAST_POS.remove(id);
                continue;
            }
            Vec3d pos = player.getPos();
            Vec3d last = LAST_POS.get(id);
            if (last == null || last.squaredDistanceTo(pos) > MIN_MOVE_SQ) {
                if (GHOSTS.size() < MAX_GHOSTS) {
                    GHOSTS.add(new Ghost(player));
                }
                LAST_POS.put(id, pos);
            }
        }

        boolean self = client.player != null && client.player.hasStatusEffect(ModEffects.SANDEVISTAN);
        tint += ((self ? 1.0f : 0.0f) - tint) * 0.2f;
    }

    private static void renderGhosts(WorldRenderContext context) {
        if (GHOSTS.isEmpty()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity self = client.player;
        VertexConsumerProvider consumers = context.consumers();
        if (self == null || consumers == null) {
            return;
        }
        Object rawRenderer = client.getEntityRenderDispatcher().getRenderer(self);
        if (!(rawRenderer instanceof PlayerEntityRenderer renderer)) {
            return;
        }

        PlayerEntityModel<AbstractClientPlayerEntity> model = renderer.getModel();
        MatrixStack matrices = context.matrixStack();
        Vec3d cam = context.camera().getPos();
        float tickDelta = context.tickDelta();
        RenderLayer lastLayer = null;

        for (Ghost ghost : GHOSTS) {
            float life = 1.0f - (ghost.age + tickDelta) / LIFETIME;
            if (life <= 0.0f) {
                continue;
            }
            float t = 1.0f - life; // 0 = recién creado, 1 = a punto de desaparecer
            float red = 0.25f + 0.10f * t;
            float green = 0.85f + 0.15f * t;
            float blue = 1.00f - 0.45f * t;
            float alpha = 0.60f * life;

            // Pose del momento en que se tomó la "foto"
            model.child = false;
            model.riding = false;
            model.sneaking = false;
            model.handSwingProgress = 0.0f;
            model.leftArmPose = BipedEntityModel.ArmPose.EMPTY;
            model.rightArmPose = BipedEntityModel.ArmPose.EMPTY;
            model.setAngles(self, ghost.limbAngle, ghost.limbDistance, ghost.ageTicks,
                    ghost.headYaw - ghost.bodyYaw, ghost.pitch);

            lastLayer = RenderLayer.getEntityTranslucentEmissive(ghost.skin);
            VertexConsumer buffer = consumers.getBuffer(lastLayer);

            matrices.push();
            matrices.translate(ghost.x - cam.x, ghost.y - cam.y, ghost.z - cam.z);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - ghost.bodyYaw));
            matrices.scale(-1.0f, -1.0f, 1.0f);
            matrices.scale(0.9375f, 0.9375f, 0.9375f);
            matrices.translate(0.0f, -1.501f, 0.0f);
            model.render(matrices, buffer, LightmapTextureManager.MAX_LIGHT_COORDINATE,
                    OverlayTexture.DEFAULT_UV, red, green, blue, alpha);
            matrices.pop();
        }

        if (lastLayer != null && consumers instanceof VertexConsumerProvider.Immediate immediate) {
            immediate.draw(lastLayer);
        }
    }
}
