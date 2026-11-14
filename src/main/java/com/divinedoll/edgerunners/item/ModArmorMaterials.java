package com.divinedoll.edgerunners.item;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import java.util.function.Supplier;

/** Material del implante Sandevistan: solo se usa en el slot del pecho. */
public enum ModArmorMaterials implements ArmorMaterial {
    SANDEVISTAN("edgerunners_sandevistan", 380, 3, 22, SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE, 1.0f, 0.0f,
            () -> Ingredient.ofItems(Items.IRON_INGOT));

    private final String name;
    private final int durability;
    private final int chestProtection;
    private final int enchantability;
    private final SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;
    private final Supplier<Ingredient> repairIngredient;

    ModArmorMaterials(String name, int durability, int chestProtection, int enchantability, SoundEvent equipSound,
                      float toughness, float knockbackResistance, Supplier<Ingredient> repairIngredient) {
        this.name = name;
        this.durability = durability;
        this.chestProtection = chestProtection;
        this.enchantability = enchantability;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = repairIngredient;
    }

    @Override
    public int getDurability(ArmorItem.Type type) {
        return durability;
    }

    @Override
    public int getProtection(ArmorItem.Type type) {
        return type == ArmorItem.Type.CHESTPLATE ? chestProtection : 0;
    }

    @Override
    public int getEnchantability() {
        return enchantability;
    }

    @Override
    public SoundEvent getEquipSound() {
        return equipSound;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairIngredient.get();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public float getToughness() {
        return toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return knockbackResistance;
    }
}
