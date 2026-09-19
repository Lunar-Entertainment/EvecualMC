package com.evecual.evecualmc.mixin;

import com.evecual.evecualmc.util.CrownHolder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityCrownMixin extends LivingEntity implements CrownHolder {
    @Unique
    private final SimpleInventory evecualmc$crownInventory = new SimpleInventory(1);

    protected PlayerEntityCrownMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public Inventory evecualmc$getCrownInventory() {
        return this.evecualmc$crownInventory;
    }

    @Override
    public ItemStack evecualmc$getCrown() {
        return this.evecualmc$crownInventory.getStack(0);
    }

    @Override
    public void evecualmc$setCrown(ItemStack stack) {
        this.evecualmc$crownInventory.setStack(0, stack);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    private void writeCrownNbt(NbtCompound nbt, CallbackInfo ci) {
        ItemStack crown = this.evecualmc$crownInventory.getStack(0);
        if (!crown.isEmpty()) {
            nbt.put("EvecualMCCrown", crown.writeNbt(new NbtCompound()));
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    private void readCrownNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("EvecualMCCrown", NbtElement.COMPOUND_TYPE)) {
            this.evecualmc$crownInventory.setStack(0, ItemStack.fromNbt(nbt.getCompound("EvecualMCCrown")));
        } else {
            this.evecualmc$crownInventory.setStack(0, ItemStack.EMPTY);
        }
    }

    @Inject(method = "dropInventory", at = @At("TAIL"))
    private void dropCrownOnDeath(CallbackInfo ci) {
        if (!this.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
            ItemStack crown = this.evecualmc$crownInventory.removeStack(0);
            if (!crown.isEmpty()) {
                ((PlayerEntity) (Object) this).dropItem(crown, true, false);
            }
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickCrownSlotItem(CallbackInfo ci) {
        ItemStack crown = this.evecualmc$getCrown();
        if (!crown.isEmpty()) {
            crown.inventoryTick(this.getWorld(), (PlayerEntity) (Object) this, -1, false);
        }
    }
}
