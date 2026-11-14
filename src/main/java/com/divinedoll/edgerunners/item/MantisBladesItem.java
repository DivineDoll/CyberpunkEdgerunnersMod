package com.divinedoll.edgerunners.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Hojas mantis: espada rápida que deja sangrando (Wither corto) al objetivo.
 * Clic derecho: pliega o despliega las hojas (el estado se guarda en el ítem).
 */
public class MantisBladesItem extends SwordItem {
    /** Misma clave que lee el cliente en MantisAnimation. */
    public static final String RETRACTED_KEY = "Retracted";

    public MantisBladesItem(Settings settings) {
        super(ModToolMaterials.MONOWIRE, 4, -1.2f, settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient) {
            NbtCompound nbt = stack.getOrCreateNbt();
            boolean retract = !nbt.getBoolean(RETRACTED_KEY);
            nbt.putBoolean(RETRACTED_KEY, retract);
            world.playSound(null, user.getBlockPos(),
                    retract ? SoundEvents.BLOCK_PISTON_CONTRACT : SoundEvents.BLOCK_PISTON_EXTEND,
                    SoundCategory.PLAYERS, 0.7f, 1.7f);
        }
        user.getItemCooldownManager().set(this, 10);
        return TypedActionResult.success(stack, world.isClient());
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 60, 0), attacker);
        return super.postHit(stack, target, attacker);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("item.edgerunners.mantis_blades.tooltip").formatted(Formatting.RED));
    }
}
