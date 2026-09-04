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
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvecualMC implements ModInitializer {
    public static final String MOD_ID = "evecualmc";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Items
    public static final Item LIGHTNING_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "lightning"),
            new LightningItem(new Item.Settings().maxCount(1)));

    public static final Item STEEL_INGOT = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "steel_ingot"),
            new Item(new Item.Settings()));

    public static final Item ENGINE = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "electric_engine"),
            new Item(new Item.Settings()));

    public static final Item UPGRADED_ENGINE = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "upgraded_electric_engine"),
            new Item(new Item.Settings()));

    public static final Item TRUNK_UPGRADE = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "trunk_upgrade"),
            new Item(new Item.Settings()));

    public static final Item CAR_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "car"),
            new CarItem(new Item.Settings().maxCount(1)));

    public static final Item RC_CAR_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "rc_car"),
            new com.evecual.evecualmc.item.RcCarItem(new Item.Settings().maxCount(1)));

    public static final Item RC_DRONE_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "rc_drone"),
            new com.evecual.evecualmc.item.RcDroneItem(new Item.Settings().maxCount(1)));

    public static final Item RC_ROBOT_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "rc_robot"),
            new com.evecual.evecualmc.item.RcRobotItem(new Item.Settings().maxCount(1)));

    public static final Item RC_CONTROLLER_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "rc_controller"),
            new com.evecual.evecualmc.item.RcControllerItem(new Item.Settings().maxCount(1)));

    public static final Item RC_SENDER_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "rc_sender"),
            new Item(new Item.Settings()));

    public static final Item RC_RECEIVER_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "rc_receiver"),
            new Item(new Item.Settings()));

    public static final Item VANILLA_ICE_CREAM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "vanilla_ice_cream"),
            new com.evecual.evecualmc.item.IceCreamItem(com.evecual.evecualmc.item.IceCreamItem.Flavor.VANILLA,
                    new Item.Settings()));

    public static final Item CHOCOLATE_ICE_CREAM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "chocolate_ice_cream"),
            new com.evecual.evecualmc.item.IceCreamItem(com.evecual.evecualmc.item.IceCreamItem.Flavor.CHOCOLATE,
                    new Item.Settings()));

    public static final Item SWEET_BERRY_ICE_CREAM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "sweet_berry_ice_cream"),
            new com.evecual.evecualmc.item.IceCreamItem(com.evecual.evecualmc.item.IceCreamItem.Flavor.SWEET_BERRY,
                    new Item.Settings()));

    public static final Item ELECTRIC_ICE_CREAM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "electric_ice_cream"),
            new com.evecual.evecualmc.item.IceCreamItem(com.evecual.evecualmc.item.IceCreamItem.Flavor.ELECTRIC,
                    new Item.Settings()));

    // Blocks (All mineable by hand and drop themselves!)
    public static final Block SOLAR_PANEL_BLOCK = Registry.register(
            Registries.BLOCK,
            new Identifier(MOD_ID, "solar_panel"),
            new SolarPanelBlock(FabricBlockSettings.create().strength(0.8f).nonOpaque().sounds(BlockSoundGroup.METAL)));

    public static final Item SOLAR_PANEL_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "solar_panel"),
            new BlockItem(SOLAR_PANEL_BLOCK, new Item.Settings()));

    public static final Block BATTERY_BLOCK = Registry.register(
            Registries.BLOCK,
            new Identifier(MOD_ID, "battery"),
            new BatteryBlock(FabricBlockSettings.create().strength(1.0f).sounds(BlockSoundGroup.METAL)));

    public static final Item BATTERY_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "battery"),
            new BlockItem(BATTERY_BLOCK, new Item.Settings()));

    public static final Block WIRE_BLOCK = Registry.register(
            Registries.BLOCK,
            new Identifier(MOD_ID, "wire"),
            new WireBlock(FabricBlockSettings.create().strength(0.3f).nonOpaque().sounds(BlockSoundGroup.COPPER)));

    public static final Item WIRE_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "wire"),
            new BlockItem(WIRE_BLOCK, new Item.Settings()));

    public static final Block ELECTRONIC_COMBINER_BLOCK = Registry.register(
            Registries.BLOCK,
            new Identifier(MOD_ID, "electronic_combiner"),
            new ElectronicCombinerBlock(FabricBlockSettings.create().strength(1.2f).sounds(BlockSoundGroup.METAL)));

    public static final Item ELECTRONIC_COMBINER_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "electronic_combiner"),
            new BlockItem(ELECTRONIC_COMBINER_BLOCK, new Item.Settings()));

    public static final Block CHARGER_BLOCK = Registry.register(
            Registries.BLOCK,
            new Identifier(MOD_ID, "charger"),
            new ChargerBlock(FabricBlockSettings.create().strength(1.0f).sounds(BlockSoundGroup.METAL)));

    public static final Item CHARGER_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "charger"),
            new BlockItem(CHARGER_BLOCK, new Item.Settings()));

    public static final Block CHARGER_EXTENSION_BLOCK = Registry.register(
            Registries.BLOCK,
            new Identifier(MOD_ID, "charger_extension"),
            new com.evecual.evecualmc.block.ChargerExtensionBlock(
                    FabricBlockSettings.create().strength(1.0f).sounds(BlockSoundGroup.METAL)));

    public static final Item CHARGER_EXTENSION_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "charger_extension"),
            new BlockItem(CHARGER_EXTENSION_BLOCK, new Item.Settings()));

    public static final Block PARKING_LINES_BLOCK = Registry.register(
            Registries.BLOCK,
            new Identifier(MOD_ID, "parking_lines"),
            new com.evecual.evecualmc.block.ParkingLinesBlock(FabricBlockSettings.create().strength(0.5f)
                    .sounds(BlockSoundGroup.STONE).nonOpaque().noCollision()));

    public static final Item PARKING_LINES_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "parking_lines"),
            new BlockItem(PARKING_LINES_BLOCK, new Item.Settings()));

    public static final Block RC_CHARGER_BLOCK = Registry.register(
            Registries.BLOCK,
            new Identifier(MOD_ID, "rc_charger"),
            new com.evecual.evecualmc.block.RcChargerBlock(
                    FabricBlockSettings.create().strength(1.2f).sounds(BlockSoundGroup.METAL)));

    public static final Item RC_CHARGER_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "rc_charger"),
            new BlockItem(RC_CHARGER_BLOCK, new Item.Settings()));

    public static final Block RC_PARKING_SPOT_BLOCK = Registry.register(
            Registries.BLOCK,
            new Identifier(MOD_ID, "rc_parking_spot"),
            new com.evecual.evecualmc.block.RcParkingSpotBlock(
                    FabricBlockSettings.create().strength(0.5f).sounds(BlockSoundGroup.METAL).nonOpaque().noCollision()));

    public static final Item RC_PARKING_SPOT_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "rc_parking_spot"),
            new BlockItem(RC_PARKING_SPOT_BLOCK, new Item.Settings()));

    public static final Block DRONE_PARKING_SPOT_BLOCK = Registry.register(
            Registries.BLOCK,
            new Identifier(MOD_ID, "drone_parking_spot"),
            new com.evecual.evecualmc.block.DroneParkingSpotBlock(
                    FabricBlockSettings.create().strength(0.5f).sounds(BlockSoundGroup.METAL).nonOpaque().noCollision()));

    public static final Item DRONE_PARKING_SPOT_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "drone_parking_spot"),
            new BlockItem(DRONE_PARKING_SPOT_BLOCK, new Item.Settings()));

    public static final Block ROBOT_PARKING_SPOT_BLOCK = Registry.register(
            Registries.BLOCK,
            new Identifier(MOD_ID, "robot_parking_spot"),
            new com.evecual.evecualmc.block.RobotParkingSpotBlock(
                    FabricBlockSettings.create().strength(0.5f).sounds(BlockSoundGroup.METAL).nonOpaque().noCollision()));

    public static final Item ROBOT_PARKING_SPOT_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "robot_parking_spot"),
            new BlockItem(ROBOT_PARKING_SPOT_BLOCK, new Item.Settings()));

    public static final Block STATIONARY_RC_CONTROLLER_BLOCK = Registry.register(
            Registries.BLOCK,
            new Identifier(MOD_ID, "stationary_rc_controller"),
            new com.evecual.evecualmc.block.StationaryRcControllerBlock(
                    FabricBlockSettings.create().strength(1.5f).sounds(BlockSoundGroup.METAL).nonOpaque()));

    public static final Item STATIONARY_RC_CONTROLLER_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "stationary_rc_controller"),
            new com.evecual.evecualmc.item.StationaryRcControllerItem(STATIONARY_RC_CONTROLLER_BLOCK,
                    new Item.Settings().maxCount(1)));

    public static final Item CHARGER_CABLE = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "charger_cable"),
            new Item(new Item.Settings().maxCount(1)));

    // Block Entities
    public static final BlockEntityType<SolarPanelBlockEntity> SOLAR_PANEL_BLOCK_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            new Identifier(MOD_ID, "solar_panel"),
            FabricBlockEntityTypeBuilder.create(SolarPanelBlockEntity::new, SOLAR_PANEL_BLOCK).build());

    public static final BlockEntityType<BatteryBlockEntity> BATTERY_BLOCK_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            new Identifier(MOD_ID, "battery"),
            FabricBlockEntityTypeBuilder.create(BatteryBlockEntity::new, BATTERY_BLOCK).build());

    public static final BlockEntityType<ElectronicCombinerBlockEntity> ELECTRONIC_COMBINER_BLOCK_ENTITY = Registry
            .register(
                    Registries.BLOCK_ENTITY_TYPE,
                    new Identifier(MOD_ID, "electronic_combiner"),
                    FabricBlockEntityTypeBuilder.create(ElectronicCombinerBlockEntity::new, ELECTRONIC_COMBINER_BLOCK)
                            .build());

    public static final BlockEntityType<ChargerBlockEntity> CHARGER_BLOCK_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            new Identifier(MOD_ID, "charger"),
            FabricBlockEntityTypeBuilder.create(ChargerBlockEntity::new, CHARGER_BLOCK).build());

    public static final BlockEntityType<com.evecual.evecualmc.block.entity.ChargerExtensionBlockEntity> CHARGER_EXTENSION_BLOCK_ENTITY = Registry
            .register(
                    Registries.BLOCK_ENTITY_TYPE,
                    new Identifier(MOD_ID, "charger_extension"),
                    FabricBlockEntityTypeBuilder
                            .create(com.evecual.evecualmc.block.entity.ChargerExtensionBlockEntity::new,
                                    CHARGER_EXTENSION_BLOCK)
                            .build());

    public static final BlockEntityType<com.evecual.evecualmc.block.entity.ParkingLinesBlockEntity> PARKING_LINES_BLOCK_ENTITY = Registry
            .register(
                    Registries.BLOCK_ENTITY_TYPE,
                    new Identifier(MOD_ID, "parking_lines"),
                    FabricBlockEntityTypeBuilder.create(com.evecual.evecualmc.block.entity.ParkingLinesBlockEntity::new,
                            PARKING_LINES_BLOCK).build());

    public static final BlockEntityType<com.evecual.evecualmc.block.entity.RcChargerBlockEntity> RC_CHARGER_BLOCK_ENTITY = Registry
            .register(
                    Registries.BLOCK_ENTITY_TYPE,
                    new Identifier(MOD_ID, "rc_charger"),
                    FabricBlockEntityTypeBuilder
                            .create(com.evecual.evecualmc.block.entity.RcChargerBlockEntity::new, RC_CHARGER_BLOCK)
                            .build());

    public static final BlockEntityType<com.evecual.evecualmc.block.entity.StationaryRcControllerBlockEntity> STATIONARY_RC_CONTROLLER_BLOCK_ENTITY = Registry
            .register(
                    Registries.BLOCK_ENTITY_TYPE,
                    new Identifier(MOD_ID, "stationary_rc_controller"),
                    FabricBlockEntityTypeBuilder
                            .create(com.evecual.evecualmc.block.entity.StationaryRcControllerBlockEntity::new,
                                    STATIONARY_RC_CONTROLLER_BLOCK)
                            .build());

    // Screen Handlers
    public static final ScreenHandlerType<ElectronicCombinerScreenHandler> ELECTRONIC_COMBINER_SCREEN_HANDLER = Registry
            .register(
                    Registries.SCREEN_HANDLER,
                    new Identifier(MOD_ID, "electronic_combiner"),
                    new ScreenHandlerType<>(ElectronicCombinerScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

    public static final ScreenHandlerType<com.evecual.evecualmc.screen.CarTrunkScreenHandler> CAR_TRUNK_SCREEN_HANDLER = Registry
            .register(
                    Registries.SCREEN_HANDLER,
                    new Identifier(MOD_ID, "car_trunk"),
                    new ScreenHandlerType<>(com.evecual.evecualmc.screen.CarTrunkScreenHandler::new,
                            FeatureFlags.VANILLA_FEATURES));

    // Entity Types
    public static final EntityType<CarEntity> CAR_ENTITY = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(MOD_ID, "car"),
            FabricEntityTypeBuilder.<CarEntity>create(SpawnGroup.MISC, CarEntity::new)
                    .dimensions(EntityDimensions.fixed(2.0f, 1.88f))
                    .trackRangeChunks(10)
                    .build());

    public static final EntityType<com.evecual.evecualmc.entity.RcCarEntity> RC_CAR_ENTITY = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(MOD_ID, "rc_car"),
            FabricEntityTypeBuilder.<com.evecual.evecualmc.entity.RcCarEntity>create(SpawnGroup.MISC,
                    com.evecual.evecualmc.entity.RcCarEntity::new)
                    .dimensions(EntityDimensions.fixed(0.7f, 0.45f))
                    .trackRangeChunks(18) // 288 blocks (> 256m)
                    .build());

    public static final EntityType<com.evecual.evecualmc.entity.RcDroneEntity> RC_DRONE_ENTITY = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(MOD_ID, "rc_drone"),
            FabricEntityTypeBuilder.<com.evecual.evecualmc.entity.RcDroneEntity>create(SpawnGroup.MISC,
                    com.evecual.evecualmc.entity.RcDroneEntity::new)
                    .dimensions(EntityDimensions.fixed(0.8f, 0.35f))
                    .trackRangeChunks(34) // 544 blocks (> 512m)
                    .build());

    public static final EntityType<com.evecual.evecualmc.entity.RcRobotEntity> RC_ROBOT_ENTITY = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(MOD_ID, "rc_robot"),
            FabricEntityTypeBuilder.<com.evecual.evecualmc.entity.RcRobotEntity>create(SpawnGroup.MISC,
                    com.evecual.evecualmc.entity.RcRobotEntity::new)
                    .dimensions(EntityDimensions.fixed(0.7f, 0.9f))
                    .trackRangeChunks(18) // 288 blocks (> 256m)
                    .build());

    // Creative Inventory Tab: "evecual" with lightning icon
    public static final RegistryKey<ItemGroup> EVECUAL_ITEM_GROUP_KEY = RegistryKey.of(
            RegistryKeys.ITEM_GROUP,
            new Identifier(MOD_ID, "evecual"));

    public static final ItemGroup EVECUAL_ITEM_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(LIGHTNING_ITEM))
            .displayName(Text.translatable("itemGroup.evecualmc.evecual"))
            .entries((displayContext, entries) -> {
                entries.add(LIGHTNING_ITEM);
                entries.add(STEEL_INGOT);
                entries.add(ENGINE);
                entries.add(UPGRADED_ENGINE);
                entries.add(TRUNK_UPGRADE);
                entries.add(CAR_ITEM);
                entries.add(RC_CAR_ITEM);
                entries.add(RC_DRONE_ITEM);
                entries.add(RC_ROBOT_ITEM);
                entries.add(RC_CONTROLLER_ITEM);
                entries.add(STATIONARY_RC_CONTROLLER_ITEM);
                entries.add(RC_SENDER_ITEM);
                entries.add(RC_RECEIVER_ITEM);
                entries.add(RC_CHARGER_ITEM);
                entries.add(RC_PARKING_SPOT_ITEM);
                entries.add(DRONE_PARKING_SPOT_ITEM);
                entries.add(ROBOT_PARKING_SPOT_ITEM);
                entries.add(SOLAR_PANEL_ITEM);
                entries.add(BATTERY_ITEM);
                entries.add(WIRE_ITEM);
                entries.add(ELECTRONIC_COMBINER_ITEM);
                entries.add(CHARGER_ITEM);
                entries.add(CHARGER_EXTENSION_ITEM);
                entries.add(CHARGER_CABLE);
                entries.add(PARKING_LINES_ITEM);
                entries.add(VANILLA_ICE_CREAM);
                entries.add(CHOCOLATE_ICE_CREAM);
                entries.add(SWEET_BERRY_ICE_CREAM);
                entries.add(ELECTRIC_ICE_CREAM);
            })
            .build();

    public static final Identifier CAR_INPUT_PACKET_ID = new Identifier(MOD_ID, "car_input");
    public static final Identifier RC_CAR_INPUT_PACKET_ID = new Identifier(MOD_ID, "rc_car_input");
    public static final Identifier RC_CAR_AUTO_DOCK_PACKET_ID = new Identifier(MOD_ID, "rc_car_auto_dock");
    public static final Identifier RC_DRONE_INPUT_PACKET_ID = new Identifier(MOD_ID, "rc_drone_input");
    public static final Identifier RC_DRONE_AUTO_DOCK_PACKET_ID = new Identifier(MOD_ID, "rc_drone_auto_dock");
    public static final Identifier RC_ROBOT_INPUT_PACKET_ID = new Identifier(MOD_ID, "rc_robot_input");
    public static final Identifier RC_ROBOT_TOOL_ACTION_PACKET_ID = new Identifier(MOD_ID, "rc_robot_tool_action");
    public static final Identifier RC_ROBOT_AUTO_DOCK_PACKET_ID = new Identifier(MOD_ID, "rc_robot_auto_dock");
    public static final Identifier OPEN_TRUNK_PACKET_ID = new Identifier(MOD_ID, "open_trunk");
    public static final Identifier CHARGER_WAYPOINT_PACKET_ID = new Identifier(MOD_ID, "charger_waypoint");
    public static final Identifier TOGGLE_CABLE_PACKET_ID = new Identifier(MOD_ID, "toggle_cable");
    public static final Identifier AUTO_PARK_PACKET_ID = new Identifier(MOD_ID, "auto_park");
    public static final Identifier START_AUTO_PARK_S2C_PACKET_ID = new Identifier(MOD_ID, "start_auto_park_s2c");
    public static final Identifier CANCEL_AUTO_PARK_S2C_PACKET_ID = new Identifier(MOD_ID, "cancel_auto_park_s2c");
    public static final Identifier TOGGLE_RC_LIGHT_PACKET_ID = new Identifier(MOD_ID, "toggle_rc_light");
    public static final Identifier OPEN_TIP_SCREEN_PACKET_ID = new Identifier(MOD_ID, "open_tip_screen");
    public static final Identifier SELECT_COMBINER_RECIPE_PACKET_ID = new Identifier(MOD_ID, "select_combiner_recipe");
    public static final Identifier ENTER_RC_STATION_PACKET_ID = new Identifier(MOD_ID, "enter_rc_station");
    public static final Identifier EXIT_RC_STATION_PACKET_ID = new Identifier(MOD_ID, "exit_rc_station");

    public static void sendOpenTipScreen(net.minecraft.server.network.ServerPlayerEntity player, String topicId, int energy, int maxEnergy, String status) {
        net.minecraft.network.PacketByteBuf buf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
        buf.writeString(topicId);
        buf.writeInt(energy);
        buf.writeInt(maxEnergy);
        buf.writeString(status != null ? status : "");
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, OPEN_TIP_SCREEN_PACKET_ID, buf);
    }

    @Override
    public void onInitialize() {
        Registry.register(Registries.ITEM_GROUP, EVECUAL_ITEM_GROUP_KEY, EVECUAL_ITEM_GROUP);

        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(CAR_INPUT_PACKET_ID,
                (server, player, handler, buf, responseSender) -> {
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

        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(RC_CAR_INPUT_PACKET_ID,
                (server, player, handler, buf, responseSender) -> {
                    java.util.UUID carUuid = buf.readUuid();
                    boolean forward = buf.readBoolean();
                    boolean back = buf.readBoolean();
                    boolean left = buf.readBoolean();
                    boolean right = buf.readBoolean();
                    boolean sprint = buf.readBoolean();
                    boolean jump = buf.readBoolean();
                    float yaw = buf.isReadable(4) ? buf.readFloat() : Float.NaN;

                    server.execute(() -> {
                        if (player.getServerWorld() != null) {
                            Entity target = player.getServerWorld().getEntity(carUuid);
                            if (target instanceof com.evecual.evecualmc.entity.RcCarEntity rcCar) {
                                if (player.squaredDistanceTo(rcCar) <= 65536.0) { // 256 blocks max range (256^2)
                                    rcCar.setRemoteInputs(forward, back, left, right, sprint, jump);
                                    if (!Float.isNaN(yaw)) {
                                        rcCar.setRemoteYaw(yaw);
                                    }
                                }
                            }
                        }
                    });
                });

        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(RC_CAR_AUTO_DOCK_PACKET_ID,
                (server, player, handler, buf, responseSender) -> {
                    java.util.UUID carUuid = buf.readUuid();
                    server.execute(() -> {
                        if (player.getServerWorld() != null) {
                            Entity target = player.getServerWorld().getEntity(carUuid);
                            if (target instanceof com.evecual.evecualmc.entity.RcCarEntity rcCar) {
                                if (player.squaredDistanceTo(rcCar) <= 65536.0) { // 256 blocks max range
                                    boolean started = rcCar.startAutoReturnToCharger();
                                    if (started) {
                                        player.sendMessage(Text.literal("§a⚡ RC Car returning to Parking Spot..."), true);
                                    } else {
                                        player.sendMessage(Text.literal("§c⚡ No RC Parking Spot found within 64 blocks!"),
                                                true);
                                    }
                                }
                            }
                        }
                    });
                });

        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(RC_DRONE_INPUT_PACKET_ID,
                (server, player, handler, buf, responseSender) -> {
                    java.util.UUID droneUuid = buf.readUuid();
                    boolean forward = buf.readBoolean();
                    boolean back = buf.readBoolean();
                    boolean left = buf.readBoolean();
                    boolean right = buf.readBoolean();
                    boolean up = buf.readBoolean();
                    boolean down = buf.readBoolean();
                    boolean sprint = buf.readBoolean();
                    float yaw = buf.isReadable(4) ? buf.readFloat() : Float.NaN;
                    boolean strafe = buf.isReadable(1) && buf.readBoolean();

                    server.execute(() -> {
                        if (player.getServerWorld() != null) {
                            Entity target = player.getServerWorld().getEntity(droneUuid);
                            if (target instanceof com.evecual.evecualmc.entity.RcDroneEntity drone) {
                                if (player.squaredDistanceTo(drone) <= 262144.0) { // 512 blocks max range (512^2)
                                    drone.setRemoteInputs(forward, back, left, right, up, down, sprint, strafe);
                                    if (!Float.isNaN(yaw)) {
                                        drone.setRemoteYaw(yaw);
                                    }
                                }
                            }
                        }
                    });
                });

        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(RC_DRONE_AUTO_DOCK_PACKET_ID,
                (server, player, handler, buf, responseSender) -> {
                    java.util.UUID droneUuid = buf.readUuid();
                    server.execute(() -> {
                        if (player.getServerWorld() != null) {
                            Entity target = player.getServerWorld().getEntity(droneUuid);
                            if (target instanceof com.evecual.evecualmc.entity.RcDroneEntity drone) {
                                if (player.squaredDistanceTo(drone) <= 262144.0) { // 512 blocks max range
                                    boolean started = drone.startAutoReturnToCharger();
                                    if (started) {
                                        player.sendMessage(Text.literal("§a⚡ RC Drone returning to Helipad..."), true);
                                    } else {
                                        player.sendMessage(Text.literal("§c⚡ No Drone Helipad found within 64 blocks!"),
                                                true);
                                    }
                                }
                            }
                        }
                    });
                });

        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(RC_ROBOT_INPUT_PACKET_ID,
                (server, player, handler, buf, responseSender) -> {
                    java.util.UUID robotUuid = buf.readUuid();
                    boolean forward = buf.readBoolean();
                    boolean back = buf.readBoolean();
                    boolean left = buf.readBoolean();
                    boolean right = buf.readBoolean();
                    boolean sprint = buf.readBoolean();
                    boolean jump = buf.readBoolean();
                    float yaw = buf.isReadable(4) ? buf.readFloat() : Float.NaN;

                    server.execute(() -> {
                        if (player.getServerWorld() != null) {
                            Entity target = player.getServerWorld().getEntity(robotUuid);
                            if (target instanceof com.evecual.evecualmc.entity.RcRobotEntity robot) {
                                if (player.squaredDistanceTo(robot) <= 65536.0) { // 256m
                                    robot.setRemoteInputs(forward, back, left, right, sprint, jump);
                                    if (!Float.isNaN(yaw)) {
                                        robot.setRemoteYaw(yaw);
                                    }
                                }
                            }
                        }
                    });
                });

        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(RC_ROBOT_TOOL_ACTION_PACKET_ID,
                (server, player, handler, buf, responseSender) -> {
                    java.util.UUID robotUuid = buf.readUuid();
                    float lookPitch = buf.readFloat();
                    float lookYaw = buf.readFloat();

                    server.execute(() -> {
                        if (player.getServerWorld() != null) {
                            Entity target = player.getServerWorld().getEntity(robotUuid);
                            if (target instanceof com.evecual.evecualmc.entity.RcRobotEntity robot) {
                                if (player.squaredDistanceTo(robot) <= 65536.0) {
                                    robot.performToolAction(lookPitch, lookYaw);
                                }
                            }
                        }
                    });
                });

        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(RC_ROBOT_AUTO_DOCK_PACKET_ID,
                (server, player, handler, buf, responseSender) -> {
                    java.util.UUID robotUuid = buf.readUuid();
                    server.execute(() -> {
                        if (player.getServerWorld() != null) {
                            Entity target = player.getServerWorld().getEntity(robotUuid);
                            if (target instanceof com.evecual.evecualmc.entity.RcRobotEntity robot) {
                                if (player.squaredDistanceTo(robot) <= 65536.0) {
                                    boolean started = robot.startAutoReturnToCharger();
                                    if (started) {
                                        player.sendMessage(Text.literal("§a⚡ RC Robot returning to Robot Parking Spot..."), true);
                                    } else {
                                        player.sendMessage(Text.literal("§c⚡ No Robot Parking Spot found within 64 blocks!"), true);
                                    }
                                }
                            }
                        }
                    });
                });

        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(OPEN_TRUNK_PACKET_ID,
                (server, player, handler, buf, responseSender) -> {
                    int entityId = buf.readInt();
                    server.execute(() -> {
                        Entity target = player.getWorld().getEntityById(entityId);
                        if (target instanceof CarEntity car && player.squaredDistanceTo(car) < 64.0) {
                            car.openTrunk(player);
                        } else if (target instanceof com.evecual.evecualmc.entity.RcCarEntity rc
                                && (player.squaredDistanceTo(rc) < 64.0 || rc.getPairedPlayerUuid().equals(player.getUuidAsString()))) {
                            rc.openTrunk(player);
                        } else if (target instanceof com.evecual.evecualmc.entity.RcDroneEntity drone
                                && (player.squaredDistanceTo(drone) < 64.0 || drone.getPairedPlayerUuid().equals(player.getUuidAsString()))) {
                            drone.openInventory(player);
                        } else if (target instanceof com.evecual.evecualmc.entity.RcRobotEntity robot
                                && (player.squaredDistanceTo(robot) < 64.0 || (robot.getPairedPlayerUuid().equals(player.getUuidAsString()) && player.squaredDistanceTo(robot) <= 65536.0))) {
                            robot.openInventory(player);
                        }
                    });
                });

        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(TOGGLE_RC_LIGHT_PACKET_ID,
                (server, player, handler, buf, responseSender) -> {
                    java.util.UUID vehicleUuid = buf.readUuid();
                    server.execute(() -> {
                        if (player.getServerWorld() != null) {
                            Entity target = player.getServerWorld().getEntity(vehicleUuid);
                            if (target instanceof com.evecual.evecualmc.entity.RcCarEntity rcCar) {
                                if (player.squaredDistanceTo(rcCar) <= 65536.0) {
                                    boolean newState = !rcCar.isLightOn();
                                    rcCar.setLightOn(newState);
                                    player.sendMessage(Text.literal("§e💡 RC Car Headlights: " + (newState ? "§aON" : "§cOFF")), true);
                                    player.getWorld().playSound(null, rcCar.getX(), rcCar.getY(), rcCar.getZ(),
                                            net.minecraft.sound.SoundEvents.BLOCK_LEVER_CLICK, net.minecraft.sound.SoundCategory.PLAYERS, 0.6F, newState ? 1.2F : 0.8F);
                                }
                            } else if (target instanceof com.evecual.evecualmc.entity.RcDroneEntity drone) {
                                if (player.squaredDistanceTo(drone) <= 262144.0) {
                                    boolean newState = !drone.isLightOn();
                                    drone.setLightOn(newState);
                                    player.sendMessage(Text.literal("§e💡 RC Drone Spotlight: " + (newState ? "§aON" : "§cOFF")), true);
                                    player.getWorld().playSound(null, drone.getX(), drone.getY(), drone.getZ(),
                                            net.minecraft.sound.SoundEvents.BLOCK_LEVER_CLICK, net.minecraft.sound.SoundCategory.PLAYERS, 0.6F, newState ? 1.2F : 0.8F);
                                }
                            } else if (target instanceof com.evecual.evecualmc.entity.RcRobotEntity robot) {
                                if (player.squaredDistanceTo(robot) <= 65536.0) {
                                    boolean newState = !robot.isLightOn();
                                    robot.setLightOn(newState);
                                    player.sendMessage(Text.literal("§e💡 RC Robot Work Light: " + (newState ? "§aON" : "§cOFF")), true);
                                    player.getWorld().playSound(null, robot.getX(), robot.getY(), robot.getZ(),
                                            net.minecraft.sound.SoundEvents.BLOCK_LEVER_CLICK, net.minecraft.sound.SoundCategory.PLAYERS, 0.6F, newState ? 1.2F : 0.8F);
                                }
                            }
                        }
                    });
                });

        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(TOGGLE_CABLE_PACKET_ID,
                (server, player, handler, buf, responseSender) -> {
                    server.execute(() -> {
                        CarEntity car = null;
                        if (player.getVehicle() instanceof CarEntity c) {
                            car = c;
                        } else {
                            net.minecraft.util.math.Box box = player.getBoundingBox().expand(16.0);
                            java.util.List<CarEntity> cars = player.getWorld().getEntitiesByClass(CarEntity.class, box,
                                    c -> true);
                            if (!cars.isEmpty()) {
                                cars.sort(java.util.Comparator.comparingDouble(c -> c.squaredDistanceTo(player)));
                                car = cars.get(0);
                            }
                        }

                        if (car == null) {
                            player.sendMessage(Text.literal("§c⚡ No electric car within 16 blocks! Park closer."),
                                    true);
                            return;
                        }

                        // If car is already plugged in: unplug!
                        if (car.isPluggedIn()) {
                            if (car.getConnectedExtensionPos() != null) {
                                net.minecraft.block.entity.BlockEntity be = player.getWorld()
                                        .getBlockEntity(car.getConnectedExtensionPos());
                                if (be instanceof com.evecual.evecualmc.block.entity.ChargerExtensionBlockEntity ext) {
                                    ext.disconnectCar();
                                }
                            }
                            car.unplug();
                            player.getWorld().playSound(null, car.getX(), car.getY(), car.getZ(),
                                    net.minecraft.sound.SoundEvents.BLOCK_LEVER_CLICK,
                                    net.minecraft.sound.SoundCategory.PLAYERS, 1.0F, 0.8F);
                            player.sendMessage(Text.literal("§6⚡ Charging cable disconnected. Vehicle ready to drive!"),
                                    true);
                            return;
                        }

                        // Find nearest ChargerExtensionBlockEntity within 16 blocks of car
                        net.minecraft.util.math.BlockPos center = car.getBlockPos();
                        com.evecual.evecualmc.block.entity.ChargerExtensionBlockEntity bestExt = null;
                        double bestDist = Double.MAX_VALUE;

                        for (net.minecraft.util.math.BlockPos p : net.minecraft.util.math.BlockPos
                                .iterate(center.add(-16, -5, -16), center.add(16, 5, 16))) {
                            net.minecraft.block.entity.BlockEntity be = player.getWorld().getBlockEntity(p);
                            if (be instanceof com.evecual.evecualmc.block.entity.ChargerExtensionBlockEntity ext) {
                                double d = car.squaredDistanceTo(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
                                if (d < bestDist) {
                                    bestDist = d;
                                    bestExt = ext;
                                }
                            }
                        }

                        if (bestExt == null) {
                            player.sendMessage(Text.literal("§c⚡ No Vehicle Charger Extension found within 16 blocks!"),
                                    true);
                            return;
                        }

                        if (!bestExt.hasCable()) {
                            // Check if player has cable in inventory
                            if (player.getInventory().contains(new ItemStack(CHARGER_CABLE))) {
                                int slot = player.getInventory().indexOf(new ItemStack(CHARGER_CABLE));
                                if (slot != -1 && !player.isCreative()) {
                                    player.getInventory().getStack(slot).decrement(1);
                                }
                                bestExt.setHasCable(true);
                                player.getWorld().playSound(null, bestExt.getPos(),
                                        net.minecraft.sound.SoundEvents.ITEM_ARMOR_EQUIP_CHAIN,
                                        net.minecraft.sound.SoundCategory.BLOCKS, 1.0F, 1.2F);
                            } else {
                                player.sendMessage(Text.literal(
                                        "§c⚡ Nearby Extension has no cable! Right-click it with a Charger Cable first."),
                                        true);
                                return;
                            }
                        }

                        // Connect!
                        bestExt.connectCar(car);
                        player.getWorld().playSound(null, car.getX(), car.getY(), car.getZ(),
                                net.minecraft.sound.SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN,
                                net.minecraft.sound.SoundCategory.PLAYERS, 1.0F, 1.8F);
                        player.sendMessage(Text.literal("§a⚡ Charging cable connected to car! (Press X to disconnect)"),
                                true);
                    });
                });

        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(AUTO_PARK_PACKET_ID,
                (server, player, handler, buf, responseSender) -> {
                    server.execute(() -> {
                        if (!(player.getVehicle() instanceof CarEntity car)) {
                            player.sendMessage(Text.literal("§c🅿️ You must be driving an Electric Car to auto-park!"),
                                    false);
                            return;
                        }

                        if (car.isAutoParking()) {
                            car.cancelAutoPark("§e🅿️ Auto-parking cancelled by driver.");
                            net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
                            net.minecraft.network.PacketByteBuf cancelBuf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs
                                    .create();
                            cancelBuf.writeInt(car.getId());
                            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player,
                                    CANCEL_AUTO_PARK_S2C_PACKET_ID, cancelBuf);
                            return;
                        }

                        // Search for nearest ParkingLinesBlock within 50 blocks (matching any part of
                        // the 3x2 bay)
                        net.minecraft.util.math.BlockPos carPos = car.getBlockPos();
                        net.minecraft.util.math.BlockPos bestSpot = null;
                        net.minecraft.util.math.Direction bestFacing = null;
                        double bestDistSq = Double.MAX_VALUE;

                        for (int x = -50; x <= 50; x++) {
                            for (int y = -8; y <= 8; y++) {
                                for (int z = -50; z <= 50; z++) {
                                    net.minecraft.util.math.BlockPos p = carPos.add(x, y, z);
                                    net.minecraft.block.BlockState s = player.getWorld().getBlockState(p);
                                    if (s.isOf(PARKING_LINES_BLOCK)) {
                                        com.evecual.evecualmc.block.ParkingLinesPart part = s
                                                .get(com.evecual.evecualmc.block.ParkingLinesBlock.PART);
                                        net.minecraft.util.math.Direction facing = s
                                                .get(com.evecual.evecualmc.block.ParkingLinesBlock.FACING);
                                        net.minecraft.util.math.Direction right = facing.rotateYClockwise();
                                        net.minecraft.util.math.BlockPos origin = com.evecual.evecualmc.block.ParkingLinesBlock
                                                .getOriginPos(p, facing, right, part);

                                        double dSq = origin.getSquaredDistance(carPos);
                                        if (dSq < bestDistSq) {
                                            bestDistSq = dSq;
                                            bestSpot = origin;
                                            bestFacing = facing;
                                        }
                                    }
                                }
                            }
                        }

                        if (bestSpot != null && bestFacing != null) {
                            net.minecraft.util.math.Direction right = bestFacing.rotateYClockwise();
                            // Target center of 3x2 bay:
                            // Front-left is bestSpot, width is along right, depth is along bestFacing
                            double targetX = bestSpot.getX() + 0.5 + right.getOffsetX() * 0.5
                                    + bestFacing.getOffsetX() * 1.0;
                            double targetY = bestSpot.getY();
                            double targetZ = bestSpot.getZ() + 0.5 + right.getOffsetZ() * 0.5
                                    + bestFacing.getOffsetZ() * 1.0;

                            // Approach point: 2.5 blocks in front of the empty entrance (opposite
                            // bestFacing)
                            double entryX = bestSpot.getX() + 0.5 + right.getOffsetX() * 0.5
                                    - bestFacing.getOffsetX() * 2.5;
                            double entryY = bestSpot.getY();
                            double entryZ = bestSpot.getZ() + 0.5 + right.getOffsetZ() * 0.5
                                    - bestFacing.getOffsetZ() * 2.5;

                            float targetYaw = bestFacing.asRotation();

                            car.startAutoPark(targetX, targetY, targetZ, targetYaw, entryX, entryY, entryZ);

                            // Sync to client so client vehicle physics simulation executes auto-park
                            // smoothly
                            net.minecraft.network.PacketByteBuf startBuf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs
                                    .create();
                            startBuf.writeInt(car.getId());
                            startBuf.writeDouble(targetX);
                            startBuf.writeDouble(targetY);
                            startBuf.writeDouble(targetZ);
                            startBuf.writeFloat(targetYaw);
                            startBuf.writeDouble(entryX);
                            startBuf.writeDouble(entryY);
                            startBuf.writeDouble(entryZ);
                            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player,
                                    START_AUTO_PARK_S2C_PACKET_ID, startBuf);

                            player.sendMessage(Text.literal("§a🅿️ Auto-parking engaged... Aligning to parking bay."),
                                    false);
                            player.sendMessage(Text.literal("§a🅿️ Auto-parking engaged... Aligning to parking bay."),
                                    true);
                        } else {
                            player.sendMessage(Text.literal("§c🅿️ No parking bay found within 50 blocks!"), false);
                        }
                    });
                });

        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(SELECT_COMBINER_RECIPE_PACKET_ID,
                (server, player, handler, buf, responseSender) -> {
                    int syncId = buf.readInt();
                    int recipeIndex = buf.readInt();
                    server.execute(() -> {
                        if (player.currentScreenHandler instanceof com.evecual.evecualmc.screen.ElectronicCombinerScreenHandler screenHandler
                                && screenHandler.syncId == syncId) {
                            screenHandler.selectRecipe(recipeIndex, player);
                        }
                    });
                });

        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(EXIT_RC_STATION_PACKET_ID,
                (server, player, handler, buf, responseSender) -> {
                    server.execute(() -> {
                        if (player.getServerWorld() != null) {
                            BlockPos ppos = player.getBlockPos();
                            for (int dx = -5; dx <= 5; dx++) {
                                for (int dy = -3; dy <= 3; dy++) {
                                    for (int dz = -5; dz <= 5; dz++) {
                                        BlockPos check = ppos.add(dx, dy, dz);
                                        if (player.getWorld().getBlockEntity(check) instanceof com.evecual.evecualmc.block.entity.StationaryRcControllerBlockEntity be) {
                                            if (player.getUuid().equals(be.getCurrentUserUuid())) {
                                                be.setCurrentUserUuid(null);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    });
                });

        LOGGER.info("========================================");
        LOGGER.info("  EvecualMC Initialized!                ");
        LOGGER.info("  Electronic Combiner & Electric Car ready!");
        LOGGER.info("========================================");
    }
}
