package com.evecual.evecualmc.entity;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.entity.RcChargerBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LightBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

import java.util.List;
import java.util.UUID;

public class RcRobotEntity extends Entity {
    public static final int MAX_ENERGY = 1000;
    public static final int INVENTORY_SIZE = 54; // Double chest capacity

    private final SimpleInventory inventory = new SimpleInventory(INVENTORY_SIZE);
    private BlockPos currentMiningPos = null;
    private int currentMiningDamage = 0;
    private int miningResetTimer = 0;

    private static final TrackedData<Integer> ENERGY = DataTracker.registerData(RcRobotEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> COLOR_VARIANT = DataTracker.registerData(RcRobotEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<String> PAIRED_PLAYER_UUID = DataTracker.registerData(RcRobotEntity.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<ItemStack> EQUIPPED_TOOL = DataTracker.registerData(RcRobotEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    private static final TrackedData<ItemStack> EQUIPPED_LEFT_ARM = DataTracker.registerData(RcRobotEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    private static final TrackedData<Float> ARM_SWING = DataTracker.registerData(RcRobotEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> LEFT_ARM_SWING = DataTracker.registerData(RcRobotEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> HEAD_PITCH = DataTracker.registerData(RcRobotEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Boolean> LIGHT_ON = DataTracker.registerData(RcRobotEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private boolean inputForward;
    private boolean inputBack;
    private boolean inputLeft;
    private boolean inputRight;
    private boolean inputSprint;
    private boolean inputJump;

    private int inputTimeoutTicks = 0;
    private double currentSpeed = 0.0;
    private float treadRoll = 0.0F;

    private int armSwingTicks = 0;
    private int leftArmSwingTicks = 0;
    private boolean autoReturning = false;
    private BlockPos targetChargerPos = null;
    private int autoReturnTicks = 0;
    private int stuckTicks = 0;
    private int reverseTicks = 0;
    private boolean explicitlyPairedInSpot = false;
    private boolean wasInParkingSpot = false;

    public RcRobotEntity(EntityType<?> type, World world) {
        super(type, world);
        this.intersectionChecked = true;
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(ENERGY, MAX_ENERGY);
        this.dataTracker.startTracking(COLOR_VARIANT, 0); // 0 = default high-tech silver/cyan
        this.dataTracker.startTracking(PAIRED_PLAYER_UUID, "");
        this.dataTracker.startTracking(EQUIPPED_TOOL, ItemStack.EMPTY);
        this.dataTracker.startTracking(EQUIPPED_LEFT_ARM, ItemStack.EMPTY);
        this.dataTracker.startTracking(ARM_SWING, 0.0F);
        this.dataTracker.startTracking(LEFT_ARM_SWING, 0.0F);
        this.dataTracker.startTracking(HEAD_PITCH, 0.0F);
        this.dataTracker.startTracking(LIGHT_ON, false);
    }

    private BlockPos currentLightPos = null;
    private float leftTreadRoll = 0.0F;
    private float rightTreadRoll = 0.0F;
    private float prevLeftTreadRoll = 0.0F;
    private float prevRightTreadRoll = 0.0F;

    public boolean isLightOn() {
        return this.dataTracker.get(LIGHT_ON);
    }

    public void setLightOn(boolean on) {
        this.dataTracker.set(LIGHT_ON, on);
        if (!on) {
            removeRealLight();
        }
    }

    public float getLeftTreadRoll(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.prevLeftTreadRoll, this.leftTreadRoll);
    }

    public float getRightTreadRoll(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.prevRightTreadRoll, this.rightTreadRoll);
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
                } else if (state.getFluidState().isOf(Fluids.WATER) && state.getBlock() instanceof Waterloggable) {
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

    private net.minecraft.util.math.ChunkPos forcedChunk = null;

    private void updateChunkLoading() {
        if (this.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
            net.minecraft.util.math.ChunkPos currentChunk = new net.minecraft.util.math.ChunkPos(this.getBlockPos());
            if (this.forcedChunk == null || !this.forcedChunk.equals(currentChunk)) {
                if (this.forcedChunk != null) {
                    serverWorld.setChunkForced(this.forcedChunk.x, this.forcedChunk.z, false);
                }
                serverWorld.setChunkForced(currentChunk.x, currentChunk.z, true);
                this.forcedChunk = currentChunk;
            }
        }
    }

    public void releaseChunkLoading() {
        if (this.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld && this.forcedChunk != null) {
            serverWorld.setChunkForced(this.forcedChunk.x, this.forcedChunk.z, false);
            this.forcedChunk = null;
        }
    }

    public static boolean isSpotOccupied(World world, BlockPos pos, Entity ignoreSelf) {
        net.minecraft.util.math.Box checkArea = new net.minecraft.util.math.Box(pos).expand(0.5);
        java.util.List<Entity> occupants = world.getEntitiesByClass(Entity.class, checkArea, e ->
                (e instanceof RcCarEntity || e instanceof RcDroneEntity || e instanceof RcRobotEntity)
                        && e != ignoreSelf && e.isAlive() && !e.isRemoved()
        );
        return !occupants.isEmpty();
    }

    @Override
    public void remove(RemovalReason reason) {
        releaseChunkLoading();
        removeRealLight();
        super.remove(reason);
    }

    public int getEnergy() {
        return this.dataTracker.get(ENERGY);
    }

    public void setEnergy(int energy) {
        this.dataTracker.set(ENERGY, MathHelper.clamp(energy, 0, MAX_ENERGY));
    }

    public ItemStack getEquippedTool() {
        return this.dataTracker.get(EQUIPPED_TOOL);
    }

    public void setEquippedTool(ItemStack tool) {
        this.dataTracker.set(EQUIPPED_TOOL, tool == null ? ItemStack.EMPTY : tool);
    }

    public ItemStack getEquippedLeftArm() {
        return this.dataTracker.get(EQUIPPED_LEFT_ARM);
    }

    public void setEquippedLeftArm(ItemStack stack) {
        this.dataTracker.set(EQUIPPED_LEFT_ARM, stack == null ? ItemStack.EMPTY : stack);
    }

    public SimpleInventory getInventory() {
        return this.inventory;
    }

    public void openInventory(PlayerEntity player) {
        if (!this.getWorld().isClient) {
            SimpleInventory armInv = new SimpleInventory(2);
            armInv.setStack(0, this.getEquippedLeftArm());
            armInv.setStack(1, this.getEquippedTool());

            player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                    (syncId, playerInventory, p) -> new com.evecual.evecualmc.screen.RcRobotScreenHandler(
                            syncId, playerInventory, armInv, this.inventory, this),
                    Text.literal("RC Robot Cargo & Dual Arms")
            ));
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.PLAYERS, 0.6f, 1.2f);
        }
    }

    public float getArmSwing() {
        return this.dataTracker.get(ARM_SWING);
    }

    public float getLeftArmSwing() {
        return this.dataTracker.get(LEFT_ARM_SWING);
    }

    public float getHeadPitch() {
        return this.dataTracker.get(HEAD_PITCH);
    }

    public void setHeadPitch(float pitch) {
        this.dataTracker.set(HEAD_PITCH, pitch);
    }

    public String getPairedPlayerUuid() {
        return this.dataTracker.get(PAIRED_PLAYER_UUID);
    }

    public void setPairedPlayerUuid(String uuid) {
        this.dataTracker.set(PAIRED_PLAYER_UUID, uuid);
    }

    public boolean isExplicitlyPairedInSpot() {
        return explicitlyPairedInSpot;
    }

    public void setExplicitlyPairedInSpot(boolean value) {
        this.explicitlyPairedInSpot = value;
    }

    public float getTreadRoll() {
        return treadRoll;
    }

    private float remoteYaw = Float.NaN;

    public void setRemoteYaw(float yaw) {
        if (this.autoReturning) return;
        this.remoteYaw = yaw;
    }

    public void setRemoteInputs(boolean forward, boolean back, boolean left, boolean right, boolean sprint, boolean jump) {
        if (this.autoReturning) {
            if (forward || back || left || right) {
                this.autoReturning = false;
                this.targetChargerPos = null;
                this.stuckTicks = 0;
                this.reverseTicks = 0;
            } else {
                return;
            }
        }
        this.inputForward = forward;
        this.inputBack = back;
        this.inputLeft = left;
        this.inputRight = right;
        this.inputSprint = sprint;
        this.inputJump = jump;
        this.inputTimeoutTicks = 5;
    }

    @Override
    public void tick() {
        super.tick();

        tickRealLight();

        if (!this.getWorld().isClient()) {
            updateChunkLoading();
        }

        if (inputTimeoutTicks > 0) {
            inputTimeoutTicks--;
            if (inputTimeoutTicks == 0) {
                inputForward = false;
                inputBack = false;
                inputLeft = false;
                inputRight = false;
                inputSprint = false;
                inputJump = false;
            }
        }

        // Arm swing animation progress (Right arm - weapon/tool)
        if (armSwingTicks > 0) {
            armSwingTicks--;
            float swing = (float) Math.sin((8 - armSwingTicks) / 8.0F * Math.PI);
            this.dataTracker.set(ARM_SWING, swing);
        } else {
            this.dataTracker.set(ARM_SWING, 0.0F);
        }

        // Arm swing animation progress (Left arm - placement)
        if (leftArmSwingTicks > 0) {
            leftArmSwingTicks--;
            float leftSwing = (float) Math.sin((8 - leftArmSwingTicks) / 8.0F * Math.PI);
            this.dataTracker.set(LEFT_ARM_SWING, leftSwing);
        } else {
            this.dataTracker.set(LEFT_ARM_SWING, 0.0F);
        }

        // Check if parked in a Robot Parking Spot block
        boolean inSpot = !this.autoReturning && isInParkingSpot();

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
                this.setVelocity(Vec3d.ZERO);
            }
            if (!this.wasInParkingSpot) {
                this.wasInParkingSpot = true;
                this.explicitlyPairedInSpot = false;
                this.currentSpeed = 0.0;
                this.inputForward = false;
                this.inputBack = false;
                this.inputLeft = false;
                this.inputRight = false;
                this.inputSprint = false;
                this.setVelocity(Vec3d.ZERO);
                if (this.autoReturning) {
                    this.autoReturning = false;
                    this.targetChargerPos = null;
                }
                if (!this.getWorld().isClient()) {
                    String pUuid = getPairedPlayerUuid();
                    if (pUuid != null && !pUuid.isEmpty()) {
                        com.evecual.evecualmc.item.RcControllerItem.unpairVehicleFromPlayer(
                                this.getWorld(), this.getUuid(), pUuid);
                        try {
                            PlayerEntity player = this.getWorld().getPlayerByUuid(java.util.UUID.fromString(pUuid));
                            if (player != null) {
                                player.sendMessage(Text.literal("§e🅿️ RC Robot docked! Vehicle turned off and unpaired."), true);
                            }
                        } catch (Exception ignored) {}
                        setPairedPlayerUuid("");
                    }
                }
            }
        } else {
            this.wasInParkingSpot = false;
            this.explicitlyPairedInSpot = false;
        }

        if (this.autoReturning) {
            tickAutoReturn();
        }

        // Handle ground movement
        boolean hasEnergy = getEnergy() > 0;
        float yaw = this.getYaw();

        if (this.autoReturning) {
            // Yaw is precisely controlled by tickAutoReturn()
            this.remoteYaw = Float.NaN;
        } else if (hasEnergy && (inputLeft || inputRight)) {
            float turnSpeed = 4.5F;
            if (inputLeft) yaw -= turnSpeed;
            if (inputRight) yaw += turnSpeed;
            this.setYaw(yaw);
        } else if (!Float.isNaN(this.remoteYaw)) {
            this.setYaw(this.remoteYaw);
            this.setBodyYaw(this.remoteYaw);
            this.setHeadYaw(this.remoteYaw);
            this.remoteYaw = Float.NaN;
        }

        double maxSpeed = inputSprint ? 0.28 : (this.autoReturning ? 0.22 : 0.18);
        double accel = 0.035;
        double decel = 0.025;

        if (hasEnergy && inputForward) {
            currentSpeed = Math.min(currentSpeed + accel, maxSpeed);
            if (this.age % (this.autoReturning ? 8 : 4) == 0) {
                setEnergy(getEnergy() - 1);
            }
        } else if (hasEnergy && inputBack) {
            currentSpeed = Math.max(currentSpeed - accel, -maxSpeed * 0.6);
            if (this.age % 4 == 0) {
                setEnergy(getEnergy() - 1);
            }
        } else {
            if (currentSpeed > 0) {
                currentSpeed = Math.max(0, currentSpeed - decel);
            } else if (currentSpeed < 0) {
                currentSpeed = Math.min(0, currentSpeed + decel);
            }
        }

        this.prevLeftTreadRoll = this.leftTreadRoll;
        this.prevRightTreadRoll = this.rightTreadRoll;

        float turnComponent = 0.0F;
        if (inputLeft) turnComponent -= 2.2F;
        if (inputRight) turnComponent += 2.2F;

        float forwardMotion = (float) currentSpeed;
        if (Math.abs(forwardMotion) < 0.001F) {
            // For remote clients observing the robot move without direct input state
            Vec3d v = this.getVelocity();
            double hSpeed = Math.sqrt(v.x * v.x + v.z * v.z);
            if (hSpeed > 0.005) {
                double radHeading = Math.toRadians(this.getYaw());
                double fwdX = -Math.sin(radHeading);
                double fwdZ = Math.cos(radHeading);
                double dot = (v.x * fwdX + v.z * fwdZ);
                forwardMotion = (float) dot;
            }
            float yawDiff = MathHelper.wrapDegrees(this.getYaw() - this.prevYaw);
            if (Math.abs(yawDiff) > 0.1F) {
                turnComponent = yawDiff * 0.4F;
            }
        }

        this.leftTreadRoll += forwardMotion * 18.0F - turnComponent;
        this.rightTreadRoll += forwardMotion * 18.0F + turnComponent;
        this.treadRoll += (float) (currentSpeed * 15.0);

        double rad = Math.toRadians(this.getYaw());
        double forwardX = -Math.sin(rad) * currentSpeed;
        double forwardZ = Math.cos(rad) * currentSpeed;

        Vec3d vel = this.getVelocity();
        double vy = vel.y - 0.08; // Gravity

        if (hasEnergy && inputJump && this.isOnGround()) {
            vy = 0.35;
        }

        // Allow robot to step up blocks and terrain effortlessly
        this.setStepHeight(1.25F);

        this.setVelocity(forwardX, vy, forwardZ);
        this.move(MovementType.SELF, this.getVelocity());

        // Particle sparks when low battery
        if (getEnergy() < 30 && this.getWorld().isClient() && this.random.nextFloat() < 0.15f) {
            this.getWorld().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0, 0.05, 0);
        }

        if (!this.getWorld().isClient()) {
            if (miningResetTimer > 0) {
                miningResetTimer--;
                if (miningResetTimer == 0 && currentMiningPos != null) {
                    this.getWorld().setBlockBreakingInfo(this.getId(), currentMiningPos, -1);
                    currentMiningPos = null;
                    currentMiningDamage = 0;
                }
            }

            // Vacuum up nearby dropped item entities into inventory
            List<ItemEntity> nearbyItems = this.getWorld().getEntitiesByClass(ItemEntity.class, this.getBoundingBox().expand(1.5), ItemEntity::isAlive);
            for (ItemEntity item : nearbyItems) {
                if (item.cannotPickup()) continue;
                ItemStack stack = item.getStack();
                ItemStack remainder = this.inventory.addStack(stack);
                if (remainder.getCount() != stack.getCount()) {
                    this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.NEUTRAL, 0.4f, 1.4f);
                }
                if (remainder.isEmpty()) {
                    item.discard();
                } else {
                    item.setStack(remainder);
                }
            }
        }
    }

    /**
     * Executes the equipped tool action (or punch) towards the target position / direction
     */
    public boolean performToolAction(float lookPitch, float lookYaw) {
        if (getEnergy() <= 0) return false;

        this.setHeadPitch(lookPitch);
        this.armSwingTicks = 8;
        this.dataTracker.set(ARM_SWING, 1.0F);

        World world = this.getWorld();
        Vec3d eyePos = this.getEyePos();
        float f = -MathHelper.sin(lookYaw * ((float)Math.PI / 180F)) * MathHelper.cos(lookPitch * ((float)Math.PI / 180F));
        float g = -MathHelper.sin(lookPitch * ((float)Math.PI / 180F));
        float h = MathHelper.cos(lookYaw * ((float)Math.PI / 180F)) * MathHelper.cos(lookPitch * ((float)Math.PI / 180F));
        Vec3d dir = new Vec3d(f, g, h).normalize();
        double reach = 4.5;
        Vec3d reachEnd = eyePos.add(dir.multiply(reach));

        if (!world.isClient()) {
            // First check entity raycast
            Box searchBox = this.getBoundingBox().stretch(dir.multiply(reach)).expand(1.0);
            Entity hitEntity = null;
            double closestDist = reach * reach;

            for (Entity entity : world.getOtherEntities(this, searchBox, e -> e instanceof LivingEntity && e.isAlive())) {
                Box box = entity.getBoundingBox().expand(0.3);
                var hitOpt = box.raycast(eyePos, reachEnd);
                if (hitOpt.isPresent()) {
                    double dist = eyePos.squaredDistanceTo(hitOpt.get());
                    if (dist < closestDist) {
                        closestDist = dist;
                        hitEntity = entity;
                    }
                }
            }

            ItemStack tool = getEquippedTool();

            if (hitEntity != null) {
                // Attack entity!
                float baseDmg = 4.0F;
                if (tool.getItem() instanceof SwordItem sword) {
                    baseDmg = sword.getAttackDamage() + 4.0F;
                } else if (tool.getItem() instanceof MiningToolItem miningTool) {
                    baseDmg = miningTool.getAttackDamage() + 2.5F;
                }

                DamageSource dmgSource = world.getDamageSources().generic();
                hitEntity.damage(dmgSource, baseDmg);

                // Knockback
                hitEntity.addVelocity(dir.x * 0.45, 0.2, dir.z * 0.45);
                hitEntity.velocityModified = true;

                // Tool damage
                if (tool.isDamageable()) {
                    tool.setDamage(tool.getDamage() + 1);
                    if (tool.getDamage() >= tool.getMaxDamage()) {
                        world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.NEUTRAL, 1.0f, 1.0f);
                        setEquippedTool(ItemStack.EMPTY);
                    }
                }

                world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.NEUTRAL, 0.9f, 1.1f);
                setEnergy(Math.max(0, getEnergy() - 8));
                return true;
            }

            // Next check block raycast
            BlockHitResult blockHit = world.raycast(new RaycastContext(eyePos, reachEnd, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, this));
            if (blockHit.getType() == HitResult.Type.BLOCK) {
                BlockPos targetPos = blockHit.getBlockPos();
                BlockState state = world.getBlockState(targetPos);

                if (!state.isAir() && state.getHardness(world, targetPos) >= 0.0F) {
                    float hardness = state.getHardness(world, targetPos);
                    boolean oneShot = isOneShotMineable(state, tool, hardness);

                    if (oneShot) {
                        if (currentMiningPos != null) {
                            world.setBlockBreakingInfo(this.getId(), currentMiningPos, -1);
                            currentMiningPos = null;
                            currentMiningDamage = 0;
                        }
                        breakBlockAndCollect(targetPos, state, tool);
                        setEnergy(Math.max(0, getEnergy() - 2));
                        return true;
                    } else {
                        if (currentMiningPos == null || !currentMiningPos.equals(targetPos)) {
                            if (currentMiningPos != null) {
                                world.setBlockBreakingInfo(this.getId(), currentMiningPos, -1);
                            }
                            currentMiningPos = targetPos;
                            currentMiningDamage = 0;
                        }
                        miningResetTimer = 40; // 2s reset timer

                        int requiredDamage = calculateRequiredHits(state, tool, hardness);
                        currentMiningDamage += 1;

                        if (currentMiningDamage >= requiredDamage) {
                            world.setBlockBreakingInfo(this.getId(), targetPos, -1);
                            currentMiningPos = null;
                            currentMiningDamage = 0;
                            breakBlockAndCollect(targetPos, state, tool);
                            setEnergy(Math.max(0, getEnergy() - 3));
                        } else {
                            int stage = (int) Math.min(9, Math.max(0, (currentMiningDamage * 10) / requiredDamage));
                            world.setBlockBreakingInfo(this.getId(), targetPos, stage);
                            world.playSound(null, targetPos, state.getSoundGroup().getHitSound(), SoundCategory.BLOCKS, 0.7f, 1.0f);
                            if (world instanceof ServerWorld sw) {
                                sw.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, state),
                                        targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5,
                                        5, 0.2, 0.2, 0.2, 0.05);
                            }
                            setEnergy(Math.max(0, getEnergy() - 1));
                        }
                        return true;
                    }
                }
            }

            // Swing in air
            world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.NEUTRAL, 0.7f, 1.3f);
            setEnergy(Math.max(0, getEnergy() - 1));
        }

        return true;
    }

    /**
     * Executes block/item placement with the left arm towards the target position / direction
     */
    public boolean performPlaceAction(float lookPitch, float lookYaw) {
        if (getEnergy() <= 0) return false;

        this.setHeadPitch(lookPitch);
        this.leftArmSwingTicks = 8;
        this.dataTracker.set(LEFT_ARM_SWING, 1.0F);

        World world = this.getWorld();
        Vec3d eyePos = this.getEyePos();
        float f = -MathHelper.sin(lookYaw * ((float)Math.PI / 180F)) * MathHelper.cos(lookPitch * ((float)Math.PI / 180F));
        float g = -MathHelper.sin(lookPitch * ((float)Math.PI / 180F));
        float h = MathHelper.cos(lookYaw * ((float)Math.PI / 180F)) * MathHelper.cos(lookPitch * ((float)Math.PI / 180F));
        Vec3d dir = new Vec3d(f, g, h).normalize();
        double reach = 4.5;
        Vec3d reachEnd = eyePos.add(dir.multiply(reach));

        if (!world.isClient()) {
            BlockHitResult blockHit = world.raycast(new RaycastContext(eyePos, reachEnd, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, this));
            if (blockHit.getType() == HitResult.Type.BLOCK) {
                ItemStack placeStack = getEquippedLeftArm();
                boolean fromArm = true;
                int invSlot = -1;

                if (placeStack.isEmpty() || !(placeStack.getItem() instanceof BlockItem)) {
                    for (int i = 0; i < this.inventory.size(); i++) {
                        ItemStack s = this.inventory.getStack(i);
                        if (!s.isEmpty() && s.getItem() instanceof BlockItem) {
                            placeStack = s;
                            fromArm = false;
                            invSlot = i;
                            break;
                        }
                    }
                }

                if (placeStack.isEmpty() || !(placeStack.getItem() instanceof BlockItem blockItem)) {
                    world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.NEUTRAL, 0.4f, 1.8f);
                    return false;
                }

                PlayerEntity player = null;
                String pUuid = getPairedPlayerUuid();
                if (pUuid != null && !pUuid.isEmpty()) {
                    try {
                        player = world.getPlayerByUuid(UUID.fromString(pUuid));
                    } catch (Exception ignored) {}
                }

                ItemPlacementContext placeCtx = new ItemPlacementContext(world, player, Hand.MAIN_HAND, placeStack, blockHit) {
                    @Override
                    public Direction[] getPlacementDirections() {
                        return new Direction[]{ blockHit.getSide(), blockHit.getSide().getOpposite() };
                    }
                    @Override
                    public Direction getPlayerLookDirection() {
                        return Direction.getFacing(dir.x, dir.y, dir.z);
                    }
                    @Override
                    public Direction getHorizontalPlayerFacing() {
                        return Direction.fromRotation(lookYaw);
                    }
                };

                BlockPos targetPos = blockHit.getBlockPos().offset(blockHit.getSide());
                BlockState existingState = world.getBlockState(blockHit.getBlockPos());
                if (existingState.canReplace(placeCtx)) {
                    targetPos = blockHit.getBlockPos();
                }

                Box targetBox = new Box(targetPos);
                if (!world.getBlockState(targetPos).canReplace(placeCtx) ||
                    !world.canPlace(blockItem.getBlock().getDefaultState(), targetPos, net.minecraft.block.ShapeContext.absent()) ||
                    world.getOtherEntities(null, targetBox, Entity::isCollidable).stream().anyMatch(e -> e != null)) {
                    return false;
                }

                BlockState placeState = blockItem.getBlock().getPlacementState(placeCtx);
                if (placeState == null) {
                    placeState = blockItem.getBlock().getDefaultState();
                }

                if (world.setBlockState(targetPos, placeState, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD)) {
                    blockItem.getBlock().onPlaced(world, targetPos, placeState, player, placeStack);
                    BlockSoundGroup soundGroup = placeState.getSoundGroup();
                    world.playSound(null, targetPos, soundGroup.getPlaceSound(), SoundCategory.BLOCKS, (soundGroup.getVolume() + 1.0F) / 2.0F, soundGroup.getPitch() * 0.8F);

                    if (world instanceof ServerWorld sw) {
                        sw.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, placeState),
                                targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5,
                                8, 0.25, 0.25, 0.25, 0.05);
                    }

                    placeStack.decrement(1);
                    if (fromArm) {
                        if (placeStack.isEmpty()) {
                            setEquippedLeftArm(ItemStack.EMPTY);
                            for (int i = 0; i < this.inventory.size(); i++) {
                                ItemStack s = this.inventory.getStack(i);
                                if (!s.isEmpty() && s.isOf(blockItem)) {
                                    setEquippedLeftArm(s.copy());
                                    this.inventory.setStack(i, ItemStack.EMPTY);
                                    break;
                                }
                            }
                        } else {
                            setEquippedLeftArm(placeStack);
                        }
                    } else {
                        if (placeStack.isEmpty()) {
                            this.inventory.setStack(invSlot, ItemStack.EMPTY);
                        } else {
                            this.inventory.setStack(invSlot, placeStack);
                        }
                    }

                    setEnergy(Math.max(0, getEnergy() - 1));
                    return true;
                }
            }
        }

        return false;
    }

    private void breakBlockAndCollect(BlockPos pos, BlockState state, ItemStack tool) {
        World world = this.getWorld();
        if (world instanceof ServerWorld sw) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            List<ItemStack> drops = Block.getDroppedStacks(state, sw, pos, blockEntity, this, tool);

            // Break block without scattering drops in world
            world.breakBlock(pos, false, this);
            world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(state));

            // Place drops into robot's 54-slot double chest inventory
            for (ItemStack drop : drops) {
                ItemStack remainder = this.inventory.addStack(drop);
                if (!remainder.isEmpty()) {
                    ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, remainder);
                    itemEntity.setToDefaultPickupDelay();
                    world.spawnEntity(itemEntity);
                }
            }

            world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BLOCK_NETHER_ORE_BREAK, SoundCategory.NEUTRAL, 0.8f, 1.2f);

            // Tool damage
            if (tool.isDamageable()) {
                tool.setDamage(tool.getDamage() + 1);
                if (tool.getDamage() >= tool.getMaxDamage()) {
                    world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.NEUTRAL, 1.0f, 1.0f);
                    setEquippedTool(ItemStack.EMPTY);
                }
            }
        }
    }

    private boolean isOneShotMineable(BlockState state, ItemStack tool, float hardness) {
        if (hardness <= 0.0F) return true; // Instabreak (tall grass, flowers, torches)

        // Leaves are always 1-shot with any tool or bare hands
        if (state.isIn(BlockTags.LEAVES) || state.getBlock().getTranslationKey().contains("leaves")) {
            return true;
        }

        Item item = tool.getItem();

        // AXE: One-shots logs, wood, planks, leaves, wooden items
        if (item instanceof AxeItem) {
            return state.isIn(BlockTags.LOGS) || state.isIn(BlockTags.PLANKS) ||
                    state.isIn(BlockTags.AXE_MINEABLE) ||
                    state.getBlock().getTranslationKey().contains("log") ||
                    state.getBlock().getTranslationKey().contains("wood") ||
                    state.getBlock().getTranslationKey().contains("plank");
        }

        // PICKAXE: One-shots stone, cobblestone, ores, concrete, bricks etc. unless obsidian/ancient debris
        if (item instanceof PickaxeItem) {
            if (state.isIn(BlockTags.PICKAXE_MINEABLE) || state.getBlock().getTranslationKey().contains("concrete")) {
                return hardness < 15.0F; // Obsidian is >= 50.0, crying obsidian >= 50.0
            }
            return hardness <= 0.3F;
        }

        // SHOVEL: One-shots dirt, sand, gravel, clay, snow
        if (item instanceof ShovelItem) {
            if (state.isIn(BlockTags.SHOVEL_MINEABLE) ||
                    state.getBlock().getTranslationKey().contains("dirt") ||
                    state.getBlock().getTranslationKey().contains("sand") ||
                    state.getBlock().getTranslationKey().contains("gravel")) {
                return true;
            }
            return hardness <= 0.3F;
        }

        // SHEARS: One-shots leaves, wool, webs
        if (item instanceof ShearsItem) {
            return state.isIn(BlockTags.LEAVES) || state.isIn(BlockTags.WOOL) || state.getBlock().getTranslationKey().contains("web");
        }

        // HOE: One-shots sculk, hay, sponges
        if (item instanceof HoeItem) {
            return state.isIn(BlockTags.HOE_MINEABLE);
        }

        // SWORD: One-shots bamboo, web
        if (item instanceof SwordItem) {
            return state.getBlock().getTranslationKey().contains("bamboo") || state.getBlock().getTranslationKey().contains("web");
        }

        // Bare hand: only very low hardness blocks
        return hardness <= 0.25F;
    }

    private int calculateRequiredHits(BlockState state, ItemStack tool, float hardness) {
        if (hardness >= 50.0F) {
            return (tool.getItem() instanceof PickaxeItem) ? 20 : 60;
        }
        if (hardness >= 20.0F) {
            return (tool.getItem() instanceof PickaxeItem) ? 12 : 35;
        }
        // Concrete (1.8), Stone (1.5), Wood (2.0) with unsuitable tool: 5-8 hits
        return (int) Math.max(4, Math.round(hardness * 3.5F));
    }

    public BlockPos getParkingSpotPos() {
        BlockPos pos = this.getBlockPos();
        if (Math.abs(this.getY() - pos.getY()) <= 0.45 && this.getWorld().getBlockState(pos).isOf(EvecualMC.ROBOT_PARKING_SPOT_BLOCK)) {
            return pos;
        }
        BlockPos down = pos.down();
        if (Math.abs(this.getY() - (down.getY() + 1.0)) <= 0.45 && this.getWorld().getBlockState(down).isOf(EvecualMC.ROBOT_PARKING_SPOT_BLOCK)) {
            return down;
        }
        return null;
    }

    public boolean isInParkingSpot() {
        if (this.autoReturning) return false;
        BlockPos spotPos = getParkingSpotPos();
        if (spotPos == null) return false;
        double dx = Math.abs(this.getX() - (spotPos.getX() + 0.5));
        double dz = Math.abs(this.getZ() - (spotPos.getZ() + 0.5));
        double dy = Math.abs(this.getY() - (spotPos.getY() + 0.0625));
        return dx <= 0.35 && dz <= 0.35 && dy <= 0.45;
    }

    public void onPairFromParkingSpot() {
        this.wasInParkingSpot = false;
        this.explicitlyPairedInSpot = true;

        BlockPos spotPos = getParkingSpotPos();
        float yaw = this.getYaw();
        if (spotPos != null) {
            BlockState bs = this.getWorld().getBlockState(spotPos);
            if (bs.contains(net.minecraft.block.HorizontalFacingBlock.FACING)) {
                yaw = bs.get(net.minecraft.block.HorizontalFacingBlock.FACING).asRotation();
                this.setYaw(yaw);
                this.setBodyYaw(yaw);
                this.setHeadYaw(yaw);
                this.prevYaw = yaw;
            }
        }

        float rad = (float) Math.toRadians(yaw);
        double forwardX = -Math.sin(rad);
        double forwardZ = Math.cos(rad);

        double newX = this.getX() + forwardX * 1.0;
        double newZ = this.getZ() + forwardZ * 1.0;
        double newY = this.getY();

        BlockPos targetPos = new BlockPos((int) Math.floor(newX), (int) Math.floor(newY), (int) Math.floor(newZ));
        if (this.getWorld().getBlockState(targetPos).isSolidBlock(this.getWorld(), targetPos)) {
            newY += 1.0;
        }

        this.setPosition(newX, newY, newZ);
        this.setVelocity(forwardX * 0.15, 0.0, forwardZ * 0.15);
        this.velocityDirty = true;
        this.velocityModified = true;
        this.currentSpeed = 0.05;

        if (this.getWorld().isClient()) {
            this.prevX = newX;
            this.prevY = newY;
            this.prevZ = newZ;
        }
    }

    public boolean startAutoReturnToCharger() {
        BlockPos center = this.getBlockPos();
        BlockPos nearest = null;
        double nearestDistSq = Double.MAX_VALUE;

        int minChunkX = (center.getX() - 96) >> 4;
        int maxChunkX = (center.getX() + 96) >> 4;
        int minChunkZ = (center.getZ() - 96) >> 4;
        int maxChunkZ = (center.getZ() + 96) >> 4;

        int minY = Math.max(this.getWorld().getBottomY(), center.getY() - 32);
        int maxY = Math.min(this.getWorld().getTopY(), center.getY() + 32);

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
                    if (!section.hasAny(bs -> bs.isOf(EvecualMC.ROBOT_PARKING_SPOT_BLOCK))) continue;

                    int secY = chunk.sectionIndexToCoord(secIdx) << 4;
                    for (int lx = 0; lx < 16; lx++) {
                        int wx = (cx << 4) + lx;
                        if (Math.abs(wx - center.getX()) > 96) continue;
                        for (int lz = 0; lz < 16; lz++) {
                            int wz = (cz << 4) + lz;
                            if (Math.abs(wz - center.getZ()) > 96) continue;
                            for (int ly = 0; ly < 16; ly++) {
                                int wy = secY + ly;
                                if (wy < minY || wy > maxY) continue;

                                BlockState bs = section.getBlockState(lx, ly, lz);
                                if (bs.isOf(EvecualMC.ROBOT_PARKING_SPOT_BLOCK)) {
                                    BlockPos p = new BlockPos(wx, wy, wz);
                                    if (isSpotOccupied(this.getWorld(), p, this)) continue;

                                    double dSq = p.getSquaredDistance(center);
                                    if (dSq < nearestDistSq) {
                                        nearestDistSq = dSq;
                                        nearest = p;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (nearest != null) {
            this.targetChargerPos = nearest;
            this.autoReturning = true;
            this.autoReturnTicks = 0;
            this.stuckTicks = 0;
            return true;
        }
        return false;
    }

    public void onReachedCharger() {
        this.autoReturning = false;
        this.targetChargerPos = null;
        this.currentSpeed = 0.0;
        this.setVelocity(Vec3d.ZERO);
        this.stuckTicks = 0;
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
                    player.sendMessage(Text.literal("§a⚡ RC Robot docked & charging!"), true);
                }
            } catch (Exception ignored) {}
        }
    }

    public boolean isAutoReturning() {
        return this.autoReturning;
    }

    private void tickAutoReturn() {
        if (targetChargerPos == null) {
            autoReturning = false;
            return;
        }

        autoReturnTicks++;
        if (autoReturnTicks > 1200) { // 60s timeout
            autoReturning = false;
            return;
        }

        double tx = targetChargerPos.getX() + 0.5;
        double ty = targetChargerPos.getY() + 0.0625;
        double tz = targetChargerPos.getZ() + 0.5;
        double dx = tx - this.getX();
        double dz = tz - this.getZ();
        double distSq = dx * dx + dz * dz;

        // Dock when squarely centered on top of the parking pad horizontally and at proper elevation
        if (distSq <= 0.12 && Math.abs(this.getY() - ty) <= 0.5) {
            this.setPosition(tx, ty, tz);
            onReachedCharger();
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 0.8f, 2.0f);
            return;
        }

        // Obstacle & elevation detection
        if (this.horizontalCollision && Math.abs(this.getVelocity().x) < 0.03 && Math.abs(this.getVelocity().z) < 0.03) {
            this.stuckTicks++;
            // Try jumping up if hitting a ledge or step
            if (this.isOnGround() && this.stuckTicks >= 3 && this.stuckTicks <= 8) {
                this.setVelocity(this.getVelocity().x, 0.42, this.getVelocity().z);
            }
            if (this.stuckTicks > 14) {
                this.reverseTicks = 14;
                this.stuckTicks = 0;
            }
        } else {
            if (this.stuckTicks > 0) this.stuckTicks--;
        }

        if (this.reverseTicks > 0) {
            this.reverseTicks--;
            this.inputForward = false;
            this.inputBack = true;
            this.inputLeft = true;
            this.inputRight = false;
            float unstickYaw = MathHelper.wrapDegrees(this.getYaw() + 6.5f);
            this.setYaw(unstickYaw);
            this.setBodyYaw(unstickYaw);
            this.setHeadYaw(unstickYaw);
        } else {
            float desiredYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            float currentYaw = this.getYaw();
            float yawDiff = MathHelper.wrapDegrees(desiredYaw - currentYaw);

            // Smooth caterpillar steering
            if (Math.abs(yawDiff) > 60.0f) {
                // Turn in place
                if (yawDiff > 0) {
                    currentYaw += 8.0f;
                    this.inputLeft = false;
                    this.inputRight = true;
                } else {
                    currentYaw -= 8.0f;
                    this.inputLeft = true;
                    this.inputRight = false;
                }
                this.inputForward = false;
                this.inputBack = false;
            } else if (Math.abs(yawDiff) > 5.0f) {
                // Curved driving turn
                if (yawDiff > 0) {
                    currentYaw += 4.5f;
                    this.inputLeft = false;
                    this.inputRight = true;
                } else {
                    currentYaw -= 4.5f;
                    this.inputLeft = true;
                    this.inputRight = false;
                }
                this.inputForward = true;
                this.inputBack = false;
            } else {
                currentYaw = desiredYaw;
                this.inputLeft = false;
                this.inputRight = false;
                this.inputForward = true;
                this.inputBack = false;
            }

            this.setYaw(currentYaw);
            this.setBodyYaw(currentYaw);
            this.setHeadYaw(currentYaw);
            this.prevYaw = currentYaw;

            // Jump up if target is higher or climbing steep steps
            if (ty > this.getY() + 0.3 && this.isOnGround() && this.horizontalCollision) {
                this.setVelocity(this.getVelocity().x, 0.42, this.getVelocity().z);
            }
        }
    }

    public ItemStack asItemStack() {
        ItemStack drop = new ItemStack(EvecualMC.RC_ROBOT_ITEM);
        NbtCompound nbt = drop.getOrCreateNbt();
        nbt.putInt("Energy", getEnergy());
        nbt.putInt("ColorVariant", this.dataTracker.get(COLOR_VARIANT));
        if (!getPairedPlayerUuid().isEmpty()) {
            nbt.putString("PairedPlayer", getPairedPlayerUuid());
        }
        if (!getEquippedTool().isEmpty()) {
            nbt.put("EquippedTool", getEquippedTool().writeNbt(new NbtCompound()));
        }
        if (!getEquippedLeftArm().isEmpty()) {
            nbt.put("EquippedLeftArm", getEquippedLeftArm().writeNbt(new NbtCompound()));
        }

        DefaultedList<ItemStack> list = DefaultedList.ofSize(this.inventory.size(), ItemStack.EMPTY);
        boolean hasItems = false;
        for (int i = 0; i < this.inventory.size(); i++) {
            ItemStack s = this.inventory.getStack(i);
            list.set(i, s);
            if (!s.isEmpty()) hasItems = true;
        }
        if (hasItems) {
            NbtCompound invNbt = new NbtCompound();
            Inventories.writeNbt(invNbt, list);
            nbt.put("RobotInventory", invNbt);
        }

        return drop;
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        ItemStack held = player.getStackInHand(hand);

        // Pairing with RC Controller
        if (held.getItem() instanceof com.evecual.evecualmc.item.RcControllerItem) {
            if (!this.getWorld().isClient()) {
                com.evecual.evecualmc.item.RcControllerItem.pairWithRobot(held, player, this);
            }
            return ActionResult.success(this.getWorld().isClient());
        }

        // Pairing with Stationary RC Controller
        if (held.getItem() instanceof com.evecual.evecualmc.item.StationaryRcControllerItem) {
            if (!this.getWorld().isClient()) {
                com.evecual.evecualmc.item.StationaryRcControllerItem.pairWithRobot(held, player, this);
            }
            return ActionResult.success(this.getWorld().isClient());
        }

        // 1. Equip weapon / tool in RIGHT arm (LMB to use)
        if (held.getItem() instanceof ToolItem || held.getItem() instanceof MiningToolItem ||
            held.getItem() instanceof SwordItem || held.getItem() instanceof ShearsItem ||
            held.getItem() instanceof RangedWeaponItem || held.getItem() instanceof TridentItem) {
            if (!this.getWorld().isClient()) {
                ItemStack oldTool = getEquippedTool();
                ItemStack equipStack = held.split(1);
                setEquippedTool(equipStack);

                if (!oldTool.isEmpty()) {
                    if (!player.giveItemStack(oldTool)) {
                        this.getWorld().spawnEntity(new ItemEntity(this.getWorld(), this.getX(), this.getY() + 0.5, this.getZ(), oldTool));
                    }
                }

                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, SoundCategory.PLAYERS, 1.0f, 1.2f);
                player.sendMessage(Text.literal("§a🤖 RC Robot right arm equipped with: §f" + equipStack.getName().getString() + " §7(Press LMB to use)"), true);
            }
            return ActionResult.SUCCESS;
        }

        // 2. Equip blocks / items in LEFT arm (RMB to place)
        if (held.getItem() instanceof BlockItem) {
            if (!this.getWorld().isClient()) {
                ItemStack oldLeft = getEquippedLeftArm();
                ItemStack equipStack = held.split(held.getCount());
                setEquippedLeftArm(equipStack);

                if (!oldLeft.isEmpty()) {
                    if (!player.giveItemStack(oldLeft)) {
                        this.getWorld().spawnEntity(new ItemEntity(this.getWorld(), this.getX(), this.getY() + 0.5, this.getZ(), oldLeft));
                    }
                }

                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, SoundCategory.PLAYERS, 1.0f, 1.2f);
                player.sendMessage(Text.literal("§a🤖 RC Robot left arm equipped with: §f" + equipStack.getName().getString() + " x" + equipStack.getCount() + " §7(Press RMB to place)"), true);
            }
            return ActionResult.SUCCESS;
        }

        // 3. Sneak + Empty hand: Pick up Robot as Item
        if (player.isSneaking() && held.isEmpty()) {
            if (!this.getWorld().isClient()) {
                ItemStack drop = asItemStack();
                if (!player.giveItemStack(drop)) {
                    this.getWorld().spawnEntity(new ItemEntity(this.getWorld(), this.getX(), this.getY() + 0.5, this.getZ(), drop));
                }
                this.discard();
            }
            return ActionResult.SUCCESS;
        }

        // 4. Normal Right Click with Empty Hand: Open Robot Cargo & Dual-Arm Control Screen!
        if (held.isEmpty()) {
            if (!this.getWorld().isClient()) {
                openInventory(player);
            }
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) return false;
        if (!this.getWorld().isClient() && !this.isRemoved()) {
            if (source.getAttacker() instanceof PlayerEntity player && player.isCreative()) {
                this.discard();
                return true;
            }

            // Drop as item with full cargo inventory and equipped tools preserved
            ItemStack drop = asItemStack();
            this.getWorld().spawnEntity(new ItemEntity(this.getWorld(), this.getX(), this.getY() + 0.5, this.getZ(), drop));
            this.discard();
            return true;
        }
        return false;
    }

    @Override
    public boolean canHit() {
        return true;
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
    public double getMountedHeightOffset() {
        return 0.5;
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.contains("Energy")) {
            setEnergy(nbt.getInt("Energy"));
        }
        if (nbt.contains("ColorVariant")) {
            this.dataTracker.set(COLOR_VARIANT, nbt.getInt("ColorVariant"));
        }
        if (nbt.contains("PairedPlayerUuid")) {
            setPairedPlayerUuid(nbt.getString("PairedPlayerUuid"));
        }
        if (nbt.contains("EquippedTool")) {
            setEquippedTool(ItemStack.fromNbt(nbt.getCompound("EquippedTool")));
        }
        if (nbt.contains("EquippedLeftArm")) {
            setEquippedLeftArm(ItemStack.fromNbt(nbt.getCompound("EquippedLeftArm")));
        }
        if (nbt.contains("LightOn")) {
            setLightOn(nbt.getBoolean("LightOn"));
        }
        if (nbt.contains("RobotInventory")) {
            NbtCompound invNbt = nbt.getCompound("RobotInventory");
            DefaultedList<ItemStack> list = DefaultedList.ofSize(this.inventory.size(), ItemStack.EMPTY);
            Inventories.readNbt(invNbt, list);
            for (int i = 0; i < this.inventory.size(); i++) {
                this.inventory.setStack(i, list.get(i));
            }
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("Energy", getEnergy());
        nbt.putInt("ColorVariant", this.dataTracker.get(COLOR_VARIANT));
        nbt.putString("PairedPlayerUuid", getPairedPlayerUuid());
        nbt.putBoolean("LightOn", isLightOn());
        if (!getEquippedTool().isEmpty()) {
            nbt.put("EquippedTool", getEquippedTool().writeNbt(new NbtCompound()));
        }
        if (!getEquippedLeftArm().isEmpty()) {
            nbt.put("EquippedLeftArm", getEquippedLeftArm().writeNbt(new NbtCompound()));
        }

        DefaultedList<ItemStack> list = DefaultedList.ofSize(this.inventory.size(), ItemStack.EMPTY);
        for (int i = 0; i < this.inventory.size(); i++) {
            list.set(i, this.inventory.getStack(i));
        }
        NbtCompound invNbt = new NbtCompound();
        Inventories.writeNbt(invNbt, list);
        nbt.put("RobotInventory", invNbt);
    }

    @Override
    protected float getEyeHeight(net.minecraft.entity.EntityPose pose, net.minecraft.entity.EntityDimensions dimensions) {
        return 0.65F;
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
