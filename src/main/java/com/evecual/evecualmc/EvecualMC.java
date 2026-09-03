package com.evecual.evecualmc;

import com.evecual.evecualmc.block.BatteryBlock;
import com.evecual.evecualmc.block.ChargerBlock;
import com.evecual.evecualmc.block.ElectronicCombinerBlock;
import com.evecual.evecualmc.block.SolarPanelBlock;
import com.evecual.evecualmc.block.WireBlock;
import com.evecual.evecualmc.block.entity.BatteryBlockEntity;
import com.evecual.evecualmc.block.entity.ChargerBlockEntity;
import com.evecual.evecualmc.block.entity.ElectronicCombinerBlockEntity;
import com.evecual.evecualmc.block.entity.SolarPanelBlockEntity;
import com.evecual.evecualmc.entity.CarEntity;
import com.evecual.evecualmc.item.CarItem;
import com.evecual.evecualmc.item.LightningItem;
import com.evecual.evecualmc.screen.ElectronicCombinerScreenHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
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

    public static final Item STEEL_INGOT = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "steel_ingot"),
            new Item(new Item.Settings())
    );

    public static final Item ENGINE = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "electric_engine"),
            new Item(new Item.Settings())
    );

    public static final Item CAR_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "car"),
            new CarItem(new Item.Settings().maxCount(1))
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

    public static final Block ELECTRONIC_COMBINER_BLOCK = Registry.register(
            Registries.BLOCK,
            new Identifier(MOD_ID, "electronic_combiner"),
            new ElectronicCombinerBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).strength(3.5f).sounds(BlockSoundGroup.METAL))
    );

    public static final Item ELECTRONIC_COMBINER_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "electronic_combiner"),
            new BlockItem(ELECTRONIC_COMBINER_BLOCK, new Item.Settings())
    );

    public static final Block CHARGER_BLOCK = Registry.register(
            Registries.BLOCK,
            new Identifier(MOD_ID, "charger"),
            new ChargerBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).strength(3.0f).sounds(BlockSoundGroup.METAL))
    );

    public static final Item CHARGER_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "charger"),
            new BlockItem(CHARGER_BLOCK, new Item.Settings())
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

    public static final BlockEntityType<ElectronicCombinerBlockEntity> ELECTRONIC_COMBINER_BLOCK_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            new Identifier(MOD_ID, "electronic_combiner"),
            FabricBlockEntityTypeBuilder.create(ElectronicCombinerBlockEntity::new, ELECTRONIC_COMBINER_BLOCK).build()
    );

    public static final BlockEntityType<ChargerBlockEntity> CHARGER_BLOCK_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            new Identifier(MOD_ID, "charger"),
            FabricBlockEntityTypeBuilder.create(ChargerBlockEntity::new, CHARGER_BLOCK).build()
    );

    // Screen Handlers
    public static final ScreenHandlerType<ElectronicCombinerScreenHandler> ELECTRONIC_COMBINER_SCREEN_HANDLER = Registry.register(
            Registries.SCREEN_HANDLER,
            new Identifier(MOD_ID, "electronic_combiner"),
            new ScreenHandlerType<>(ElectronicCombinerScreenHandler::new, FeatureFlags.VANILLA_FEATURES)
    );

    public static final ScreenHandlerType<com.evecual.evecualmc.screen.CarTrunkScreenHandler> CAR_TRUNK_SCREEN_HANDLER = Registry.register(
            Registries.SCREEN_HANDLER,
            new Identifier(MOD_ID, "car_trunk"),
            new ScreenHandlerType<>(com.evecual.evecualmc.screen.CarTrunkScreenHandler::new, FeatureFlags.VANILLA_FEATURES)
    );

    // Entity Types
    public static final EntityType<CarEntity> CAR_ENTITY = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(MOD_ID, "car"),
            FabricEntityTypeBuilder.<CarEntity>create(SpawnGroup.MISC, CarEntity::new)
                    .dimensions(EntityDimensions.fixed(2.2f, 1.4f))
                    .trackRangeBlocks(10)
                    .build()
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
                entries.add(STEEL_INGOT);
                entries.add(ENGINE);
                entries.add(CAR_ITEM);
                entries.add(SOLAR_PANEL_ITEM);
                entries.add(BATTERY_ITEM);
                entries.add(WIRE_ITEM);
                entries.add(ELECTRONIC_COMBINER_ITEM);
                entries.add(CHARGER_ITEM);
            })
            .build();

    public static final Identifier CAR_INPUT_PACKET_ID = new Identifier(MOD_ID, "car_input");
    public static final Identifier OPEN_TRUNK_PACKET_ID = new Identifier(MOD_ID, "open_trunk");

    @Override
    public void onInitialize() {
        Registry.register(Registries.ITEM_GROUP, EVECUAL_ITEM_GROUP_KEY, EVECUAL_ITEM_GROUP);

        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(CAR_INPUT_PACKET_ID, (server, player, handler, buf, responseSender) -> {
            boolean forward = buf.readBoolean();
            boolean back = buf.readBoolean();
            boolean left = buf.readBoolean();
            boolean right = buf.readBoolean();
            boolean sprint = buf.readBoolean();

            server.execute(() -> {
                if (player.getVehicle() instanceof CarEntity car) {
                    car.setInputs(forward, back, left, right, sprint);
                }
            });
        });

        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(OPEN_TRUNK_PACKET_ID, (server, player, handler, buf, responseSender) -> {
            int entityId = buf.readInt();
            server.execute(() -> {
                Entity target = player.getWorld().getEntityById(entityId);
                if (target instanceof CarEntity car && player.squaredDistanceTo(car) < 64.0) {
                    car.openTrunk(player);
                }
            });
        });

        LOGGER.info("========================================");
        LOGGER.info("  EvecualMC Initialized!                ");
        LOGGER.info("  Electronic Combiner & Electric Car ready!");
        LOGGER.info("========================================");
    }
}
