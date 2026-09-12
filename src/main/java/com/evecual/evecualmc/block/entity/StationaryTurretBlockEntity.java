package com.evecual.evecualmc.block.entity;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.entity.TurretBulletEntity;
import com.evecual.evecualmc.screen.StationaryTurretScreenHandler;
import com.evecual.evecualmc.turret.AmmoType;
import com.evecual.evecualmc.turret.TurretTargetFilter;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public class StationaryTurretBlockEntity extends BlockEntity implements NamedScreenHandlerFactory {
    public static final int COOLDOWN_TICKS = 40; // 2.0s cooldown between shots

    private final TurretTargetFilter targetFilter = new TurretTargetFilter();
    private int cooldown = 0;
    @Nullable
    private BlockPos linkedAmmoContainerPos = null;

    // Client-side animation tracking
    public float curYaw = 0.0f;
    public float curPitch = 0.0f;
    public float targetYaw = 0.0f;
    public float targetPitch = 0.0f;
    @Nullable
    private LivingEntity currentTarget = null;

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
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> cooldown = value;
                case 1 -> targetFilter.setRadius(MathHelper.clamp(value, 8, 256));
                case 2 -> targetFilter.setTargetPlayers(value == 1);
                case 3 -> targetFilter.setTargetMonsters(value == 1);
                case 4 -> targetFilter.setTargetAnimals(value == 1);
                case 5 -> targetFilter.setTargetBosses(value == 1);
            }
        }

        @Override
        public int size() {
            return 11;
        }
    };

    public StationaryTurretBlockEntity(BlockPos pos, BlockState state) {
        super(EvecualMC.STATIONARY_TURRET_BLOCK_ENTITY, pos, state);
        targetFilter.setRadius(64); // Default 128x128 area, max 256 = 512x512
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
        markDirty();
        sync();
    }

    public static void tick(World world, BlockPos pos, BlockState state, StationaryTurretBlockEntity be) {
        if (be.cooldown > 0) {
            be.cooldown--;
        }

        // Search & Acquire Target
        if (world.getTime() % 4 == 0 || be.currentTarget == null || !be.currentTarget.isAlive()) {
            be.scanForTarget(world, pos);
        }

        // Aim & Shoot
        if (be.currentTarget != null && be.currentTarget.isAlive()) {
            Vec3d turretOrigin = Vec3d.ofCenter(pos).add(0, 0.86, 0);
            Vec3d targetEye = be.currentTarget.getEyePos().subtract(0, 0.2, 0);
            Vec3d aimVec = targetEye.subtract(turretOrigin);

            double dX = aimVec.x;
            double dY = aimVec.y;
            double dZ = aimVec.z;
            double horizDist = Math.sqrt(dX * dX + dZ * dZ);

            be.targetYaw = (float) Math.toDegrees(Math.atan2(-dX, dZ));
            be.targetPitch = (float) Math.toDegrees(Math.atan2(-dY, horizDist));

            // Server-side firing logic
            if (!world.isClient && be.cooldown <= 0) {
                be.fireAtTarget(world, pos, turretOrigin, aimVec.normalize());
            }
        } else {
            // Idle scanning rotation
            be.targetPitch = 0.0f;
            be.targetYaw = (be.targetYaw + 1.5f) % 360.0f;
        }

        // Smooth visual interpolation
        be.curYaw = MathHelper.lerpAngleDegrees(0.25f, be.curYaw, be.targetYaw);
        be.curPitch = MathHelper.lerp(0.25f, be.curPitch, be.targetPitch);
    }

    private void scanForTarget(World world, BlockPos pos) {
        int r = targetFilter.getRadius();
        Box area = new Box(pos.getX() - r, pos.getY() - 32, pos.getZ() - r,
                pos.getX() + r + 1, pos.getY() + 64, pos.getZ() + r + 1);

        List<LivingEntity> candidates = world.getEntitiesByClass(LivingEntity.class, area, targetFilter::isValidTarget);

        if (!candidates.isEmpty()) {
            candidates.sort(Comparator.comparingDouble(e -> e.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)));
            this.currentTarget = candidates.get(0);
        } else {
            this.currentTarget = null;
        }
    }

    private void fireAtTarget(World world, BlockPos pos, Vec3d muzzlePos, Vec3d aimDir) {
        TurretAmmoContainerBlockEntity ammoContainer = getLinkedAmmoContainer();
        if (ammoContainer == null || !ammoContainer.hasAmmo()) {
            // Out of ammo: Click sound every 20 ticks
            if (world.getTime() % 20 == 0) {
                world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 0.8F, 1.6F);
            }
            return;
        }

        AmmoType ammo = ammoContainer.consumeBestAmmo();
        if (ammo == null) return;

        // Spawn Projectile
        TurretBulletEntity bullet = new TurretBulletEntity(world, muzzlePos.x + aimDir.x * 0.6, muzzlePos.y + aimDir.y * 0.6, muzzlePos.z + aimDir.z * 0.6, ammo);
        float velocity = 3.8f * ammo.getVelocityMultiplier();
        bullet.setVelocity(aimDir.x * velocity, aimDir.y * velocity, aimDir.z * velocity);
        world.spawnEntity(bullet);

        // Sound & Particles
        world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 0.7F, 1.8F);
        world.playSound(null, pos, SoundEvents.ITEM_CROSSBOW_SHOOT, SoundCategory.BLOCKS, 1.0F, 0.8F);

        if (world instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.FLASH, muzzlePos.x + aimDir.x * 0.8, muzzlePos.y + aimDir.y * 0.8, muzzlePos.z + aimDir.z * 0.8, 1, 0, 0, 0, 0);
            serverWorld.spawnParticles(ParticleTypes.SMOKE, muzzlePos.x + aimDir.x * 0.8, muzzlePos.y + aimDir.y * 0.8, muzzlePos.z + aimDir.z * 0.8, 4, 0.05, 0.05, 0.05, 0.02);
        }

        this.cooldown = COOLDOWN_TICKS;
        markDirty();
        sync();
    }

    @Nullable
    public TurretAmmoContainerBlockEntity getLinkedAmmoContainer() {
        if (world == null || linkedAmmoContainerPos == null) return null;
        if (world.isChunkLoaded(linkedAmmoContainerPos)) {
            BlockEntity be = world.getBlockEntity(linkedAmmoContainerPos);
            if (be instanceof TurretAmmoContainerBlockEntity container) {
                return container;
            }
        }
        return null;
    }

    public void sync() {
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("🛡️ Stationary Defense Turret");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new StationaryTurretScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        targetFilter.readNbt(nbt);
        cooldown = nbt.getInt("Cooldown");
        if (nbt.contains("LinkedContainer")) {
            linkedAmmoContainerPos = NbtHelper.toBlockPos(nbt.getCompound("LinkedContainer"));
        } else {
            linkedAmmoContainerPos = null;
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        targetFilter.writeNbt(nbt);
        nbt.putInt("Cooldown", cooldown);
        if (linkedAmmoContainerPos != null) {
            nbt.put("LinkedContainer", NbtHelper.fromBlockPos(linkedAmmoContainerPos));
        }
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }
}
