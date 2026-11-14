package com.divinedoll.edgerunners.item;

import com.divinedoll.edgerunners.TimeSlow;
import com.divinedoll.edgerunners.effect.ModEffects;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Sandevistan: implante de columna que se lleva en el slot del pecho.
 * Se activa con una tecla (por defecto V): tú te mueves a toda velocidad mientras el tiempo
 * a tu alrededor casi se detiene (ver TimeSlow). A cambio te da cyberpsicosis un rato y tiene
 * 30 s de enfriamiento.
 */
public class SandevistanItem extends ArmorItem {
    /** Duración del efecto: 15 s. */
    public static final int DURATION_TICKS = 20 * 15;
    private static final int COOLDOWN_TICKS = 20 * 30;

    public SandevistanItem(Settings settings) {
        super(ModArmorMaterials.SANDEVISTAN, Type.CHESTPLATE, settings);
    }

    /** Lo llama el servidor cuando el jugador pulsa la tecla de activación. */
    public static void activate(ServerPlayerEntity player) {
        ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
        if (!chest.isOf(ModItems.SANDEVISTAN)) {
            player.sendMessage(Text.translatable("message.edgerunners.sandevistan_not_worn")
                    .formatted(Formatting.RED), true);
            return;
        }
        if (player.getItemCooldownManager().isCoolingDown(chest.getItem())) {
            return;
        }

        ServerWorld world = player.getServerWorld();
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, DURATION_TICKS, 4));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, DURATION_TICKS, 4));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, DURATION_TICKS, 2));
        player.addStatusEffect(new StatusEffectInstance(ModEffects.CYBERPSYCHOSIS, DURATION_TICKS + 100, 0));
        // Marcador que activa la estela de imágenes fantasma y el tinte verde en el cliente
        player.addStatusEffect(new StatusEffectInstance(ModEffects.SANDEVISTAN, DURATION_TICKS, 0, false, false, true));

        // Respaldo por si el mixin del tiempo no se aplicara: Lentitud fuerte dentro del radio
        Box area = player.getBoundingBox().expand(TimeSlow.RADIUS);
        for (LivingEntity other : world.getEntitiesByClass(LivingEntity.class, area,
                entity -> entity != player && entity.isAlive())) {
            other.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 200, 5), player);
        }

        world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_ACTIVATE,
                SoundCategory.PLAYERS, 1.2f, 1.6f);
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_WARDEN_HEARTBEAT,
                SoundCategory.PLAYERS, 1.0f, 0.6f);
        world.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                player.getX(), player.getY() + 1.0, player.getZ(), 80, 0.7, 0.9, 0.7, 0.2);

        player.getItemCooldownManager().set(chest.getItem(), COOLDOWN_TICKS);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("item.edgerunners.sandevistan.tooltip").formatted(Formatting.AQUA));
    }
}
