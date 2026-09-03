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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CarEntity extends Entity {
    public static final int MAX_ENERGY = 1000;

    private static final TrackedData<Integer> ENERGY = DataTracker.registerData(CarEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public CarEntity(EntityType<? extends CarEntity> type, World world) {
        super(type, world);
        this.intersectionChecked = true;
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(ENERGY, 500); // starts with 500 E
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
                // Drop car item and remove
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
    public void tick() {
        super.tick();

        // Apply gravity and ground physics
        if (!this.hasNoGravity()) {
            this.setVelocity(this.getVelocity().add(0.0, -0.04, 0.0));
        }

        Entity passenger = this.getFirstPassenger();
        if (passenger instanceof PlayerEntity player) {
            // Update yaw to match player
            this.setYaw(player.getYaw());
            this.prevYaw = this.getYaw();

            float forward = player.forwardSpeed;
            float sideways = player.sidewaysSpeed;

            int energy = getEnergy();
            if (energy > 0 && (forward != 0.0f || sideways != 0.0f)) {
                // Moving forward / reverse
                double speed = forward > 0 ? 0.45 : (forward < 0 ? -0.2 : 0.0);
                double rad = Math.toRadians(this.getYaw());
                double vx = -Math.sin(rad) * speed;
                double vz = Math.cos(rad) * speed;

                if (sideways != 0.0f) {
                    double strafeSpeed = 0.2 * Math.signum(sideways);
                    vx += Math.cos(rad) * strafeSpeed;
                    vz += Math.sin(rad) * strafeSpeed;
                }

                this.setVelocity(vx, this.getVelocity().y, vz);

                // Drain energy while driving
                if (!this.getWorld().isClient && this.age % 4 == 0) {
                    setEnergy(energy - 1);
                }

                // Electric drive particles
                if (this.getWorld().isClient && this.random.nextFloat() < 0.25f) {
                    this.getWorld().addParticle(ParticleTypes.ELECTRIC_SPARK,
                            this.getX() + (random.nextDouble() - 0.5) * 0.8,
                            this.getY() + 0.1,
                            this.getZ() + (random.nextDouble() - 0.5) * 0.8,
                            0, 0.05, 0);
                }
            } else {
                // Apply braking friction
                this.setVelocity(this.getVelocity().multiply(0.8, 0.98, 0.8));
            }
        } else {
            // Idle friction
            this.setVelocity(this.getVelocity().multiply(0.6, 0.98, 0.6));
        }

        this.move(MovementType.SELF, this.getVelocity());
    }

    @Override
    protected void updatePassengerPosition(Entity passenger, PositionUpdater positionUpdater) {
        if (this.hasPassenger(passenger)) {
            // Driver seat offset in local car coordinates: seated on driver cushion in interior
            double localX = -0.50;
            double localY = 0.38;
            double localZ = 0.20;

            float rad = (float) Math.toRadians(this.getYaw());
            double cos = Math.cos(rad);
            double sin = Math.sin(rad);

            double worldX = this.getX() + (localX * cos - localZ * sin);
            double worldY = this.getY() + localY;
            double worldZ = this.getZ() + (localX * sin + localZ * cos);

            positionUpdater.accept(passenger, worldX, worldY, worldZ);
        }
    }

    @Override
    public double getMountedHeightOffset() {
        return 0.38;
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
