package com.evecual.evecualmc.block.entity;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.entity.CarEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ChargerExtensionBlockEntity extends BlockEntity {
    private boolean hasCable = false;
    private int connectedCarId = -1;

    public ChargerExtensionBlockEntity(BlockPos pos, BlockState state) {
        super(EvecualMC.CHARGER_EXTENSION_BLOCK_ENTITY, pos, state);
    }

    public boolean hasCable() {
        return hasCable;
    }

    public void setHasCable(boolean hasCable) {
        this.hasCable = hasCable;
        if (this.world != null && !this.world.isClient) {
            BlockState state = this.world.getBlockState(this.pos);
            if (state.isOf(EvecualMC.CHARGER_EXTENSION_BLOCK)) {
                this.world.setBlockState(this.pos, state.with(com.evecual.evecualmc.block.ChargerExtensionBlock.HAS_CABLE, hasCable), Block.NOTIFY_ALL);
            }
        }
        markDirty();
        sync();
    }

    public boolean isConnected() {
        return connectedCarId != -1;
    }

    public int getConnectedCarId() {
        return connectedCarId;
    }

    public void connectCar(CarEntity car) {
        this.connectedCarId = car.getId();
        car.setPluggedIn(this.pos);
        if (this.world != null && !this.world.isClient) {
            BlockState state = this.world.getBlockState(this.pos);
            if (state.isOf(EvecualMC.CHARGER_EXTENSION_BLOCK)) {
                this.world.setBlockState(this.pos, state.with(com.evecual.evecualmc.block.ChargerExtensionBlock.HAS_CABLE, true)
                        .with(com.evecual.evecualmc.block.ChargerExtensionBlock.CONNECTED, true), Block.NOTIFY_ALL);
            }
        }
        markDirty();
        sync();
    }

    public void disconnectCar() {
        if (this.world != null && this.connectedCarId != -1) {
            Entity entity = this.world.getEntityById(this.connectedCarId);
            if (entity instanceof CarEntity car) {
                car.unplug();
            }
        }
        this.connectedCarId = -1;
        if (this.world != null && !this.world.isClient) {
            BlockState state = this.world.getBlockState(this.pos);
            if (state.isOf(EvecualMC.CHARGER_EXTENSION_BLOCK)) {
                this.world.setBlockState(this.pos, state.with(com.evecual.evecualmc.block.ChargerExtensionBlock.CONNECTED, false), Block.NOTIFY_ALL);
            }
        }
        markDirty();
        sync();
    }

    public static void tick(World world, BlockPos pos, BlockState state, ChargerExtensionBlockEntity be) {
        if (be.connectedCarId == -1) return;

        Entity entity = world.getEntityById(be.connectedCarId);
        if (entity instanceof CarEntity car && car.isAlive() && car.squaredDistanceTo(pos.toCenterPos()) <= 100.0) {
            // Keep car immobilized while plugged in
            car.setPluggedIn(pos);

            // Extract power from ChargerBlock below
            if (!world.isClient) {
                BlockEntity below = world.getBlockEntity(pos.down());
                if (below instanceof ChargerBlockEntity charger && charger.getEnergy() > 0) {
                    int needed = car.getMaxEnergy() - car.getEnergy();
                    int transfer = Math.min((int) charger.getEnergy(), Math.min(needed, 10)); // 10 E per tick = 200 E/s
                    if (transfer > 0) {
                        charger.extractEnergy(transfer, false);
                        car.charge(transfer);
                    }
                }

                // Render continuous charging cable particle line between extension and car
                if (world instanceof ServerWorld serverWorld && world.getTime() % 2 == 0) {
                    Vec3d start = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5);
                    Vec3d end = new Vec3d(car.getX(), car.getY() + 0.6, car.getZ());
                    Vec3d diff = end.subtract(start);
                    int steps = (int) (diff.length() * 3);

                    for (int i = 0; i <= steps; ++i) {
                        double t = (double) i / Math.max(1, steps);
                        double px = start.x + diff.x * t;
                        double py = start.y + diff.y * t - Math.sin(t * Math.PI) * 0.25; // slight natural cable sag
                        double pz = start.z + diff.z * t;
                        serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, px, py, pz, 1, 0.02, 0.02, 0.02, 0.0);
                    }

                    serverWorld.spawnParticles(ParticleTypes.COMPOSTER, end.x, end.y, end.z, 2, 0.1, 0.1, 0.1, 0.02);
                }
            }
        } else {
            // Disconnect if car drove away, picked up, or out of range
            be.disconnectCar();
        }
    }

    public void sync() {
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putBoolean("HasCable", hasCable);
        nbt.putInt("ConnectedCarId", connectedCarId);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.hasCable = nbt.getBoolean("HasCable");
        this.connectedCarId = nbt.getInt("ConnectedCarId");
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }
}
