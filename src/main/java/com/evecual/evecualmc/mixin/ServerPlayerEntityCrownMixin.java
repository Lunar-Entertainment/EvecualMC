package com.evecual.evecualmc.mixin;

import com.evecual.evecualmc.util.CrownHelper;
import com.evecual.evecualmc.util.CrownHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityCrownMixin {

    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void copyCrownFromOldPlayer(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        if (alive || player.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
            CrownHolder oldHolder = (CrownHolder) oldPlayer;
            CrownHolder newHolder = (CrownHolder) player;
            ItemStack oldCrown = oldHolder.evecualmc$getCrown();
            newHolder.evecualmc$setCrown(oldCrown.isEmpty() ? ItemStack.EMPTY : oldCrown.copy());
        }
        CrownHelper.syncCrownToTracking(player);
    }

    @Inject(method = "worldChanged", at = @At("TAIL"))
    private void syncCrownOnWorldChange(ServerWorld origin, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        CrownHelper.syncCrownToTracking(player);
    }
}
