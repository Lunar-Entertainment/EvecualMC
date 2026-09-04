package com.evecual.evecualmc.entity;

import com.evecual.evecualmc.EvecualMC;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LightBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.UUID;

public class RcCarEntity extends Entity {
    public static final int MAX_ENERGY = 500;

    private final SimpleInventory trunk = new SimpleInventory(9); // 9-slot compact RC car trunk

    private static final TrackedData<Integer> ENERGY = DataTracker.registerData(RcCarEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> COLOR_VARIANT = DataTracker.registerData(RcCarEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Float> STEERING_ANGLE = DataTracker.registerData(RcCarEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<String> PAIRED_PLAYER_UUID = DataTracker.registerData(RcCarEntity.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<Boolean> LIGHT_ON = DataTracker.registerData(RcCarEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private boolean inputForward;
    private boolean inputBack;
    private boolean inputLeft;
    private boolean inputRight;
    private boolean inputSprint;
    private boolean inputJump;

    private int inputTimeoutTicks = 0;
    private double currentSpeed = 0.0;
    private float wheelRoll = 0.0F;

    private boolean autoReturning = false;
    private net.minecraft.util.math.BlockPos targetChargerPos = null;
    private int autoReturnTicks = 0;
    private int stuckTicks = 0;
    private int reverseTicks = 0;

    private boolean wasInParkingSpot = false;
    private boolean explicitlyPairedInSpot = false;

    public RcCarEntity(EntityType<?> type, World world) {
        super(type, world);
        this.setStepHeight(1.0F); // Effortlessly drive over slabs and 1-block steps!
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(ENERGY, MAX_ENERGY);
        this.dataTracker.startTracking(COLOR_VARIANT, 0); // 0: Crimson Red
        this.dataTracker.startTracking(STEERING_ANGLE, 0.0F);
        this.dataTracker.startTracking(PAIRED_PLAYER_UUID, "");
        this.dataTracker.startTracking(LIGHT_ON, false);
    }

    private BlockPos currentLightPos = null;

    public boolean isLightOn() {
        return this.dataTracker.get(LIGHT_ON);
    }

    public void setLightOn(boolean on) {
        this.dataTracker.set(LIGHT_ON, on);
        if (!on) {
            removeRealLight();
        }
    }

    public void tickRealLight() {
        if (this.getWorld().isClient) return;

        boolean active = isLightOn() && getEnergy() > 0 && isAlive() && !isRemoved();

        if (active) {
            if (this.age % 20 == 0 && !this.getWorld().isClient) {
                int e = getEnergy();
                if (e > 0) {
                    setEnergy(e - 1);
                    if (e - 1 <= 0) {
                        setLightOn(false);
                        removeRealLight();
                    }
                }
            }
            BlockPos targetPos = this.getBlockPos();
            BlockState state = this.getWorld().getBlockState(targetPos);

            if (!state.isAir() && !state.isOf(Blocks.LIGHT) && !state.getFluidState().isOf(Fluids.WATER)) {
                targetPos = targetPos.up();
                state = this.getWorld().getBlockState(targetPos);
            }

            if (currentLightPos == null || !currentLightPos.equals(targetPos)) {
                removeRealLight();

                if (state.isAir()) {
                    this.getWorld().setBlockState(targetPos, Blocks.LIGHT.getDefaultState().with(LightBlock.LEVEL_15, 15), Block.NOTIFY_ALL);
                    this.currentLightPos = targetPos;
                } else if (state.isOf(Blocks.WATER) && state.getFluidState().isStill()) {
                    this.getWorld().setBlockState(targetPos, Blocks.LIGHT.getDefaultState().with(LightBlock.LEVEL_15, 15).with(LightBlock.WATERLOGGED, true), Block.NOTIFY_ALL);
                    this.currentLightPos = targetPos;
                } else if (state.isOf(Blocks.LIGHT)) {
                    this.currentLightPos = targetPos;
                }
            }
        } else {
            removeRealLight();
        }
    }

    public void removeRealLight() {
        if (this.getWorld().isClient) return;
        if (this.currentLightPos != null) {
            BlockState oldState = this.getWorld().getBlockState(this.currentLightPos);
            if (oldState.isOf(Blocks.LIGHT)) {
                if (oldState.contains(LightBlock.WATERLOGGED) && oldState.get(LightBlock.WATERLOGGED)) {
                    this.getWorld().setBlockState(this.currentLightPos, Blocks.WATER.getDefaultState(), Block.NOTIFY_ALL);
                } else {
                    this.getWorld().setBlockState(this.currentLightPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                }
            }
            this.currentLightPos = null;
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        removeRealLight();
        super.remove(reason);
    }

    public int getEnergy() {
        return this.dataTracker.get(ENERGY);
    }

    public void setEnergy(int energy) {
        this.dataTracker.set(ENERGY, MathHelper.clamp(energy, 0, MAX_ENERGY));
    }

    public int getColorVariant() {
        return this.dataTracker.get(COLOR_VARIANT);
    }

    public void setColorVariant(int variant) {
        this.dataTracker.set(COLOR_VARIANT, variant % 6);
    }

    public float getSteeringAngle() {
        return this.dataTracker.get(STEERING_ANGLE);
    }

    public void setSteeringAngle(float angle) {
        this.dataTracker.set(STEERING_ANGLE, angle);
    }

    public String getPairedPlayerUuid() {
        return this.dataTracker.get(PAIRED_PLAYER_UUID);
    }

    public void setPairedPlayerUuid(String uuid) {
        this.dataTracker.set(PAIRED_PLAYER_UUID, uuid != null ? uuid : "");
    }

    public float getWheelRoll() {
        return this.wheelRoll;
    }

    public double getCurrentSpeed() {
        return this.currentSpeed;
    }

    public boolean isAutoReturning() {
        return this.autoReturning;
    }

    public BlockPos getParkingSpotPos() {
        BlockPos pos = this.getBlockPos();
        if (this.getWorld().getBlockState(pos).isOf(EvecualMC.RC_PARKING_SPOT_BLOCK)) return pos;
        if (this.getWorld().getBlockState(pos.down()).isOf(EvecualMC.RC_PARKING_SPOT_BLOCK)) return pos.down();
        return null;
    }

    public boolean isInParkingSpot() {
        if (this.autoReturning) return false;
        BlockPos spotPos = getParkingSpotPos();
        if (spotPos == null) return false;
        double dx = Math.abs(this.getX() - (spotPos.getX() + 0.5));
        double dz = Math.abs(this.getZ() - (spotPos.getZ() + 0.5));
        return dx <= 0.32 && dz <= 0.32;
    }

    public void setExplicitlyPairedInSpot(boolean val) {
        this.explicitlyPairedInSpot = val;
    }

    public void onReachedCharger() {
        this.autoReturning = false;
        this.targetChargerPos = null;
        this.currentSpeed = 0.0;
        this.setVelocity(Vec3d.ZERO);
        this.setSteeringAngle(0.0F);
        this.stuckTicks = 0;
        this.reverseTicks = 0;
        this.wasInParkingSpot = true;
        this.explicitlyPairedInSpot = false;

        BlockPos spotPos = getParkingSpotPos();
        if (spotPos != null) {
            BlockState bs = this.getWorld().getBlockState(spotPos);
            if (bs.contains(net.minecraft.block.HorizontalFacingBlock.FACING)) {
                float targetYaw = bs.get(net.minecraft.block.HorizontalFacingBlock.FACING).asRotation();
                this.setYaw(targetYaw);
                this.setBodyYaw(targetYaw);
                this.setHeadYaw(targetYaw);
                this.prevYaw = targetYaw;
            }
            this.setPosition(spotPos.getX() + 0.5, spotPos.getY() + 0.0625, spotPos.getZ() + 0.5);
        }

        String pUuid = getPairedPlayerUuid();
        if (pUuid != null && !pUuid.isEmpty()) {
            if (!this.getWorld().isClient) {
                com.evecual.evecualmc.item.RcControllerItem.unpairVehicleFromPlayer(this.getWorld(), this.getUuid(), pUuid);
                setPairedPlayerUuid("");
            }
            try {
                PlayerEntity player = this.getWorld().getPlayerByUuid(java.util.UUID.fromString(pUuid));
                if (player != null) {
                    player.sendMessage(Text.literal("§a⚡ RC Car docked & charging!"), true);
                }
            } catch (Exception ignored) {}
        }
    }

    public void cancelAutoReturn() {
        if (this.autoReturning) {
            this.autoReturning = false;
            this.targetChargerPos = null;
            this.stuckTicks = 0;
            this.reverseTicks = 0;
        }
    }

    public boolean startAutoReturnToCharger() {
        BlockPos carPos = this.getBlockPos();
        BlockPos bestCharger = null;
        double bestDistSq = Double.MAX_VALUE;

        int minChunkX = (carPos.getX() - 64) >> 4;
        int maxChunkX = (carPos.getX() + 64) >> 4;
        int minChunkZ = (carPos.getZ() - 64) >> 4;
        int maxChunkZ = (carPos.getZ() + 64) >> 4;

        int minY = Math.max(this.getWorld().getBottomY(), carPos.getY() - 16);
        int maxY = Math.min(this.getWorld().getTopY(), carPos.getY() + 16);

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                if (!this.getWorld().isChunkLoaded(cx, cz)) continue;
                net.minecraft.world.chunk.Chunk chunk = this.getWorld().getChunk(cx, cz);
                if (chunk == null) continue;

                int minSec = Math.max(0, (minY - chunk.getBottomY()) >> 4);
                int maxSec = Math.min(chunk.getSectionArray().length - 1, (maxY - chunk.getBottomY()) >> 4);

                for (int secIdx = minSec; secIdx <= maxSec; secIdx++) {
                    net.minecraft.world.chunk.ChunkSection section = chunk.getSectionArray()[secIdx];
                    if (section == null || section.isEmpty()) continue;

                    int secY = chunk.sectionIndexToCoord(secIdx) << 4;
                    for (int lx = 0; lx < 16; lx++) {
                        int wx = (cx << 4) + lx;
                        if (Math.abs(wx - carPos.getX()) > 64) continue;
                        for (int lz = 0; lz < 16; lz++) {
                            int wz = (cz << 4) + lz;
                            if (Math.abs(wz - carPos.getZ()) > 64) continue;
                            for (int ly = 0; ly < 16; ly++) {
                                int wy = secY + ly;
                                if (wy < minY || wy > maxY) continue;

                                BlockState bs = section.getBlockState(lx, ly, lz);
                                if (bs.isOf(EvecualMC.RC_PARKING_SPOT_BLOCK)) {
                                    BlockPos p = new BlockPos(wx, wy, wz);
                                    double dSq = p.getSquaredDistance(carPos);
                                    if (dSq < bestDistSq) {
                                        bestDistSq = dSq;
                                        bestCharger = p;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (bestCharger != null) {
            this.autoReturning = true;
            this.targetChargerPos = bestCharger;
            this.autoReturnTicks = 0;
            return true;
        }
        return false;
    }

    private float remoteYaw = Float.NaN;

    public void setRemoteYaw(float yaw) {
        this.remoteYaw = yaw;
    }

    public void setRemoteInputs(boolean forward, boolean back, boolean left, boolean right, boolean sprint, boolean jump) {
        if (this.autoReturning && (forward || back || left || right)) {
            cancelAutoReturn();
        }
        this.inputForward = forward;
        this.inputBack = back;
        this.inputLeft = left;
        this.inputRight = right;
        this.inputSprint = sprint;
        this.inputJump = jump;
        this.inputTimeoutTicks = 0;
    }

    @Override
    public void tick() {
        super.tick();

        tickRealLight();

        // Timeout remote inputs if transmitter signal stops
        this.inputTimeoutTicks++;
        if (this.inputTimeoutTicks > 6) {
            this.inputForward = false;
            this.inputBack = false;
            this.inputLeft = false;
            this.inputRight = false;
            this.inputSprint = false;
            this.inputJump = false;
        }

        // Apply gravity
        if (!this.isOnGround()) {
            this.setVelocity(this.getVelocity().add(0, -0.04, 0));
        }

        // Parking Spot: turn off and unpair until paired again
        boolean inSpot = isInParkingSpot();
        if (inSpot) {
            BlockPos spotPos = getParkingSpotPos();
            if (spotPos != null) {
                BlockState bs = this.getWorld().getBlockState(spotPos);
                if (bs.contains(net.minecraft.block.HorizontalFacingBlock.FACING)) {
                    float targetYaw = bs.get(net.minecraft.block.HorizontalFacingBlock.FACING).asRotation();
                    this.setYaw(targetYaw);
                    this.setBodyYaw(targetYaw);
                    this.setHeadYaw(targetYaw);
                    this.prevYaw = targetYaw;
                }
                this.setPosition(spotPos.getX() + 0.5, spotPos.getY() + 0.0625, spotPos.getZ() + 0.5);
                this.setVelocity(0.0, 0.0, 0.0);
            }
            if (!this.wasInParkingSpot) {
                this.wasInParkingSpot = true;
                this.explicitlyPairedInSpot = false;
                this.currentSpeed = 0.0;
                this.setRemoteInputs(false, false, false, false, false, false);
                this.setVelocity(0.0, this.getVelocity().y, 0.0);
                if (this.autoReturning) {
                    cancelAutoReturn();
                }
                if (!this.getWorld().isClient) {
                    String pUuidStr = getPairedPlayerUuid();
                    if (pUuidStr != null && !pUuidStr.isEmpty()) {
                        com.evecual.evecualmc.item.RcControllerItem.unpairVehicleFromPlayer(this.getWorld(), this.getUuid(), pUuidStr);
                        try {
                            PlayerEntity player = this.getWorld().getPlayerByUuid(java.util.UUID.fromString(pUuidStr));
                            if (player != null) {
                                player.sendMessage(Text.literal("§e🅿️ RC Car parked! Vehicle turned off and unpaired."), true);
                            }
                        } catch (Exception ignored) {}
                    }
                    setPairedPlayerUuid("");
                }
            }
            if (!this.explicitlyPairedInSpot) {
                this.currentSpeed = 0.0;
                this.inputForward = false;
                this.inputBack = false;
                this.inputLeft = false;
                this.inputRight = false;
                this.inputSprint = false;
                this.inputJump = false;
            }
        } else {
            this.wasInParkingSpot = false;
            this.explicitlyPairedInSpot = false;
        }

        int energy = getEnergy();
        boolean hasPower = energy > 0;

        // Auto Return to Charger when battery reaches <= 5% (<= 25 E)
        if (!this.getWorld().isClient && !this.autoReturning && energy <= 25 && energy > 0) {
            if (this.age % 100 == 0) { // check every 5 seconds
                startAutoReturnToCharger();
            }
        }

        // Auto-navigation driving physics
        if (this.autoReturning && this.targetChargerPos != null && hasPower) {
            this.autoReturnTicks++;
            if (this.autoReturnTicks > 1200) { // 60s timeout
                cancelAutoReturn();
            } else {
                double targetX = this.targetChargerPos.getX() + 0.5;
                double targetY = this.targetChargerPos.getY() + 0.25;
                double targetZ = this.targetChargerPos.getZ() + 0.5;
                double dx = targetX - this.getX();
                double dz = targetZ - this.getZ();
                double distSq = dx * dx + dz * dz;

                // Stop only when squarely docked on top of the charging pad (within 0.30m of center)
                if (distSq <= 0.09) {
                    this.setPosition(targetX, targetY, targetZ);
                    onReachedCharger();
                    this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 0.8f, 2.0f);
                } else {
                    double dist = Math.sqrt(distSq);

                    // Obstacle unstick routine if blocked
                    if (this.horizontalCollision && Math.abs(this.getVelocity().x) < 0.02 && Math.abs(this.getVelocity().z) < 0.02) {
                        this.stuckTicks++;
                        if (this.stuckTicks > 12) {
                            this.reverseTicks = 16;
                            this.stuckTicks = 0;
                        }
                    } else {
                        this.stuckTicks = 0;
                    }

                    if (this.reverseTicks > 0) {
                        this.reverseTicks--;
                        this.currentSpeed = -0.14;
                        setSteeringAngle(28.0f);
                        this.setYaw(MathHelper.wrapDegrees(this.getYaw() - 6.0f));
                    } else {
                        // Smooth throttle based on proximity to charger
                        double targetSpeed;
                        if (dist > 3.5) {
                            targetSpeed = 0.24;
                        } else if (dist > 1.2) {
                            targetSpeed = 0.16;
                        } else {
                            targetSpeed = 0.10; // Precision approach speed onto pad
                        }

                        float desiredYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
                        float yawDiff = MathHelper.wrapDegrees(desiredYaw - this.getYaw());
                        float steer = MathHelper.clamp(yawDiff, -32.0f, 32.0f);
                        setSteeringAngle(steer);

                        // Direct yaw heading alignment during auto-nav
                        float yawStep = MathHelper.clamp(yawDiff * 0.35f, -8.0f, 8.0f);
                        this.setYaw(MathHelper.wrapDegrees(this.getYaw() + yawStep));

                        // Reduce speed on sharp turns
                        if (Math.abs(yawDiff) > 50.0f) {
                            targetSpeed = 0.08;
                        }

                        if (this.currentSpeed < targetSpeed) {
                            this.currentSpeed = Math.min(this.currentSpeed + 0.03, targetSpeed);
                        } else {
                            this.currentSpeed = Math.max(this.currentSpeed - 0.04, targetSpeed);
                        }

                        // Hop if blocked by a step or half-slab obstacle
                        if (this.horizontalCollision && this.isOnGround()) {
                            this.setVelocity(this.getVelocity().x, 0.42, this.getVelocity().z);
                        }
                    }
                }
            }
        } else if (hasPower) {
            double topSpeed = this.inputSprint ? 0.36 : 0.22;
            double accel = this.inputSprint ? 0.04 : 0.025;

            if (this.inputForward) {
                this.currentSpeed = Math.min(this.currentSpeed + accel, topSpeed);
                if (this.age % 20 == 0 && !this.getWorld().isClient) {
                    setEnergy(energy - 1);
                }
            } else if (this.inputBack) {
                this.currentSpeed = Math.max(this.currentSpeed - 0.025, -0.15);
                if (this.age % 25 == 0 && !this.getWorld().isClient) {
                    setEnergy(energy - 1);
                }
            } else {
                this.currentSpeed *= 0.86;
                if (Math.abs(this.currentSpeed) < 0.005) {
                    this.currentSpeed = 0.0;
                }
            }
        } else {
            this.currentSpeed *= 0.85;
            if (Math.abs(this.currentSpeed) < 0.005) {
                this.currentSpeed = 0.0;
            }
        }

        // Steering for manual driving
        if (!this.autoReturning) {
            float targetAngle = 0.0F;
            if (this.inputLeft) targetAngle -= 32.0F;
            if (this.inputRight) targetAngle += 32.0F;

            float currentAngle = getSteeringAngle();
            currentAngle += (targetAngle - currentAngle) * 0.4F;
            setSteeringAngle(currentAngle);

            if (Math.abs(this.currentSpeed) > 0.01) {
                float yawDelta = (currentAngle / 32.0F) * (float) this.currentSpeed * 14.0F;
                this.setYaw(MathHelper.wrapDegrees(this.getYaw() + yawDelta));
                this.wheelRoll += (float) (this.currentSpeed * 18.0);
            }
            if (!Float.isNaN(this.remoteYaw)) {
                if (!this.inputLeft && !this.inputRight) {
                    this.setYaw(this.remoteYaw);
                    this.setBodyYaw(this.remoteYaw);
                    this.setHeadYaw(this.remoteYaw);
                }
                this.remoteYaw = Float.NaN;
            }
        } else {
            this.wheelRoll += (float) (this.currentSpeed * 18.0);
        }

        // Hop / Jump
        if (this.inputJump && this.isOnGround() && hasPower) {
            this.setVelocity(this.getVelocity().x, 0.40, this.getVelocity().z);
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_BAT_TAKEOFF, SoundCategory.NEUTRAL, 0.7f, 1.8f);
            this.inputJump = false; // consume hop
        }

        // Movement application
        Vec3d forwardVec = Vec3d.fromPolar(0, this.getYaw());
        Vec3d horizontalMovement = new Vec3d(forwardVec.x * this.currentSpeed, this.getVelocity().y, forwardVec.z * this.currentSpeed);
        this.setVelocity(horizontalMovement);
        this.move(MovementType.SELF, this.getVelocity());

        // Particle sparks when boosting
        if (this.getWorld().isClient && this.inputSprint && Math.abs(this.currentSpeed) > 0.15) {
            Vec3d backPos = this.getPos().subtract(forwardVec.multiply(0.4));
            this.getWorld().addParticle(ParticleTypes.ELECTRIC_SPARK, backPos.x, backPos.y + 0.1, backPos.z,
                    (this.random.nextDouble() - 0.5) * 0.1, 0.05, (this.random.nextDouble() - 0.5) * 0.1);
        }

        // Sound effect while driving
        if (Math.abs(this.currentSpeed) > 0.05 && this.age % 10 == 0) {
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_MINECART_RIDING, SoundCategory.NEUTRAL, 0.25f, 1.9f);
        }
    }

    public SimpleInventory getTrunk() {
        return this.trunk;
    }

    public void openTrunk(PlayerEntity player) {
        if (!this.getWorld().isClient) {
            player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                    (syncId, playerInventory, p) -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X1, syncId, playerInventory, this.trunk, 1),
                    Text.literal("RC Car Trunk (9 Slots)")
            ));
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.PLAYERS, 0.7f, 1.8f);
        }
    }

    public ItemStack asItemStack() {
        ItemStack stack = new ItemStack(EvecualMC.RC_CAR_ITEM);
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("Energy", getEnergy());
        nbt.putInt("ColorVariant", getColorVariant());
        if (!getPairedPlayerUuid().isEmpty()) {
            nbt.putString("PairedPlayer", getPairedPlayerUuid());
        }

        // Save trunk items in the item stack
        DefaultedList<ItemStack> list = DefaultedList.ofSize(this.trunk.size(), ItemStack.EMPTY);
        boolean hasItems = false;
        for (int i = 0; i < this.trunk.size(); ++i) {
            ItemStack s = this.trunk.getStack(i);
            list.set(i, s);
            if (!s.isEmpty()) hasItems = true;
        }
        if (hasItems) {
            NbtCompound trunkNbt = new NbtCompound();
            Inventories.writeNbt(trunkNbt, list);
            nbt.put("TrunkItems", trunkNbt);
        }

        stack.setNbt(nbt);
        return stack;
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        ItemStack held = player.getStackInHand(hand);

        // Pairing with RC Controller
        if (held.getItem() instanceof com.evecual.evecualmc.item.RcControllerItem) {
            if (!this.getWorld().isClient) {
                com.evecual.evecualmc.item.RcControllerItem.pairWithCar(held, player, this);
            }
            return ActionResult.success(this.getWorld().isClient);
        }

        // Pairing with Stationary RC Controller
        if (held.getItem() instanceof com.evecual.evecualmc.item.StationaryRcControllerItem) {
            if (!this.getWorld().isClient) {
                com.evecual.evecualmc.item.StationaryRcControllerItem.pairWithCar(held, player, this);
            }
            return ActionResult.success(this.getWorld().isClient);
        }

        // Shift + Empty hand: Pick up the RC car
        if (player.isSneaking() && held.isEmpty()) {
            if (!this.getWorld().isClient) {
                ItemStack drop = asItemStack();
                if (!player.getInventory().insertStack(drop)) {
                    this.getWorld().spawnEntity(new ItemEntity(this.getWorld(), this.getX(), this.getY(), this.getZ(), drop));
                }
                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.8f, 1.4f);
                this.discard();
            }
            return ActionResult.success(this.getWorld().isClient);
        }

        // Right-click with empty hand (not sneaking): Open RC Car Trunk!
        if (held.isEmpty() && !player.isSneaking()) {
            openTrunk(player);
            return ActionResult.success(this.getWorld().isClient);
        }

        // Dyeing color on right-click with dye
        if (held.getItem() instanceof DyeItem dye) {
            int newColor = switch (dye.getColor()) {
                case BLUE, CYAN, LIGHT_BLUE -> 1;
                case BLACK, GRAY -> 2;
                case LIME, GREEN -> 3;
                case WHITE -> 4;
                case YELLOW, ORANGE -> 5;
                default -> 0; // Red
            };
            if (!this.getWorld().isClient) {
                setColorVariant(newColor);
                if (!player.isCreative()) held.decrement(1);
                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ITEM_DYE_USE, SoundCategory.PLAYERS, 0.8f, 1.2f);
            }
            return ActionResult.success(this.getWorld().isClient);
        }

        return super.interact(player, hand);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (!this.getWorld().isClient && !this.isRemoved()) {
            this.getWorld().spawnEntity(new ItemEntity(this.getWorld(), this.getX(), this.getY(), this.getZ(), asItemStack()));
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.NEUTRAL, 0.8f, 1.2f);
            this.discard();
            return true;
        }
        return super.damage(source, amount);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.contains("Energy")) setEnergy(nbt.getInt("Energy"));
        if (nbt.contains("ColorVariant")) setColorVariant(nbt.getInt("ColorVariant"));
        if (nbt.contains("PairedPlayer")) setPairedPlayerUuid(nbt.getString("PairedPlayer"));
        if (nbt.contains("LightOn")) setLightOn(nbt.getBoolean("LightOn"));
        if (nbt.contains("TrunkItems")) {
            DefaultedList<ItemStack> list = DefaultedList.ofSize(this.trunk.size(), ItemStack.EMPTY);
            Inventories.readNbt(nbt.getCompound("TrunkItems"), list);
            for (int i = 0; i < list.size(); ++i) {
                this.trunk.setStack(i, list.get(i));
            }
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("Energy", getEnergy());
        nbt.putInt("ColorVariant", getColorVariant());
        nbt.putString("PairedPlayer", getPairedPlayerUuid());
        nbt.putBoolean("LightOn", isLightOn());

        DefaultedList<ItemStack> list = DefaultedList.ofSize(this.trunk.size(), ItemStack.EMPTY);
        for (int i = 0; i < this.trunk.size(); ++i) {
            list.set(i, this.trunk.getStack(i));
        }
        NbtCompound trunkNbt = new NbtCompound();
        Inventories.writeNbt(trunkNbt, list);
        nbt.put("TrunkItems", trunkNbt);
    }

    @Override
    public boolean collidesWith(Entity other) {
        return false;
    }

    @Override
    public boolean canHit() {
        return !this.isRemoved();
    }

    @Override
    public boolean isCollidable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    protected float getEyeHeight(net.minecraft.entity.EntityPose pose, net.minecraft.entity.EntityDimensions dimensions) {
        return 0.35F;
    }

    @Override
    public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate) {
        if (this.getWorld().isClient()) {
            net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
            if (mc != null && mc.getCameraEntity() == this) {
                // If local client is actively camera-linked to this vehicle, prevent server packet jitter
                if (this.squaredDistanceTo(x, y, z) > 4.0) {
                    this.setPosition(x, y, z);
                }
                return;
            }
            // Smoothly lerp for remote observers without snapping
            this.setPosition(MathHelper.lerp(0.5, this.getX(), x), MathHelper.lerp(0.5, this.getY(), y), MathHelper.lerp(0.5, this.getZ(), z));
            this.setRotation(MathHelper.lerpAngleDegrees(0.5F, this.getYaw(), yaw), pitch);
            this.prevYaw = this.getYaw();
            return;
        }
        this.setPosition(x, y, z);
        this.setRotation(yaw, pitch);
    }
}
