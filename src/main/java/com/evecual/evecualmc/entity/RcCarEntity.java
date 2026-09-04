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
import net.minecraft.world.World;

import java.util.UUID;

public class RcCarEntity extends Entity {
    public static final int MAX_ENERGY = 500;

    private static final TrackedData<Integer> ENERGY = DataTracker.registerData(RcCarEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> COLOR_VARIANT = DataTracker.registerData(RcCarEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Float> STEERING_ANGLE = DataTracker.registerData(RcCarEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<String> PAIRED_PLAYER_UUID = DataTracker.registerData(RcCarEntity.class, TrackedDataHandlerRegistry.STRING);

    private boolean inputForward;
    private boolean inputBack;
    private boolean inputLeft;
    private boolean inputRight;
    private boolean inputSprint;
    private boolean inputJump;

    private int inputTimeoutTicks = 0;
    private double currentSpeed = 0.0;
    private float wheelRoll = 0.0F;

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

    public void setRemoteInputs(boolean forward, boolean back, boolean left, boolean right, boolean sprint, boolean jump) {
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

        // Driving physics
        int energy = getEnergy();
        boolean hasPower = energy > 0;

        if (hasPower) {
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

        // Steering
        float targetAngle = 0.0F;
        if (this.inputLeft) targetAngle -= 32.0F;
        if (this.inputRight) targetAngle += 32.0F;

        float currentAngle = getSteeringAngle();
        currentAngle += (targetAngle - currentAngle) * 0.4F;
        setSteeringAngle(currentAngle);

        if (Math.abs(this.currentSpeed) > 0.01) {
            float yawDelta = (currentAngle / 32.0F) * (float) this.currentSpeed * 14.0F;
            this.setYaw(this.getYaw() + yawDelta);
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

    public ItemStack asItemStack() {
        ItemStack stack = new ItemStack(EvecualMC.RC_CAR_ITEM);
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("Energy", getEnergy());
        nbt.putInt("ColorVariant", getColorVariant());
        if (!getPairedPlayerUuid().isEmpty()) {
            nbt.putString("PairedPlayer", getPairedPlayerUuid());
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

        if (player.isSneaking() && held.isEmpty()) {
            // Pick up the RC car
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
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("Energy", getEnergy());
        nbt.putInt("ColorVariant", getColorVariant());
        nbt.putString("PairedPlayer", getPairedPlayerUuid());
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
}
