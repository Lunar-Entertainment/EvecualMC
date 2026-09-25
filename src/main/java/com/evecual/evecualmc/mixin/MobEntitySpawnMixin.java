package com.evecual.evecualmc.mixin;

import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public class MobEntitySpawnMixin {
    private static final Identifier EVECUAL_DIMENSION_ID = new Identifier("evecualmc", "evecual");

    @Inject(method = "canSpawn(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/entity/SpawnReason;)Z", at = @At("HEAD"), cancellable = true)
    private void evecual$preventMobSpawns(WorldAccess world, SpawnReason spawnReason, CallbackInfoReturnable<Boolean> cir) {
        if (world instanceof ServerWorld serverWorld && serverWorld.getRegistryKey().getValue().equals(EVECUAL_DIMENSION_ID)) {
            if (spawnReason != SpawnReason.SPAWN_EGG && spawnReason != SpawnReason.COMMAND) {
                cir.setReturnValue(false);
            }
        }
    }
}
