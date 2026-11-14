package com.divinedoll.edgerunners.item;

import com.divinedoll.edgerunners.EdgerunnersMod;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class ModItems {
    public static final Item SANDEVISTAN = new SandevistanItem(
            new FabricItemSettings().rarity(Rarity.EPIC));
    public static final Item MANTIS_BLADES = new MantisBladesItem(
            new FabricItemSettings().rarity(Rarity.RARE));
    public static final Item STIM = new StimItem(
            new FabricItemSettings().maxCount(16));

    public static final ItemGroup EDGERUNNERS_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(SANDEVISTAN))
            .displayName(Text.translatable("itemGroup.edgerunners.main"))
            .entries((context, entries) -> {
                entries.add(SANDEVISTAN);
                entries.add(MANTIS_BLADES);
                entries.add(STIM);
            })
            .build();

    public static void register() {
        register("sandevistan", SANDEVISTAN);
        register("mantis_blades", MANTIS_BLADES);
        register("stim", STIM);
        Registry.register(Registries.ITEM_GROUP,
                new Identifier(EdgerunnersMod.MOD_ID, "main"), EDGERUNNERS_GROUP);
    }

    private static void register(String name, Item item) {
        Registry.register(Registries.ITEM, new Identifier(EdgerunnersMod.MOD_ID, name), item);
    }
}
