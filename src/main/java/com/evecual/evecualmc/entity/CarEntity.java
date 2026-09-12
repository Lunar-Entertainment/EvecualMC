package com.evecual.evecualmc.entity;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.entity.ElectronicCombinerBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.GlassBlock;
import net.minecraft.block.StainedGlassBlock;
import net.minecraft.block.StainedGlassPaneBlock;
import net.minecraft.block.TintedGlassBlock;
import net.minecraft.block.entity.BlockEntity;
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
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CarEntity extends Entity {
    public static final int BASE_MAX_ENERGY = 1000;
    public static final int UPGRADED_MAX_ENERGY = 1500;

    private static final TrackedData<Integer> ENERGY = DataTracker.registerData(CarEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> COLOR_VARIANT = DataTracker.registerData(CarEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> GLASS_COLOR = DataTracker.registerData(CarEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> UPGRADED_ENGINE = DataTracker.registerData(CarEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> TRUNK_TIER = DataTracker.registerData(CarEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Float> STEERING_ANGLE = DataTracker.registerData(CarEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Boolean> PLUGGED_IN = DataTracker.registerData(CarEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private final SimpleInventory trunk = new SimpleInventory(27); // Supports up to 27 slots
    private BlockPos connectedExtensionPos = null;

    private boolean inputForward;
    private boolean inputBack;
    private boolean inputLeft;
    private boolean inputRight;
    private boolean inputSprint;

    private double currentSpeed = 0.0;
    private double angularVelocity = 0.0;
    private float wheelRoll = 0.0F;

    private boolean autoParking = false;
    private int autoParkPhase = 0;
    private int autoParkGraceTicks = 0;
    private double targetParkX, targetParkZ;
    private float targetParkYaw;
    private double entryApproachX, entryApproachZ;

    public float getWheelRoll() {
        return this.wheelRoll;
    }

    public boolean isAutoParking() {
        return this.autoParking;
    }

    public void startAutoPark(double px, double py, double pz, float pyaw, double ex, double ey, double ez) {
        this.autoParking = true;
        this.autoParkPhase = 0;
        this.autoParkGraceTicks = 15; // 15 ticks grace period so residual key presses don't cancel it
        this.inputForward = false;
        this.inputBack = false;
        this.inputLeft = false;
        this.inputRight = false;
        this.targetParkX = px;
        this.targetParkZ = pz;
        this.targetParkYaw = pyaw;
        this.entryApproachX = ex;
        this.entryApproachZ = ez;
    }

    public void cancelAutoPark(String message) {
        if (this.autoParking) {
            this.autoParking = false;
            this.autoParkGraceTicks = 0;
            if (this.getFirstPassenger() instanceof PlayerEntity player && message != null) {
                player.sendMessage(Text.literal(message), false);
            }
        }
    }

    public CarEntity(EntityType<? extends CarEntity> type, World world) {
        super(type, world);
        this.intersectionChecked = true;
        this.setStepHeight(1.0F);
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(ENERGY, 500);
        this.dataTracker.startTracking(COLOR_VARIANT, 0); // 0: Red, 1: Blue, 2: Black, 3: Lime, 4: White, 5: Yellow
        this.dataTracker.startTracking(GLASS_COLOR, 0);    // 0: Clear, 1: Smoked, 4: Red, etc.
        this.dataTracker.startTracking(UPGRADED_ENGINE, false);
        this.dataTracker.startTracking(TRUNK_TIER, 0);     // 0: Standard (18), 1: Upgraded (27)
        this.dataTracker.startTracking(STEERING_ANGLE, 0.0F);
        this.dataTracker.startTracking(PLUGGED_IN, false);
    }

    public boolean isPluggedIn() {
        return this.dataTracker.get(PLUGGED_IN);
    }

    public void setPluggedIn(BlockPos pos) {
        this.dataTracker.set(PLUGGED_IN, true);
        this.connectedExtensionPos = pos;
    }

    public double getCurrentSpeed() {
        return this.getVelocity().horizontalLength();
    }

    public void unplug() {
        this.dataTracker.set(PLUGGED_IN, false);
        this.connectedExtensionPos = null;
    }

    public BlockPos getConnectedExtensionPos() {
        return this.connectedExtensionPos;
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

    public int getTrunkTier() {
        return this.dataTracker.get(TRUNK_TIER);
    }

    public void setTrunkTier(int tier) {
        this.dataTracker.set(TRUNK_TIER, MathHelper.clamp(tier, 0, 1));
    }

    public float getSteeringAngle() {
        return this.dataTracker.get(STEERING_ANGLE);
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

    public void setInputs(boolean forward, boolean back, boolean left, boolean right, boolean sprint) {
        this.inputForward = forward;
        this.inputBack = back;
        this.inputLeft = left;
        this.inputRight = right;
        this.inputSprint = sprint;
    }

    public void openTrunk(PlayerEntity player) {
        if (!this.getWorld().isClient) {
            boolean isExpanded = getTrunkTier() == 1;
            player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                    (syncId, playerInventory, p) -> isExpanded
                            ? new GenericContainerScreenHandler(net.minecraft.screen.ScreenHandlerType.GENERIC_9X3, syncId, playerInventory, this.trunk, 3)
                            : new GenericContainerScreenHandler(net.minecraft.screen.ScreenHandlerType.GENERIC_9X2, syncId, playerInventory, this.trunk, 2),
                    Text.literal(isExpanded ? "Car Trunk (Expanded - 27 Slots)" : "Car Trunk (Standard - 18 Slots)")
            ));
        }
    }

    public void applyItemNbt(NbtCompound nbt) {
        if (nbt.contains("ColorVariant")) setColorVariant(nbt.getInt("ColorVariant"));
        if (nbt.contains("GlassColor")) setGlassColor(nbt.getInt("GlassColor"));
        if (nbt.contains("UpgradedEngine")) setUpgradedEngine(nbt.getBoolean("UpgradedEngine"));
        if (nbt.contains("TrunkTier")) setTrunkTier(nbt.getInt("TrunkTier"));
    }

    public ItemStack createCarDropItem() {
        ItemStack stack = new ItemStack(EvecualMC.CAR_ITEM);
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putInt("ColorVariant", getColorVariant());
        nbt.putInt("GlassColor", getGlassColor());
        nbt.putBoolean("UpgradedEngine", isUpgradedEngine());
        nbt.putInt("TrunkTier", getTrunkTier());
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
                player.sendMessage(Text.literal("§b⚡ Glass canopy tinted!"), true);
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
                player.sendMessage(Text.literal("§b⚡ Advanced Turbo Engine Installed! (+40% Boost Speed)"), true);
            }
            return ActionResult.SUCCESS;
        }

        // 4. Trunk Upgrade Application
        if ((held.isOf(EvecualMC.TRUNK_UPGRADE) || held.isOf(Items.CHEST)) && getTrunkTier() == 0) {
            if (!this.getWorld().isClient) {
                setTrunkTier(1);
                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.PLAYERS, 1.0F, 1.2F);
                if (!player.isCreative()) {
                    held.decrement(1);
                }
                player.sendMessage(Text.literal("§6📦 Trunk Expanded to 27 Slots!"), true);
            }
            return ActionResult.SUCCESS;
        }

        // 5. Right-click with Charger Cable in hand: attach to station and connect to car!
        if (held.isOf(EvecualMC.CHARGER_CABLE)) {
            if (!this.getWorld().isClient) {
                com.evecual.evecualmc.block.entity.ChargerExtensionBlockEntity nearbyExt = findNearbyExtension();
                if (nearbyExt != null) {
                    nearbyExt.setHasCable(true);
                    if (!player.isCreative()) {
                        held.decrement(1);
                    }
                    nearbyExt.connectCar(this);
                    this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, SoundCategory.PLAYERS, 1.0F, 1.8F);
                    player.sendMessage(Text.literal("§a⚡ Charger Cable attached and plugged into car! (Press X to disconnect)"), true);
                } else {
                    player.sendMessage(Text.literal("§c⚡ No Vehicle Charger Extension found within 16 blocks!"), true);
                }
            }
            return ActionResult.SUCCESS;
        }

        // 6. Unplugging charging cable with empty hand
        if (isPluggedIn() && held.isEmpty()) {
            if (!this.getWorld().isClient) {
                if (connectedExtensionPos != null) {
                    BlockEntity be = this.getWorld().getBlockEntity(connectedExtensionPos);
                    if (be instanceof com.evecual.evecualmc.block.entity.ChargerExtensionBlockEntity extension) {
                        extension.disconnectCar();
                    }
                }
                unplug();
                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.PLAYERS, 1.0F, 0.8F);
                player.sendMessage(Text.literal("§6⚡ Charging cable disconnected. Vehicle ready to drive!"), true);
            }
            return ActionResult.SUCCESS;
        }

        // 7. Sneak + Click: Pick up car & drop trunk contents
        if (player.isSneaking()) {
            if (!this.getWorld().isClient) {
                if (isPluggedIn() && connectedExtensionPos != null) {
                    BlockEntity be = this.getWorld().getBlockEntity(connectedExtensionPos);
                    if (be instanceof com.evecual.evecualmc.block.entity.ChargerExtensionBlockEntity extension) {
                        extension.disconnectCar();
                    }
                }
                dropTrunkContents();
                ItemEntity droppedCar = new ItemEntity(this.getWorld(), this.getX(), this.getY() + 0.5, this.getZ(), createCarDropItem());
                this.getWorld().spawnEntity(droppedCar);
                this.discard();
            }
            return ActionResult.SUCCESS;
        }

        // 8. Otherwise, right-clicking always enters the car to drive
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
            if (isPluggedIn() && connectedExtensionPos != null) {
                BlockEntity be = this.getWorld().getBlockEntity(connectedExtensionPos);
                if (be instanceof com.evecual.evecualmc.block.entity.ChargerExtensionBlockEntity extension) {
                    extension.disconnectCar();
                }
            }
            dropTrunkContents();
            ItemEntity droppedCar = new ItemEntity(this.getWorld(), this.getX(), this.getY() + 0.5, this.getZ(), createCarDropItem());
            this.getWorld().spawnEntity(droppedCar);
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
        boolean pluggedIn = isPluggedIn();

        if (passenger instanceof PlayerEntity player) {
            if (pluggedIn) {
                currentSpeed = 0.0;
                if ((inputForward || inputBack) && this.age % 20 == 0) {
                    player.sendMessage(Text.literal("§c⚡ Cannot drive: Charging cable plugged in! Press X to disconnect."), true);
                }
            }

            // 2. Target speed calculation with Upgraded Engine & CTRL Boost
            double targetSpeed = 0.0;
            double normalMax = isUpgradedEngine() ? 0.72 : 0.52;
            double boostMax = isUpgradedEngine() ? 1.35 : 1.00;

            if (hasPower && !pluggedIn) {
                if (inputForward) {
                    targetSpeed = inputSprint ? boostMax : normalMax;
                } else if (inputBack) {
                    targetSpeed = -0.28;
                }
            }

            // Auto-Parking Controller
            double autoParkAngular = 0.0;
            boolean isSteeringAutoPark = false;
            if (autoParking) {
                if (autoParkGraceTicks > 0) {
                    autoParkGraceTicks--;
                } else if (inputForward || inputBack || inputLeft || inputRight) {
                    cancelAutoPark("§e🅿️ Auto-parking cancelled by driver.");
                }

                if (autoParking) {
                    double destX = (autoParkPhase == 0) ? entryApproachX : targetParkX;
                    double destZ = (autoParkPhase == 0) ? entryApproachZ : targetParkZ;

                    double dx = destX - this.getX();
                    double dz = destZ - this.getZ();
                    double dist = Math.sqrt(dx * dx + dz * dz);

                    if (autoParkPhase == 0) {
                        if (dist < 1.2) {
                            autoParkPhase = 1;
                        } else {
                            targetSpeed = 0.28;
                            float desiredYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
                            float diff = MathHelper.wrapDegrees(desiredYaw - this.getYaw());
                            autoParkAngular = MathHelper.clamp(diff * 0.20, -7.0, 7.0);
                            isSteeringAutoPark = true;
                        }
                    } else if (autoParkPhase == 1) {
                        if (dist < 0.25) {
                            autoParkPhase = 2;
                        } else {
                            targetSpeed = 0.16;
                            float desiredYaw = targetParkYaw;
                            float diff = MathHelper.wrapDegrees(desiredYaw - this.getYaw());
                            autoParkAngular = MathHelper.clamp(diff * 0.20, -6.0, 6.0);
                            isSteeringAutoPark = true;
                        }
                    } else if (autoParkPhase == 2) {
                        targetSpeed = 0.0;
                        autoParkAngular = 0.0;
                        this.setYaw(targetParkYaw);
                        this.prevYaw = targetParkYaw;
                        this.currentSpeed = 0.0;
                        this.autoParking = false;
                        this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                                net.minecraft.sound.SoundEvents.BLOCK_IRON_DOOR_CLOSE, net.minecraft.sound.SoundCategory.PLAYERS, 0.8F, 1.2F);
                        player.sendMessage(Text.literal("§a🅿️ Vehicle parked successfully! Ready for charging."), false);
                    }
                }
            }

            // 3. Smooth continuous acceleration & braking
            double accel = (targetSpeed > currentSpeed) ? 0.06 : 0.12;
            currentSpeed = MathHelper.stepTowards((float) currentSpeed, (float) targetSpeed, (float) accel);

            // 4. Power consumption & Turbo Particles
            if (Math.abs(currentSpeed) > 0.02 && !this.getWorld().isClient) {
                int drainInterval = inputSprint ? 8 : 20;
                if (this.age % drainInterval == 0) {
                    setEnergy(Math.max(0, energy - 1));
                }

                if (inputSprint && this.age % 2 == 0 && this.getWorld() instanceof ServerWorld serverWorld) {
                    double radHeading = Math.toRadians(this.getYaw());
                    double rearX = this.getX() + Math.sin(radHeading) * 1.5;
                    double rearY = this.getY() + 0.3;
                    double rearZ = this.getZ() - Math.cos(radHeading) * 1.5;
                    serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, rearX, rearY, rearZ, 2, 0.1, 0.05, 0.1, 0.02);
                }
            }

            // 5. Angular Steering Physics & Decreased Turn Radius
            double targetAngular = 0.0;
            if (isSteeringAutoPark) {
                targetAngular = autoParkAngular;
            } else if (Math.abs(currentSpeed) > 0.01) {
                double turnSensitivity = Math.max(0.65, Math.min(1.0, Math.abs(currentSpeed) / 0.25));
                double baseTurnRate = 7.5 * turnSensitivity;
                double turnDir = (currentSpeed < 0) ? -1.0 : 1.0;

                if (inputLeft) {
                    targetAngular = -baseTurnRate * turnDir;
                } else if (inputRight) {
                    targetAngular = baseTurnRate * turnDir;
                }
            }

            angularVelocity = MathHelper.stepTowards((float) angularVelocity, (float) targetAngular, 1.4F);
            float candidateYaw = (float) (this.getYaw() + angularVelocity);
            if (canRotateTo(candidateYaw)) {
                this.prevYaw = this.getYaw();
                this.setYaw(candidateYaw);
            } else {
                angularVelocity = 0.0;
            }

            // 6. Smooth steering wheel angle up to 45 degrees (0.785 rad)
            float targetSteer = 0.0F;
            if (inputLeft) targetSteer = -0.785F;
            else if (inputRight) targetSteer = 0.785F;
            float currentSteer = getSteeringAngle();
            this.dataTracker.set(STEERING_ANGLE, MathHelper.stepTowards(currentSteer, targetSteer, 0.22F));
        } else {
            currentSpeed = MathHelper.stepTowards((float) currentSpeed, 0.0F, 0.08F);
            angularVelocity = MathHelper.stepTowards((float) angularVelocity, 0.0F, 0.8F);
            float candidateYaw = (float) (this.getYaw() + angularVelocity);
            if (canRotateTo(candidateYaw)) {
                this.prevYaw = this.getYaw();
                this.setYaw(candidateYaw);
            } else {
                angularVelocity = 0.0;
            }
            this.dataTracker.set(STEERING_ANGLE, MathHelper.stepTowards(getSteeringAngle(), 0.0F, 0.1F));
        }

        // Always update bounding box BEFORE move!
        this.setBoundingBox(this.calculateBoundingBox());

        // 7. Apply velocity smoothly along vehicle heading
        double rad = Math.toRadians(this.getYaw());
        double vx = -Math.sin(rad) * currentSpeed;
        double vz = Math.cos(rad) * currentSpeed;
        this.setVelocity(vx, this.getVelocity().y, vz);

        // 8. Continuous Wheel Rolling Animation around axle
        double actualSpeed = Math.sqrt(vx * vx + vz * vz);
        if (actualSpeed > 0.005) {
            double dir = (currentSpeed < 0) ? -1.0 : 1.0;
            this.wheelRoll += (float) (actualSpeed * dir * 2.5);
        }

        this.move(MovementType.SELF, this.getVelocity());
        if (this.horizontalCollision) {
            this.currentSpeed = 0.0;
        }
    }

    private boolean canRotateTo(float candidateYaw) {
        float oldYaw = this.getYaw();
        Box oldBox = this.getBoundingBox();
        this.setYaw(candidateYaw);
        Box newBox = this.calculateBoundingBox();
        boolean spaceEmpty = this.getWorld().isSpaceEmpty(this, newBox);
        this.setYaw(oldYaw);
        this.setBoundingBox(oldBox);
        return spaceEmpty;
    }

    @Override
    public Box calculateBoundingBox() {
        double rad = Math.toRadians(this.getYaw());
        double halfLen = 1.45; // 2.9 blocks length
        double halfWid = 0.95; // 1.9 blocks width (fits through 2-wide parking bays and garage doors)
        double extX = Math.abs(Math.cos(rad)) * halfWid + Math.abs(Math.sin(rad)) * halfLen;
        double extZ = Math.abs(Math.sin(rad)) * halfWid + Math.abs(Math.cos(rad)) * halfLen;
        return new Box(
                this.getX() - extX, this.getY(), this.getZ() - extZ,
                this.getX() + extX, this.getY() + 1.85, this.getZ() + extZ
        );
    }

    protected void clampPassengerYaw(Entity passenger) {
        passenger.setBodyYaw(this.getYaw());
        float f = MathHelper.wrapDegrees(passenger.getYaw() - this.getYaw());
        float g = MathHelper.clamp(f, -110.0F, 110.0F);
        passenger.prevYaw += g - f;
        passenger.setYaw(passenger.getYaw() + g - f);
        passenger.setHeadYaw(passenger.getYaw());
    }

    @Override
    protected void updatePassengerPosition(Entity passenger, PositionUpdater positionUpdater) {
        if (this.hasPassenger(passenger)) {
            double forwardOffset = -0.10;
            double leftOffset = 0.00;
            double heightOffset = 0.12;

            float rad = (float) Math.toRadians(this.getYaw());
            double worldX = this.getX() - Math.sin(rad) * forwardOffset + Math.cos(rad) * leftOffset;
            double worldY = this.getY() + heightOffset;
            double worldZ = this.getZ() + Math.cos(rad) * forwardOffset + Math.sin(rad) * leftOffset;

            positionUpdater.accept(passenger, worldX, worldY, worldZ);

            float deltaYaw = this.getYaw() - this.prevYaw;
            passenger.setYaw(passenger.getYaw() + deltaYaw);
            clampPassengerYaw(passenger);
        }
    }

    @Override
    public Vec3d updatePassengerForDismount(LivingEntity passenger) {
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
        if (nbt.contains("ColorVariant")) {
            setColorVariant(nbt.getInt("ColorVariant"));
        }
        if (nbt.contains("GlassColor")) {
            setGlassColor(nbt.getInt("GlassColor"));
        }
        if (nbt.contains("UpgradedEngine")) {
            setUpgradedEngine(nbt.getBoolean("UpgradedEngine"));
        }
        if (nbt.contains("TrunkTier")) {
            setTrunkTier(nbt.getInt("TrunkTier"));
        }
        if (nbt.contains("TrunkItems")) {
            DefaultedList<ItemStack> list = DefaultedList.ofSize(trunk.size(), ItemStack.EMPTY);
            Inventories.readNbt(nbt.getCompound("TrunkItems"), list);
            for (int i = 0; i < list.size(); ++i) {
                trunk.setStack(i, list.get(i));
            }
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("Energy", getEnergy());
        nbt.putInt("ColorVariant", getColorVariant());
        nbt.putInt("GlassColor", getGlassColor());
        nbt.putBoolean("UpgradedEngine", isUpgradedEngine());
        nbt.putInt("TrunkTier", getTrunkTier());

        DefaultedList<ItemStack> list = DefaultedList.ofSize(trunk.size(), ItemStack.EMPTY);
        for (int i = 0; i < trunk.size(); ++i) {
            list.set(i, trunk.getStack(i));
        }
        NbtCompound trunkNbt = new NbtCompound();
        Inventories.writeNbt(trunkNbt, list);
        nbt.put("TrunkItems", trunkNbt);
    }

    @Nullable
    public com.evecual.evecualmc.block.entity.ChargerExtensionBlockEntity findNearbyExtension() {
        BlockPos carPos = this.getBlockPos();
        com.evecual.evecualmc.block.entity.ChargerExtensionBlockEntity best = null;
        double bestDist = Double.MAX_VALUE;

        for (BlockPos p : BlockPos.iterate(carPos.add(-16, -5, -16), carPos.add(16, 5, 16))) {
            BlockEntity be = this.getWorld().getBlockEntity(p);
            if (be instanceof com.evecual.evecualmc.block.entity.ChargerExtensionBlockEntity ext) {
                double d = this.squaredDistanceTo(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
                if (d < bestDist) {
                    bestDist = d;
                    best = ext;
                }
            }
        }
        return best;
    }
}
