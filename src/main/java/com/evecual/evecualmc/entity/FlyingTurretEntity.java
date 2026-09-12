package com.evecual.evecualmc.entity;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.entity.TurretAmmoContainerBlockEntity;
import com.evecual.evecualmc.screen.FlyingTurretScreenHandler;
import com.evecual.evecualmc.turret.AmmoType;
import com.evecual.evecualmc.turret.TurretTargetFilter;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public class FlyingTurretEntity extends PathAwareEntity implements NamedScreenHandlerFactory {
    public static final int COOLDOWN_TICKS = 40; // 2.0s cooldown

    public static DefaultAttributeContainer.Builder createFlyingTurretAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.6)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0);
    }

    private static final TrackedData<Integer> RADIUS = DataTracker.registerData(FlyingTurretEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> TARGET_PLAYERS = DataTracker.registerData(FlyingTurretEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> TARGET_MONSTERS = DataTracker.registerData(FlyingTurretEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> TARGET_ANIMALS = DataTracker.registerData(FlyingTurretEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> TARGET_BOSSES = DataTracker.registerData(FlyingTurretEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Float> PITCH_TILT = DataTracker.registerData(FlyingTurretEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> ROLL_TILT = DataTracker.registerData(FlyingTurretEntity.class, TrackedDataHandlerRegistry.FLOAT);

    private final TurretTargetFilter targetFilter = new TurretTargetFilter();
    private int cooldown = 0;
    private int patrolAltitude = 16;
    private BlockPos homePos = BlockPos.ORIGIN;
    @Nullable
    private Vec3d stationPos = null;
    @Nullable
    private Vec3d recallTarget = null;
    @Nullable
    private BlockPos linkedAmmoContainerPos = null;
    @Nullable
    private LivingEntity currentTarget = null;
    @Nullable
    private String pairedPlayerUuid = null;

    // Remote Piloting via Defense Controller
    private int remoteControlTicks = 0;
    private boolean remoteFwd = false;
    private boolean remoteBack = false;
    private boolean remoteLeft = false;
    private boolean remoteRight = false;
    private boolean remoteUp = false;
    private boolean remoteDown = false;
    private boolean remoteSprint = false;
    private float remoteYaw = 0.0f;
    private float remotePitch = 0.0f;

    public float rotorAngle = 0.0f;
    public float gimbalYaw = 0.0f;
    public float gimbalPitch = 0.0f;

    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> cooldown;
                case 1 -> targetFilter.getRadius();
                case 2 -> targetFilter.isTargetPlayers() ? 1 : 0;
                case 3 -> targetFilter.isTargetMonsters() ? 1 : 0;
                case 4 -> targetFilter.isTargetAnimals() ? 1 : 0;
                case 5 -> targetFilter.isTargetBosses() ? 1 : 0;
                case 6 -> linkedAmmoContainerPos != null ? 1 : 0;
                case 7 -> linkedAmmoContainerPos != null ? linkedAmmoContainerPos.getX() : 0;
                case 8 -> linkedAmmoContainerPos != null ? linkedAmmoContainerPos.getY() : 0;
                case 9 -> linkedAmmoContainerPos != null ? linkedAmmoContainerPos.getZ() : 0;
                case 10 -> currentTarget != null ? 1 : 0;
                case 11 -> patrolAltitude;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> cooldown = value;
                case 1 -> {
                    int r = MathHelper.clamp(value, 16, 512);
                    targetFilter.setRadius(r);
                    dataTracker.set(RADIUS, r);
                }
                case 2 -> {
                    targetFilter.setTargetPlayers(value == 1);
                    dataTracker.set(TARGET_PLAYERS, value == 1);
                }
                case 3 -> {
                    targetFilter.setTargetMonsters(value == 1);
                    dataTracker.set(TARGET_MONSTERS, value == 1);
                }
                case 4 -> {
                    targetFilter.setTargetAnimals(value == 1);
                    dataTracker.set(TARGET_ANIMALS, value == 1);
                }
                case 5 -> {
                    targetFilter.setTargetBosses(value == 1);
                    dataTracker.set(TARGET_BOSSES, value == 1);
                }
                case 11 -> patrolAltitude = MathHelper.clamp(value, 6, 48);
            }
        }

        @Override
        public int size() {
            return 12;
        }
    };

    public FlyingTurretEntity(EntityType<? extends FlyingTurretEntity> type, World world) {
        super(type, world);
        this.setNoGravity(true);
        this.targetFilter.setRadius(128); // Default 256x256 coverage, up to 512 = 1024x1024
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(RADIUS, 128);
        this.dataTracker.startTracking(TARGET_PLAYERS, false);
        this.dataTracker.startTracking(TARGET_MONSTERS, true);
        this.dataTracker.startTracking(TARGET_ANIMALS, false);
        this.dataTracker.startTracking(TARGET_BOSSES, true);
        this.dataTracker.startTracking(PITCH_TILT, 0.0f);
        this.dataTracker.startTracking(ROLL_TILT, 0.0f);
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

    public TurretTargetFilter getTargetFilter() {
        return targetFilter;
    }

    public PropertyDelegate getPropertyDelegate() {
        return propertyDelegate;
    }

    @Nullable
    public BlockPos getLinkedAmmoContainerPos() {
        return linkedAmmoContainerPos;
    }

    public void setLinkedAmmoContainerPos(@Nullable BlockPos pos) {
        this.linkedAmmoContainerPos = pos;
    }

    public void setStationPos(Vec3d pos) {
        this.stationPos = pos;
        this.homePos = BlockPos.ofFloored(pos);
    }

    @Nullable
    public Vec3d getStationPos() {
        return this.stationPos;
    }

    public void setRemoteInputs(boolean fwd, boolean back, boolean left, boolean right, boolean up, boolean down, boolean sprint, float yaw, float pitch) {
        this.remoteControlTicks = 20; // Active for 1 second if packets continue
        this.remoteFwd = fwd;
        this.remoteBack = back;
        this.remoteLeft = left;
        this.remoteRight = right;
        this.remoteUp = up;
        this.remoteDown = down;
        this.remoteSprint = sprint;
        this.remoteYaw = yaw;
        this.remotePitch = pitch;
    }

    public void recallTo(BlockPos pos) {
        this.recallTarget = Vec3d.ofCenter(pos);
        this.currentTarget = null;
        this.remoteControlTicks = 0;
    }

    @Nullable
    public String getPairedPlayerUuid() {
        return pairedPlayerUuid;
    }

    public void setPairedPlayerUuid(@Nullable String uuid) {
        this.pairedPlayerUuid = uuid;
    }

    public boolean isRemoteControlled() {
        return this.remoteControlTicks > 0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.stationPos == null) {
            this.stationPos = this.getPos();
            this.homePos = this.getBlockPos();
        }

        if (this.cooldown > 0) {
            this.cooldown--;
        }

        // Client-side effects: continuous rotor spin, drone audio hum & sparks
        if (this.getWorld().isClient()) {
            this.rotorAngle = (this.rotorAngle + 45.0f) % 360.0f;
            if (this.age % 10 == 0 && this.random.nextFloat() < 0.3F) {
                this.getWorld().addParticle(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() - 0.2, this.getZ(), 0, -0.05, 0);
            }
            if (this.age % 8 == 0) {
                float soundPitch = (this.remoteControlTicks > 0 && this.remoteSprint) ? 2.0F : 1.75F;
                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.ENTITY_BEE_LOOP, SoundCategory.NEUTRAL, 0.18F, soundPitch);
            }
        }

        // Server-side Flight & Defense Turret Execution
        if (!this.getWorld().isClient()) {
            if (this.remoteControlTicks > 0) {
                this.remoteControlTicks--;

                // Manual Piloting via Defense Controller
                this.setYaw(this.remoteYaw);
                this.setPitch(this.remotePitch);
                this.setBodyYaw(this.remoteYaw);
                this.setHeadYaw(this.remoteYaw);

                double rad = Math.toRadians(this.remoteYaw);
                double fwdX = -Math.sin(rad);
                double fwdZ = Math.cos(rad);
                double rightX = -Math.sin(rad - Math.PI / 2);
                double rightZ = Math.cos(rad - Math.PI / 2);

                double speed = this.remoteSprint ? 1.05 : 0.52;
                double moveX = 0;
                double moveZ = 0;
                double moveY = 0;

                if (this.remoteFwd) { moveX += fwdX * speed; moveZ += fwdZ * speed; }
                if (this.remoteBack) { moveX -= fwdX * (speed * 0.7); moveZ -= fwdZ * (speed * 0.7); }
                if (this.remoteLeft) { moveX -= rightX * (speed * 0.7); moveZ -= rightZ * (speed * 0.7); }
                if (this.remoteRight) { moveX += rightX * (speed * 0.7); moveZ += rightZ * (speed * 0.7); }
                if (this.remoteUp) { moveY += speed * 0.8; }
                if (this.remoteDown) { moveY -= speed * 0.8; }

                this.setVelocity(this.getVelocity().multiply(0.75).add(moveX * 0.25, moveY * 0.25, moveZ * 0.25));
                this.move(MovementType.SELF, this.getVelocity());

                // Aerodynamic Flight Tilts
                float targetPitchTilt = 0.0f;
                if (this.remoteFwd) targetPitchTilt = this.remoteSprint ? -22.0f : -15.0f;
                if (this.remoteBack) targetPitchTilt = 12.0f;
                setPitchTilt(getPitchTilt() + (targetPitchTilt - getPitchTilt()) * 0.3f);

                float targetRollTilt = 0.0f;
                if (this.remoteLeft) targetRollTilt = -15.0f;
                if (this.remoteRight) targetRollTilt = 15.0f;
                setRollTilt(getRollTilt() + (targetRollTilt - getRollTilt()) * 0.3f);

                // Update stationary defense anchor point to the newly piloted location
                this.stationPos = this.getPos();
                this.homePos = this.getBlockPos();

            } else if (this.recallTarget != null) {
                // Recall to Player coordinates
                Vec3d toRecall = this.recallTarget.subtract(this.getPos());
                double distSq = toRecall.lengthSquared();
                if (distSq <= 4.0) { // Within 2 blocks: arrived
                    this.recallTarget = null;
                    this.setVelocity(Vec3d.ZERO);
                    this.stationPos = this.getPos();
                    this.homePos = this.getBlockPos();
                    setPitchTilt(0.0f);
                    setRollTilt(0.0f);
                } else {
                    Vec3d dir = toRecall.normalize();
                    double recallSpeed = 0.75;
                    this.setVelocity(this.getVelocity().multiply(0.8).add(dir.multiply(recallSpeed * 0.2)));
                    this.move(MovementType.SELF, this.getVelocity());
                    this.setYaw((float) Math.toDegrees(Math.atan2(-dir.x, dir.z)));
                    setPitchTilt(-12.0f);
                }
            } else {
                // DEFAULT DEFENSE TURRET MODE:
                // Requires Defense Controller to move! Holds station position firmly in place.
                setPitchTilt(getPitchTilt() * 0.8f);
                setRollTilt(getRollTilt() * 0.8f);

                if (this.stationPos != null) {
                    Vec3d drift = this.stationPos.subtract(this.getPos());
                    if (drift.lengthSquared() > 0.005) {
                        // Smoothly snap back to stationed coordinates if bumped or pushed
                        this.setVelocity(drift.multiply(0.25));
                        this.move(MovementType.SELF, this.getVelocity());
                    } else {
                        // Subtle gentle hovering wave (no net displacement)
                        double waveY = Math.sin(this.age * 0.15) * 0.003;
                        this.setVelocity(new Vec3d(0, waveY, 0));
                    }
                } else {
                    this.stationPos = this.getPos();
                    this.setVelocity(Vec3d.ZERO);
                }

                // Target scanning
                if (this.age % 4 == 0) {
                    this.scanForTarget();
                }

                // If target locked: rotate yaw & pitch to aim and fire kinetic round
                if (this.currentTarget != null && this.currentTarget.isAlive()) {
                    Vec3d targetPos = this.currentTarget.getPos().add(0, this.currentTarget.getHeight() * 0.5, 0);
                    Vec3d dronePos = this.getPos();
                    Vec3d aimDir = targetPos.subtract(dronePos);
                    double horizDist = Math.sqrt(aimDir.x * aimDir.x + aimDir.z * aimDir.z);
                    double totalDist = aimDir.length();

                    float targetYaw = (float) Math.toDegrees(Math.atan2(-aimDir.x, aimDir.z));
                    float targetPitch = (float) Math.toDegrees(Math.atan2(-aimDir.y, horizDist));

                    this.setYaw(MathHelper.lerpAngleDegrees(0.35f, this.getYaw(), targetYaw));
                    this.setPitch(MathHelper.lerp(0.35f, this.getPitch(), targetPitch));
                    this.setHeadYaw(this.getYaw());
                    this.setBodyYaw(this.getYaw());

                    // Fire kinetic cannon at target if cooldown elapsed and within range
                    if (this.cooldown <= 0 && totalDist <= Math.max(64.0, this.targetFilter.getRadius())) {
                        Vec3d muzzlePos = dronePos.add(0, -0.35, 0);
                        this.fireAtTarget(muzzlePos, aimDir.normalize());
                    }
                }
            }
        }
    }

    private void scanForTarget() {
        int r = this.targetFilter.getRadius();
        Box area = new Box(this.getX() - r, this.getY() - 64, this.getZ() - r,
                this.getX() + r, this.getY() + 64, this.getZ() + r);

        List<LivingEntity> candidates = this.getWorld().getEntitiesByClass(LivingEntity.class, area, this.targetFilter::isValidTarget);

        if (!candidates.isEmpty()) {
            candidates.sort(Comparator.comparingDouble(e -> e.squaredDistanceTo(this)));
            this.currentTarget = candidates.get(0);
        } else {
            this.currentTarget = null;
        }
    }

    private void fireAtTarget(Vec3d muzzlePos, Vec3d aimDir) {
        TurretAmmoContainerBlockEntity container = getLinkedAmmoContainer();
        AmmoType ammoType = (container != null) ? container.consumeBestAmmo() : null;

        if (ammoType == null) {
            if (this.age % 40 == 0 && this.getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.SMOKE, muzzlePos.x, muzzlePos.y, muzzlePos.z, 2, 0.05, 0.05, 0.05, 0.01);
            }
            return;
        }

        TurretBulletEntity bullet = new TurretBulletEntity(EvecualMC.TURRET_BULLET_ENTITY, this.getWorld());
        bullet.setPosition(muzzlePos.x, muzzlePos.y, muzzlePos.z);
        bullet.setAmmoType(ammoType);

        double speed = 2.4 * ammoType.getVelocityMultiplier();
        bullet.setVelocity(aimDir.x * speed, aimDir.y * speed, aimDir.z * speed);

        this.getWorld().spawnEntity(bullet);
        this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 0.65F, 1.9F + (this.random.nextFloat() * 0.2F));

        if (this.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.FLASH, muzzlePos.x, muzzlePos.y, muzzlePos.z, 1, 0, 0, 0, 0);
            serverWorld.spawnParticles(ParticleTypes.SMOKE, muzzlePos.x, muzzlePos.y, muzzlePos.z, 3, 0.05, 0.05, 0.05, 0.02);
        }

        this.cooldown = COOLDOWN_TICKS;
    }

    public boolean fireManual(Vec3d aimDir) {
        if (this.cooldown > 0) return false;

        TurretAmmoContainerBlockEntity container = getLinkedAmmoContainer();
        AmmoType ammoType = (container != null) ? container.consumeBestAmmo() : null;

        if (ammoType == null) {
            if (this.getWorld() instanceof ServerWorld serverWorld) {
                Vec3d p = this.getPos().add(0, -0.35, 0);
                serverWorld.spawnParticles(ParticleTypes.SMOKE, p.x, p.y, p.z, 4, 0.05, 0.05, 0.05, 0.02);
            }
            return false;
        }

        Vec3d muzzlePos = this.getPos().add(0, -0.35, 0);
        TurretBulletEntity bullet = new TurretBulletEntity(EvecualMC.TURRET_BULLET_ENTITY, this.getWorld());
        bullet.setPosition(muzzlePos.x, muzzlePos.y, muzzlePos.z);
        bullet.setAmmoType(ammoType);

        double speed = 2.4 * ammoType.getVelocityMultiplier();
        bullet.setVelocity(aimDir.x * speed, aimDir.y * speed, aimDir.z * speed);

        this.getWorld().spawnEntity(bullet);
        this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 0.7F, 1.9F + (this.random.nextFloat() * 0.2F));

        if (this.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.FLASH, muzzlePos.x, muzzlePos.y, muzzlePos.z, 1, 0, 0, 0, 0);
            serverWorld.spawnParticles(ParticleTypes.SMOKE, muzzlePos.x, muzzlePos.y, muzzlePos.z, 3, 0.05, 0.05, 0.05, 0.02);
        }

        this.cooldown = COOLDOWN_TICKS;
        return true;
    }

    @Nullable
    public TurretAmmoContainerBlockEntity getLinkedAmmoContainer() {
        if (this.getWorld() == null || this.linkedAmmoContainerPos == null) return null;
        if (this.getWorld().isChunkLoaded(this.linkedAmmoContainerPos)) {
            BlockEntity be = this.getWorld().getBlockEntity(this.linkedAmmoContainerPos);
            if (be instanceof TurretAmmoContainerBlockEntity container) {
                return container;
            }
        }
        return null;
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack held = player.getStackInHand(hand);

        // 1. Linking with Turret Linker
        if (held.isOf(EvecualMC.TURRET_LINKER)) {
            if (held.hasNbt() && held.getNbt().contains("ContainerPos")) {
                BlockPos containerPos = NbtHelper.toBlockPos(held.getNbt().getCompound("ContainerPos"));
                this.setLinkedAmmoContainerPos(containerPos);

                if (!this.getWorld().isClient) {
                    BlockEntity cBe = this.getWorld().getBlockEntity(containerPos);
                    if (cBe instanceof TurretAmmoContainerBlockEntity c) {
                        c.linkFlyingTurret(this.getUuid());
                    }
                    player.sendMessage(Text.literal("§a🔗 Defense Drone linked to Ammo Container at §f[" + containerPos.getX() + ", " + containerPos.getY() + ", " + containerPos.getZ() + "]!"), true);
                }
                return ActionResult.SUCCESS;
            } else {
                if (!this.getWorld().isClient) {
                    player.sendMessage(Text.literal("§c⚠️ Right-click an Ammo Container first to establish a link coordinate!"), true);
                }
                return ActionResult.SUCCESS;
            }
        }

        // 2. Pairing with Defense Controller
        if (held.isOf(EvecualMC.FLYING_TURRET_CONTROLLER_ITEM)) {
            if (!this.getWorld().isClient) {
                com.evecual.evecualmc.item.FlyingTurretControllerItem.pairWithTurret(held, player, this);
            }
            return ActionResult.success(this.getWorld().isClient);
        }

        // 3. Sneaking with empty hand: Pick up the Defense Drone
        if (player.isSneaking() && held.isEmpty()) {
            if (!this.getWorld().isClient) {
                ItemStack drop = new ItemStack(EvecualMC.FLYING_TURRET_ITEM);
                if (this.linkedAmmoContainerPos != null) {
                    NbtCompound nbt = drop.getOrCreateNbt();
                    nbt.put("LinkedContainer", NbtHelper.fromBlockPos(this.linkedAmmoContainerPos));
                }
                if (!player.getInventory().insertStack(drop)) {
                    this.dropStack(drop);
                }
                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.8f, 1.5f);
                this.discard();
            }
            return ActionResult.success(this.getWorld().isClient);
        }

        // 4. Normal Right-Click: Open Defense Drone Terminal GUI
        if (!this.getWorld().isClient) {
            player.openHandledScreen(this);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.getAttacker() instanceof PlayerEntity player && player.isCreative()) {
            this.discard();
            return true;
        }
        return super.damage(source, amount);
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);
        if (!this.getWorld().isClient) {
            this.dropItem(EvecualMC.FLYING_TURRET_ITEM);
        }
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("🚁 Defense Drone");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new FlyingTurretScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        this.targetFilter.writeNbt(nbt);
        nbt.putInt("Cooldown", this.cooldown);
        nbt.putInt("PatrolAltitude", this.patrolAltitude);
        nbt.put("HomePos", NbtHelper.fromBlockPos(this.homePos));
        if (this.stationPos != null) {
            nbt.putDouble("StationX", this.stationPos.x);
            nbt.putDouble("StationY", this.stationPos.y);
            nbt.putDouble("StationZ", this.stationPos.z);
        }
        if (this.linkedAmmoContainerPos != null) {
            nbt.put("LinkedContainer", NbtHelper.fromBlockPos(this.linkedAmmoContainerPos));
        }
        if (this.pairedPlayerUuid != null) {
            nbt.putString("PairedPlayer", this.pairedPlayerUuid);
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.targetFilter.readNbt(nbt);
        this.cooldown = nbt.getInt("Cooldown");
        this.patrolAltitude = nbt.contains("PatrolAltitude") ? nbt.getInt("PatrolAltitude") : 16;
        if (nbt.contains("HomePos")) {
            this.homePos = NbtHelper.toBlockPos(nbt.getCompound("HomePos"));
        }
        if (nbt.contains("StationX") && nbt.contains("StationY") && nbt.contains("StationZ")) {
            this.stationPos = new Vec3d(nbt.getDouble("StationX"), nbt.getDouble("StationY"), nbt.getDouble("StationZ"));
        }
        if (nbt.contains("LinkedContainer")) {
            this.linkedAmmoContainerPos = NbtHelper.toBlockPos(nbt.getCompound("LinkedContainer"));
        }
        if (nbt.contains("PairedPlayer")) {
            this.pairedPlayerUuid = nbt.getString("PairedPlayer");
        }
    }
}
