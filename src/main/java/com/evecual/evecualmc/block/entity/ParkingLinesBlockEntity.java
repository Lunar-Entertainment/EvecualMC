package com.evecual.evecualmc.block.entity;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.ParkingLinesBlock;
import com.evecual.evecualmc.entity.CarEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;

public class ParkingLinesBlockEntity extends BlockEntity {
    public ParkingLinesBlockEntity(BlockPos pos, BlockState state) {
        super(EvecualMC.PARKING_LINES_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, ParkingLinesBlockEntity be) {
        if (world.isClient || !(world instanceof ServerWorld serverWorld)) return;

        if (world.getTime() % 5 != 0) return; // Tick every quarter-second

        // Calculate 3x2 bounding box
        Direction facing = state.get(ParkingLinesBlock.FACING);
        Direction right = facing.rotateYClockwise();

        BlockPos corner1 = pos;
        BlockPos corner2 = pos.offset(facing, 2).offset(right, 1);

        double minX = Math.min(corner1.getX(), corner2.getX()) - 0.5;
        double maxX = Math.max(corner1.getX(), corner2.getX()) + 1.5;
        double minY = pos.getY() - 0.5;
        double maxY = pos.getY() + 2.0;
        double minZ = Math.min(corner1.getZ(), corner2.getZ()) - 0.5;
        double maxZ = Math.max(corner1.getZ(), corner2.getZ()) + 1.5;

        Box parkingBox = new Box(minX, minY, minZ, maxX, maxY, maxZ);
        List<CarEntity> cars = world.getEntitiesByClass(CarEntity.class, parkingBox, car -> true);

        if (cars.isEmpty()) return;

        for (CarEntity car : cars) {
            if (car.getEnergy() >= car.getMaxEnergy()) continue;

            // Search for vehicle charger within 12 blocks
            BlockPos chargerPos = findNearbyCharger(world, pos, 12);
            if (chargerPos != null) {
                BlockEntity chargerBe = world.getBlockEntity(chargerPos);
                long availableEnergy = 0;
                if (chargerBe instanceof ChargerBlockEntity charger) {
                    availableEnergy = charger.getEnergy();
                }

                if (availableEnergy > 0) {
                    int needed = car.getMaxEnergy() - car.getEnergy();
                    int transfer = Math.min(4, Math.min(needed, (int) availableEnergy));
                    if (transfer > 0) {
                        if (chargerBe instanceof ChargerBlockEntity charger) {
                            charger.extractEnergy(transfer, false);
                        }
                        car.setEnergy(car.getEnergy() + transfer);

                        // Visual feedback
                        if (world.getTime() % 10 == 0) {
                            serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                                    car.getX(), car.getY() + 0.4, car.getZ(),
                                    3, 0.4, 0.2, 0.4, 0.02);
                        }

                        if (car.getFirstPassenger() instanceof PlayerEntity player && world.getTime() % 20 == 0) {
                            player.sendMessage(Text.literal("§a⚡ EV Parking Bay: Auto-charging (" + car.getEnergy() + "/" + car.getMaxEnergy() + " E)"), true);
                        }
                    }
                }
            }
        }
    }

    private static BlockPos findNearbyCharger(World world, BlockPos origin, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos p = origin.add(x, y, z);
                    BlockState s = world.getBlockState(p);
                    if (s.isOf(EvecualMC.CHARGER_BLOCK) || s.isOf(EvecualMC.CHARGER_EXTENSION_BLOCK)) {
                        return p;
                    }
                }
            }
        }
        return null;
    }

    public boolean isCarParked() {
        if (world == null) return false;
        BlockState state = getCachedState();
        if (!state.isOf(EvecualMC.PARKING_LINES_BLOCK)) return false;
        Direction facing = state.get(ParkingLinesBlock.FACING);
        Direction right = facing.rotateYClockwise();

        BlockPos corner1 = pos;
        BlockPos corner2 = pos.offset(facing, 2).offset(right, 1);

        double minX = Math.min(corner1.getX(), corner2.getX()) - 0.5;
        double maxX = Math.max(corner1.getX(), corner2.getX()) + 1.5;
        double minY = pos.getY() - 0.5;
        double maxY = pos.getY() + 2.0;
        double minZ = Math.min(corner1.getZ(), corner2.getZ()) - 0.5;
        double maxZ = Math.max(corner1.getZ(), corner2.getZ()) + 1.5;

        Box parkingBox = new Box(minX, minY, minZ, maxX, maxY, maxZ);
        List<CarEntity> cars = world.getEntitiesByClass(CarEntity.class, parkingBox, car -> true);
        return !cars.isEmpty();
    }
}
