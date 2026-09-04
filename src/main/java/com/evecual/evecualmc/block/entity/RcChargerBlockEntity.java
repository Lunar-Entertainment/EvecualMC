package com.evecual.evecualmc.block.entity;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.energy.EnergyStorage;
import com.evecual.evecualmc.entity.RcCarEntity;
import com.evecual.evecualmc.entity.RcDroneEntity;
import com.evecual.evecualmc.entity.RcRobotEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RcChargerBlockEntity extends BlockEntity implements EnergyStorage {
    public static final int MAX_ENERGY = 2000;
    public static final double BROADCAST_RADIUS = 16.0;
    public static final double BROADCAST_RADIUS_SQ = BROADCAST_RADIUS * BROADCAST_RADIUS;

    private int energy = 0;
    private int scanTimer = 0;
    private final List<BlockPos> connectedSpots = new ArrayList<>();

    public RcChargerBlockEntity(BlockPos pos, BlockState state) {
        super(EvecualMC.RC_CHARGER_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, RcChargerBlockEntity be) {
        // 1. Periodically discover all valid parking spots within the 16-block broadcast radius
        be.scanTimer++;
        if (be.scanTimer % 40 == 1 || be.connectedSpots.isEmpty()) {
            be.connectedSpots.clear();
            int rad = (int) BROADCAST_RADIUS;
            for (int dx = -rad; dx <= rad; dx++) {
                for (int dy = -8; dy <= 8; dy++) {
                    for (int dz = -rad; dz <= rad; dz++) {
                        if (dx * dx + dy * dy + dz * dz <= BROADCAST_RADIUS_SQ) {
                            BlockPos checkPos = pos.add(dx, dy, dz);
                            BlockState bs = world.getBlockState(checkPos);
                            if (bs.isOf(EvecualMC.RC_PARKING_SPOT_BLOCK) ||
                                bs.isOf(EvecualMC.DRONE_PARKING_SPOT_BLOCK) ||
                                bs.isOf(EvecualMC.ROBOT_PARKING_SPOT_BLOCK)) {
                                be.connectedSpots.add(checkPos.toImmutable());
                            }
                        }
                    }
                }
            }
        }

        // 2. Idle "Charge Ready" particle effect on all in-range parking spots when charger is powered
        if (be.energy > 0) {
            if (!world.isClient() && world instanceof ServerWorld serverWorld) {
                for (BlockPos spotPos : be.connectedSpots) {
                    if (world.random.nextFloat() < 0.35f) {
                        double px = spotPos.getX() + 0.15 + world.random.nextDouble() * 0.7;
                        double py = spotPos.getY() + 0.08;
                        double pz = spotPos.getZ() + 0.15 + world.random.nextDouble() * 0.7;
                        serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, px, py, pz, 1, 0.0, 0.02, 0.0, 0.0);
                    }
                }
            } else if (world.isClient()) {
                for (BlockPos spotPos : be.connectedSpots) {
                    if (world.random.nextFloat() < 0.25f) {
                        double px = spotPos.getX() + 0.15 + world.random.nextDouble() * 0.7;
                        double py = spotPos.getY() + 0.08;
                        double pz = spotPos.getZ() + 0.15 + world.random.nextDouble() * 0.7;
                        world.addParticle(ParticleTypes.ELECTRIC_SPARK, px, py, pz, 0.0, 0.02, 0.0);
                    }
                }
            }
        }

        // 3. Wireless Inductive Charging to parked vehicles in their respective spots
        if (be.energy > 0) {
            Box searchBox = new Box(pos).expand(BROADCAST_RADIUS);

            // A. RC Cars parked in RC Parking Spot
            List<RcCarEntity> parkedCars = world.getEntitiesByClass(RcCarEntity.class, searchBox,
                    c -> c.isAlive() && c.isInParkingSpot() && pos.getSquaredDistance(c.getBlockPos()) <= BROADCAST_RADIUS_SQ);
            for (RcCarEntity car : parkedCars) {
                if (be.energy <= 0) break;
                if (car.getEnergy() < RcCarEntity.MAX_ENERGY) {
                    int needed = RcCarEntity.MAX_ENERGY - car.getEnergy();
                    int transfer = (int) be.extractEnergy(Math.min(needed, 10), false);
                    car.setEnergy(car.getEnergy() + transfer);

                    if (world.isClient && world.random.nextFloat() < 0.45f) {
                        double t = world.random.nextDouble();
                        double px = MathHelper.lerp(t, pos.getX() + 0.5, car.getX());
                        double py = MathHelper.lerp(t, pos.getY() + 0.5, car.getY() + 0.15);
                        double pz = MathHelper.lerp(t, pos.getZ() + 0.5, car.getZ());
                        world.addParticle(ParticleTypes.ELECTRIC_SPARK, px, py, pz, 0, 0.02, 0);

                        world.addParticle(ParticleTypes.ELECTRIC_SPARK,
                                car.getX() + (world.random.nextDouble() - 0.5) * 0.35,
                                car.getY() + 0.15,
                                car.getZ() + (world.random.nextDouble() - 0.5) * 0.35,
                                0, 0.08, 0);
                    }

                    if (car.age % 25 == 0) {
                        world.playSound(null, car.getBlockPos(), SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 0.25f, 2.0f);
                    }
                }
            }

            // B. RC Drones landed in Drone Helipad
            List<RcDroneEntity> parkedDrones = world.getEntitiesByClass(RcDroneEntity.class, searchBox,
                    d -> d.isAlive() && d.isInParkingSpot() && pos.getSquaredDistance(d.getBlockPos()) <= BROADCAST_RADIUS_SQ);
            for (RcDroneEntity drone : parkedDrones) {
                if (be.energy <= 0) break;
                if (drone.getEnergy() < RcDroneEntity.MAX_ENERGY) {
                    int needed = RcDroneEntity.MAX_ENERGY - drone.getEnergy();
                    int transfer = (int) be.extractEnergy(Math.min(needed, 10), false);
                    drone.setEnergy(drone.getEnergy() + transfer);

                    if (world.isClient && world.random.nextFloat() < 0.45f) {
                        double t = world.random.nextDouble();
                        double px = MathHelper.lerp(t, pos.getX() + 0.5, drone.getX());
                        double py = MathHelper.lerp(t, pos.getY() + 0.5, drone.getY() + 0.15);
                        double pz = MathHelper.lerp(t, pos.getZ() + 0.5, drone.getZ());
                        world.addParticle(ParticleTypes.ELECTRIC_SPARK, px, py, pz, 0, 0.02, 0);

                        world.addParticle(ParticleTypes.ELECTRIC_SPARK,
                                drone.getX() + (world.random.nextDouble() - 0.5) * 0.35,
                                drone.getY() + 0.15,
                                drone.getZ() + (world.random.nextDouble() - 0.5) * 0.35,
                                0, 0.08, 0);
                    }

                    if (drone.age % 25 == 0) {
                        world.playSound(null, drone.getBlockPos(), SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 0.25f, 2.2f);
                    }
                }
            }

            // C. RC Robots docked in Robot Parking Spot
            List<RcRobotEntity> parkedRobots = world.getEntitiesByClass(RcRobotEntity.class, searchBox,
                    r -> r.isAlive() && r.isInParkingSpot() && pos.getSquaredDistance(r.getBlockPos()) <= BROADCAST_RADIUS_SQ);
            for (RcRobotEntity robot : parkedRobots) {
                if (be.energy <= 0) break;
                if (robot.getEnergy() < RcRobotEntity.MAX_ENERGY) {
                    int needed = RcRobotEntity.MAX_ENERGY - robot.getEnergy();
                    int transfer = (int) be.extractEnergy(Math.min(needed, 10), false);
                    robot.setEnergy(robot.getEnergy() + transfer);

                    if (world.isClient && world.random.nextFloat() < 0.45f) {
                        double t = world.random.nextDouble();
                        double px = MathHelper.lerp(t, pos.getX() + 0.5, robot.getX());
                        double py = MathHelper.lerp(t, pos.getY() + 0.5, robot.getY() + 0.15);
                        double pz = MathHelper.lerp(t, pos.getZ() + 0.5, robot.getZ());
                        world.addParticle(ParticleTypes.ELECTRIC_SPARK, px, py, pz, 0, 0.02, 0);

                        world.addParticle(ParticleTypes.ELECTRIC_SPARK,
                                robot.getX() + (world.random.nextDouble() - 0.5) * 0.35,
                                robot.getY() + 0.15,
                                robot.getZ() + (world.random.nextDouble() - 0.5) * 0.35,
                                0, 0.08, 0);
                    }

                    if (robot.age % 25 == 0) {
                        world.playSound(null, robot.getBlockPos(), SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 0.25f, 1.8f);
                    }
                }
            }
        }
    }

    public List<BlockPos> getConnectedSpots() {
        return this.connectedSpots;
    }

    @Override
    public long getEnergy() {
        return this.energy;
    }

    @Override
    public long getMaxEnergy() {
        return MAX_ENERGY;
    }

    @Override
    public long insertEnergy(long amount, boolean simulate) {
        long accepted = Math.min(amount, MAX_ENERGY - this.energy);
        if (!simulate) {
            this.energy += (int) accepted;
            markDirty();
        }
        return accepted;
    }

    @Override
    public long extractEnergy(long amount, boolean simulate) {
        long extracted = Math.min(amount, this.energy);
        if (!simulate) {
            this.energy -= (int) extracted;
            markDirty();
        }
        return extracted;
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("Energy", this.energy);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("Energy")) {
            this.energy = nbt.getInt("Energy");
        }
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = new NbtCompound();
        writeNbt(nbt);
        return nbt;
    }
}
