package com.evecual.evecualmc.mixin;

import com.evecual.evecualmc.util.NoFireLightning;
import net.minecraft.entity.LightningEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightningEntity.class)
public class LightningEntityMixin implements NoFireLightning {
    @Unique
    private boolean evecual$noFire = false;

    @Override
    public void evecual$setNoFire(boolean noFire) {
        this.evecual$noFire = noFire;
    }

    @Override
    public boolean evecual$isNoFire() {
        return this.evecual$noFire;
    }

    @Inject(method = "spawnFire", at = @At("HEAD"), cancellable = true)
    private void evecual$cancelSpawnFire(int spreadAttempts, CallbackInfo ci) {
        if (this.evecual$noFire) {
            ci.cancel();
        }
    }
}
