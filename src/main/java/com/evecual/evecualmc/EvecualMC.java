package com.evecual.evecualmc;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvecualMC implements ModInitializer {
    public static final String MOD_ID = "evecualmc";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Lightning item used as tab icon and item
    public static final Item LIGHTNING_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "lightning"),
            new Item(new Item.Settings())
    );

    // Sample custom item
    public static final Item EXAMPLE_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "example_item"),
            new Item(new Item.Settings())
    );

    // Creative Inventory Tab: "evecual" with lightning icon
    public static final RegistryKey<ItemGroup> EVECUAL_ITEM_GROUP_KEY = RegistryKey.of(
            RegistryKeys.ITEM_GROUP,
            new Identifier(MOD_ID, "evecual")
    );

    public static final ItemGroup EVECUAL_ITEM_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(LIGHTNING_ITEM))
            .displayName(Text.translatable("itemGroup.evecualmc.evecual"))
            .entries((displayContext, entries) -> {
                entries.add(LIGHTNING_ITEM);
                entries.add(EXAMPLE_ITEM);
            })
            .build();

    @Override
    public void onInitialize() {
        Registry.register(Registries.ITEM_GROUP, EVECUAL_ITEM_GROUP_KEY, EVECUAL_ITEM_GROUP);

        LOGGER.info("========================================");
        LOGGER.info("  EvecualMC Initialized!                ");
        LOGGER.info("  Added 'evecual' creative tab with ⚡   ");
        LOGGER.info("========================================");
    }
}
