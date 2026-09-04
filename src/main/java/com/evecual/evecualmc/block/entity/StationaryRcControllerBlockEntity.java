package com.evecual.evecualmc.block.entity;

import com.evecual.evecualmc.EvecualMC;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class StationaryRcControllerBlockEntity extends BlockEntity {
    private UUID pairedVehicleUuid = null;
    private String pairedType = "";
    private String vehicleName = "";
    private UUID currentUserUuid = null;

    public StationaryRcControllerBlockEntity(BlockPos pos, BlockState state) {
        super(EvecualMC.STATIONARY_RC_CONTROLLER_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, StationaryRcControllerBlockEntity be) {
        if (world.isClient) return;

        if (be.currentUserUuid != null && world instanceof ServerWorld serverWorld) {
            ServerPlayerEntity user = serverWorld.getServer().getPlayerManager().getPlayer(be.currentUserUuid);
            boolean valid = false;

            if (user != null && user.isAlive() && user.getWorld() == world) {
                double distSq = user.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                if (distSq <= 36.0) { // Within 6 blocks
                    Entity vehicle = serverWorld.getEntity(be.pairedVehicleUuid);
                    if (vehicle != null && vehicle.isAlive()) {
                        valid = true;
                    }
                }
            }

            if (!valid) {
                if (user != null) {
                    ServerPlayNetworking.send(user, EvecualMC.EXIT_RC_STATION_PACKET_ID, PacketByteBufs.empty());
                }
                be.currentUserUuid = null;
                be.markDirty();
                be.sync();
            }
        }
    }

    public boolean isPaired() {
        return this.pairedVehicleUuid != null;
    }

    public @Nullable UUID getPairedVehicleUuid() {
        return this.pairedVehicleUuid;
    }

    public String getPairedType() {
        return this.pairedType;
    }

    public String getVehicleName() {
        return this.vehicleName;
    }

    public void setPairedVehicle(@Nullable UUID uuid, String type, String name) {
        this.pairedVehicleUuid = uuid;
        this.pairedType = type != null ? type : "";
        this.vehicleName = name != null ? name : "";
        markDirty();
        sync();
    }

    public @Nullable UUID getCurrentUserUuid() {
        return this.currentUserUuid;
    }

    public void setCurrentUserUuid(@Nullable UUID userUuid) {
        this.currentUserUuid = userUuid;
        markDirty();
        sync();
    }

    public void sync() {
        if (this.world != null && !this.world.isClient) {
            this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (this.pairedVehicleUuid != null) {
            nbt.putUuid("PairedVehicle", this.pairedVehicleUuid);
        }
        nbt.putString("PairedType", this.pairedType);
        nbt.putString("VehicleName", this.vehicleName);
        if (this.currentUserUuid != null) {
            nbt.putUuid("CurrentUser", this.currentUserUuid);
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.containsUuid("PairedVehicle")) {
            this.pairedVehicleUuid = nbt.getUuid("PairedVehicle");
        } else {
            this.pairedVehicleUuid = null;
        }
        this.pairedType = nbt.getString("PairedType");
        this.vehicleName = nbt.getString("VehicleName");
        if (nbt.containsUuid("CurrentUser")) {
            this.currentUserUuid = nbt.getUuid("CurrentUser");
        } else {
            this.currentUserUuid = null;
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
