package com.evecual.evecualmc.block.entity;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.AutoPickupBlock;
import com.evecual.evecualmc.entity.PickupDroneEntity;
import com.evecual.evecualmc.screen.AutoPickupScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AutoPickupBlockEntity extends BlockEntity implements Inventory, NamedScreenHandlerFactory {
    public static final double SCAN_RADIUS = 96.0;
    public static final int FILTER_SIZE = 9;

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(FILTER_SIZE, ItemStack.EMPTY);
    private UUID linkedDroneUuid = null;
    private String linkedDroneName = "None";
    private int scanCooldown = 0;
    private int totalHarvests = 0;
    private String lastStatus = "Awaiting Drone Link";

    public AutoPickupBlockEntity(BlockPos pos, BlockState state) {
        super(EvecualMC.AUTO_PICKUP_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, AutoPickupBlockEntity be) {
        if (world.isClient) return;

        if (be.scanCooldown > 0) {
            be.scanCooldown--;
        }

        if (be.linkedDroneUuid == null) {
            if (state.get(AutoPickupBlock.ACTIVE)) {
                world.setBlockState(pos, state.with(AutoPickupBlock.ACTIVE, false), 3);
            }
            be.lastStatus = "⚠️ Standby: Right-click with paired RC Controller to link Pickup Drone";
            return;
        }

        if (!(world instanceof ServerWorld serverWorld)) return;

        Entity entity = serverWorld.getEntity(be.linkedDroneUuid);
        if (!(entity instanceof PickupDroneEntity drone) || !drone.isAlive()) {
            if (state.get(AutoPickupBlock.ACTIVE)) {
                world.setBlockState(pos, state.with(AutoPickupBlock.ACTIVE, false), 3);
            }
            be.lastStatus = "⚠️ Link Offline: Linked Pickup Drone not found or destroyed";
            return;
        }

        // Active link established
        if (!state.get(AutoPickupBlock.ACTIVE)) {
            world.setBlockState(pos, state.with(AutoPickupBlock.ACTIVE, true), 3);
        }

        // Sync filter blacklist items to drone so it also ignores them in local searches
        drone.setIgnoredItems(be.getBlacklistedItems());

        if (be.scanCooldown <= 0) {
            be.scanCooldown = 20; // Check every second

            // If player is manually controlling the drone with RC Controller, do not interrupt
            if (!drone.getPairedPlayerUuid().isEmpty()) {
                be.lastStatus = "🎮 Manual Override: Pickup Drone currently controlled by player";
                return;
            }

            // If drone is dead or completely unpowered, do not dispatch
            if (drone.getEnergy() <= 10) {
                be.lastStatus = "⚡ Low Battery: Pickup Drone requires charging (≤ 10 EU)";
                return;
            }

            // If drone cargo is full, ensure it returns home
            if (drone.isCargoFull()) {
                be.lastStatus = "📦 Cargo Full: Drone returning to base to unload into Storage Unit";
                if (!drone.isAutoReturning() && !drone.isInParkingSpot()) {
                    drone.cancelAutoHarvest();
                    drone.startAutoReturnToCharger();
                }
                return;
            }

            // Safe harvest radius (128 blocks = 256m wide active radar zone)
            Box scanArea = new Box(pos).expand(128.0);
            List<ItemEntity> items = serverWorld.getEntitiesByClass(ItemEntity.class, scanArea,
                    item -> isItemHarvestable(serverWorld, item, be));

            if (!items.isEmpty()) {
                // If drone is already harvesting and its target is still valid, don't interrupt
                if (drone.isAutoHarvesting() && drone.getHarvestTargetItemUuid() != null) {
                    Entity currentTarget = serverWorld.getEntity(drone.getHarvestTargetItemUuid());
                    if (currentTarget instanceof ItemEntity ie && isItemHarvestable(serverWorld, ie, be)) {
                        be.lastStatus = "🎯 Mission Active: Harvesting " + ie.getStack().getName().getString() + " (" + items.size() + " in radar)";
                        return;
                    }
                }

                // Prioritize reachable item closest to the drone
                Vec3d dronePos = drone.getPos();
                ItemEntity closest = items.stream()
                        .min(Comparator.comparingDouble(i -> i.squaredDistanceTo(dronePos)))
                        .orElse(null);

                if (closest != null) {
                    drone.dispatchAutoHarvest(closest.getPos(), closest.getUuid());
                    be.totalHarvests++;
                    be.lastStatus = "🎯 Mission Active: Dispatched drone to harvest dropped " + closest.getStack().getName().getString() + " (" + items.size() + " in radar)";
                    be.markDirty();
                    be.sync();

                    // Radar pulse particles & audio
                    serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                            pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                            5, 0.2, 0.1, 0.2, 0.05);
                    serverWorld.spawnParticles(ParticleTypes.ENCHANT,
                            pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                            8, 0.3, 0.2, 0.3, 0.1);
                    world.playSound(null, pos, SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE.value(), SoundCategory.BLOCKS, 0.4F, 2.0F);
                }
            } else {
                if (drone.isAutoHarvesting()) {
                    // No items left in radar, order drone to return to base
                    drone.cancelAutoHarvest();
                    drone.startAutoReturnToCharger();
                    be.lastStatus = "🟢 Area Clear: Drone returning to dock on Drone Pickup Station";
                } else if (drone.isAutoReturning()) {
                    be.lastStatus = "🟢 Area Clear: Drone returning to landing pad";
                } else if (drone.isInParkingSpot()) {
                    be.lastStatus = "🟢 Standby: Radar scanning (128m radius | Drone docked & ready)";
                } else {
                    // Area is clear and drone is undocked/floating: ensure it returns home and docks!
                    if (!drone.isAutoReturning()) {
                        drone.startAutoReturnToCharger();
                    }
                    be.lastStatus = "🟢 Area Clear: Drone returning to dock on Drone Pickup Station";
                }
            }
        }
    }

    public static boolean isItemHarvestable(ServerWorld world, ItemEntity item, @Nullable AutoPickupBlockEntity be) {
        if (!item.isAlive() || item.cannotPickup() || item.getStack().isEmpty()) return false;
        if (be != null && be.isBlacklisted(item.getStack())) return false;

        BlockPos itemPos = item.getBlockPos();
        if (itemPos.getY() < world.getBottomY() || itemPos.getY() > world.getTopY()) return false;

        // 1. Water / Lava check: flying drone cannot dive into water/lava (e.g. glow ink sacs in underground water caverns)
        if (item.isTouchingWater() || item.isSubmergedInWater()
                || world.getFluidState(itemPos).isIn(net.minecraft.registry.tag.FluidTags.WATER)
                || world.getFluidState(itemPos).isIn(net.minecraft.registry.tag.FluidTags.LAVA)) {
            return false;
        }

        // 2. Trapped inside solid block
        if (world.getBlockState(itemPos).isSolidBlock(world, itemPos)) return false;

        // 3. Subterranean check: if item is deep underground while station is on surface
        if (be != null) {
            BlockPos stationPos = be.getPos();
            int topY = world.getTopPosition(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING, itemPos).getY();
            if (topY - itemPos.getY() > 5 && itemPos.getY() < stationPos.getY() - 4) {
                return false; // Buried deep underground under solid terrain
            }
        }

        return true;
    }

    public void setLinkedDrone(UUID uuid, String name) {
        this.linkedDroneUuid = uuid;
        this.linkedDroneName = name != null ? name : "Pickup Drone";
        this.lastStatus = "🟢 Linked to " + this.linkedDroneName + " (Radar active)";
        if (this.world instanceof ServerWorld sw) {
            Entity ent = sw.getEntity(uuid);
            if (ent instanceof PickupDroneEntity drone) {
                if (drone.getHomeHelipadPos() == null) {
                    BlockPos.findClosest(this.pos, 16, 8, p -> sw.getBlockState(p).isOf(EvecualMC.PICKUP_DRONE_PARKING_SPOT_BLOCK))
                            .ifPresent(drone::setHomeHelipadPos);
                }
            }
        }
        markDirty();
        sync();
    }

    public boolean isLinked() {
        return this.linkedDroneUuid != null;
    }

    @Nullable
    public UUID getLinkedDroneUuid() {
        return this.linkedDroneUuid;
    }

    public String getLinkedDroneName() {
        return this.linkedDroneName;
    }

    public int getTotalHarvests() {
        return this.totalHarvests;
    }

    public String getStatusMessage() {
        return this.lastStatus;
    }

    public void sync() {
        if (this.world instanceof ServerWorld sw) {
            sw.getChunkManager().markForUpdate(this.pos);
        }
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, this.inventory);
        if (this.linkedDroneUuid != null) {
            nbt.putUuid("LinkedDrone", this.linkedDroneUuid);
        }
        nbt.putString("DroneName", this.linkedDroneName);
        nbt.putInt("TotalHarvests", this.totalHarvests);
        nbt.putString("LastStatus", this.lastStatus);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, this.inventory);
        if (nbt.contains("LinkedDrone")) {
            this.linkedDroneUuid = nbt.getUuid("LinkedDrone");
        } else {
            this.linkedDroneUuid = null;
        }
        if (nbt.contains("DroneName")) {
            this.linkedDroneName = nbt.getString("DroneName");
        }
        if (nbt.contains("TotalHarvests")) {
            this.totalHarvests = nbt.getInt("TotalHarvests");
        }
        if (nbt.contains("LastStatus")) {
            this.lastStatus = nbt.getString("LastStatus");
        }
    }

    public boolean isBlacklisted(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        for (ItemStack filter : this.inventory) {
            if (!filter.isEmpty() && filter.isOf(stack.getItem())) {
                return true;
            }
        }
        return false;
    }

    public Set<Item> getBlacklistedItems() {
        Set<Item> set = new HashSet<>();
        for (ItemStack filter : this.inventory) {
            if (!filter.isEmpty()) {
                set.add(filter.getItem());
            }
        }
        return set;
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("📡 Auto Pickup Filter");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new AutoPickupScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public int size() {
        return this.inventory.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.inventory) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack result = Inventories.splitStack(this.inventory, slot, amount);
        if (!result.isEmpty()) markDirty();
        return result;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack result = Inventories.removeStack(this.inventory, slot);
        if (!result.isEmpty()) markDirty();
        return result;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.inventory.set(slot, stack);
        if (stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }
        markDirty();
    }

    @Override
    public int getMaxCountPerStack() {
        return 1;
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return Inventory.canPlayerUse(this, player);
    }

    @Override
    public void clear() {
        this.inventory.clear();
        markDirty();
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
