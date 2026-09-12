package com.evecual.evecualmc.block.entity;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.entity.PickupDroneEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
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
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DronePickupBlockEntity extends BlockEntity {
    private boolean droneDocked = false;
    private int dockedDroneId = -1;

    public DronePickupBlockEntity(BlockPos pos, BlockState state) {
        super(EvecualMC.DRONE_PICKUP_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, DronePickupBlockEntity be) {
        if (world.isClient) return;

        // Check if special parking spot is on top
        BlockPos topPos = pos.up();
        boolean hasSpot = world.getBlockState(topPos).isOf(EvecualMC.PICKUP_DRONE_PARKING_SPOT_BLOCK);

        PickupDroneEntity drone = null;
        if (hasSpot) {
            drone = be.findDockedDrone(topPos);
        }

        boolean wasDocked = be.droneDocked;
        be.droneDocked = (drone != null);
        be.dockedDroneId = (drone != null) ? drone.getId() : -1;

        if (be.droneDocked && world instanceof ServerWorld serverWorld && world.getTime() % 20 == 0) {
            // Subtle cyan docking particles
            serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                    pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                    2, 0.2, 0.05, 0.2, 0.01);
        }

        if (wasDocked != be.droneDocked) {
            be.markDirty();
        }
    }

    @Nullable
    public PickupDroneEntity findDockedDrone(BlockPos spotPos) {
        if (this.world == null) return null;
        Box detectionBox = new Box(
                spotPos.getX() - 0.2, spotPos.getY() - 0.2, spotPos.getZ() - 0.2,
                spotPos.getX() + 1.2, spotPos.getY() + 1.2, spotPos.getZ() + 1.2
        );
        List<PickupDroneEntity> list = this.world.getEntitiesByClass(PickupDroneEntity.class, detectionBox,
                d -> d.isAlive() && (d.isOnGround() || Math.abs(d.getY() - (spotPos.getY() + 0.1)) < 0.6));
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    @Nullable
    public PickupDroneEntity getParkedDrone() {
        if (this.world == null) return null;
        BlockPos topPos = this.pos.up();
        return findDockedDrone(topPos);
    }

    public boolean hasParkedDrone() {
        return getParkedDrone() != null;
    }

    /**
     * Extracts items from the docked Pickup Drone's cargo bay.
     */
    public ItemStack extractItem(int maxCount) {
        PickupDroneEntity drone = getParkedDrone();
        if (drone == null) return ItemStack.EMPTY;

        Inventory trunk = drone.getTrunk();
        for (int i = 0; i < trunk.size(); i++) {
            ItemStack stack = trunk.getStack(i);
            if (!stack.isEmpty()) {
                ItemStack extracted = trunk.removeStack(i, maxCount);
                if (!extracted.isEmpty()) {
                    trunk.markDirty();
                    if (this.world != null) {
                        this.world.playSound(null, this.pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.3F, 1.8F);
                    }
                    return extracted;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public String getStatusMessage() {
        if (this.world == null) return "⚪ Drone Pickup Station: Initializing";
        BlockPos topPos = this.pos.up();
        if (!this.world.getBlockState(topPos).isOf(EvecualMC.PICKUP_DRONE_PARKING_SPOT_BLOCK)) {
            return "⚠️ Missing Parking Spot: Place a Pickup Drone Parking Spot on top!";
        }
        PickupDroneEntity drone = getParkedDrone();
        if (drone != null) {
            int items = 0;
            for (int i = 0; i < drone.getTrunk().size(); i++) {
                items += drone.getTrunk().getStack(i).getCount();
            }
            return "🚁 Pickup Drone Docked: " + items + " items in cargo bay (Ready for chute transfer)";
        }
        return "⚪ Drone Pickup Station: Ready for Pickup Drone landing";
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putBoolean("DroneDocked", this.droneDocked);
        nbt.putInt("DockedDroneId", this.dockedDroneId);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.droneDocked = nbt.getBoolean("DroneDocked");
        this.dockedDroneId = nbt.getInt("DockedDroneId");
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
