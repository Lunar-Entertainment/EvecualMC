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
    private static final TrackedData<Float> ARM_SWING = DataTracker.registerData(RcRobotEntity.class, TrackedDataHandlerRegistry.FLOAT);
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
    private boolean autoReturning = false;
    private BlockPos targetChargerPos = null;
    private int autoReturnTicks = 0;
    private int stuckTicks = 0;
    private boolean explicitlyPairedInSpot = false;

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
        this.dataTracker.startTracking(ARM_SWING, 0.0F);
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

    public ItemStack getEquippedTool() {
        return this.dataTracker.get(EQUIPPED_TOOL);
    }

    public void setEquippedTool(ItemStack tool) {
        this.dataTracker.set(EQUIPPED_TOOL, tool == null ? ItemStack.EMPTY : tool);
    }

    public SimpleInventory getInventory() {
        return this.inventory;
    }

    public void openInventory(PlayerEntity player) {
        if (!this.getWorld().isClient) {
            player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                    (syncId, playerInventory, p) -> new GenericContainerScreenHandler(
                            ScreenHandlerType.GENERIC_9X6, syncId, playerInventory, this.inventory, 6),
                    Text.literal("RC Robot Cargo (54 Slots)")
            ));
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.PLAYERS, 0.6f, 1.2f);
        }
    }

    public float getArmSwing() {
        return this.dataTracker.get(ARM_SWING);
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

    public void setRemoteInputs(boolean forward, boolean back, boolean left, boolean right, boolean sprint, boolean jump) {
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

        // Arm swing animation progress
        if (armSwingTicks > 0) {
            armSwingTicks--;
            float swing = (float) Math.sin((8 - armSwingTicks) / 8.0F * Math.PI);
            this.dataTracker.set(ARM_SWING, swing);
        } else {
            this.dataTracker.set(ARM_SWING, 0.0F);
        }

        // Check if parked in a Parking Spot block
        BlockPos currentPos = this.getBlockPos();
        BlockState belowState = this.getWorld().getBlockState(currentPos);
        BlockState groundState = this.getWorld().getBlockState(currentPos.down());
        boolean inSpot = belowState.isOf(EvecualMC.RC_PARKING_SPOT_BLOCK) || groundState.isOf(EvecualMC.RC_PARKING_SPOT_BLOCK);

        if (inSpot && !this.getWorld().isClient()) {
            if (!this.explicitlyPairedInSpot && !getPairedPlayerUuid().isEmpty()) {
                com.evecual.evecualmc.item.RcControllerItem.unpairVehicleFromPlayer(
                        this.getWorld(), this.getUuid(), getPairedPlayerUuid());
                setPairedPlayerUuid("");
                this.inputForward = false;
                this.inputBack = false;
                this.inputLeft = false;
                this.inputRight = false;
                this.autoReturning = false;
                this.currentSpeed = 0.0;
                this.setVelocity(Vec3d.ZERO);
            }
        } else if (!inSpot) {
            this.explicitlyPairedInSpot = false;
        }

        if (this.autoReturning) {
            tickAutoReturn();
        }

        // Handle ground movement
        boolean hasEnergy = getEnergy() > 0;
        float yaw = this.getYaw();

        if (hasEnergy && (inputLeft || inputRight)) {
            float turnSpeed = 4.5F;
            if (inputLeft) yaw -= turnSpeed;
            if (inputRight) yaw += turnSpeed;
            this.setYaw(yaw);
        }

        double maxSpeed = inputSprint ? 0.28 : 0.18;
        double accel = 0.035;
        double decel = 0.025;

        if (hasEnergy && inputForward) {
            currentSpeed = Math.min(currentSpeed + accel, maxSpeed);
            if (this.age % 4 == 0) {
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
        if (this.getWorld().isClient()) {
            double dx = this.getX() - this.prevX;
            double dz = this.getZ() - this.prevZ;
            forwardMotion = (float) (-Math.sin(Math.toRadians(this.getYaw())) * dx + Math.cos(Math.toRadians(this.getYaw())) * dz);
            float yawDiff = MathHelper.wrapDegrees(this.getYaw() - this.prevYaw);
            turnComponent = yawDiff * 0.4F;
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

        // Allow robot to step up blocks cleanly
        this.setStepHeight(1.0F);

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

    public boolean startAutoReturnToCharger() {
        BlockPos nearest = null;
        double nearestDistSq = Double.MAX_VALUE;
        BlockPos center = this.getBlockPos();
        int radius = 64;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -16; dy <= 16; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos p = center.add(dx, dy, dz);
                    BlockState s = this.getWorld().getBlockState(p);
                    if (s.isOf(EvecualMC.RC_CHARGER_BLOCK) || s.isOf(EvecualMC.RC_PARKING_SPOT_BLOCK)) {
                        double d = this.squaredDistanceTo(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
                        if (d < nearestDistSq) {
                            nearestDistSq = d;
                            nearest = p;
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

    private void tickAutoReturn() {
        if (targetChargerPos == null) {
            autoReturning = false;
            return;
        }

        autoReturnTicks++;
        if (autoReturnTicks > 1200) {
            autoReturning = false;
            return;
        }

        double tx = targetChargerPos.getX() + 0.5;
        double tz = targetChargerPos.getZ() + 0.5;
        double distSq = this.squaredDistanceTo(tx, this.getY(), tz);

        if (distSq < 1.0) {
            this.autoReturning = false;
            this.currentSpeed = 0.0;
            this.setVelocity(Vec3d.ZERO);
            return;
        }

        double dx = tx - this.getX();
        double dz = tz - this.getZ();
        float desiredYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float yawDiff = MathHelper.wrapDegrees(desiredYaw - this.getYaw());

        if (yawDiff > 5.0f) {
            this.setYaw(this.getYaw() + 4.5f);
        } else if (yawDiff < -5.0f) {
            this.setYaw(this.getYaw() - 4.5f);
        }

        this.inputForward = true;
        this.inputBack = false;
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

        // 1. Equip tool if holding a tool or weapon
        if (held.getItem() instanceof ToolItem || held.getItem() instanceof MiningToolItem ||
            held.getItem() instanceof SwordItem || held.getItem() instanceof ShearsItem) {
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
                player.sendMessage(Text.literal("§a🤖 RC Robot equipped with: §f" + equipStack.getName().getString() + " §7(Press LMB to use)"), true);
            }
            return ActionResult.SUCCESS;
        }

        // 2. Sneak + Empty hand: Pick up Robot as Item
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

        // 3. Normal Right Click with Empty Hand: Retrieve equipped tool
        if (held.isEmpty() && !getEquippedTool().isEmpty()) {
            if (!this.getWorld().isClient()) {
                ItemStack tool = getEquippedTool();
                setEquippedTool(ItemStack.EMPTY);
                if (!player.giveItemStack(tool)) {
                    this.getWorld().spawnEntity(new ItemEntity(this.getWorld(), this.getX(), this.getY() + 0.5, this.getZ(), tool));
                }
                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.0f);
                player.sendMessage(Text.literal("§e🤖 Retrieved §f" + tool.getName().getString() + " §efrom RC Robot"), true);
            }
            return ActionResult.SUCCESS;
        }

        // 4. Normal Right Click with Empty Hand when NO tool is equipped: Open Cargo Inventory!
        if (held.isEmpty() && getEquippedTool().isEmpty()) {
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

            // Drop as item with full cargo inventory and equipped tool preserved
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
}
