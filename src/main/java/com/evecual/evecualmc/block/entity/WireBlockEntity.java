package com.evecual.evecualmc.block.entity;

import com.evecual.evecualmc.EvecualMC;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;

public class WireBlockEntity extends BlockEntity {
    private int transferRate = 0; // Current EU/t moving through this wire
    private int tickTracker = 0;

    public WireBlockEntity(BlockPos pos, BlockState state) {
        super(EvecualMC.WIRE_BLOCK_ENTITY, pos, state);
    }

    public static void tick(net.minecraft.world.World world, BlockPos pos, BlockState state, WireBlockEntity be) {
        if (world.isClient) return;

        be.tickTracker++;
        if (be.tickTracker >= 20) {
            be.tickTracker = 0;
            // Decay throughput gradually if no active transfer
            if (be.transferRate > 0) {
                be.transferRate = Math.max(0, be.transferRate - 5);
                be.markDirty();
                be.sync();
            }
        }
    }

    public void recordEnergyTransfer(long amount) {
        this.transferRate = (int) Math.max(this.transferRate, amount);
        this.tickTracker = 0;
        markDirty();
        sync();
    }

    public int getTransferRate() {
        return transferRate;
    }

    public void sync() {
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), net.minecraft.block.Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.transferRate = nbt.getInt("TransferRate");
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("TransferRate", this.transferRate);
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
