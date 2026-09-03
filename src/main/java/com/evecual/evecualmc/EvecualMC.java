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

    public static final Item UPGRADED_ENGINE = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "upgraded_electric_engine"),
            new Item(new Item.Settings())
    );

    public static final Item TRUNK_UPGRADE = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "trunk_upgrade"),
            new Item(new Item.Settings())
    );

    public static final Item CAR_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "car"),
            new CarItem(new Item.Settings().maxCount(1))
    );

    // Blocks (All mineable by hand and drop themselves!)
    public static final Block SOLAR_PANEL_BLOCK = Registry.register(
            Registries.BLOCK,
            new Identifier(MOD_ID, "solar_panel"),
            new SolarPanelBlock(FabricBlockSettings.create().strength(0.8f).nonOpaque().sounds(BlockSoundGroup.METAL))
    );

    public static final Item SOLAR_PANEL_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "solar_panel"),
            new BlockItem(SOLAR_PANEL_BLOCK, new Item.Settings())
    );

    public static final Block BATTERY_BLOCK = Registry.register(
            Registries.BLOCK,
            new Identifier(MOD_ID, "battery"),
            new BatteryBlock(FabricBlockSettings.create().strength(1.0f).sounds(BlockSoundGroup.METAL))
    );

    public static final Item BATTERY_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "battery"),
            new BlockItem(BATTERY_BLOCK, new Item.Settings())
    );

    public static final Block WIRE_BLOCK = Registry.register(
            Registries.BLOCK,
            new Identifier(MOD_ID, "wire"),
            new WireBlock(FabricBlockSettings.create().strength(0.3f).nonOpaque().sounds(BlockSoundGroup.COPPER))
    );

    public static final Item WIRE_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "wire"),
            new BlockItem(WIRE_BLOCK, new Item.Settings())
    );

    public static final Block ELECTRONIC_COMBINER_BLOCK = Registry.register(
            Registries.BLOCK,
            new Identifier(MOD_ID, "electronic_combiner"),
            new ElectronicCombinerBlock(FabricBlockSettings.create().strength(1.2f).sounds(BlockSoundGroup.METAL))
    );

    public static final Item ELECTRONIC_COMBINER_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "electronic_combiner"),
            new BlockItem(ELECTRONIC_COMBINER_BLOCK, new Item.Settings())
    );

    public static final Block CHARGER_BLOCK = Registry.register(
            Registries.BLOCK,
            new Identifier(MOD_ID, "charger"),
            new ChargerBlock(FabricBlockSettings.create().strength(1.0f).sounds(BlockSoundGroup.METAL))
    );

    public static final Item CHARGER_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "charger"),
            new BlockItem(CHARGER_BLOCK, new Item.Settings())
    );

    public static final Block CHARGER_EXTENSION_BLOCK = Registry.register(
            Registries.BLOCK,
            new Identifier(MOD_ID, "charger_extension"),
            new com.evecual.evecualmc.block.ChargerExtensionBlock(FabricBlockSettings.create().strength(1.0f).sounds(BlockSoundGroup.METAL))
    );

    public static final Item CHARGER_EXTENSION_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "charger_extension"),
            new BlockItem(CHARGER_EXTENSION_BLOCK, new Item.Settings())
    );

    public static final Block PARKING_LINES_BLOCK = Registry.register(
            Registries.BLOCK,
            new Identifier(MOD_ID, "parking_lines"),
            new com.evecual.evecualmc.block.ParkingLinesBlock(FabricBlockSettings.create().strength(0.5f).sounds(BlockSoundGroup.STONE).nonOpaque().noCollision())
    );

    public static final Item PARKING_LINES_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "parking_lines"),
            new BlockItem(PARKING_LINES_BLOCK, new Item.Settings())
    );

    public static final Item CHARGER_CABLE = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "charger_cable"),
            new Item(new Item.Settings().maxCount(1))
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

    public static final BlockEntityType<com.evecual.evecualmc.block.entity.ChargerExtensionBlockEntity> CHARGER_EXTENSION_BLOCK_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            new Identifier(MOD_ID, "charger_extension"),
            FabricBlockEntityTypeBuilder.create(com.evecual.evecualmc.block.entity.ChargerExtensionBlockEntity::new, CHARGER_EXTENSION_BLOCK).build()
    );

    public static final BlockEntityType<com.evecual.evecualmc.block.entity.ParkingLinesBlockEntity> PARKING_LINES_BLOCK_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            new Identifier(MOD_ID, "parking_lines"),
            FabricBlockEntityTypeBuilder.create(com.evecual.evecualmc.block.entity.ParkingLinesBlockEntity::new, PARKING_LINES_BLOCK).build()
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
                    .dimensions(EntityDimensions.fixed(2.6f, 1.88f))
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
                entries.add(UPGRADED_ENGINE);
                entries.add(TRUNK_UPGRADE);
                entries.add(CAR_ITEM);
                entries.add(SOLAR_PANEL_ITEM);
                entries.add(BATTERY_ITEM);
                entries.add(WIRE_ITEM);
                entries.add(ELECTRONIC_COMBINER_ITEM);
                entries.add(CHARGER_ITEM);
                entries.add(CHARGER_EXTENSION_ITEM);
                entries.add(CHARGER_CABLE);
                entries.add(PARKING_LINES_ITEM);
            })
            .build();

    public static final Identifier CAR_INPUT_PACKET_ID = new Identifier(MOD_ID, "car_input");
    public static final Identifier OPEN_TRUNK_PACKET_ID = new Identifier(MOD_ID, "open_trunk");
    public static final Identifier CHARGER_WAYPOINT_PACKET_ID = new Identifier(MOD_ID, "charger_waypoint");
    public static final Identifier TOGGLE_CABLE_PACKET_ID = new Identifier(MOD_ID, "toggle_cable");
    public static final Identifier AUTO_PARK_PACKET_ID = new Identifier(MOD_ID, "auto_park");

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

        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(TOGGLE_CABLE_PACKET_ID, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                CarEntity car = null;
                if (player.getVehicle() instanceof CarEntity c) {
                    car = c;
                } else {
                    net.minecraft.util.math.Box box = player.getBoundingBox().expand(16.0);
                    java.util.List<CarEntity> cars = player.getWorld().getEntitiesByClass(CarEntity.class, box, c -> true);
                    if (!cars.isEmpty()) {
                        cars.sort(java.util.Comparator.comparingDouble(c -> c.squaredDistanceTo(player)));
                        car = cars.get(0);
                    }
                }

                if (car == null) {
                    player.sendMessage(Text.literal("§c⚡ No electric car within 16 blocks! Park closer."), true);
                    return;
                }

                // If car is already plugged in: unplug!
                if (car.isPluggedIn()) {
                    if (car.getConnectedExtensionPos() != null) {
                        net.minecraft.block.entity.BlockEntity be = player.getWorld().getBlockEntity(car.getConnectedExtensionPos());
                        if (be instanceof com.evecual.evecualmc.block.entity.ChargerExtensionBlockEntity ext) {
                            ext.disconnectCar();
                        }
                    }
                    car.unplug();
                    player.getWorld().playSound(null, car.getX(), car.getY(), car.getZ(),
                            net.minecraft.sound.SoundEvents.BLOCK_LEVER_CLICK, net.minecraft.sound.SoundCategory.PLAYERS, 1.0F, 0.8F);
                    player.sendMessage(Text.literal("§6⚡ Charging cable disconnected. Vehicle ready to drive!"), true);
                    return;
                }

                // Find nearest ChargerExtensionBlockEntity within 16 blocks of car
                net.minecraft.util.math.BlockPos center = car.getBlockPos();
                com.evecual.evecualmc.block.entity.ChargerExtensionBlockEntity bestExt = null;
                double bestDist = Double.MAX_VALUE;

                for (net.minecraft.util.math.BlockPos p : net.minecraft.util.math.BlockPos.iterate(center.add(-16, -5, -16), center.add(16, 5, 16))) {
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
                    player.sendMessage(Text.literal("§c⚡ No Vehicle Charger Extension found within 16 blocks!"), true);
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
                        player.getWorld().playSound(null, bestExt.getPos(), net.minecraft.sound.SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, net.minecraft.sound.SoundCategory.BLOCKS, 1.0F, 1.2F);
                    } else {
                        player.sendMessage(Text.literal("§c⚡ Nearby Extension has no cable! Right-click it with a Charger Cable first."), true);
                        return;
                    }
                }

                // Connect!
                bestExt.connectCar(car);
                player.getWorld().playSound(null, car.getX(), car.getY(), car.getZ(),
                        net.minecraft.sound.SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, net.minecraft.sound.SoundCategory.PLAYERS, 1.0F, 1.8F);
                player.sendMessage(Text.literal("§a⚡ Charging cable connected to car! (Press X to disconnect)"), true);
            });
        });

        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(AUTO_PARK_PACKET_ID, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                if (!(player.getVehicle() instanceof CarEntity car)) {
                    player.sendMessage(Text.literal("§c🅿️ You must be driving an Electric Car to auto-park!"), false);
                    return;
                }

                if (car.isAutoParking()) {
                    car.cancelAutoPark("§e🅿️ Auto-parking cancelled by driver.");
                    return;
                }

                // Search for nearest ParkingLinesBlock within 15 blocks (matching any part of the 3x2 bay)
                net.minecraft.util.math.BlockPos carPos = car.getBlockPos();
                net.minecraft.util.math.BlockPos bestSpot = null;
                net.minecraft.util.math.Direction bestFacing = null;
                double bestDistSq = Double.MAX_VALUE;

                for (int x = -15; x <= 15; x++) {
                    for (int y = -4; y <= 4; y++) {
                        for (int z = -15; z <= 15; z++) {
                            net.minecraft.util.math.BlockPos p = carPos.add(x, y, z);
                            net.minecraft.block.BlockState s = player.getWorld().getBlockState(p);
                            if (s.isOf(PARKING_LINES_BLOCK)) {
                                com.evecual.evecualmc.block.ParkingLinesPart part = s.get(com.evecual.evecualmc.block.ParkingLinesBlock.PART);
                                net.minecraft.util.math.Direction facing = s.get(com.evecual.evecualmc.block.ParkingLinesBlock.FACING);
                                net.minecraft.util.math.Direction right = facing.rotateYClockwise();
                                net.minecraft.util.math.BlockPos origin = com.evecual.evecualmc.block.ParkingLinesBlock.getOriginPos(p, facing, right, part);

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
                    double targetX = bestSpot.getX() + 0.5 + right.getOffsetX() * 0.5 + bestFacing.getOffsetX() * 1.0;
                    double targetY = bestSpot.getY();
                    double targetZ = bestSpot.getZ() + 0.5 + right.getOffsetZ() * 0.5 + bestFacing.getOffsetZ() * 1.0;

                    // Approach point: 2.5 blocks in front of the empty entrance (opposite bestFacing)
                    double entryX = bestSpot.getX() + 0.5 + right.getOffsetX() * 0.5 - bestFacing.getOffsetX() * 2.5;
                    double entryY = bestSpot.getY();
                    double entryZ = bestSpot.getZ() + 0.5 + right.getOffsetZ() * 0.5 - bestFacing.getOffsetZ() * 2.5;

                    float targetYaw = bestFacing.asRotation();

                    car.startAutoPark(targetX, targetY, targetZ, targetYaw, entryX, entryY, entryZ);
                    player.sendMessage(Text.literal("§a🅿️ Auto-parking engaged... Aligning to parking bay."), false);
                    player.sendMessage(Text.literal("§a🅿️ Auto-parking engaged... Aligning to parking bay."), true);
                } else {
                    player.sendMessage(Text.literal("§c🅿️ No parking bay found within 15 blocks!"), false);
                }
            });
        });

        LOGGER.info("========================================");
        LOGGER.info("  EvecualMC Initialized!                ");
        LOGGER.info("  Electronic Combiner & Electric Car ready!");
        LOGGER.info("========================================");
    }
}
