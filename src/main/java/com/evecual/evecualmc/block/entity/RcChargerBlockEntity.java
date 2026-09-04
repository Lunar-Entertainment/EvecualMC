package com.evecual.evecualmc.block.entity;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.energy.EnergyStorage;
import com.evecual.evecualmc.entity.RcCarEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RcChargerBlockEntity extends BlockEntity implements EnergyStorage {
    public static final int MAX_ENERGY = 2000;
    private int energy = 0; // Starts with 0 energy; requires external power from Solar Panels, Batteries, or Wires!

    public RcChargerBlockEntity(BlockPos pos, BlockState state) {
        super(EvecualMC.RC_CHARGER_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, RcChargerBlockEntity be) {
        // 1. Direct charging for RC Cars parked squarely on the charger pad
        Box area = new Box(pos.getX() + 0.05, pos.getY(), pos.getZ() + 0.05, pos.getX() + 0.95, pos.getY() + 0.6, pos.getZ() + 0.95);
        List<RcCarEntity> cars = world.getEntitiesByClass(RcCarEntity.class, area, RcCarEntity::isAlive);

        for (RcCarEntity car : cars) {
            // Stop auto-returning once docked
            if (car.isAutoReturning()) {
                car.onReachedCharger();
            }

            if (be.energy > 0 && car.getEnergy() < RcCarEntity.MAX_ENERGY) {
                int needed = RcCarEntity.MAX_ENERGY - car.getEnergy();
                int transfer = (int) be.extractEnergy(Math.min(needed, 10), false);
                car.setEnergy(car.getEnergy() + transfer);

                // Visual spark particles
                if (world.isClient && world.random.nextFloat() < 0.45f) {
                    world.addParticle(ParticleTypes.ELECTRIC_SPARK,
                            car.getX() + (world.random.nextDouble() - 0.5) * 0.35,
                            car.getY() + 0.15,
                            car.getZ() + (world.random.nextDouble() - 0.5) * 0.35,
                            0, 0.08, 0);
                }

                // Sound feedback occasionally
                if (car.age % 25 == 0) {
                    world.playSound(null, pos, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 0.35f, 2.0f);
                }
            }
        }

        // 2. Direct charging for RC Drones landed or hovering directly on the charger pad
        Box droneArea = new Box(pos.getX() + 0.05, pos.getY(), pos.getZ() + 0.05, pos.getX() + 0.95, pos.getY() + 1.2, pos.getZ() + 0.95);
        List<com.evecual.evecualmc.entity.RcDroneEntity> drones = world.getEntitiesByClass(com.evecual.evecualmc.entity.RcDroneEntity.class, droneArea, com.evecual.evecualmc.entity.RcDroneEntity::isAlive);

        for (com.evecual.evecualmc.entity.RcDroneEntity drone : drones) {
            // Stop auto-returning once docked
            if (drone.isAutoReturning()) {
                drone.onReachedCharger();
            }

            if (be.energy > 0 && drone.getEnergy() < com.evecual.evecualmc.entity.RcDroneEntity.MAX_ENERGY) {
                int needed = com.evecual.evecualmc.entity.RcDroneEntity.MAX_ENERGY - drone.getEnergy();
                int transfer = (int) be.extractEnergy(Math.min(needed, 10), false);
                drone.setEnergy(drone.getEnergy() + transfer);

                // Visual spark particles
                if (world.isClient && world.random.nextFloat() < 0.45f) {
                    world.addParticle(ParticleTypes.ELECTRIC_SPARK,
                            drone.getX() + (world.random.nextDouble() - 0.5) * 0.35,
                            drone.getY() + 0.15,
                            drone.getZ() + (world.random.nextDouble() - 0.5) * 0.35,
                            0, 0.08, 0);
                }

                if (drone.age % 25 == 0) {
                    world.playSound(null, pos, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 0.35f, 2.2f);
                }
            }
        }

        // 3. Wireless Inductive Charging to RC Parking Spots and Drone Parking Spots within 16 blocks radius
        if (be.energy > 0) {
            Box searchBox = new Box(pos).expand(16.0);

            // Wirelessly charge RC Cars parked in an RC Parking Spot
            List<RcCarEntity> parkedCars = world.getEntitiesByClass(RcCarEntity.class, searchBox, c -> c.isAlive() && c.isInParkingSpot());
            for (RcCarEntity car : parkedCars) {
                if (be.energy <= 0) break;
                if (car.getEnergy() < RcCarEntity.MAX_ENERGY) {
                    int needed = RcCarEntity.MAX_ENERGY - car.getEnergy();
                    int transfer = (int) be.extractEnergy(Math.min(needed, 10), false);
                    car.setEnergy(car.getEnergy() + transfer);

                    // Wireless particle transmission beam
                    if (world.isClient && world.random.nextFloat() < 0.45f) {
                        double t = world.random.nextDouble();
                        double px = net.minecraft.util.math.MathHelper.lerp(t, pos.getX() + 0.5, car.getX());
                        double py = net.minecraft.util.math.MathHelper.lerp(t, pos.getY() + 0.3, car.getY() + 0.15);
                        double pz = net.minecraft.util.math.MathHelper.lerp(t, pos.getZ() + 0.5, car.getZ());
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

            // Wirelessly charge RC Drones landed in a Drone Parking Spot
            List<com.evecual.evecualmc.entity.RcDroneEntity> parkedDrones = world.getEntitiesByClass(com.evecual.evecualmc.entity.RcDroneEntity.class, searchBox, d -> d.isAlive() && d.isInParkingSpot());
            for (com.evecual.evecualmc.entity.RcDroneEntity drone : parkedDrones) {
                if (be.energy <= 0) break;
                if (drone.getEnergy() < com.evecual.evecualmc.entity.RcDroneEntity.MAX_ENERGY) {
                    int needed = com.evecual.evecualmc.entity.RcDroneEntity.MAX_ENERGY - drone.getEnergy();
                    int transfer = (int) be.extractEnergy(Math.min(needed, 10), false);
                    drone.setEnergy(drone.getEnergy() + transfer);

                    // Wireless particle transmission beam
                    if (world.isClient && world.random.nextFloat() < 0.45f) {
                        double t = world.random.nextDouble();
                        double px = net.minecraft.util.math.MathHelper.lerp(t, pos.getX() + 0.5, drone.getX());
                        double py = net.minecraft.util.math.MathHelper.lerp(t, pos.getY() + 0.3, drone.getY() + 0.15);
                        double pz = net.minecraft.util.math.MathHelper.lerp(t, pos.getZ() + 0.5, drone.getZ());
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
        }
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
