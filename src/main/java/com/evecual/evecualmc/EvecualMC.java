package com.evecual.evecualmc;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvecualMC implements ModInitializer {
    public static final String MOD_ID = "evecualmc";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Sample custom item registered by the base mod
    public static final Item EXAMPLE_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "example_item"),
            new Item(new Item.Settings())
    );

    @Override
    public void onInitialize() {
        LOGGER.info("========================================");
        LOGGER.info("  EvecualMC Fabric Base Mod Initialized! ");
        LOGGER.info("  Minecraft Version: 1.20.1              ");
        LOGGER.info("========================================");
    }
}
