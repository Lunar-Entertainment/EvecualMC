package com.evecual.evecualmc.mixin;

import com.evecual.evecualmc.util.NoFireLightning;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightningEntity.class)
public class LightningEntityMixin implements NoFireLightning {
    @Unique
    private boolean evecual$noFire = false;
    @Unique
    private boolean evecual$soulFire = false;
    @Unique
    private float evecual$bonusDamage = 0.0f;

    @Override
    public void evecual$setNoFire(boolean noFire) {
        this.evecual$noFire = noFire;
    }

    @Override
    public boolean evecual$isNoFire() {
        return this.evecual$noFire;
    }

    @Override
    public void evecual$setSoulFire(boolean soulFire) {
        this.evecual$soulFire = soulFire;
    }

    @Override
    public boolean evecual$isSoulFire() {
        return this.evecual$soulFire;
    }

    @Override
    public void evecual$setBonusDamage(float bonusDamage) {
        this.evecual$bonusDamage = bonusDamage;
    }

    @Override
    public float evecual$getBonusDamage() {
        return this.evecual$bonusDamage;
    }

    @Inject(method = "spawnFire", at = @At("HEAD"), cancellable = true)
    private void evecual$handleFireSpawn(int spreadAttempts, CallbackInfo ci) {
        if (this.evecual$noFire) {
            ci.cancel();
            return;
        }

        if (this.evecual$soulFire) {
            ci.cancel();
            LightningEntity lightning = (LightningEntity) (Object) this;
            World world = lightning.getWorld();
            if (!world.isClient() && world.getGameRules().getBoolean(GameRules.DO_FIRE_TICK)) {
                BlockPos strikePos = lightning.getBlockPos();
                for (int i = 0; i < spreadAttempts + 2; i++) {
                    BlockPos targetPos = strikePos.add(
                            world.getRandom().nextInt(3) - 1,
                            world.getRandom().nextInt(3) - 1,
                            world.getRandom().nextInt(3) - 1
                    );
                    if (world.getBlockState(targetPos).isAir()) {
                        BlockPos floorPos = targetPos.down();
                        BlockState floorState = world.getBlockState(floorPos);
                        if (floorState.isSolidBlock(world, floorPos)) {
                            // If not already a soul fire base, convert to SOUL_SOIL so soul fire can burn indefinitely
                            if (!floorState.isOf(Blocks.SOUL_SAND) && !floorState.isOf(Blocks.SOUL_SOIL) && floorState.getHardness(world, floorPos) >= 0) {
                                world.setBlockState(floorPos, Blocks.SOUL_SOIL.getDefaultState());
                            }
                            world.setBlockState(targetPos, Blocks.SOUL_FIRE.getDefaultState());
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void evecual$applyBonusDamage(CallbackInfo ci) {
        if (this.evecual$bonusDamage > 0.0f) {
            LightningEntity lightning = (LightningEntity) (Object) this;
            World world = lightning.getWorld();
            if (world instanceof ServerWorld serverWorld && !lightning.isRemoved()) {
                lightning.getStruckEntities().forEach(entity -> {
                    if (entity instanceof LivingEntity living && living.isAlive()) {
                        living.damage(serverWorld.getDamageSources().lightningBolt(), this.evecual$bonusDamage);
                        living.setOnFireFor(8);
                    }
                });
            }
        }
    }
}
