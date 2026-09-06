package com.evecual.evecualmc.entity;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.block.HeliChargerBlock;
import com.evecual.evecualmc.block.HeliChargerPart;
import com.evecual.evecualmc.block.entity.ElectronicCombinerBlockEntity;
import com.evecual.evecualmc.screen.HeliUpgradeScreenHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
    private static final TrackedData<Boolean> AUTO_RETURNING = DataTracker.registerData(HeliEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> LEFT_ARM = DataTracker.registerData(HeliEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> RIGHT_ARM = DataTracker.registerData(HeliEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> STORAGE_UNLOCKED = DataTracker.registerData(HeliEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> MAX_ENERGY_CAP = DataTracker.registerData(HeliEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<String> PAIRED_PLAYER_UUID = DataTracker.registerData(HeliEntity.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<Boolean> ACTIVE_RC_LINK = DataTracker.registerData(HeliEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private final SimpleInventory trunk = new SimpleInventory(27);
    private final SimpleInventory upgrades = new SimpleInventory(5);

    private boolean inputForward;
    private boolean inputBack;
    private boolean inputLeft;
    private boolean inputRight;
    private boolean inputUp;
    private boolean inputDown;
    private boolean inputSprint;

    private int remoteControlTimeout = 0;

    private BlockPos targetHelipadPos = null;
    private int autoReturnStage = 0;
    private int autoReturnTicks = 0;
    private int autoParkGraceTicks = 0;
    private double cruiseAltitude = 0.0;

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
        this.dataTracker.startTracking(AUTO_RETURNING, false);
        this.dataTracker.startTracking(LEFT_ARM, 0); // 0: None, 1: Mining Arm, 2: Weapon Arm
        this.dataTracker.startTracking(RIGHT_ARM, 0);
        this.dataTracker.startTracking(STORAGE_UNLOCKED, false);
        this.dataTracker.startTracking(MAX_ENERGY_CAP, BASE_MAX_ENERGY);
        this.dataTracker.startTracking(PAIRED_PLAYER_UUID, "");
        this.dataTracker.startTracking(ACTIVE_RC_LINK, false);
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
        return this.dataTracker.get(MAX_ENERGY_CAP);
    }

    public void setMaxEnergyCap(int cap) {
        this.dataTracker.set(MAX_ENERGY_CAP, Math.max(BASE_MAX_ENERGY, cap));
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

    public int getLeftArmType() {
        return this.dataTracker.get(LEFT_ARM);
    }

    public void setLeftArmType(int arm) {
        this.dataTracker.set(LEFT_ARM, arm);
    }

    public int getRightArmType() {
        return this.dataTracker.get(RIGHT_ARM);
    }

    public void setRightArmType(int arm) {
        this.dataTracker.set(RIGHT_ARM, arm);
    }

    public boolean isStorageUnlocked() {
        return this.dataTracker.get(STORAGE_UNLOCKED);
    }

    public void setStorageUnlocked(boolean unlocked) {
        this.dataTracker.set(STORAGE_UNLOCKED, unlocked);
    }

    public String getPairedPlayerUuid() {
        return this.dataTracker.get(PAIRED_PLAYER_UUID);
    }

    public void setPairedPlayerUuid(String uuid) {
        this.dataTracker.set(PAIRED_PLAYER_UUID, uuid != null ? uuid : "");
    }

    public boolean hasActiveRcLink() {
        return this.dataTracker.get(ACTIVE_RC_LINK);
    }

    public void setActiveRcLink(boolean active) {
        this.dataTracker.set(ACTIVE_RC_LINK, active);
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

    public SimpleInventory getUpgrades() {
        return this.upgrades;
    }

    public SimpleInventory getTrunk() {
        return this.trunk;
    }

    public void syncUpgrades() {
        // Slot 0: Speed Mod (Turbo Engine)
        ItemStack engine = upgrades.getStack(0);
        boolean isUpgraded = engine.isOf(EvecualMC.UPGRADED_ENGINE);
        setUpgradedEngine(isUpgraded);

        // Slot 1: Cargo Mod (Trunk Upgrade)
        ItemStack trunkStack = upgrades.getStack(1);
        setStorageUnlocked(!trunkStack.isEmpty() && trunkStack.isOf(EvecualMC.TRUNK_UPGRADE));

        // Slot 2: Energy Storage Mod (Battery Item)
        ItemStack batteryStack = upgrades.getStack(2);
        int batteryCount = batteryStack.isOf(EvecualMC.BATTERY_ITEM) ? batteryStack.getCount() : 0;
        int baseCap = isUpgraded ? UPGRADED_MAX_ENERGY : BASE_MAX_ENERGY;
        int totalMaxEnergy = baseCap + (batteryCount * 1000);
        setMaxEnergyCap(totalMaxEnergy);

        // Slot 3: Left Wing Arm Hardpoint
        ItemStack leftArm = upgrades.getStack(3);
        int leftType = leftArm.isOf(EvecualMC.HELI_MINING_ARM) ? 1 : leftArm.isOf(EvecualMC.HELI_WEAPON_ARM) ? 2 : 0;
        setLeftArmType(leftType);

        // Slot 4: Right Wing Arm Hardpoint
        ItemStack rightArm = upgrades.getStack(4);
        int rightType = rightArm.isOf(EvecualMC.HELI_MINING_ARM) ? 1 : rightArm.isOf(EvecualMC.HELI_WEAPON_ARM) ? 2 : 0;
        setRightArmType(rightType);
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

    public void setRemoteInputs(boolean forward, boolean back, boolean left, boolean right, boolean up, boolean down, boolean sprint) {
        setInputs(forward, back, left, right, up, down, sprint);
        this.remoteControlTimeout = 10;
    }

    public void setRemoteYaw(float yaw) {
        this.prevYaw = this.getYaw();
        this.setYaw(yaw);
        this.setBodyYaw(yaw);
        this.setHeadYaw(yaw);
    }

    public void openTrunk(PlayerEntity player) {
        if (!this.getWorld().isClient) {
            if (!isStorageUnlocked() && !player.isCreative() && isTrunkEmpty()) {
                player.sendMessage(Text.literal("§c📦 Cargo Bay Locked! Install a Trunk Upgrade via Heli Upgrade Terminal [X]."), true);
                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.PLAYERS, 0.8F, 1.2F);
                return;
            }

            player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                    (syncId, playerInventory, p) -> new GenericContainerScreenHandler(
                            net.minecraft.screen.ScreenHandlerType.GENERIC_9X3, syncId, playerInventory, this.trunk, 3),
                    Text.literal("EV Heli Cargo (27 Slots)")
            ));
        }
    }

    private boolean isTrunkEmpty() {
        for (int i = 0; i < trunk.size(); i++) {
            if (!trunk.getStack(i).isEmpty()) return false;
        }
        return true;
    }

    public void openUpgradeScreen(PlayerEntity player) {
        if (!this.getWorld().isClient && player instanceof ServerPlayerEntity sp) {
            sp.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                    (syncId, playerInventory, p) -> new HeliUpgradeScreenHandler(syncId, playerInventory, this.upgrades, this),
                    Text.literal("EV Heli Upgrade Terminal")
            ));
        }
    }

    public void performArmAction(PlayerEntity player, int armIndex) {
        if (this.getWorld().isClient) return;

        int energy = getEnergy();
        if (energy < 1) {
            player.sendMessage(Text.literal("§c⚡ Heli Energy too low to fire hardpoint arm!"), true);
            return;
        }

        int leftArm = getLeftArmType();
        int rightArm = getRightArmType();

        if (armIndex == 0) { // Left Arm [ I Key ]
            if (leftArm == 0) {
                player.sendMessage(Text.literal("§e⚠️ No Left Wing Arm installed! Press [X] looking at Heli to install in Left Arm slot."), true);
                return;
            }
            setEnergy(energy - 1);
            double radYaw = Math.toRadians(this.getYaw());
            double curPitch = this.getPitchTilt();
            double radPitch = Math.toRadians(curPitch);

            Vec3d forwardDir = new Vec3d(-Math.sin(radYaw) * Math.cos(radPitch), -Math.sin(radPitch), Math.cos(radYaw) * Math.cos(radPitch)).normalize();
            Vec3d rightDir = new Vec3d(Math.cos(radYaw), 0, Math.sin(radYaw)).normalize();
            Vec3d leftArmPos = this.getPos().add(0, 0.6, 0).subtract(rightDir.multiply(1.3)).add(forwardDir.multiply(0.8));

            if (leftArm == 1) fireMiningBeam(player, leftArmPos, forwardDir);
            else if (leftArm == 2) fireWeaponPlasma(player, leftArmPos, forwardDir);
        } else { // Right Arm [ O Key ]
            if (rightArm == 0) {
                player.sendMessage(Text.literal("§e⚠️ No Right Wing Arm installed! Press [X] looking at Heli to install in Right Arm slot."), true);
                return;
            }
            setEnergy(energy - 1);
            double radYaw = Math.toRadians(this.getYaw());
            double curPitch = this.getPitchTilt();
            double radPitch = Math.toRadians(curPitch);

            Vec3d forwardDir = new Vec3d(-Math.sin(radYaw) * Math.cos(radPitch), -Math.sin(radPitch), Math.cos(radYaw) * Math.cos(radPitch)).normalize();
            Vec3d rightDir = new Vec3d(Math.cos(radYaw), 0, Math.sin(radYaw)).normalize();
            Vec3d rightArmPos = this.getPos().add(0, 0.6, 0).add(rightDir.multiply(1.3)).add(forwardDir.multiply(0.8));

            if (rightArm == 1) fireMiningBeam(player, rightArmPos, forwardDir);
            else if (rightArm == 2) fireWeaponPlasma(player, rightArmPos, forwardDir);
        }
    }

    private void fireMiningBeam(PlayerEntity player, Vec3d startPos, Vec3d dir) {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return;

        double maxDist = 16.0;
        Vec3d endPos = startPos.add(dir.multiply(maxDist));
        BlockHitResult hit = this.getWorld().raycast(new RaycastContext(
                startPos, endPos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, this));

        // Beam particle trail
        for (double d = 0.5; d < maxDist; d += 0.8) {
            Vec3d p = startPos.add(dir.multiply(d));
            serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, p.x, p.y, p.z, 1, 0.02, 0.02, 0.02, 0.01);
            if (hit.getType() != HitResult.Type.MISS && p.squaredDistanceTo(hit.getPos()) < 1.0) {
                break;
            }
        }

        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = hit.getBlockPos();
            BlockState state = serverWorld.getBlockState(pos);
            float hardness = state.getHardness(serverWorld, pos);

            if (hardness >= 0 && hardness <= 50.0F && !state.isAir()) {
                serverWorld.spawnParticles(ParticleTypes.CRIT, hit.getPos().x, hit.getPos().y, hit.getPos().z, 6, 0.15, 0.15, 0.15, 0.05);
                serverWorld.playSound(null, pos, SoundEvents.BLOCK_ANVIL_HIT, SoundCategory.BLOCKS, 0.6F, 1.8F);

                List<ItemStack> drops = Block.getDroppedStacks(state, serverWorld, pos, null, player, new ItemStack(Items.DIAMOND_PICKAXE));
                serverWorld.breakBlock(pos, false, player);

                for (ItemStack drop : drops) {
                    if (isStorageUnlocked()) {
                        ItemStack remainder = this.trunk.addStack(drop);
                        if (!remainder.isEmpty()) {
                            serverWorld.spawnEntity(new ItemEntity(serverWorld, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, remainder));
                        }
                    } else {
                        serverWorld.spawnEntity(new ItemEntity(serverWorld, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop));
                    }
                }
            }
        }
    }

    private void fireWeaponPlasma(PlayerEntity player, Vec3d startPos, Vec3d dir) {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return;

        double maxDist = 48.0;
        Vec3d endPos = startPos.add(dir.multiply(maxDist));

        // Raycast entities
        Box searchBox = new Box(startPos, endPos).expand(1.8);
        List<Entity> targets = serverWorld.getOtherEntities(this, searchBox, e -> e instanceof LivingEntity && e != player && e.isAlive());

        LivingEntity hitEntity = null;
        double closestDistSq = Double.MAX_VALUE;

        for (Entity e : targets) {
            Box bbox = e.getBoundingBox().expand(0.4);
            if (bbox.raycast(startPos, endPos).isPresent()) {
                double dSq = startPos.squaredDistanceTo(e.getPos());
                if (dSq < closestDistSq) {
                    closestDistSq = dSq;
                    hitEntity = (LivingEntity) e;
                }
            }
        }

        double impactDist = hitEntity != null ? Math.sqrt(closestDistSq) : maxDist;

        // Rotary minigun muzzle flash particles & rocket blast trail
        serverWorld.spawnParticles(ParticleTypes.FIREWORK, startPos.x, startPos.y, startPos.z, 4, 0.08, 0.08, 0.08, 0.05);
        serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, startPos.x, startPos.y, startPos.z, 6, 0.1, 0.1, 0.1, 0.08);

        // High-speed kinetic plasma tracer beam
        for (double d = 0.4; d < impactDist; d += 0.5) {
            Vec3d p = startPos.add(dir.multiply(d));
            serverWorld.spawnParticles(ParticleTypes.SONIC_BOOM, p.x, p.y, p.z, 1, 0, 0, 0, 0);
            serverWorld.spawnParticles(ParticleTypes.CRIT, p.x, p.y, p.z, 1, 0.03, 0.03, 0.03, 0.02);
        }

        // Heavy Vulcan Minigun assault audio
        serverWorld.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 0.5F, 1.85F);
        serverWorld.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, SoundCategory.PLAYERS, 0.7F, 1.6F);

        if (hitEntity != null) {
            DamageSource ds = serverWorld.getDamageSources().playerAttack(player);
            hitEntity.damage(ds, 14.0F);
            serverWorld.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, hitEntity.getX(), hitEntity.getY() + 0.5, hitEntity.getZ(), 1, 0, 0, 0, 0);
            serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, hitEntity.getX(), hitEntity.getY() + 0.5, hitEntity.getZ(), 10, 0.2, 0.2, 0.2, 0.1);
        }
    }

    public void applyItemNbt(NbtCompound nbt) {
        if (nbt.contains("ColorVariant")) setColorVariant(nbt.getInt("ColorVariant"));
        if (nbt.contains("GlassColor")) setGlassColor(nbt.getInt("GlassColor"));
        if (nbt.contains("UpgradedEngine")) setUpgradedEngine(nbt.getBoolean("UpgradedEngine"));
        if (nbt.contains("Energy")) setEnergy(nbt.getInt("Energy"));
        if (nbt.contains("Upgrades", 10)) upgrades.readNbtList(nbt.getList("Upgrades", 10));
        syncUpgrades();
    }

    public ItemStack createHeliDropItem() {
        ItemStack stack = new ItemStack(EvecualMC.HELI_ITEM);
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putInt("ColorVariant", getColorVariant());
        nbt.putInt("GlassColor", getGlassColor());
        nbt.putBoolean("UpgradedEngine", isUpgradedEngine());
        nbt.putInt("Energy", getEnergy());
        nbt.put("Upgrades", upgrades.toNbtList());
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

        // 3. Engine Upgrade Application directly via item
        if (held.isOf(EvecualMC.UPGRADED_ENGINE) && !isUpgradedEngine()) {
            if (!this.getWorld().isClient) {
                upgrades.setStack(0, held.split(1));
                syncUpgrades();
                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.BLOCK_ANVIL_USE, SoundCategory.PLAYERS, 1.0F, 1.4F);
                player.sendMessage(Text.literal("§b⚡ High-Power Turbine Upgrade Installed! (+50% Battery & Efficiency)"), true);
            }
            return ActionResult.SUCCESS;
        }

        // 4. Sneak + Click: Pick up EV Heli
        if (player.isSneaking()) {
            if (!this.getWorld().isClient) {
                dropTrunkContents();
                dropUpgradesContents();
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

    private void dropUpgradesContents() {
        for (int i = 0; i < upgrades.size(); ++i) {
            ItemStack stack = upgrades.getStack(i);
            if (!stack.isEmpty()) {
                ItemEntity itemEntity = new ItemEntity(this.getWorld(), this.getX(), this.getY() + 0.5, this.getZ(), stack.copy());
                this.getWorld().spawnEntity(itemEntity);
                upgrades.setStack(i, ItemStack.EMPTY);
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
            dropUpgradesContents();
            ItemEntity dropped = new ItemEntity(this.getWorld(), this.getX(), this.getY() + 0.5, this.getZ(), createHeliDropItem());
            this.getWorld().spawnEntity(dropped);
            this.discard();
        }
        return true;
    }

    protected void clampPassengerYaw(Entity passenger) {
        passenger.setBodyYaw(this.getYaw());
        float f = MathHelper.wrapDegrees(passenger.getYaw() - this.getYaw());
        float g = MathHelper.clamp(f, -120.0F, 120.0F);
        passenger.prevYaw += g - f;
        passenger.setYaw(passenger.getYaw() + g - f);
        passenger.setHeadYaw(passenger.getYaw());
    }

    @Override
    protected void updatePassengerPosition(Entity passenger, PositionUpdater positionUpdater) {
        if (this.hasPassenger(passenger)) {
            double radYaw = Math.toRadians(this.getYaw());
            double curPitch = this.dataTracker.get(PITCH_TILT);
            double radPitch = Math.toRadians(curPitch);

            double localZ = 0.85; // forward in cockpit
            double localY = 0.40; // seat height

            double tiltedZ = localZ * Math.cos(radPitch) - localY * Math.sin(radPitch);
            double tiltedY = localY * Math.cos(radPitch) + localZ * Math.sin(radPitch);

            double px = this.getX() - Math.sin(radYaw) * tiltedZ;
            double py = this.getY() + tiltedY;
            double pz = this.getZ() + Math.cos(radYaw) * tiltedZ;
            positionUpdater.accept(passenger, px, py, pz);

            float deltaYaw = this.getYaw() - this.prevYaw;
            passenger.setYaw(passenger.getYaw() + deltaYaw);
            clampPassengerYaw(passenger);
        }
    }

    @Override
    public Vec3d updatePassengerForDismount(LivingEntity passenger) {
        Direction dir = this.getHorizontalFacing().rotateYClockwise();
        return new Vec3d(this.getX() + dir.getOffsetX() * 2.0, this.getY() + 0.1, this.getZ() + dir.getOffsetZ() * 2.0);
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        this.inputForward = false;
        this.inputBack = false;
        this.inputLeft = false;
        this.inputRight = false;
        this.inputUp = false;
        this.inputDown = false;
        this.inputSprint = false;
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        Entity passenger = this.getFirstPassenger();
        return passenger instanceof LivingEntity living ? living : null;
    }

    @Override
    public Box calculateBoundingBox() {
        double rad = Math.toRadians(this.getYaw());
        double halfLen = 2.4; // 4.8 blocks length, centered exactly on rotor mast
        double halfWid = 1.3; // 2.6 blocks width
        double extX = Math.abs(Math.cos(rad)) * halfWid + Math.abs(Math.sin(rad)) * halfLen;
        double extZ = Math.abs(Math.sin(rad)) * halfWid + Math.abs(Math.cos(rad)) * halfLen;
        return new Box(
                this.getX() - extX, this.getY(), this.getZ() - extZ,
                this.getX() + extX, this.getY() + 2.4, this.getZ() + extZ
        );
    }

    public boolean isAutoReturning() {
        return this.dataTracker.get(AUTO_RETURNING);
    }

    public BlockPos getTargetHelipadPos() {
        return this.targetHelipadPos;
    }

    public void startAutoPark(BlockPos center) {
        this.targetHelipadPos = center;
        this.dataTracker.set(AUTO_RETURNING, true);
        this.autoReturnStage = 0;
        this.autoReturnTicks = 0;
        this.autoParkGraceTicks = 20; // 1 second grace period to prevent residual key presses from cancelling auto-park
        this.inputForward = false;
        this.inputBack = false;
        this.inputLeft = false;
        this.inputRight = false;
        this.inputUp = false;
        this.inputDown = false;
        this.cruiseAltitude = Math.max(this.getY() + 8.0, center.getY() + 8.0);
    }

    public void toggleAutoPark(PlayerEntity player) {
        if (isAutoReturning()) {
            cancelAutoPark(player);
            return;
        }

        BlockPos heliPos = this.getBlockPos();
        BlockPos bestSpot = null;
        double bestDistSq = Double.MAX_VALUE;

        int radH = 64;
        int radV = 24;
        for (int x = -radH; x <= radH; x += 2) {
            for (int y = -radV; y <= radV; y++) {
                for (int z = -radH; z <= radH; z += 2) {
                    BlockPos p = heliPos.add(x, y, z);
                    BlockState state = this.getWorld().getBlockState(p);
                    if (state.isOf(EvecualMC.HELI_CHARGER_BLOCK)) {
                        HeliChargerPart part = state.get(HeliChargerBlock.PART);
                        BlockPos center = HeliChargerBlock.getCenterPos(p, part);
                        double distSq = heliPos.getSquaredDistance(center);
                        if (distSq < bestDistSq) {
                            bestDistSq = distSq;
                            bestSpot = center;
                        }
                    }
                }
            }
        }

        if (bestSpot != null) {
            startAutoPark(bestSpot);
            if (player instanceof ServerPlayerEntity sp) {
                net.minecraft.network.PacketByteBuf buf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
                buf.writeInt(this.getId());
                buf.writeInt(bestSpot.getX());
                buf.writeInt(bestSpot.getY());
                buf.writeInt(bestSpot.getZ());
                net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(sp, EvecualMC.START_HELI_AUTO_PARK_S2C_PACKET_ID, buf);
            }
            player.sendMessage(Text.literal("§a🚁 Helipad Autopilot engaged! Navigating to 3x3 Helipad..."), true);
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.PLAYERS, 1.0F, 1.5F);
        } else {
            player.sendMessage(Text.literal("§c❌ No 3x3 Helipad found within 64 blocks!"), true);
        }
    }

    public void cancelAutoPark(Entity player) {
        if (isAutoReturning()) {
            this.dataTracker.set(AUTO_RETURNING, false);
            this.targetHelipadPos = null;
            this.autoReturnStage = 0;
            this.autoParkGraceTicks = 0;
            if (player instanceof ServerPlayerEntity sp) {
                net.minecraft.network.PacketByteBuf buf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
                buf.writeInt(this.getId());
                net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(sp, EvecualMC.CANCEL_HELI_AUTO_PARK_S2C_PACKET_ID, buf);
                sp.sendMessage(Text.literal("§e⚠️ Helipad Autopilot cancelled by pilot."), true);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        updateChunkLoading();

        Entity passenger = this.getFirstPassenger();
        int energy = getEnergy();
        boolean hasPower = energy > 0;
        boolean hasPilot = passenger instanceof PlayerEntity;

        if (this.remoteControlTimeout > 0) {
            this.remoteControlTimeout--;
        }
        boolean isRemotePiloted = this.remoteControlTimeout > 0;
        boolean isPilotedOrRemote = hasPilot || isRemotePiloted;

        if (this.autoParkGraceTicks > 0) {
            this.autoParkGraceTicks--;
        }

        // 1. Rotor Speed & Spool-up calculation:
        // When leaving the heli, it turns off and propellers stop spinning!
        float targetRotorSpeed = 0.0F;
        if (hasPower && (isPilotedOrRemote || isAutoReturning() || hasActiveRcLink())) {
            targetRotorSpeed = inputSprint ? 1.5F : 1.0F;
        } else if (hasPower && !this.isOnGround()) {
            targetRotorSpeed = 0.6F; // emergency auto-rotation descent until landed
        } else {
            targetRotorSpeed = 0.0F; // Engine OFF! Propellers stop completely!
        }

        float currentRotorSpeed = this.dataTracker.get(ROTOR_SPEED);
        currentRotorSpeed = MathHelper.stepTowards(currentRotorSpeed, targetRotorSpeed, 0.12F);
        this.dataTracker.set(ROTOR_SPEED, currentRotorSpeed);

        // Update Rotor Angles for client animations
        this.rotorAngle += currentRotorSpeed * 50.0F;
        this.tailRotorAngle += currentRotorSpeed * 70.0F;

        boolean flying = currentRotorSpeed > 0.3F && !this.isOnGround() && !isCharging();
        this.dataTracker.set(IN_FLIGHT, flying);

        // 2. Flight Dynamics & Speed Control
        double targetHozSpeed = 0.0;
        double targetVy = 0.0;
        float targetYawDelta = 0.0F;
        float targetPitch = 0.0F;
        float targetRoll = 0.0F;

        // --- Auto-Park Navigation to 3x3 Helipad ---
        if (isAutoReturning() && this.targetHelipadPos != null && hasPower) {
            boolean hasManualMove = (this.autoParkGraceTicks == 0) && (inputForward || inputBack || inputLeft || inputRight || inputUp || inputDown);
            if (hasManualMove) {
                cancelAutoPark(passenger);
            } else {
                this.autoReturnTicks++;
                if (this.autoReturnTicks > 2400) { // 120s timeout
                    cancelAutoPark(passenger);
                } else {
                    double targetX = this.targetHelipadPos.getX() + 0.5;
                    double targetY = this.targetHelipadPos.getY() + 0.0625;
                    double targetZ = this.targetHelipadPos.getZ() + 0.5;

                    double dx = targetX - this.getX();
                    double dz = targetZ - this.getZ();
                    double horizDist = Math.sqrt(dx * dx + dz * dz);

                    // Stage 0: Ascend to safe cruising altitude while aligning initial heading
                    if (this.autoReturnStage == 0) {
                        float desiredYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
                        float yawDiff = MathHelper.wrapDegrees(desiredYaw - this.getYaw());
                        targetYawDelta = MathHelper.clamp(yawDiff * 0.25F, -6.0F, 6.0F);

                        if (this.getY() < this.cruiseAltitude - 0.5) {
                            targetVy = 0.40; // climb
                            targetHozSpeed = 0.0;
                            targetPitch = 0.0F;
                            targetRoll = 0.0F;
                        } else {
                            this.autoReturnStage = 1;
                        }
                    }
                    // Stage 1: Proportional navigation & course heading alignment
                    else if (this.autoReturnStage == 1) {
                        float desiredYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
                        float yawDiff = MathHelper.wrapDegrees(desiredYaw - this.getYaw());
                        targetYawDelta = MathHelper.clamp(yawDiff * 0.30F, -7.0F, 7.0F);

                        // Throttle scales with heading alignment & remaining distance
                        double alignment = Math.max(0.1, Math.cos(Math.toRadians(yawDiff)));
                        double speedCap = Math.min(NORMAL_CRUISE_SPEED, horizDist * 0.35);

                        targetHozSpeed = speedCap * alignment;
                        targetPitch = (float) (-15.0 * alignment);
                        targetRoll = MathHelper.clamp(-yawDiff * 0.6F, -20.0F, 20.0F);
                        targetVy = 0.0; // altitude hold

                        if (horizDist < 1.5) {
                            this.autoReturnStage = 2; // Arrived above pad: transition to precision hover
                        }
                    }
                    // Stage 2: Precision hover & centering directly over Helipad
                    else if (this.autoReturnStage == 2) {
                        targetHozSpeed = Math.min(horizDist * 0.3, 0.15);
                        float alignYawDiff = MathHelper.wrapDegrees(0.0F - this.getYaw()); // Align North / pad orientation
                        targetYawDelta = MathHelper.clamp(alignYawDiff * 0.25F, -5.0F, 5.0F);
                        targetPitch = 0.0F;
                        targetRoll = 0.0F;
                        targetVy = 0.0;

                        if (horizDist < 0.30 && Math.abs(alignYawDiff) < 6.0F) {
                            this.autoReturnStage = 3; // Position locked: begin vertical touchdown
                        }
                    }
                    // Stage 3: Controlled vertical touchdown & docking
                    else if (this.autoReturnStage == 3) {
                        this.setPosition(MathHelper.lerp(0.35, this.getX(), targetX), this.getY(), MathHelper.lerp(0.35, this.getZ(), targetZ));
                        targetHozSpeed = 0.0;
                        targetYawDelta = 0.0F;
                        targetPitch = 0.0F;
                        targetRoll = 0.0F;
                        targetVy = -0.22; // smooth touchdown descent

                        if (this.isOnGround() || this.getY() <= targetY + 0.12) {
                            this.dataTracker.set(AUTO_RETURNING, false);
                            this.targetHelipadPos = null;
                            this.autoReturnStage = 0;
                            this.setPosition(targetX, Math.max(this.getY(), targetY), targetZ);
                            this.setVelocity(0, 0, 0);
                            this.setOnGround(true);
                            setCharging(true);
                            if (passenger instanceof PlayerEntity p) {
                                p.sendMessage(Text.literal("§a⚡ Precision Touchdown Complete! Recharging on 3x3 Helipad..."), true);
                            }
                            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                                     SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0F, 1.2F);
                        }
                    }
                }
            }
        }
        else if (isPilotedOrRemote && hasPower) {
            // Horizontal Cruise Speed: 12 blocks/s normal (0.60), 20 blocks/s boost (1.00)
            double maxCruise = (inputSprint || isUpgradedEngine()) ? BOOST_CRUISE_SPEED : NORMAL_CRUISE_SPEED;
            if (inputSprint && isUpgradedEngine()) {
                maxCruise = 1.25; // 25 blocks/sec with turbo engine boost!
            }

            if (inputForward) {
                targetHozSpeed = maxCruise;
                targetPitch = inputSprint ? -20.0F : -14.0F; // Realistic helicopter nose-down tilt forward
            } else if (inputBack) {
                targetHozSpeed = -0.35;
                targetPitch = 12.0F; // Realistic helicopter nose-up flare backward
            }

            // Yaw Steering
            if (inputLeft) {
                targetYawDelta = -4.2F;
                targetRoll = -16.0F; // bank left
            } else if (inputRight) {
                targetYawDelta = 4.2F;
                targetRoll = 16.0F; // bank right
            }

            // Vertical Flight (Up: Space, Down: Shift/Down)
            if (inputUp) {
                targetVy = 0.45; // 9.0 blocks/sec climb
            } else if (inputDown) {
                targetVy = -0.40; // 8.0 blocks/sec descent
            } else {
                targetVy = 0.0; // altitude lock
            }

            // Power consumption & effects
            if (!this.getWorld().isClient) {
                int drainInterval = inputSprint ? 6 : (inputForward || inputUp ? 12 : 20);
                if (this.age % drainInterval == 0) {
                    setEnergy(Math.max(0, energy - 1));
                }

                // Boost exhaust particles
                if (inputSprint && this.age % 2 == 0 && this.getWorld() instanceof ServerWorld serverWorld) {
                    double radHeading = Math.toRadians(this.getYaw());
                    double rearX = this.getX() + Math.sin(radHeading) * 2.2;
                    double rearY = this.getY() + 1.4;
                    double rearZ = this.getZ() - Math.cos(radHeading) * 2.2;
                    serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, rearX, rearY, rearZ, 3, 0.1, 0.05, 0.1, 0.03);
                }

                // Ambient turbine sound
                if (this.age % 12 == 0) {
                    this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.ENTITY_PHANTOM_FLAP, SoundCategory.PLAYERS,
                            0.7F, 1.1F + currentRotorSpeed * 0.4F);
                }
            }
        } else {
            // No pilot and no active remote control: reset movement inputs and apply gentle descent/gravity
            this.inputForward = false;
            this.inputBack = false;
            this.inputLeft = false;
            this.inputRight = false;
            this.inputUp = false;
            this.inputDown = false;
            this.inputSprint = false;

            if (this.isOnGround()) {
                targetVy = 0.0;
            } else {
                targetVy = -0.15; // gentle descent
            }
        }

        // Smooth Acceleration & Deceleration
        double accel = (targetHozSpeed > currentSpeed) ? 0.055 : 0.09;
        currentSpeed = MathHelper.stepTowards((float) currentSpeed, (float) targetHozSpeed, (float) accel);

        // Apply Yaw Rotation
        this.prevYaw = this.getYaw();
        if (Math.abs(targetYawDelta) > 0.01F) {
            this.setYaw(this.getYaw() + targetYawDelta);
        }

        // Smooth Pitch & Roll Tracking
        float curPitch = this.dataTracker.get(PITCH_TILT);
        float curRoll = this.dataTracker.get(ROLL_TILT);
        curPitch = MathHelper.lerp(0.20F, curPitch, targetPitch);
        curRoll = MathHelper.lerp(0.20F, curRoll, targetRoll);
        this.dataTracker.set(PITCH_TILT, curPitch);
        this.dataTracker.set(ROLL_TILT, curRoll);

        // Compute 3D Velocity Vector
        double radYaw = Math.toRadians(this.getYaw());
        double vx = -Math.sin(radYaw) * currentSpeed;
        double vz = Math.cos(radYaw) * currentSpeed;
        double vy = MathHelper.stepTowards((float) this.getVelocity().y, (float) targetVy, 0.08F);

        this.setVelocity(vx, vy, vz);
        this.move(MovementType.SELF, this.getVelocity());

        // Ground collision check
        if (this.isOnGround() && this.getVelocity().y < 0) {
            this.setVelocity(this.getVelocity().x, 0.0, this.getVelocity().z);
        }
        this.setBoundingBox(this.calculateBoundingBox());
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        setEnergy(nbt.getInt("Energy"));
        setColorVariant(nbt.getInt("ColorVariant"));
        setGlassColor(nbt.getInt("GlassColor"));
        setUpgradedEngine(nbt.getBoolean("UpgradedEngine"));
        if (nbt.contains("LeftArm")) setLeftArmType(nbt.getInt("LeftArm"));
        if (nbt.contains("RightArm")) setRightArmType(nbt.getInt("RightArm"));
        if (nbt.contains("StorageUnlocked")) setStorageUnlocked(nbt.getBoolean("StorageUnlocked"));
        if (nbt.contains("MaxEnergyCap")) setMaxEnergyCap(nbt.getInt("MaxEnergyCap"));
        if (nbt.contains("PairedPlayerUuid")) setPairedPlayerUuid(nbt.getString("PairedPlayerUuid"));
        if (nbt.contains("Trunk", 10)) {
            trunk.readNbtList(nbt.getList("Trunk", 10));
        }
        if (nbt.contains("Upgrades", 10)) {
            upgrades.readNbtList(nbt.getList("Upgrades", 10));
        }
        syncUpgrades();
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("Energy", getEnergy());
        nbt.putInt("ColorVariant", getColorVariant());
        nbt.putInt("GlassColor", getGlassColor());
        nbt.putBoolean("UpgradedEngine", isUpgradedEngine());
        nbt.putInt("LeftArm", getLeftArmType());
        nbt.putInt("RightArm", getRightArmType());
        nbt.putBoolean("StorageUnlocked", isStorageUnlocked());
        nbt.putInt("MaxEnergyCap", getMaxEnergy());
        nbt.putString("PairedPlayerUuid", getPairedPlayerUuid());
        nbt.put("Trunk", trunk.toNbtList());
        nbt.put("Upgrades", upgrades.toNbtList());
    }
}
