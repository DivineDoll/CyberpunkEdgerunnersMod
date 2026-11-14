package com.divinedoll.edgerunners;

import com.divinedoll.edgerunners.effect.ModEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.List;

/**
 * Ralentización del tiempo del Sandevistan.
 * Mientras un jugador tiene el efecto, todas las entidades (menos jugadores y sus propios
 * proyectiles) dentro de RADIUS bloques solo avanzan 1 de cada FACTOR ticks.
 */
public final class TimeSlow {
    /** Alcance de la ralentización, en bloques. */
    public static final double RADIUS = 20.0;
    /** Las entidades afectadas solo avanzan 1 de cada FACTOR ticks (1/20 = 5 % de la velocidad). */
    public static final int FACTOR = 20;

    private static final List<ServerPlayerEntity> ACTIVE = new ArrayList<>();
    private static ServerWorld cachedWorld;
    private static long cachedTime = Long.MIN_VALUE;

    private TimeSlow() {
    }

    private static void refresh(ServerWorld world) {
        long time = world.getTime();
        if (world == cachedWorld && time == cachedTime) {
            return;
        }
        cachedWorld = world;
        cachedTime = time;
        ACTIVE.clear();
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (player.isAlive() && player.hasStatusEffect(ModEffects.SANDEVISTAN)) {
                ACTIVE.add(player);
            }
        }
    }

    /** true si esta entidad debe saltarse su tick en este momento. */
    public static boolean shouldSkip(ServerWorld world, Entity entity) {
        if (entity instanceof PlayerEntity) {
            return false;
        }
        refresh(world);
        if (ACTIVE.isEmpty() || world.getTime() % FACTOR == 0) {
            return false;
        }
        if (entity instanceof ProjectileEntity projectile && ACTIVE.contains(projectile.getOwner())) {
            return false;
        }
        double radiusSq = RADIUS * RADIUS;
        for (ServerPlayerEntity player : ACTIVE) {
            if (player.squaredDistanceTo(entity) <= radiusSq) {
                return true;
            }
        }
        return false;
    }
}
