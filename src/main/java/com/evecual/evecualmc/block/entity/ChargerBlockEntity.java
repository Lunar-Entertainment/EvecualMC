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

        // Base charger holds power for the extension
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
