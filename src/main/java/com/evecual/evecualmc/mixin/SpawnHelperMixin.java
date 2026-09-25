package com.evecual.evecualmc.mixin;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpawnHelper.class)
public class SpawnHelperMixin {
    private static final Identifier EVECUAL_DIMENSION_ID = new Identifier("evecualmc", "evecual");

    @Inject(method = "spawn", at = @At("HEAD"), cancellable = true)
    private static void evecual$preventSpawnsInEvecual(ServerWorld world, WorldChunk chunk, SpawnHelper.Info info, boolean spawnAnimals, boolean spawnMonsters, boolean rare, CallbackInfo ci) {
        if (world != null && world.getRegistryKey().getValue().equals(EVECUAL_DIMENSION_ID)) {
            ci.cancel();
        }
    }
}
