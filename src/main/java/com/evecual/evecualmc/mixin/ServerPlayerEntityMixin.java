package com.evecual.evecualmc.mixin;

import com.evecual.evecualmc.util.CrownHolder;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void evecual$copyCrownData(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        if (alive || self.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
            CrownHolder oldHolder = (CrownHolder) oldPlayer;
            CrownHolder newHolder = (CrownHolder) self;
            newHolder.evecual$setCrown(oldHolder.evecual$getCrown().copy());
        }
    }
}
