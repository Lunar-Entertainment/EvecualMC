package com.evecual.evecualmc.entity;

import com.evecual.evecualmc.EvecualMC;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public class CarEntity extends LivingEntity {
    public static final int MAX_ENERGY = 1000;

    private static final TrackedData<Integer> ENERGY = DataTracker.registerData(CarEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public CarEntity(EntityType<? extends CarEntity> type, World world) {
        super(type, world);
        this.setStepHeight(1.0F); // Climb 1-block terrain effortlessly
    }

    public static DefaultAttributeContainer.Builder createCarAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 60.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.45)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(ENERGY, 500);
    }

    public int getEnergy() {
        return this.dataTracker.get(ENERGY);
    }

    public void setEnergy(int energy) {
        this.dataTracker.set(ENERGY, MathHelper.clamp(energy, 0, MAX_ENERGY));
    }

    public int getMaxEnergy() {
        return MAX_ENERGY;
    }

    public int charge(int amount) {
        int current = getEnergy();
        int canAdd = Math.min(amount, MAX_ENERGY - current);
        if (canAdd > 0) {
            setEnergy(current + canAdd);
        }
        return canAdd;
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
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (!this.getWorld().isClient) {
            if (player.isSneaking()) {
                this.dropItem(EvecualMC.CAR_ITEM);
                this.discard();
                return ActionResult.SUCCESS;
            } else {
                player.startRiding(this);
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (!this.getWorld().isClient && !this.isRemoved()) {
            this.dropItem(EvecualMC.CAR_ITEM);
            this.discard();
        }
        return true;
    }

    @Override
    public void travel(Vec3d movementInput) {
        if (!this.isAlive()) return;

        Entity passenger = this.getFirstPassenger();
        if (passenger instanceof LivingEntity driver) {
            // Keep body and head aligned straight forward with car
            this.setYaw(driver.getYaw());
            this.prevYaw = this.getYaw();
            this.setPitch(driver.getPitch() * 0.5F);
            this.setRotation(this.getYaw(), this.getPitch());
            this.bodyYaw = this.getYaw();
            this.headYaw = this.bodyYaw;

            float sideways = driver.sidewaysSpeed * 0.5F;
            float forward = driver.forwardSpeed;

            int energy = getEnergy();
            if (energy <= 0) {
                forward = 0.0F;
                sideways = 0.0F;
            }

            // CTRL / Sprint speed boost
            boolean isBoosting = driver.isSprinting();
            float speedFactor = 1.0F;

            if (isBoosting && forward > 0.0F && energy > 0) {
                speedFactor = 1.85F; // CTRL Boost speed!
            }

            // Energy drain & particles
            if (!this.getWorld().isClient && energy > 0 && (forward != 0.0F || sideways != 0.0F)) {
                int drainTicks = isBoosting ? 1 : 4; // Takes 4x energy while holding CTRL boost
                if (this.age % drainTicks == 0) {
                    setEnergy(energy - 1);
                }
            }

            if (this.getWorld().isClient && energy > 0 && (forward != 0.0F || sideways != 0.0F)) {
                if (isBoosting) {
                    // Boost flames and heavy smoke from rear exhaust
                    this.getWorld().addParticle(ParticleTypes.FLAME,
                            this.getX() + (random.nextDouble() - 0.5) * 0.4,
                            this.getY() + 0.3,
                            this.getZ() + (random.nextDouble() - 0.5) * 0.4,
                            0, 0.05, 0);
                }
                if (this.random.nextFloat() < 0.25F) {
                    this.getWorld().addParticle(ParticleTypes.ELECTRIC_SPARK,
                            this.getX() + (random.nextDouble() - 0.5) * 0.8,
                            this.getY() + 0.1,
                            this.getZ() + (random.nextDouble() - 0.5) * 0.8,
                            0, 0.05, 0);
                }
            }

            float baseSpeed = (float) this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
            this.setMovementSpeed(baseSpeed * speedFactor);
            super.travel(new Vec3d(sideways, movementInput.y, forward));
            return;
        }

        super.travel(movementInput);
    }

    @Override
    protected void updatePassengerPosition(Entity passenger, PositionUpdater positionUpdater) {
        if (this.hasPassenger(passenger)) {
            // Player sits in the CENTER, facing straight forward, inside the cabin:
            // forwardOffset: -0.10 (firmly on seat cushion)
            // leftOffset: 0.00 (centered in the car, arms enclosed!)
            // heightOffset: 0.12 (plenty of headroom under the roof!)
            double forwardOffset = -0.10;
            double leftOffset = 0.00;
            double heightOffset = 0.12;

            float rad = (float) Math.toRadians(this.getYaw());
            double worldX = this.getX() - Math.sin(rad) * forwardOffset + Math.cos(rad) * leftOffset;
            double worldY = this.getY() + heightOffset;
            double worldZ = this.getZ() + Math.cos(rad) * forwardOffset + Math.sin(rad) * leftOffset;

            positionUpdater.accept(passenger, worldX, worldY, worldZ);

            // Keep passenger facing straight forward with the car
            passenger.setYaw(this.getYaw());
            if (passenger instanceof LivingEntity living) {
                living.setBodyYaw(this.getYaw());
                living.prevBodyYaw = this.getYaw();
            }
        }
    }

    @Override
    public Vec3d updatePassengerForDismount(LivingEntity passenger) {
        // Dismount safely to the side on the ground (no floating mid air!)
        Direction dir = this.getHorizontalFacing().rotateYClockwise();
        return new Vec3d(this.getX() + dir.getOffsetX() * 1.6, this.getY(), this.getZ() + dir.getOffsetZ() * 1.6);
    }

    @Override
    public double getMountedHeightOffset() {
        return 0.12;
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        Entity passenger = this.getFirstPassenger();
        return passenger instanceof LivingEntity living ? living : null;
    }

    // Required LivingEntity abstract methods
    @Override
    public Iterable<ItemStack> getArmorItems() {
        return Collections.emptyList();
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {
    }

    @Override
    public Arm getMainArm() {
        return Arm.RIGHT;
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("Energy")) {
            setEnergy(nbt.getInt("Energy"));
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Energy", getEnergy());
    }
}
