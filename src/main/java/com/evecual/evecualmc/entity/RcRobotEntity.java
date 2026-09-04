package com.evecual.evecualmc.entity;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.entity.RcChargerBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
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

import java.util.UUID;

public class RcRobotEntity extends Entity {
    public static final int MAX_ENERGY = 1000;

    private static final TrackedData<Integer> ENERGY = DataTracker.registerData(RcRobotEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> COLOR_VARIANT = DataTracker.registerData(RcRobotEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<String> PAIRED_PLAYER_UUID = DataTracker.registerData(RcRobotEntity.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<ItemStack> EQUIPPED_TOOL = DataTracker.registerData(RcRobotEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    private static final TrackedData<Float> ARM_SWING = DataTracker.registerData(RcRobotEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> HEAD_PITCH = DataTracker.registerData(RcRobotEntity.class, TrackedDataHandlerRegistry.FLOAT);

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

        treadRoll += (float) (currentSpeed * 15.0);

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
                    // Block break!
                    world.breakBlock(targetPos, true, null);
                    world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, targetPos, Block.getRawIdFromState(state));

                    // Tool damage
                    if (tool.isDamageable()) {
                        tool.setDamage(tool.getDamage() + 1);
                        if (tool.getDamage() >= tool.getMaxDamage()) {
                            world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.NEUTRAL, 1.0f, 1.0f);
                            setEquippedTool(ItemStack.EMPTY);
                        }
                    }

                    world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BLOCK_NETHER_ORE_BREAK, SoundCategory.NEUTRAL, 0.8f, 1.2f);
                    setEnergy(Math.max(0, getEnergy() - 5));
                    return true;
                }
            }

            // Swing in air
            world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.NEUTRAL, 0.7f, 1.3f);
            setEnergy(Math.max(0, getEnergy() - 2));
        }

        return true;
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
                ItemStack drop = new ItemStack(EvecualMC.RC_ROBOT_ITEM);
                NbtCompound nbt = drop.getOrCreateNbt();
                nbt.putInt("Energy", getEnergy());
                if (!getEquippedTool().isEmpty()) {
                    nbt.put("EquippedTool", getEquippedTool().writeNbt(new NbtCompound()));
                }

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

            // Drop as item
            ItemStack drop = new ItemStack(EvecualMC.RC_ROBOT_ITEM);
            NbtCompound nbt = drop.getOrCreateNbt();
            nbt.putInt("Energy", getEnergy());
            if (!getEquippedTool().isEmpty()) {
                nbt.put("EquippedTool", getEquippedTool().writeNbt(new NbtCompound()));
            }
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
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("Energy", getEnergy());
        nbt.putInt("ColorVariant", this.dataTracker.get(COLOR_VARIANT));
        nbt.putString("PairedPlayerUuid", getPairedPlayerUuid());
        if (!getEquippedTool().isEmpty()) {
            nbt.put("EquippedTool", getEquippedTool().writeNbt(new NbtCompound()));
        }
    }
}
