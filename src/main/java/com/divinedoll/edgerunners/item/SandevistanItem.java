package com.divinedoll.edgerunners.item;

import com.divinedoll.edgerunners.effect.ModEffects;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Sandevistan: clic derecho para ir a toda velocidad mientras el mundo se ralentiza
 * a tu alrededor. A cambio, te da cyberpsicosis un rato y tiene 30 s de enfriamiento.
 */
public class SandevistanItem extends Item {
    private static final int COOLDOWN_TICKS = 20 * 30;

    public SandevistanItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient) {
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 200, 3));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 200, 3));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 200, 1));
            user.addStatusEffect(new StatusEffectInstance(ModEffects.CYBERPSYCHOSIS, 300, 0));
            // Marcador que activa la estela de imágenes fantasma y el tinte verde en el cliente
            user.addStatusEffect(new StatusEffectInstance(ModEffects.SANDEVISTAN, 200, 0, false, false, true));

            Box area = user.getBoundingBox().expand(12.0);
            for (LivingEntity other : world.getEntitiesByClass(LivingEntity.class, area,
                    entity -> entity != user && entity.isAlive())) {
                other.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 160, 2), user);
            }

            world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_BEACON_ACTIVATE,
                    SoundCategory.PLAYERS, 1.0f, 1.8f);
            if (world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                        user.getX(), user.getY() + 1.0, user.getZ(), 40, 0.6, 0.8, 0.6, 0.15);
            }
        }

        user.getItemCooldownManager().set(this, COOLDOWN_TICKS);
        return TypedActionResult.success(stack, world.isClient());
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("item.edgerunners.sandevistan.tooltip").formatted(Formatting.AQUA));
    }
}
