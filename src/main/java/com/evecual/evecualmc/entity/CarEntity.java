package com.evecual.evecualmc.entity;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.screen.CarTrunkScreenHandler;
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
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CarEntity extends Entity {
    public static final int MAX_ENERGY = 1000;

    private static final TrackedData<Integer> ENERGY = DataTracker.registerData(CarEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> COLOR_VARIANT = DataTracker.registerData(CarEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Float> STEERING_ANGLE = DataTracker.registerData(CarEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Boolean> PLUGGED_IN = DataTracker.registerData(CarEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private final SimpleInventory trunk = new SimpleInventory(18);
    private BlockPos connectedExtensionPos = null;

    private boolean inputForward;
    private boolean inputBack;
    private boolean inputLeft;
    private boolean inputRight;
    private boolean inputSprint;

    private double currentSpeed = 0.0;
    private double angularVelocity = 0.0;

    public CarEntity(EntityType<? extends CarEntity> type, World world) {
        super(type, world);
        this.intersectionChecked = true;
        this.setStepHeight(1.0F);
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(ENERGY, 500);
        this.dataTracker.startTracking(COLOR_VARIANT, 0); // 0: Red, 1: Blue, 2: Black, 3: Lime, 4: White, 5: Yellow
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
        this.dataTracker.set(ENERGY, MathHelper.clamp(energy, 0, MAX_ENERGY));
    }

    public int getMaxEnergy() {
        return MAX_ENERGY;
    }

    public int getColorVariant() {
        return this.dataTracker.get(COLOR_VARIANT);
    }

    public void setColorVariant(int variant) {
        this.dataTracker.set(COLOR_VARIANT, MathHelper.clamp(variant, 0, 5));
    }

    public float getSteeringAngle() {
        return this.dataTracker.get(STEERING_ANGLE);
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

    public void openTrunk(PlayerEntity player) {
        if (!this.getWorld().isClient) {
            player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                    (syncId, playerInventory, p) -> new CarTrunkScreenHandler(syncId, playerInventory, this.trunk),
                    Text.translatable("container.evecualmc.car_trunk")
            ));
        }
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

        // 2. Plugging in unholstered charging cable
        if (com.evecual.evecualmc.block.ChargerExtensionBlock.PENDING_CABLES.containsKey(player.getUuid())) {
            BlockPos extPos = com.evecual.evecualmc.block.ChargerExtensionBlock.PENDING_CABLES.remove(player.getUuid());
            if (this.squaredDistanceTo(extPos.toCenterPos()) <= 100.0) {
                BlockEntity be = this.getWorld().getBlockEntity(extPos);
                if (be instanceof com.evecual.evecualmc.block.entity.ChargerExtensionBlockEntity extension) {
                    if (!this.getWorld().isClient) {
                        extension.connectCar(this);
                        this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                                SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, SoundCategory.PLAYERS, 1.0F, 1.8F);
                        player.sendMessage(Text.literal("§a⚡ Vehicle plugged in! Charging from station... (Right-click to unplug)"), true);
                    }
                    return ActionResult.SUCCESS;
                }
            } else {
                if (!this.getWorld().isClient) {
                    player.sendMessage(Text.literal("§c⚡ Too far from charging station!"), true);
                }
                return ActionResult.SUCCESS;
            }
        }

        // 3. Unplugging charging cable with empty hand
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

        // 4. Sneak + Click: Pick up car & drop trunk contents
        if (player.isSneaking()) {
            if (!this.getWorld().isClient) {
                if (isPluggedIn() && connectedExtensionPos != null) {
                    BlockEntity be = this.getWorld().getBlockEntity(connectedExtensionPos);
                    if (be instanceof com.evecual.evecualmc.block.entity.ChargerExtensionBlockEntity extension) {
                        extension.disconnectCar();
                    }
                }
                dropTrunkContents();
                this.dropItem(EvecualMC.CAR_ITEM);
                this.discard();
            }
            return ActionResult.SUCCESS;
        }

        // 5. Right clicking the rear/trunk: Open trunk inventory
        Vec3d toPlayer = player.getPos().subtract(this.getPos());
        double rad = Math.toRadians(this.getYaw());
        // Forward vector: -sin(rad), cos(rad). Rear is opposite: sin(rad), -cos(rad).
        double dotRear = toPlayer.x * Math.sin(rad) - toPlayer.z * Math.cos(rad);

        if (dotRear > 0.4 && !this.hasPassenger(player)) {
            openTrunk(player);
            return ActionResult.SUCCESS;
        }

        // 6. Otherwise, enter car to drive
        if (!this.getWorld().isClient) {
            player.startRiding(this);
        }
        return ActionResult.SUCCESS;
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
        boolean pluggedIn = isPluggedIn();

        if (passenger instanceof PlayerEntity player) {
            // Check if plugged in: cannot move!
            if (pluggedIn) {
                currentSpeed = 0.0;
                if ((inputForward || inputBack) && this.age % 20 == 0) {
                    player.sendMessage(Text.literal("§c⚡ Cannot drive: Charging cable plugged in! Right-click vehicle to disconnect."), true);
                }
            }

            // 2. Target speed calculation with CTRL Boost
            double targetSpeed = 0.0;
            if (hasPower && !pluggedIn) {
                if (inputForward) {
                    targetSpeed = inputSprint ? 1.0 : 0.52;
                } else if (inputBack) {
                    targetSpeed = -0.25;
                }
            }

            // 3. Smooth vehicular acceleration and coasting friction
            if (targetSpeed > currentSpeed) {
                currentSpeed = Math.min(targetSpeed, currentSpeed + 0.04);
            } else if (targetSpeed < currentSpeed) {
                currentSpeed = Math.max(targetSpeed, currentSpeed - 0.06);
            }

            // 4. Smooth Automotive Steering Physics (Continuous Damped Angular Velocity)
            float targetSteer = 0.0F;
            if (inputLeft) targetSteer -= 0.50F;
            if (inputRight) targetSteer += 0.50F;

            float currentSteer = this.getSteeringAngle();
            currentSteer += (targetSteer - currentSteer) * 0.35F;
            this.dataTracker.set(STEERING_ANGLE, currentSteer);

            // Responsive turn rate scaling: agile steering while moving, smooth pivot when starting
            double speedAbs = Math.abs(currentSpeed);
            double targetTurnRate = 0.0;
            if (speedAbs > 0.01 || inputForward || inputBack) {
                double speedFactor = Math.min(1.0, speedAbs / 0.30);
                double maxTurn = 3.6 + 2.8 * speedFactor; // 3.6 to 6.4 deg/tick
                if (inputLeft) targetTurnRate -= maxTurn;
                if (inputRight) targetTurnRate += maxTurn;
            }

            // Smooth angular acceleration & damping (automotive inertia)
            angularVelocity += (targetTurnRate - angularVelocity) * 0.32;

            if (Math.abs(angularVelocity) > 0.02) {
                float dir = currentSpeed >= 0 ? 1.0F : -1.0F;
                this.setYaw(this.getYaw() + (float)(angularVelocity * dir));
            }

            // 5. Energy Consumption (Server authoritative)
            if (!this.getWorld().isClient && Math.abs(currentSpeed) > 0.04 && energy > 0) {
                int drainTicks = inputSprint ? 1 : 3;
                if (this.age % drainTicks == 0) {
                    setEnergy(energy - 1);
                }
            }

            // 6. Visual drive & boost exhaust particles
            if (this.getWorld().isClient && Math.abs(currentSpeed) > 0.04 && energy > 0) {
                double rad = Math.toRadians(this.getYaw());
                if (inputSprint && inputForward) {
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
            angularVelocity *= 0.7;
            this.dataTracker.set(STEERING_ANGLE, 0.0F);
        }

        // 7. Apply velocity smoothly along vehicle heading
        double rad = Math.toRadians(this.getYaw());
        double vx = -Math.sin(rad) * currentSpeed;
        double vz = Math.cos(rad) * currentSpeed;
        this.setVelocity(vx, this.getVelocity().y, vz);

        this.move(MovementType.SELF, this.getVelocity());
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
        DefaultedList<ItemStack> list = DefaultedList.ofSize(trunk.size(), ItemStack.EMPTY);
        for (int i = 0; i < trunk.size(); ++i) {
            list.set(i, trunk.getStack(i));
        }
        NbtCompound trunkNbt = new NbtCompound();
        Inventories.writeNbt(trunkNbt, list);
        nbt.put("TrunkItems", trunkNbt);
    }
}
