package com.evecual.evecualmc.entity;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.turret.AmmoType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.joml.Vector3f;

import java.util.List;

public class TurretBulletEntity extends Entity {
    private static final TrackedData<Integer> AMMO_ORDINAL = DataTracker.registerData(TurretBulletEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private int lifeTicks = 0;
    private float damage = 14.0f;
    private AmmoType ammoType = AmmoType.IRON;

    public TurretBulletEntity(EntityType<? extends TurretBulletEntity> type, World world) {
        super(type, world);
        this.noClip = false;
    }

    public TurretBulletEntity(World world, double x, double y, double z, AmmoType ammoType) {
        this(EvecualMC.TURRET_BULLET_ENTITY, world);
        this.setPosition(x, y, z);
        this.setAmmoType(ammoType);
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(AMMO_ORDINAL, AmmoType.IRON.ordinal());
    }

    public void setAmmoType(AmmoType type) {
        this.ammoType = type != null ? type : AmmoType.IRON;
        this.damage = this.ammoType.getDamage();
        this.dataTracker.set(AMMO_ORDINAL, this.ammoType.ordinal());
    }

    public AmmoType getAmmoType() {
        int ord = this.dataTracker.get(AMMO_ORDINAL);
        if (ord >= 0 && ord < AmmoType.values().length) {
            return AmmoType.values()[ord];
        }
        return AmmoType.IRON;
    }

    @Override
    public void tick() {
        super.tick();

        Vec3d start = this.getPos();
        Vec3d velocity = this.getVelocity();
        Vec3d end = start.add(velocity);

        // 1. Raycast for block collisions
        BlockHitResult blockHit = this.getWorld().raycast(new RaycastContext(
                start, end,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                this
        ));

        if (blockHit.getType() != HitResult.Type.MISS) {
            end = blockHit.getPos();
        }

        // 2. Entity Hit Detection along raycast trajectory
        Box scanBox = this.getBoundingBox().stretch(velocity).expand(1.0);
        List<Entity> candidateEntities = this.getWorld().getOtherEntities(this, scanBox, e -> e instanceof LivingEntity && e.isAlive());

        EntityHitResult entityHit = null;
        double minDistanceSq = Double.MAX_VALUE;

        for (Entity candidate : candidateEntities) {
            Box candidateBox = candidate.getBoundingBox().expand(0.3);
            var optHit = candidateBox.raycast(start, end);
            if (optHit.isPresent()) {
                double dSq = start.squaredDistanceTo(optHit.get());
                if (dSq < minDistanceSq) {
                    minDistanceSq = dSq;
                    entityHit = new EntityHitResult(candidate, optHit.get());
                }
            }
        }

        // 3. Process Entity Hit
        if (entityHit != null) {
            this.onEntityHit(entityHit);
            return;
        }

        // 4. Process Block Hit
        if (blockHit.getType() != HitResult.Type.MISS) {
            this.onBlockHit(blockHit);
            return;
        }

        // 5. Advance position
        this.setPosition(end);

        // Particles along trajectory
        if (this.getWorld().isClient()) {
            AmmoType type = getAmmoType();
            Vector3f pColor = switch (type) {
                case COPPER -> new Vector3f(0.85F, 0.45F, 0.05F);
                case IRON -> new Vector3f(0.9F, 0.9F, 0.95F);
                case STEEL -> new Vector3f(0.40F, 0.48F, 0.60F);
                case DIAMOND -> new Vector3f(0.0F, 0.9F, 1.0F);
                case ELACTORITE -> new Vector3f(0.75F, 0.25F, 1.0F);
            };
            this.getWorld().addParticle(new DustParticleEffect(pColor, 0.8F), this.getX(), this.getY(), this.getZ(), 0, 0, 0);
            if (type == AmmoType.STEEL) {
                this.getWorld().addParticle(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
            } else if (type == AmmoType.DIAMOND) {
                this.getWorld().addParticle(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
            } else if (type == AmmoType.ELACTORITE) {
                this.getWorld().addParticle(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
                if (this.random.nextFloat() < 0.3F) {
                    this.getWorld().addParticle(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
                }
            }
        }

        this.lifeTicks++;
        if (this.lifeTicks > 80) { // Max 4 seconds flight time
            this.discard();
        }
    }

    private void onEntityHit(EntityHitResult hit) {
        if (!this.getWorld().isClient()) {
            Entity target = hit.getEntity();
            AmmoType type = getAmmoType();
            DamageSource source = this.getDamageSources().generic();

            target.damage(source, this.damage);
            target.timeUntilRegen = 0; // Bypass immunity ticks for responsive kinetic impact

            if (this.getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.CRIT, hit.getPos().x, hit.getPos().y, hit.getPos().z, 8, 0.1, 0.1, 0.1, 0.15);
                if (type == AmmoType.STEEL) {
                    serverWorld.spawnParticles(ParticleTypes.CRIT, hit.getPos().x, hit.getPos().y, hit.getPos().z, 14, 0.2, 0.2, 0.2, 0.2);
                    this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.PLAYERS, 0.5F, 1.8F);
                } else if (type == AmmoType.DIAMOND) {
                    serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, hit.getPos().x, hit.getPos().y, hit.getPos().z, 12, 0.2, 0.2, 0.2, 0.2);
                } else if (type == AmmoType.ELACTORITE) {
                    serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, hit.getPos().x, hit.getPos().y, hit.getPos().z, 24, 0.3, 0.3, 0.3, 0.15);
                    serverWorld.spawnParticles(ParticleTypes.FLASH, hit.getPos().x, hit.getPos().y, hit.getPos().z, 1, 0, 0, 0, 0);
                    this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.PLAYERS, 0.8F, 1.8F);

                    // AOE Shock arc to up to 2 nearby entities
                    Box shockBox = target.getBoundingBox().expand(5.0);
                    List<LivingEntity> nearby = this.getWorld().getEntitiesByClass(LivingEntity.class, shockBox,
                            e -> e != target && e.isAlive());
                    int chained = 0;
                    for (LivingEntity chainTarget : nearby) {
                        if (chained >= 2) break;
                        chainTarget.damage(source, 12.0F);
                        serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, chainTarget.getX(), chainTarget.getY() + 0.5, chainTarget.getZ(), 8, 0.2, 0.2, 0.2, 0.05);
                        chained++;
                    }
                }
            }
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ARROW_HIT, SoundCategory.PLAYERS, 1.0F, 1.4F);
        }
        this.discard();
    }

    private void onBlockHit(BlockHitResult hit) {
        if (!this.getWorld().isClient()) {
            if (this.getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.SMOKE, hit.getPos().x, hit.getPos().y, hit.getPos().z, 4, 0.05, 0.05, 0.05, 0.02);
            }
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BLOCK_ANVIL_HIT, SoundCategory.BLOCKS, 0.4F, 2.0F);
        }
        this.discard();
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.contains("AmmoType")) {
            int ord = nbt.getInt("AmmoType");
            if (ord >= 0 && ord < AmmoType.values().length) {
                setAmmoType(AmmoType.values()[ord]);
            }
        }
        this.lifeTicks = nbt.getInt("LifeTicks");
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("AmmoType", getAmmoType().ordinal());
        nbt.putInt("LifeTicks", this.lifeTicks);
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }
}
