package com.evecual.evecualmc.block.entity;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.ChargerExtensionBlock;
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
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ChargerExtensionBlockEntity extends BlockEntity {
    private boolean hasCable = false;
    private int connectedCarId = -1;
    private UUID connectedCarUuid = null;
    private int disconnectGraceTicks = 0;

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
                this.world.setBlockState(this.pos, state.with(ChargerExtensionBlock.HAS_CABLE, hasCable), Block.NOTIFY_ALL);
            }
        }
        markDirty();
        sync();
    }

    public boolean isConnected() {
        return connectedCarUuid != null || connectedCarId != -1;
    }

    public void connectCar(CarEntity car) {
        this.connectedCarUuid = car.getUuid();
        this.connectedCarId = car.getId();
        this.disconnectGraceTicks = 0;
        car.setPluggedIn(this.pos);

        if (this.world != null && !this.world.isClient) {
            BlockState state = this.world.getBlockState(this.pos);
            if (state.isOf(EvecualMC.CHARGER_EXTENSION_BLOCK)) {
                this.world.setBlockState(this.pos, state.with(ChargerExtensionBlock.HAS_CABLE, true)
                        .with(ChargerExtensionBlock.CONNECTED, true), Block.NOTIFY_ALL);
            }
        }
        markDirty();
        sync();
    }

    public void disconnectCar() {
        if (this.world != null) {
            CarEntity car = getConnectedCar();
            if (car != null) {
                car.unplug();
            }
        }
        this.connectedCarUuid = null;
        this.connectedCarId = -1;
        this.disconnectGraceTicks = 0;

        if (this.world != null && !this.world.isClient) {
            BlockState state = this.world.getBlockState(this.pos);
            if (state.isOf(EvecualMC.CHARGER_EXTENSION_BLOCK)) {
                this.world.setBlockState(this.pos, state.with(ChargerExtensionBlock.CONNECTED, false), Block.NOTIFY_ALL);
            }
        }
        markDirty();
        sync();
    }

    @Nullable
    public CarEntity getConnectedCar() {
        if (this.world == null) return null;
        if (this.world instanceof ServerWorld serverWorld && this.connectedCarUuid != null) {
            Entity e = serverWorld.getEntity(this.connectedCarUuid);
            if (e instanceof CarEntity car) return car;
        }
        if (this.connectedCarId != -1) {
            Entity e = this.world.getEntityById(this.connectedCarId);
            if (e instanceof CarEntity car) return car;
        }
        return null;
    }

    public static void tick(World world, BlockPos pos, BlockState state, ChargerExtensionBlockEntity be) {
        // Run authoritative logic on Server only to avoid client-side race desync!
        if (world.isClient) return;

        if (!be.isConnected()) return;

        CarEntity car = be.getConnectedCar();
        if (car != null && car.isAlive() && car.squaredDistanceTo(pos.toCenterPos()) <= 256.0) {
            be.disconnectGraceTicks = 0;
            car.setPluggedIn(pos);

            // Extract power from ChargerBlock below or adjacent
            ChargerBlockEntity charger = null;
            BlockEntity below = world.getBlockEntity(pos.down());
            if (below instanceof ChargerBlockEntity c) {
                charger = c;
            } else {
                for (Direction dir : Direction.values()) {
                    BlockEntity adj = world.getBlockEntity(pos.offset(dir));
                    if (adj instanceof ChargerBlockEntity c) {
                        charger = c;
                        break;
                    }
                }
            }

            if (charger != null && charger.getEnergy() > 0) {
                int needed = car.getMaxEnergy() - car.getEnergy();
                int transfer = Math.min((int) charger.getEnergy(), Math.min(needed, 10)); // 200 E/s
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
                int steps = Math.max(4, (int) (diff.length() * 3));

                for (int i = 0; i <= steps; ++i) {
                    double t = (double) i / steps;
                    double px = start.x + diff.x * t;
                    double py = start.y + diff.y * t - Math.sin(t * Math.PI) * 0.25;
                    double pz = start.z + diff.z * t;
                    serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, px, py, pz, 1, 0.02, 0.02, 0.02, 0.0);
                }

                serverWorld.spawnParticles(ParticleTypes.COMPOSTER, end.x, end.y, end.z, 2, 0.1, 0.1, 0.1, 0.02);
            }
        } else {
            // Grace period of 15 ticks before disconnecting to prevent chunk load glitches
            if (++be.disconnectGraceTicks > 15) {
                be.disconnectCar();
            }
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
        if (connectedCarUuid != null) {
            nbt.putUuid("ConnectedCarUuid", connectedCarUuid);
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.hasCable = nbt.getBoolean("HasCable");
        this.connectedCarId = nbt.getInt("ConnectedCarId");
        if (nbt.containsUuid("ConnectedCarUuid")) {
            this.connectedCarUuid = nbt.getUuid("ConnectedCarUuid");
        }
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
