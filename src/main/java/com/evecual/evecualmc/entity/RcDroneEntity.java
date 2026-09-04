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
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LightBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.Fluids;
import net.minecraft.world.World;

public class RcDroneEntity extends Entity {
    public static final int MAX_ENERGY = 600;
    public static final double MAX_RANGE = 512.0;

    private static final TrackedData<Integer> ENERGY = DataTracker.registerData(RcDroneEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> COLOR_VARIANT = DataTracker.registerData(RcDroneEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<String> PAIRED_PLAYER_UUID = DataTracker.registerData(RcDroneEntity.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<Float> PITCH_TILT = DataTracker.registerData(RcDroneEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> ROLL_TILT = DataTracker.registerData(RcDroneEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Boolean> FLYING = DataTracker.registerData(RcDroneEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> LIGHT_ON = DataTracker.registerData(RcDroneEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private final SimpleInventory trunk = new SimpleInventory(9); // 9-slot compact drone cargo bay

    private boolean inputForward;
    private boolean inputBack;
    private boolean inputLeft;
    private boolean inputRight;
    private boolean inputUp;
    private boolean inputDown;
    private boolean inputSprint;

    private int inputTimeoutTicks = 0;
    private float propAngle = 0.0F;
    private float prevPropAngle = 0.0F;
    private float propSpeed = 0.0F;

    private boolean autoReturning = false;
    private BlockPos targetChargerPos = null;
    private int autoReturnTicks = 0;
    private int autoReturnStage = 0; // 0 = ascend, 1 = cruise to X/Z, 2 = descend to pad
    private double cruiseAltitude = 0.0;

    private boolean wasInParkingSpot = false;
    private boolean explicitlyPairedInSpot = false;

    public RcDroneEntity(EntityType<?> type, World world) {
        super(type, world);
        this.setStepHeight(1.0F);
        this.noClip = false;
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(ENERGY, MAX_ENERGY);
        this.dataTracker.startTracking(COLOR_VARIANT, 1); // Default 1: Cyber Blue
        this.dataTracker.startTracking(PAIRED_PLAYER_UUID, "");
        this.dataTracker.startTracking(PITCH_TILT, 0.0F);
        this.dataTracker.startTracking(ROLL_TILT, 0.0F);
        this.dataTracker.startTracking(FLYING, false);
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

    public String getPairedPlayerUuid() {
        return this.dataTracker.get(PAIRED_PLAYER_UUID);
    }

    public void setPairedPlayerUuid(String uuid) {
        this.dataTracker.set(PAIRED_PLAYER_UUID, uuid != null ? uuid : "");
    }

    public float getPitchTilt() {
        return this.dataTracker.get(PITCH_TILT);
    }

    public void setPitchTilt(float tilt) {
        this.dataTracker.set(PITCH_TILT, tilt);
    }

    public float getRollTilt() {
        return this.dataTracker.get(ROLL_TILT);
    }

    public void setRollTilt(float tilt) {
        this.dataTracker.set(ROLL_TILT, tilt);
    }

    public boolean isFlying() {
        return this.dataTracker.get(FLYING);
    }

    public void setFlying(boolean flying) {
        this.dataTracker.set(FLYING, flying);
    }

    public float getPropAngle(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.prevPropAngle, this.propAngle);
    }

    public float getPropSpeed() {
        return this.propSpeed;
    }

    public SimpleInventory getTrunk() {
        return this.trunk;
    }

    public boolean isAutoReturning() {
        return this.autoReturning;
    }

    public BlockPos getParkingSpotPos() {
        BlockPos pos = this.getBlockPos();
        if (this.getWorld().getBlockState(pos).isOf(EvecualMC.DRONE_PARKING_SPOT_BLOCK)) return pos;
        if (this.getWorld().getBlockState(pos.down()).isOf(EvecualMC.DRONE_PARKING_SPOT_BLOCK)) return pos.down();
        return null;
    }

    public boolean isInParkingSpot() {
        if (this.autoReturning) return false;
        BlockPos spotPos = getParkingSpotPos();
        if (spotPos == null) return false;
        double dx = Math.abs(this.getX() - (spotPos.getX() + 0.5));
        double dz = Math.abs(this.getZ() - (spotPos.getZ() + 0.5));
        double dy = Math.abs(this.getY() - (spotPos.getY() + 0.05));
        // Must be squarely centered horizontally and resting on or close to the pad
        return dx <= 0.35 && dz <= 0.35 && (dy <= 0.35 || this.isOnGround());
    }

    public void setExplicitlyPairedInSpot(boolean val) {
        this.explicitlyPairedInSpot = val;
    }

    public void cancelAutoReturn() {
        if (this.autoReturning) {
            this.autoReturning = false;
            this.targetChargerPos = null;
            this.autoReturnStage = 0;
        }
    }

    public void onReachedCharger() {
        this.autoReturning = false;
        this.targetChargerPos = null;
        this.autoReturnStage = 0;
        this.setVelocity(Vec3d.ZERO);
        setFlying(false);
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
                    player.sendMessage(Text.literal("§a⚡ RC Drone landed & charging!"), true);
                }
            } catch (Exception ignored) {}
        }
    }

    public boolean startAutoReturnToCharger() {
        BlockPos dronePos = this.getBlockPos();
        BlockPos bestCharger = null;
        double bestDistSq = Double.MAX_VALUE;

        int minChunkX = (dronePos.getX() - 64) >> 4;
        int maxChunkX = (dronePos.getX() + 64) >> 4;
        int minChunkZ = (dronePos.getZ() - 64) >> 4;
        int maxChunkZ = (dronePos.getZ() + 64) >> 4;

        int minY = Math.max(this.getWorld().getBottomY(), dronePos.getY() - 32);
        int maxY = Math.min(this.getWorld().getTopY(), dronePos.getY() + 32);

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
                        if (Math.abs(wx - dronePos.getX()) > 64) continue;
                        for (int lz = 0; lz < 16; lz++) {
                            int wz = (cz << 4) + lz;
                            if (Math.abs(wz - dronePos.getZ()) > 64) continue;
                            for (int ly = 0; ly < 16; ly++) {
                                int wy = secY + ly;
                                if (wy < minY || wy > maxY) continue;

                                BlockState bs = section.getBlockState(lx, ly, lz);
                                if (bs.isOf(EvecualMC.DRONE_PARKING_SPOT_BLOCK)) {
                                    BlockPos p = new BlockPos(wx, wy, wz);
                                    double dSq = p.getSquaredDistance(dronePos);
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
            this.autoReturnStage = 0;
            this.cruiseAltitude = Math.max(this.getY() + 2.5, bestCharger.getY() + 5.5);
            setFlying(true);
            return true;
        }
        return false;
    }

    private float remoteYaw = Float.NaN;
    private boolean strafeMode = false;

    public void setRemoteYaw(float yaw) {
        this.remoteYaw = yaw;
    }

    public void setRemoteInputs(boolean forward, boolean back, boolean left, boolean right, boolean up, boolean down, boolean sprint) {
        setRemoteInputs(forward, back, left, right, up, down, sprint, false);
    }

    public void setRemoteInputs(boolean forward, boolean back, boolean left, boolean right, boolean up, boolean down, boolean sprint, boolean strafe) {
        if (this.autoReturning && (forward || back || left || right || up || down)) {
            cancelAutoReturn();
        }
        this.inputForward = forward;
        this.inputBack = back;
        this.inputLeft = left;
        this.inputRight = right;
        this.inputUp = up;
        this.inputDown = down;
        this.inputSprint = sprint;
        this.strafeMode = strafe;
        this.inputTimeoutTicks = 0;
    }

    @Override
    public void tick() {
        super.tick();

        tickRealLight();

        this.prevPropAngle = this.propAngle;

        // Remote signal timeout (stops inputs if controller link is interrupted)
        this.inputTimeoutTicks++;
        if (this.inputTimeoutTicks > 8) {
            this.inputForward = false;
            this.inputBack = false;
            this.inputLeft = false;
            this.inputRight = false;
            this.inputUp = false;
            this.inputDown = false;
            this.inputSprint = false;
        }

        int energy = getEnergy();
        boolean hasPower = energy > 0;

        // Auto Return to Charger when battery reaches <= 5% (<= 30 E)
        if (!this.getWorld().isClient && !this.autoReturning && energy <= 30 && energy > 0) {
            if (this.age % 100 == 0) {
                startAutoReturnToCharger();
            }
        }

        // Parking Spot: turn off motors and unpair until paired again
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
                setFlying(false);
                this.inputUp = false;
                this.inputDown = false;
                this.inputForward = false;
                this.inputBack = false;
                this.inputLeft = false;
                this.inputRight = false;
                this.inputSprint = false;
                this.propSpeed = 0.0F;
                this.setVelocity(0.0, Math.min(this.getVelocity().y, 0.0), 0.0);
                if (!this.getWorld().isClient) {
                    String pUuidStr = getPairedPlayerUuid();
                    if (pUuidStr != null && !pUuidStr.isEmpty()) {
                        com.evecual.evecualmc.item.RcControllerItem.unpairVehicleFromPlayer(this.getWorld(), this.getUuid(), pUuidStr);
                        try {
                            PlayerEntity player = this.getWorld().getPlayerByUuid(java.util.UUID.fromString(pUuidStr));
                            if (player != null) {
                                player.sendMessage(Text.literal("§e🅿️ RC Drone docked! Motors turned off and unpaired."), true);
                            }
                        } catch (Exception ignored) {}
                    }
                    setPairedPlayerUuid("");
                }
            }
            if (!this.explicitlyPairedInSpot) {
                setFlying(false);
                this.inputUp = false;
                this.inputDown = false;
                this.inputForward = false;
                this.inputBack = false;
                this.inputLeft = false;
                this.inputRight = false;
                this.inputSprint = false;
                this.propSpeed = 0.0F;
            }
        } else {
            this.wasInParkingSpot = false;
            this.explicitlyPairedInSpot = false;
        }

        Vec3d vel = this.getVelocity();

        if (hasPower && (isFlying() || this.inputUp || this.inputForward || this.inputBack || this.inputLeft || this.inputRight || this.autoReturning)) {
            setFlying(true);
            float targetPropSpeed = this.inputSprint ? 2.2F : 1.6F;
            this.propSpeed += (targetPropSpeed - this.propSpeed) * 0.25F;
        } else {
            this.propSpeed *= 0.88F;
            if (this.propSpeed < 0.05F) {
                this.propSpeed = 0.0F;
                if (this.isOnGround()) {
                    setFlying(false);
                }
            }
        }
        this.propAngle += this.propSpeed;

        // Auto-navigation autopilot return to charger
        if (this.autoReturning && this.targetChargerPos != null && hasPower) {
            this.autoReturnTicks++;
            if (this.autoReturnTicks > 1800) { // 90s timeout
                cancelAutoReturn();
            } else {
                double targetX = this.targetChargerPos.getX() + 0.5;
                double targetY = this.targetChargerPos.getY() + 0.08;
                double targetZ = this.targetChargerPos.getZ() + 0.5;

                double dx = targetX - this.getX();
                double dz = targetZ - this.getZ();
                double horizDist = Math.sqrt(dx * dx + dz * dz);

                // Stage 0: Ascend to cruise altitude
                if (this.autoReturnStage == 0) {
                    if (this.getY() < this.cruiseAltitude - 0.4) {
                        vel = new Vec3d(vel.x * 0.7, 0.35, vel.z * 0.7);
                    } else {
                        this.autoReturnStage = 1;
                    }
                }
                // Stage 1: Fly horizontally toward target X/Z
                else if (this.autoReturnStage == 1) {
                    float desiredYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
                    float yawDiff = MathHelper.wrapDegrees(desiredYaw - this.getYaw());
                    this.setYaw(MathHelper.wrapDegrees(this.getYaw() + MathHelper.clamp(yawDiff * 0.3F, -8.0F, 8.0F)));

                    double speed = MathHelper.clamp(horizDist * 0.2, 0.25, 0.65);
                    Vec3d dir = new Vec3d(dx / horizDist, 0, dz / horizDist).multiply(speed);

                    // Maintain cruise altitude
                    double dy = this.cruiseAltitude - this.getY();
                    double yVel = MathHelper.clamp(dy * 0.25, -0.25, 0.25);
                    vel = new Vec3d(dir.x, yVel, dir.z);

                    if (horizDist < 0.45) {
                        this.autoReturnStage = 2;
                    }
                }
                // Stage 2: Vertical descent and dock squarely onto pad
                else if (this.autoReturnStage == 2) {
                    double speed = MathHelper.clamp(horizDist * 0.4, 0.02, 0.15);
                    Vec3d align = horizDist > 0.03 ? new Vec3d(dx / horizDist, 0, dz / horizDist).multiply(speed) : Vec3d.ZERO;

                    double dy = targetY - this.getY();
                    double descSpeed = MathHelper.clamp(dy * 0.25, -0.25, 0.05);
                    vel = new Vec3d(align.x, descSpeed, align.z);

                    if (horizDist <= 0.30 && (Math.abs(dy) <= 0.25 || this.isOnGround())) {
                        this.setPosition(targetX, targetY, targetZ);
                        onReachedCharger();
                        this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                                SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 0.8f, 2.0f);
                    }
                }
            }
        }
        // Manual flight controls
        else if (hasPower && isFlying()) {
            double topSpeed = this.inputSprint ? 0.78 : 0.42;
            double accel = this.inputSprint ? 0.08 : 0.045;

            Vec3d forwardVec = Vec3d.fromPolar(0, this.getYaw());
            Vec3d rightVec = new Vec3d(-forwardVec.z, 0, forwardVec.x);

            Vec3d moveDir = Vec3d.ZERO;
            if (this.inputForward) moveDir = moveDir.add(forwardVec);
            if (this.inputBack) moveDir = moveDir.subtract(forwardVec);

            if (this.strafeMode) {
                // In FP mode: A and D strafe left / right!
                if (this.inputLeft) moveDir = moveDir.subtract(rightVec);
                if (this.inputRight) moveDir = moveDir.add(rightVec);
            }

            if (moveDir.lengthSquared() > 0.001) {
                moveDir = moveDir.normalize().multiply(topSpeed);
                vel = new Vec3d(
                        vel.x + (moveDir.x - vel.x) * accel * 8.0,
                        vel.y,
                        vel.z + (moveDir.z - vel.z) * accel * 8.0
                );

                // Consume battery during flight
                if (this.age % 25 == 0 && !this.getWorld().isClient) {
                    setEnergy(energy - 1);
                }
            } else {
                // When hovering or rotating on the spot, rapidly damp horizontal velocity so it stays right where it is
                vel = new Vec3d(vel.x * 0.70, vel.y, vel.z * 0.70);
                if (Math.abs(vel.x) < 0.005) vel = new Vec3d(0, vel.y, vel.z);
                if (Math.abs(vel.z) < 0.005) vel = new Vec3d(vel.x, vel.y, 0);
            }

            // Vertical flight: Ascend (Space) and Descend (Shift)
            if (this.inputUp) {
                double targetY = this.inputSprint ? 0.55 : 0.32;
                vel = new Vec3d(vel.x, vel.y + (targetY - vel.y) * 0.35, vel.z);
                if (this.age % 25 == 0 && !this.getWorld().isClient) setEnergy(energy - 1);
            } else if (this.inputDown) {
                double targetY = this.inputSprint ? -0.45 : -0.28;
                vel = new Vec3d(vel.x, vel.y + (targetY - vel.y) * 0.35, vel.z);
                if (this.age % 30 == 0 && !this.getWorld().isClient) setEnergy(energy - 1);
            } else {
                // Gyro-stabilized altitude hold hover physics
                double hoverDamping = 0.70;
                double subtleHoverWave = Math.sin(this.age * 0.18) * 0.012;
                vel = new Vec3d(vel.x, vel.y * hoverDamping + subtleHoverWave, vel.z);
            }

            // In TP mode: A/D rotates on the spot. In FP mode: mouse rotates, A/D strafes.
            if (!this.strafeMode) {
                float turnSpeed = this.inputSprint ? 5.5F : 4.0F;
                if (this.inputLeft) {
                    this.setYaw(MathHelper.wrapDegrees(this.getYaw() - turnSpeed));
                }
                if (this.inputRight) {
                    this.setYaw(MathHelper.wrapDegrees(this.getYaw() + turnSpeed));
                }
            }

            if (!Float.isNaN(this.remoteYaw)) {
                if (!this.inputLeft && !this.inputRight || this.strafeMode) {
                    this.setYaw(this.remoteYaw);
                    this.setBodyYaw(this.remoteYaw);
                    this.setHeadYaw(this.remoteYaw);
                }
                this.remoteYaw = Float.NaN;
            }

            // Aerodynamic tilt (pitch forward/back, roll on strafe or bank)
            float targetPitch = 0.0F;
            if (this.inputForward) targetPitch = this.inputSprint ? -25.0F : -16.0F;
            if (this.inputBack) targetPitch = 14.0F;
            setPitchTilt(getPitchTilt() + (targetPitch - getPitchTilt()) * 0.25F);

            float targetRoll = 0.0F;
            if (this.strafeMode) {
                if (this.inputLeft) targetRoll = -18.0F;
                if (this.inputRight) targetRoll = 18.0F;
            } else if (this.inputForward || this.inputBack) {
                if (this.inputLeft) targetRoll = -10.0F;
                if (this.inputRight) targetRoll = 10.0F;
            }
            setRollTilt(getRollTilt() + (targetRoll - getRollTilt()) * 0.25F);

        } else {
            // Gravity applies when engines are off or out of battery
            if (!this.isOnGround()) {
                vel = vel.add(0, -0.04, 0);
            } else {
                vel = new Vec3d(vel.x * 0.8, 0, vel.z * 0.8);
            }
            setPitchTilt(getPitchTilt() * 0.8F);
            setRollTilt(getRollTilt() * 0.8F);
        }

        this.setVelocity(vel);
        this.move(MovementType.SELF, this.getVelocity());

        // Particles & Sounds
        if (this.getWorld().isClient && isFlying()) {
            // Turbo boost sparks
            if (this.inputSprint && this.random.nextFloat() < 0.4F) {
                Vec3d back = this.getPos().subtract(Vec3d.fromPolar(0, this.getYaw()).multiply(0.4));
                this.getWorld().addParticle(ParticleTypes.ELECTRIC_SPARK,
                        back.x + (this.random.nextDouble() - 0.5) * 0.3,
                        back.y + 0.1,
                        back.z + (this.random.nextDouble() - 0.5) * 0.3,
                        0, 0.05, 0);
            }

            // Ground downdraft dust when near ground
            if (this.getY() - this.getBlockPos().getY() < 2.5 && this.random.nextFloat() < 0.25F) {
                this.getWorld().addParticle(ParticleTypes.SMOKE,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.8,
                        this.getY() - 0.2,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.8,
                        (this.random.nextDouble() - 0.5) * 0.1, 0.01, (this.random.nextDouble() - 0.5) * 0.1);
            }
        }

        // Quadcopter engine drone hum
        if (isFlying() && this.age % 8 == 0) {
            float pitch = this.inputSprint ? 2.0F : 1.7F;
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENTITY_BEE_LOOP, SoundCategory.NEUTRAL, 0.22F, pitch);
        }
    }

    public void openTrunk(PlayerEntity player) {
        if (!this.getWorld().isClient) {
            player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                    (syncId, playerInventory, p) -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X1, syncId, playerInventory, this.trunk, 1),
                    Text.literal("RC Drone Cargo (9 Slots)")
            ));
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.PLAYERS, 0.6f, 2.0f);
        }
    }

    public void openInventory(PlayerEntity player) {
        openTrunk(player);
    }

    public ItemStack asItemStack() {
        ItemStack stack = new ItemStack(EvecualMC.RC_DRONE_ITEM);
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("Energy", getEnergy());
        nbt.putInt("ColorVariant", getColorVariant());
        if (!getPairedPlayerUuid().isEmpty()) {
            nbt.putString("PairedPlayer", getPairedPlayerUuid());
        }

        // Save cargo items in item stack
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
                com.evecual.evecualmc.item.RcControllerItem.pairWithDrone(held, player, this);
            }
            return ActionResult.success(this.getWorld().isClient);
        }

        // Pairing with Stationary RC Controller
        if (held.getItem() instanceof com.evecual.evecualmc.item.StationaryRcControllerItem) {
            if (!this.getWorld().isClient) {
                com.evecual.evecualmc.item.StationaryRcControllerItem.pairWithDrone(held, player, this);
            }
            return ActionResult.success(this.getWorld().isClient);
        }

        // Sneaking with empty hand: Pick up the RC drone
        if (player.isSneaking() && held.isEmpty()) {
            if (!this.getWorld().isClient) {
                ItemStack drop = asItemStack();
                if (!player.getInventory().insertStack(drop)) {
                    this.getWorld().spawnEntity(new ItemEntity(this.getWorld(), this.getX(), this.getY(), this.getZ(), drop));
                }
                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.8f, 1.6f);
                this.discard();
            }
            return ActionResult.success(this.getWorld().isClient);
        }

        // Right click with empty hand without sneaking: Open Drone Cargo!
        if (held.isEmpty() && !player.isSneaking()) {
            openTrunk(player);
            return ActionResult.success(this.getWorld().isClient);
        }

        // Dyeing color on right click with dye
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
                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.ITEM_DYE_USE, SoundCategory.PLAYERS, 0.8f, 1.4f);
            }
            return ActionResult.success(this.getWorld().isClient);
        }

        return super.interact(player, hand);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (!this.getWorld().isClient && !this.isRemoved()) {
            this.getWorld().spawnEntity(new ItemEntity(this.getWorld(), this.getX(), this.getY(), this.getZ(), asItemStack()));
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.NEUTRAL, 0.8f, 1.4f);
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
        if (nbt.contains("Flying")) setFlying(nbt.getBoolean("Flying"));
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
        nbt.putBoolean("Flying", isFlying());
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
    protected float getEyeHeight(net.minecraft.entity.EntityPose pose, net.minecraft.entity.EntityDimensions dimensions) {
        return 0.25F;
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
