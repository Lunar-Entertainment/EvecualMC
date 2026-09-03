package com.evecual.evecualmc.block.entity;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.energy.EnergyStorage;
import com.evecual.evecualmc.entity.CarEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

public class ChargerBlockEntity extends BlockEntity implements EnergyStorage {
    public static final int MAX_CAPACITY = 1000;
    private int energy = 0;
    private boolean isCharging = false;

    public ChargerBlockEntity(BlockPos pos, BlockState state) {
        super(EvecualMC.CHARGER_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, ChargerBlockEntity be) {
        if (world.isClient) return;

        boolean wasCharging = be.isCharging;
        be.isCharging = false;

        if (be.energy > 0) {
            // Find nearby electric cars within 3.5 blocks
            Box searchBox = new Box(pos).expand(3.5);
            List<CarEntity> cars = world.getEntitiesByClass(CarEntity.class, searchBox, car -> car.getEnergy() < car.getMaxEnergy());

            if (!cars.isEmpty()) {
                CarEntity car = cars.get(0);
                int needed = car.getMaxEnergy() - car.getEnergy();
                int toCharge = Math.min(be.energy, Math.min(needed, 5)); // 5 E per tick = 100 E/s

                if (toCharge > 0) {
                    car.charge(toCharge);
                    be.energy -= toCharge;
                    be.isCharging = true;

                    // Emit electric charging particles
                    if (world instanceof ServerWorld serverWorld && world.getTime() % 3 == 0) {
                        serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                                pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5,
                                3, 0.1, 0.1, 0.1, 0.05);

                        serverWorld.spawnParticles(ParticleTypes.COMPOSTER,
                                car.getX(), car.getY() + 0.5, car.getZ(),
                                2, 0.2, 0.2, 0.2, 0.02);
                    }

                    be.markDirty();
                }
            }
        }

        if (wasCharging != be.isCharging) {
            be.sync();
        }
    }

    public boolean isCharging() {
        return isCharging;
    }

    @Override
    public long getEnergy() {
        return energy;
    }

    @Override
    public long getMaxEnergy() {
        return MAX_CAPACITY;
    }

    @Override
    public long insertEnergy(long amount, boolean simulate) {
        long canInsert = Math.min(amount, MAX_CAPACITY - energy);
        if (!simulate && canInsert > 0) {
            energy += canInsert;
            markDirty();
            sync();
        }
        return canInsert;
    }

    @Override
    public long extractEnergy(long amount, boolean simulate) {
        long canExtract = Math.min(amount, energy);
        if (!simulate && canExtract > 0) {
            energy -= canExtract;
            markDirty();
            sync();
        }
        return canExtract;
    }

    public void sync() {
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.energy = nbt.getInt("Energy");
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("Energy", this.energy);
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }
}
