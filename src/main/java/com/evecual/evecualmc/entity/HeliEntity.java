package com.evecual.evecualmc.entity;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.entity.ElectronicCombinerBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.GlassBlock;
import net.minecraft.block.StainedGlassBlock;
import net.minecraft.block.StainedGlassPaneBlock;
import net.minecraft.block.TintedGlassBlock;
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
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class HeliEntity extends Entity {
    public static final int BASE_MAX_ENERGY = 2000;
    public static final int UPGRADED_MAX_ENERGY = 3000;

    // Normal move speed: 12 blocks/sec = 0.60 blocks/tick
    public static final double NORMAL_CRUISE_SPEED = 0.60;
    // Boost move speed: 20 blocks/sec = 1.00 blocks/tick
    public static final double BOOST_CRUISE_SPEED = 1.00;

    private static final TrackedData<Integer> ENERGY = DataTracker.registerData(HeliEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> COLOR_VARIANT = DataTracker.registerData(HeliEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> GLASS_COLOR = DataTracker.registerData(HeliEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> UPGRADED_ENGINE = DataTracker.registerData(HeliEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Float> PITCH_TILT = DataTracker.registerData(HeliEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> ROLL_TILT = DataTracker.registerData(HeliEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> ROTOR_SPEED = DataTracker.registerData(HeliEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Boolean> CHARGING = DataTracker.registerData(HeliEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> IN_FLIGHT = DataTracker.registerData(HeliEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private final SimpleInventory trunk = new SimpleInventory(27);

    private boolean inputForward;
    private boolean inputBack;
    private boolean inputLeft;
    private boolean inputRight;
    private boolean inputUp;
    private boolean inputDown;
    private boolean inputSprint;

    private double currentSpeed = 0.0;
    private float rotorAngle = 0.0F;
    private float tailRotorAngle = 0.0F;

    private ChunkPos forcedChunk = null;

    public HeliEntity(EntityType<? extends HeliEntity> type, World world) {
        super(type, world);
        this.intersectionChecked = true;
        this.setStepHeight(1.0F);
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(ENERGY, BASE_MAX_ENERGY);
        this.dataTracker.startTracking(COLOR_VARIANT, 1); // 1: Cyber Blue default
        this.dataTracker.startTracking(GLASS_COLOR, 0);    // 0: Clear Glass
        this.dataTracker.startTracking(UPGRADED_ENGINE, false);
        this.dataTracker.startTracking(PITCH_TILT, 0.0F);
        this.dataTracker.startTracking(ROLL_TILT, 0.0F);
        this.dataTracker.startTracking(ROTOR_SPEED, 0.0F);
        this.dataTracker.startTracking(CHARGING, false);
        this.dataTracker.startTracking(IN_FLIGHT, false);
    }

    private void updateChunkLoading() {
        if (!this.getWorld().isClient && this.getWorld() instanceof ServerWorld serverWorld) {
            ChunkPos currentPos = new ChunkPos(this.getBlockPos());
            if (this.forcedChunk == null || !this.forcedChunk.equals(currentPos)) {
                if (this.forcedChunk != null) {
                    serverWorld.setChunkForced(this.forcedChunk.x, this.forcedChunk.z, false);
                }
                serverWorld.setChunkForced(currentPos.x, currentPos.z, true);
                this.forcedChunk = currentPos;
            }
        }
    }

    private void releaseChunkLoading() {
        if (!this.getWorld().isClient && this.getWorld() instanceof ServerWorld serverWorld) {
            if (this.forcedChunk != null) {
                serverWorld.setChunkForced(this.forcedChunk.x, this.forcedChunk.z, false);
                this.forcedChunk = null;
            }
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        releaseChunkLoading();
        super.remove(reason);
    }

    public int getEnergy() {
        return this.dataTracker.get(ENERGY);
    }

    public void setEnergy(int energy) {
        this.dataTracker.set(ENERGY, MathHelper.clamp(energy, 0, getMaxEnergy()));
    }

    public int getMaxEnergy() {
        return isUpgradedEngine() ? UPGRADED_MAX_ENERGY : BASE_MAX_ENERGY;
    }

    public int getColorVariant() {
        return this.dataTracker.get(COLOR_VARIANT);
    }

    public void setColorVariant(int variant) {
        this.dataTracker.set(COLOR_VARIANT, MathHelper.clamp(variant, 0, 5));
    }

    public int getGlassColor() {
        return this.dataTracker.get(GLASS_COLOR);
    }

    public void setGlassColor(int glassColor) {
        this.dataTracker.set(GLASS_COLOR, MathHelper.clamp(glassColor, 0, 11));
    }

    public boolean isUpgradedEngine() {
        return this.dataTracker.get(UPGRADED_ENGINE);
    }

    public void setUpgradedEngine(boolean upgraded) {
        this.dataTracker.set(UPGRADED_ENGINE, upgraded);
    }

    public float getPitchTilt() {
        return this.dataTracker.get(PITCH_TILT);
    }

    public float getRollTilt() {
        return this.dataTracker.get(ROLL_TILT);
    }

    public float getRotorSpeed() {
        return this.dataTracker.get(ROTOR_SPEED);
    }

    public boolean isCharging() {
        return this.dataTracker.get(CHARGING);
    }

    public void setCharging(boolean charging) {
        this.dataTracker.set(CHARGING, charging);
    }

    public boolean isInFlight() {
        return this.dataTracker.get(IN_FLIGHT);
    }

    public float getRotorAngle() {
        return this.rotorAngle;
    }

    public float getTailRotorAngle() {
        return this.tailRotorAngle;
    }

    public double getCurrentSpeed() {
        return this.currentSpeed;
    }

    public int charge(int amount) {
        int current = getEnergy();
        int max = getMaxEnergy();
        int canAdd = Math.min(amount, max - current);
        if (canAdd > 0) {
            setEnergy(current + canAdd);
        }
        return canAdd;
    }

    public void setInputs(boolean forward, boolean back, boolean left, boolean right, boolean up, boolean down, boolean sprint) {
        this.inputForward = forward;
        this.inputBack = back;
        this.inputLeft = left;
        this.inputRight = right;
        this.inputUp = up;
        this.inputDown = down;
        this.inputSprint = sprint;
    }

    public void openTrunk(PlayerEntity player) {
        if (!this.getWorld().isClient) {
            player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                    (syncId, playerInventory, p) -> new GenericContainerScreenHandler(
                            net.minecraft.screen.ScreenHandlerType.GENERIC_9X3, syncId, playerInventory, this.trunk, 3),
                    Text.literal("EV Heli Cargo (27 Slots)")
            ));
        }
    }

    public void applyItemNbt(NbtCompound nbt) {
        if (nbt.contains("ColorVariant")) setColorVariant(nbt.getInt("ColorVariant"));
        if (nbt.contains("GlassColor")) setGlassColor(nbt.getInt("GlassColor"));
        if (nbt.contains("UpgradedEngine")) setUpgradedEngine(nbt.getBoolean("UpgradedEngine"));
        if (nbt.contains("Energy")) setEnergy(nbt.getInt("Energy"));
    }

    public ItemStack createHeliDropItem() {
        ItemStack stack = new ItemStack(EvecualMC.HELI_ITEM);
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putInt("ColorVariant", getColorVariant());
        nbt.putInt("GlassColor", getGlassColor());
        nbt.putBoolean("UpgradedEngine", isUpgradedEngine());
        nbt.putInt("Energy", getEnergy());
        return stack;
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
        ItemStack held = player.getStackInHand(hand);

        // 1. Color Customization using Dyes
        if (held.getItem() instanceof DyeItem dye) {
            DyeColor color = dye.getColor();
            int newVariant = switch (color) {
                case RED -> 0;
                case BLUE, CYAN, LIGHT_BLUE -> 1;
                case BLACK, GRAY, LIGHT_GRAY -> 2;
                case LIME, GREEN -> 3;
                case WHITE -> 4;
                case YELLOW, ORANGE -> 5;
                default -> -1;
            };

            if (newVariant != -1) {
                if (!this.getWorld().isClient) {
                    setColorVariant(newVariant);
                    this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.ITEM_DYE_USE, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    if (!player.isCreative()) {
                        held.decrement(1);
                    }
                    player.sendMessage(Text.literal("§b⚡ Heli body livery applied!"), true);
                }
                return ActionResult.SUCCESS;
            }
        }

        // 2. Glass Customization using Stained Glass Blocks or Panes
        if (isGlassItem(held.getItem())) {
            int newGlass = ElectronicCombinerBlockEntity.getGlassColorFromItem(held.getItem());
            if (!this.getWorld().isClient) {
                setGlassColor(newGlass);
                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.BLOCK_GLASS_PLACE, SoundCategory.PLAYERS, 1.0F, 1.2F);
                if (!player.isCreative()) {
                    held.decrement(1);
                }
                player.sendMessage(Text.literal("§b⚡ Cockpit canopy tinted!"), true);
            }
            return ActionResult.SUCCESS;
        }

        // 3. Engine Upgrade Application
        if (held.isOf(EvecualMC.UPGRADED_ENGINE) && !isUpgradedEngine()) {
            if (!this.getWorld().isClient) {
                setUpgradedEngine(true);
                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.BLOCK_ANVIL_USE, SoundCategory.PLAYERS, 1.0F, 1.4F);
                if (!player.isCreative()) {
                    held.decrement(1);
                }
                player.sendMessage(Text.literal("§b⚡ High-Power Turbine Upgrade Installed! (+50% Battery & Efficiency)"), true);
            }
            return ActionResult.SUCCESS;
        }

        // 4. Sneak + Click: Pick up EV Heli
        if (player.isSneaking()) {
            if (!this.getWorld().isClient) {
                dropTrunkContents();
                ItemEntity dropped = new ItemEntity(this.getWorld(), this.getX(), this.getY() + 0.5, this.getZ(), createHeliDropItem());
                this.getWorld().spawnEntity(dropped);
                this.discard();
            }
            return ActionResult.SUCCESS;
        }

        // 5. Otherwise, enter helicopter cockpit
        if (!this.getWorld().isClient) {
            player.startRiding(this);
        }
        return ActionResult.SUCCESS;
    }

    private boolean isGlassItem(net.minecraft.item.Item item) {
        if (item == Items.GLASS || item == Items.GLASS_PANE || item == Items.TINTED_GLASS) return true;
        if (item instanceof BlockItem bi) {
            Block b = bi.getBlock();
            return b instanceof GlassBlock || b instanceof StainedGlassBlock || b instanceof StainedGlassPaneBlock || b instanceof TintedGlassBlock;
        }
        return false;
    }

    private void dropTrunkContents() {
        for (int i = 0; i < trunk.size(); ++i) {
            ItemStack stack = trunk.getStack(i);
            if (!stack.isEmpty()) {
                ItemEntity itemEntity = new ItemEntity(this.getWorld(), this.getX(), this.getY() + 0.5, this.getZ(), stack.copy());
                this.getWorld().spawnEntity(itemEntity);
                trunk.setStack(i, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (!this.getWorld().isClient && !this.isRemoved()) {
            dropTrunkContents();
            ItemEntity dropped = new ItemEntity(this.getWorld(), this.getX(), this.getY() + 0.5, this.getZ(), createHeliDropItem());
            this.getWorld().spawnEntity(dropped);
            this.discard();
        }
        return true;
    }

    @Override
    protected void updatePassengerPosition(Entity passenger, PositionUpdater positionUpdater) {
        if (this.hasPassenger(passenger)) {
            // Position pilot comfortably inside cockpit seat
            double rad = Math.toRadians(this.getYaw());
            double forwardOffset = 0.35; // slightly forward in cockpit
            double px = this.getX() - Math.sin(rad) * forwardOffset;
            double py = this.getY() + 0.55;
            double pz = this.getZ() + Math.cos(rad) * forwardOffset;
            positionUpdater.accept(passenger, px, py, pz);
        }
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        Entity passenger = this.getFirstPassenger();
        return passenger instanceof LivingEntity living ? living : null;
    }

    @Override
    public void tick() {
        super.tick();
        updateChunkLoading();

        Entity passenger = this.getFirstPassenger();
        int energy = getEnergy();
        boolean hasPower = energy > 0;
        boolean hasPilot = passenger instanceof PlayerEntity;

        // 1. Target Rotor Speed & Aerodynamic state
        float targetRotorSpeed = 0.0F;
        if (hasPower && hasPilot) {
            targetRotorSpeed = inputSprint ? 1.4F : 1.0F;
        } else if (hasPower && !this.isOnGround()) {
            targetRotorSpeed = 0.6F; // emergency auto-rotation descent
        }

        float currentRotorSpeed = this.dataTracker.get(ROTOR_SPEED);
        currentRotorSpeed = MathHelper.stepTowards(currentRotorSpeed, targetRotorSpeed, 0.05F);
        this.dataTracker.set(ROTOR_SPEED, currentRotorSpeed);

        // Update Rotor Angles for client animations
        this.rotorAngle += currentRotorSpeed * 45.0F;
        this.tailRotorAngle += currentRotorSpeed * 65.0F;

        boolean flying = currentRotorSpeed > 0.4F && !this.isOnGround();
        this.dataTracker.set(IN_FLIGHT, flying);

        // 2. Flight Dynamics & Speed Control
        if (!this.getWorld().isClient) {
            double targetHozSpeed = 0.0;
            double targetVy = 0.0;
            float targetYawDelta = 0.0F;
            float targetPitch = 0.0F;
            float targetRoll = 0.0F;

            if (hasPilot && hasPower && currentRotorSpeed >= 0.7F) {
                // Horizontal Cruise Speed: 12 blocks/s normal (0.60), 20 blocks/s boost (1.00)
                double maxCruise = inputSprint ? BOOST_CRUISE_SPEED : NORMAL_CRUISE_SPEED;

                if (inputForward) {
                    targetHozSpeed = maxCruise;
                    targetPitch = inputSprint ? 16.0F : 10.0F; // tilt nose down forward
                } else if (inputBack) {
                    targetHozSpeed = -0.35;
                    targetPitch = -8.0F; // tilt nose up backward
                }

                // Yaw Steering
                if (inputLeft) {
                    targetYawDelta = -3.8F;
                    targetRoll = -14.0F; // bank left
                } else if (inputRight) {
                    targetYawDelta = 3.8F;
                    targetRoll = 14.0F; // bank right
                }

                // Vertical Flight (Up: Space, Down: Shift/Down)
                if (inputUp) {
                    targetVy = 0.42; // ~8.4 blocks/sec climb
                } else if (inputDown) {
                    targetVy = -0.38; // ~7.6 blocks/sec descent
                } else {
                    // Hover stability: active altitude hold
                    targetVy = 0.0;
                }

                // Power consumption
                int drainInterval = inputSprint ? 6 : (inputForward || inputUp ? 12 : 20);
                if (this.age % drainInterval == 0) {
                    setEnergy(Math.max(0, energy - 1));
                }

                // Boost particles
                if (inputSprint && this.age % 2 == 0 && this.getWorld() instanceof ServerWorld serverWorld) {
                    double radHeading = Math.toRadians(this.getYaw());
                    double rearX = this.getX() + Math.sin(radHeading) * 1.8;
                    double rearY = this.getY() + 1.2;
                    double rearZ = this.getZ() - Math.cos(radHeading) * 1.8;
                    serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, rearX, rearY, rearZ, 2, 0.1, 0.05, 0.1, 0.02);
                }

                // Ambient turbine whoosh sound
                if (this.age % 15 == 0) {
                    this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.ENTITY_PHANTOM_FLAP, SoundCategory.PLAYERS,
                            0.6F, 1.2F + currentRotorSpeed * 0.4F);
                }
            } else {
                // Gravity & Landing descent when unpowered or no pilot
                if (this.isOnGround()) {
                    targetVy = 0.0;
                } else {
                    targetVy = -0.12; // gentle descent
                }
            }

            // Smooth Acceleration & Deceleration
            double accel = (targetHozSpeed > currentSpeed) ? 0.045 : 0.08;
            currentSpeed = MathHelper.stepTowards((float) currentSpeed, (float) targetHozSpeed, (float) accel);

            // Apply Yaw Rotation
            if (Math.abs(targetYawDelta) > 0.01F) {
                this.setYaw(this.getYaw() + targetYawDelta);
            }

            // Smooth Pitch & Roll Tracking
            float curPitch = this.dataTracker.get(PITCH_TILT);
            float curRoll = this.dataTracker.get(ROLL_TILT);
            curPitch = MathHelper.lerp(0.15F, curPitch, targetPitch);
            curRoll = MathHelper.lerp(0.15F, curRoll, targetRoll);
            this.dataTracker.set(PITCH_TILT, curPitch);
            this.dataTracker.set(ROLL_TILT, curRoll);

            // Compute 3D Velocity Vector
            double radYaw = Math.toRadians(this.getYaw());
            double vx = -Math.sin(radYaw) * currentSpeed;
            double vz = Math.cos(radYaw) * currentSpeed;
            double vy = MathHelper.stepTowards((float) this.getVelocity().y, (float) targetVy, 0.06F);

            this.setVelocity(vx, vy, vz);
            this.move(MovementType.SELF, this.getVelocity());

            // Ground collision check
            if (this.isOnGround() && this.getVelocity().y < 0) {
                this.setVelocity(this.getVelocity().x, 0.0, this.getVelocity().z);
            }
        }
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        setEnergy(nbt.getInt("Energy"));
        setColorVariant(nbt.getInt("ColorVariant"));
        setGlassColor(nbt.getInt("GlassColor"));
        setUpgradedEngine(nbt.getBoolean("UpgradedEngine"));
        if (nbt.contains("Trunk", 10)) {
            trunk.readNbtList(nbt.getList("Trunk", 10));
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("Energy", getEnergy());
        nbt.putInt("ColorVariant", getColorVariant());
        nbt.putInt("GlassColor", getGlassColor());
        nbt.putBoolean("UpgradedEngine", isUpgradedEngine());
        nbt.put("Trunk", trunk.toNbtList());
    }
}
