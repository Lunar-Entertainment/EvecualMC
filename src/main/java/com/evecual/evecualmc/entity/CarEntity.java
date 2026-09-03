package com.evecual.evecualmc.entity;

import com.evecual.evecualmc.EvecualMC;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CarEntity extends Entity {
    public static final int MAX_ENERGY = 1000;

    private static final TrackedData<Integer> ENERGY = DataTracker.registerData(CarEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private boolean inputForward;
    private boolean inputBack;
    private boolean inputLeft;
    private boolean inputRight;
    private boolean inputSprint;

    private double currentSpeed = 0.0;

    public CarEntity(EntityType<? extends CarEntity> type, World world) {
        super(type, world);
        this.intersectionChecked = true;
        this.setStepHeight(1.0F); // Climb 1-block terrain smoothly without bumpiness
    }

    @Override
    protected void initDataTracker() {
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

    public void setInputs(boolean forward, boolean back, boolean left, boolean right, boolean sprint) {
        this.inputForward = forward;
        this.inputBack = back;
        this.inputLeft = left;
        this.inputRight = right;
        this.inputSprint = sprint;
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
        if (player.isSneaking()) {
            if (!this.getWorld().isClient) {
                this.dropItem(EvecualMC.CAR_ITEM);
                this.discard();
            }
            return ActionResult.SUCCESS;
        } else {
            if (!this.getWorld().isClient) {
                player.startRiding(this);
            }
            return ActionResult.SUCCESS;
        }
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
    public void tick() {
        super.tick();

        // 1. Gravity & Ground adherence
        if (!this.hasNoGravity()) {
            if (this.isOnGround()) {
                this.setVelocity(this.getVelocity().x, 0.0, this.getVelocity().z);
            } else {
                this.setVelocity(this.getVelocity().add(0.0, -0.05, 0.0));
            }
        }

        Entity passenger = this.getFirstPassenger();
        int energy = getEnergy();
        boolean hasPower = energy > 0;

        if (passenger instanceof PlayerEntity) {
            // 2. Target speed calculation with CTRL Boost
            double targetSpeed = 0.0;
            if (hasPower) {
                if (inputForward) {
                    // Normal cruising speed is 0.50; holding CTRL boosts to 0.95!
                    targetSpeed = inputSprint ? 0.95 : 0.50;
                } else if (inputBack) {
                    targetSpeed = -0.22; // Reverse gear
                }
            }

            // 3. Smooth vehicular acceleration and coasting friction
            if (targetSpeed > currentSpeed) {
                currentSpeed = Math.min(targetSpeed, currentSpeed + 0.035);
            } else if (targetSpeed < currentSpeed) {
                currentSpeed = Math.max(targetSpeed, currentSpeed - 0.055);
            }

            // 4. Smooth steering when vehicle is in motion
            if (Math.abs(currentSpeed) > 0.01) {
                float turnSpeed = 3.8F;
                float dir = Math.signum((float) currentSpeed);
                if (inputLeft) {
                    this.setYaw(this.getYaw() - turnSpeed * dir);
                }
                if (inputRight) {
                    this.setYaw(this.getYaw() + turnSpeed * dir);
                }
                this.prevYaw = this.getYaw();
            }

            // 5. Energy Consumption (Server authoritative)
            if (!this.getWorld().isClient && Math.abs(currentSpeed) > 0.05 && energy > 0) {
                // Boost consumes 1 E every single tick (20 E/s); normal driving consumes 1 E every 3 ticks (~6.6 E/s)
                int drainTicks = inputSprint ? 1 : 3;
                if (this.age % drainTicks == 0) {
                    setEnergy(energy - 1);
                }
            }

            // 6. Visual drive & boost exhaust particles
            if (this.getWorld().isClient && Math.abs(currentSpeed) > 0.05 && energy > 0) {
                double rad = Math.toRadians(this.getYaw());
                if (inputSprint && inputForward) {
                    // Fiery turbo boost flames emitting from exhaust pipes
                    double exhaustX = this.getX() + Math.sin(rad) * 1.5;
                    double exhaustZ = this.getZ() - Math.cos(rad) * 1.5;
                    this.getWorld().addParticle(ParticleTypes.FLAME, exhaustX, this.getY() + 0.35, exhaustZ, 0, 0.02, 0);
                }
                if (this.random.nextFloat() < 0.25F) {
                    this.getWorld().addParticle(ParticleTypes.ELECTRIC_SPARK,
                            this.getX() + (random.nextDouble() - 0.5) * 0.8,
                            this.getY() + 0.15,
                            this.getZ() + (random.nextDouble() - 0.5) * 0.8,
                            0, 0.05, 0);
                }
            }
        } else {
            // Decelerate smoothly when parked or no passenger
            currentSpeed *= 0.85;
            if (Math.abs(currentSpeed) < 0.01) {
                currentSpeed = 0.0;
            }
        }

        // 7. Apply velocity smoothly along vehicle heading
        double rad = Math.toRadians(this.getYaw());
        double vx = -Math.sin(rad) * currentSpeed;
        double vz = Math.cos(rad) * currentSpeed;
        this.setVelocity(vx, this.getVelocity().y, vz);

        this.move(MovementType.SELF, this.getVelocity());
    }

    @Override
    protected void updatePassengerPosition(Entity passenger, PositionUpdater positionUpdater) {
        if (this.hasPassenger(passenger)) {
            // Player sits centered, facing straight forward, inside the cabin
            double forwardOffset = -0.10;
            double leftOffset = 0.00;
            double heightOffset = 0.12;

            float rad = (float) Math.toRadians(this.getYaw());
            double worldX = this.getX() - Math.sin(rad) * forwardOffset + Math.cos(rad) * leftOffset;
            double worldY = this.getY() + heightOffset;
            double worldZ = this.getZ() + Math.cos(rad) * forwardOffset + Math.sin(rad) * leftOffset;

            positionUpdater.accept(passenger, worldX, worldY, worldZ);
            passenger.setYaw(this.getYaw());
        }
    }

    @Override
    public Vec3d updatePassengerForDismount(LivingEntity passenger) {
        // Dismount safely to the side onto the ground (no floating mid-air)
        Direction dir = this.getHorizontalFacing().rotateYClockwise();
        return new Vec3d(this.getX() + dir.getOffsetX() * 1.6, this.getY(), this.getZ() + dir.getOffsetZ() * 1.6);
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        this.inputForward = false;
        this.inputBack = false;
        this.inputLeft = false;
        this.inputRight = false;
        this.inputSprint = false;
        this.currentSpeed = 0.0;
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

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.contains("Energy")) {
            setEnergy(nbt.getInt("Energy"));
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("Energy", getEnergy());
    }
}
