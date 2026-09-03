package com.evecual.evecualmc;

import com.evecual.evecualmc.block.BatteryBlock;
import com.evecual.evecualmc.block.SolarPanelBlock;
import com.evecual.evecualmc.block.WireBlock;
import com.evecual.evecualmc.block.entity.BatteryBlockEntity;
import com.evecual.evecualmc.block.entity.SolarPanelBlockEntity;
import com.evecual.evecualmc.item.LightningItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvecualMC implements ModInitializer {
    public static final String MOD_ID = "evecualmc";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Items
    public static final Item LIGHTNING_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "lightning"),
            new LightningItem(new Item.Settings().maxCount(1))
    );

    // Blocks
    public static final Block SOLAR_PANEL_BLOCK = Registry.register(
            Registries.BLOCK,
            new Identifier(MOD_ID, "solar_panel"),
            new SolarPanelBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).strength(2.0f).nonOpaque().sounds(BlockSoundGroup.METAL))
    );

    public static final Item SOLAR_PANEL_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "solar_panel"),
            new BlockItem(SOLAR_PANEL_BLOCK, new Item.Settings())
    );

    public static final Block BATTERY_BLOCK = Registry.register(
            Registries.BLOCK,
            new Identifier(MOD_ID, "battery"),
            new BatteryBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).strength(3.0f).sounds(BlockSoundGroup.METAL))
    );

    public static final Item BATTERY_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "battery"),
            new BlockItem(BATTERY_BLOCK, new Item.Settings())
    );

    public static final Block WIRE_BLOCK = Registry.register(
            Registries.BLOCK,
            new Identifier(MOD_ID, "wire"),
            new WireBlock(FabricBlockSettings.copyOf(Blocks.COPPER_BLOCK).strength(0.5f).nonOpaque().sounds(BlockSoundGroup.COPPER))
    );

    public static final Item WIRE_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "wire"),
            new BlockItem(WIRE_BLOCK, new Item.Settings())
    );

    // Block Entities
    public static final BlockEntityType<SolarPanelBlockEntity> SOLAR_PANEL_BLOCK_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            new Identifier(MOD_ID, "solar_panel"),
            FabricBlockEntityTypeBuilder.create(SolarPanelBlockEntity::new, SOLAR_PANEL_BLOCK).build()
    );

    public static final BlockEntityType<BatteryBlockEntity> BATTERY_BLOCK_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            new Identifier(MOD_ID, "battery"),
            FabricBlockEntityTypeBuilder.create(BatteryBlockEntity::new, BATTERY_BLOCK).build()
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
                entries.add(SOLAR_PANEL_ITEM);
                entries.add(BATTERY_ITEM);
                entries.add(WIRE_ITEM);
            })
            .build();

    @Override
    public void onInitialize() {
        Registry.register(Registries.ITEM_GROUP, EVECUAL_ITEM_GROUP_KEY, EVECUAL_ITEM_GROUP);

        LOGGER.info("========================================");
        LOGGER.info("  EvecualMC Initialized!                ");
        LOGGER.info("  Electricity system ready:             ");
        LOGGER.info("   - Solar Panel (Sun-tracking power)   ");
        LOGGER.info("   - Battery (600 Energy Storage)       ");
        LOGGER.info("   - Electrical Wire                    ");
        LOGGER.info("========================================");
    }
}
