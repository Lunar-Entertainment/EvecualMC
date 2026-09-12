package com.evecual.evecualmc.entity;

import com.evecual.evecualmc.EvecualMC;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;

public class PickupDroneEntity extends RcDroneEntity {

    public static final int CARGO_SIZE = 27;
    private final net.minecraft.inventory.SimpleInventory cargo = new net.minecraft.inventory.SimpleInventory(CARGO_SIZE);

    private final Set<ChunkPos> forcedPickupChunks = new HashSet<>();

    private Vec3d harvestTargetPos = null;
    private UUID harvestTargetItemUuid = null;
    private boolean isAutoHarvesting = false;
    private int harvestTimeoutTicks = 0;

    public PickupDroneEntity(EntityType<? extends PickupDroneEntity> type, World world) {
        super(type, world);
        this.setColorVariant(6); // 6 = Tactical Defense (Pure Gunmetal & Hazard)
    }

    @Override
    public net.minecraft.inventory.SimpleInventory getTrunk() {
        return this.cargo;
    }

    @Override
    public double getTopSpeed(boolean sprint) {
        boolean lowBattery = getEnergy() <= (MAX_ENERGY * 0.05); // under 5% (< 30 EU)
        if (lowBattery) {
            return 0.75; // 15 m/s (15 / 20 = 0.75 blocks/tick)
        }
        return sprint ? 2.0 : 1.5; // 40 m/s when boosting (2.0 blocks/tick), 30 m/s normal (1.5 blocks/tick)
    }

    @Override
    public double getAcceleration(boolean sprint) {
        boolean lowBattery = getEnergy() <= (MAX_ENERGY * 0.05);
        if (lowBattery) return 0.10;
        return sprint ? 0.22 : 0.14;
    }

    @Override
    public int getBatteryDrainInterval(boolean sprint) {
        boolean lowBattery = getEnergy() <= (MAX_ENERGY * 0.05);
        if (lowBattery) return 80; // Significantly reduced power consumption when under 5% (speed 15)
        return sprint ? 18 : 30;
    }

    public void dispatchAutoHarvest(Vec3d target, UUID itemUuid) {
        if (getEnergy() <= 10) return;
        this.harvestTargetPos = target;
        this.harvestTargetItemUuid = itemUuid;
        this.isAutoHarvesting = true;
        this.harvestTimeoutTicks = 0;
        setFlying(true);
        if (isInParkingSpot() || this.wasInParkingSpot) {
            this.wasInParkingSpot = false;
            this.explicitlyPairedInSpot = true;
        }
    }

    public void cancelAutoHarvest() {
        this.isAutoHarvesting = false;
        this.harvestTargetPos = null;
        this.harvestTargetItemUuid = null;
        this.harvestTimeoutTicks = 0;
    }

    public boolean isAutoHarvesting() {
        return this.isAutoHarvesting;
    }

    public boolean isCargoFull() {
        for (int i = 0; i < this.cargo.size(); i++) {
            ItemStack s = this.cargo.getStack(i);
            if (s.isEmpty() || s.getCount() < s.getMaxCount()) {
                return false;
            }
        }
        return true;
    }

    private void updatePickupChunkLoading() {
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            ChunkPos center = new ChunkPos(this.getBlockPos());
            Set<ChunkPos> desired = new HashSet<>();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    desired.add(new ChunkPos(center.x + dx, center.z + dz));
                }
            }
            if (!desired.equals(this.forcedPickupChunks)) {
                for (ChunkPos old : this.forcedPickupChunks) {
                    if (!desired.contains(old)) {
                        serverWorld.setChunkForced(old.x, old.z, false);
                    }
                }
                for (ChunkPos fresh : desired) {
                    if (!this.forcedPickupChunks.contains(fresh)) {
                        serverWorld.setChunkForced(fresh.x, fresh.z, true);
                    }
                }
                this.forcedPickupChunks.clear();
                this.forcedPickupChunks.addAll(desired);
            }
        }
    }

    public void releasePickupChunkLoading() {
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            for (ChunkPos cp : this.forcedPickupChunks) {
                serverWorld.setChunkForced(cp.x, cp.z, false);
            }
            this.forcedPickupChunks.clear();
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        releasePickupChunkLoading();
        super.remove(reason);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.getWorld().isClient()) {
            updatePickupChunkLoading();
        }

        // Server-side automated item pickup / vacuum into 27-slot cargo bay
        if (!this.getWorld().isClient && this.isAlive() && !this.isRemoved()) {
            List<ItemEntity> nearbyItems = this.getWorld().getEntitiesByClass(
                    ItemEntity.class,
                    this.getBoundingBox().expand(1.8),
                    ItemEntity::isAlive
            );
            for (ItemEntity item : nearbyItems) {
                if (item.cannotPickup()) continue;
                ItemStack stack = item.getStack();
                ItemStack remainder = this.getTrunk().addStack(stack);
                if (remainder.getCount() != stack.getCount()) {
                    this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.NEUTRAL, 0.4f, 1.4f);
                }
                if (remainder.isEmpty()) {
                    item.discard();
                } else {
                    item.setStack(remainder);
                }
            }

            // Autonomous Harvest Navigation
            if (this.isAutoHarvesting && !isAutoReturning() && getEnergy() > 0) {
                this.harvestTimeoutTicks++;
                if (this.harvestTimeoutTicks > 1200 || isCargoFull()) { // 60s timeout or full
                    cancelAutoHarvest();
                    startAutoReturnToCharger();
                } else if (this.harvestTargetPos != null) {
                    double targetX = this.harvestTargetPos.x;
                    double targetY = this.harvestTargetPos.y;
                    double targetZ = this.harvestTargetPos.z;

                    double dx = targetX - this.getX();
                    double dz = targetZ - this.getZ();
                    double horizDist = Math.sqrt(dx * dx + dz * dz);

                    // Desired cruising height vs descent height
                    double cruiseY = Math.max(this.getY(), targetY + 3.5);
                    double desiredY = horizDist > 2.5 ? cruiseY : targetY + 0.3;

                    float desiredYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
                    float yawDiff = MathHelper.wrapDegrees(desiredYaw - this.getYaw());
                    this.setYaw(MathHelper.wrapDegrees(this.getYaw() + MathHelper.clamp(yawDiff * 0.45F, -14.0F, 14.0F)));
                    this.setBodyYaw(this.getYaw());
                    this.setHeadYaw(this.getYaw());

                    // Banking tilt
                    float targetRoll = MathHelper.clamp(-yawDiff * 0.8F, -25.0F, 25.0F);
                    this.setRollTilt(MathHelper.lerp(0.25F, this.getRollTilt(), targetRoll));
                    this.setPitchTilt(MathHelper.lerp(0.25F, this.getPitchTilt(), -15.0F));

                    double topSpeed = getTopSpeed(true); // Auto harvest uses boost speed (40 m/s or 15 m/s low batt)
                    double speed = MathHelper.clamp(horizDist * 0.35, 0.25, topSpeed);
                    Vec3d hDir = horizDist > 0.01 ? new Vec3d(dx / horizDist, 0, dz / horizDist).multiply(speed) : Vec3d.ZERO;

                    double dy = desiredY - this.getY();
                    double yVel = MathHelper.clamp(dy * 0.30, -0.65, 0.65);
                    Vec3d targetVel = new Vec3d(hDir.x, yVel, hDir.z);

                    Vec3d curVel = this.getVelocity();
                    this.setVelocity(
                            MathHelper.lerp(0.35, curVel.x, targetVel.x),
                            MathHelper.lerp(0.35, curVel.y, targetVel.y),
                            MathHelper.lerp(0.35, curVel.z, targetVel.z)
                    );

                    // Drain battery periodically
                    if (this.age % getBatteryDrainInterval(true) == 0) {
                        setEnergy(getEnergy() - 1);
                    }

                    // Once reached close enough (< 1.5m) or item collected: search next item
                    if (horizDist < 1.5 && Math.abs(this.getY() - targetY) < 1.8) {
                        // Look for next nearby item
                        List<ItemEntity> items = this.getWorld().getEntitiesByClass(ItemEntity.class,
                                this.getBoundingBox().expand(48.0),
                                i -> i.isAlive() && !i.cannotPickup() && !i.getStack().isEmpty());

                        ItemEntity nextItem = items.stream()
                                .min(Comparator.comparingDouble(i -> i.squaredDistanceTo(PickupDroneEntity.this)))
                                .orElse(null);

                        if (nextItem != null && !isCargoFull()) {
                            this.harvestTargetPos = nextItem.getPos();
                            this.harvestTargetItemUuid = nextItem.getUuid();
                            this.harvestTimeoutTicks = 0;
                        } else {
                            // No more items or full: initiate return to base!
                            cancelAutoHarvest();
                            startAutoReturnToCharger();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void openTrunk(PlayerEntity player) {
        if (!this.getWorld().isClient) {
            player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                    (syncId, playerInventory, p) -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X3, syncId, playerInventory, this.cargo, 3),
                    Text.literal("Pickup Drone Cargo (27 Slots)")
            ));
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.PLAYERS, 0.6f, 2.0f);
        }
    }

    @Override
    protected boolean isParkingSpotBlock(net.minecraft.block.BlockState bs) {
        return bs.isOf(EvecualMC.PICKUP_DRONE_PARKING_SPOT_BLOCK) || bs.isOf(EvecualMC.DRONE_PARKING_SPOT_BLOCK);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("CargoItems")) {
            DefaultedList<ItemStack> list = DefaultedList.ofSize(CARGO_SIZE, ItemStack.EMPTY);
            Inventories.readNbt(nbt.getCompound("CargoItems"), list);
            for (int i = 0; i < list.size(); ++i) {
                this.cargo.setStack(i, list.get(i));
            }
        } else if (nbt.contains("TrunkItems")) {
            DefaultedList<ItemStack> list = DefaultedList.ofSize(CARGO_SIZE, ItemStack.EMPTY);
            Inventories.readNbt(nbt.getCompound("TrunkItems"), list);
            for (int i = 0; i < list.size(); ++i) {
                this.cargo.setStack(i, list.get(i));
            }
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        DefaultedList<ItemStack> list = DefaultedList.ofSize(CARGO_SIZE, ItemStack.EMPTY);
        for (int i = 0; i < CARGO_SIZE; ++i) {
            list.set(i, this.cargo.getStack(i));
        }
        NbtCompound cargoNbt = new NbtCompound();
        Inventories.writeNbt(cargoNbt, list);
        nbt.put("CargoItems", cargoNbt);
        nbt.put("TrunkItems", cargoNbt);
    }

    @Override
    public ItemStack asItemStack() {
        ItemStack stack = new ItemStack(EvecualMC.PICKUP_DRONE_ITEM);
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("Energy", getEnergy());
        nbt.putInt("ColorVariant", getColorVariant());
        if (!getPairedPlayerUuid().isEmpty()) {
            nbt.putString("PairedPlayer", getPairedPlayerUuid());
        }

        // Save cargo items into item stack NBT
        DefaultedList<ItemStack> list = DefaultedList.ofSize(CARGO_SIZE, ItemStack.EMPTY);
        boolean hasItems = false;
        for (int i = 0; i < CARGO_SIZE; ++i) {
            ItemStack s = this.cargo.getStack(i);
            list.set(i, s);
            if (!s.isEmpty()) hasItems = true;
        }
        if (hasItems) {
            NbtCompound trunkNbt = new NbtCompound();
            Inventories.writeNbt(trunkNbt, list);
            nbt.put("TrunkItems", trunkNbt);
            nbt.put("CargoItems", trunkNbt);
        }

        stack.setNbt(nbt);
        return stack;
    }
}
