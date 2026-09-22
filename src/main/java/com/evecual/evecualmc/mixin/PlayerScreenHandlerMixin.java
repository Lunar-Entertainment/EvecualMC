package com.evecual.evecualmc.mixin;

import com.evecual.evecualmc.util.CrownHelper;
import com.evecual.evecualmc.util.CrownHolder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerScreenHandler.class)
public abstract class PlayerScreenHandlerMixin extends AbstractRecipeScreenHandler<CraftingInventory> {

    public PlayerScreenHandlerMixin(ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    // NOTE: We deliberately do NOT inject a raw slot into this.slots via addSlot()!
    // Injecting into PlayerScreenHandler.slots shifts slot indices and corrupts vanilla
    // slot tracking when slot-modifying mods (Trinkets, Traveler's Backpack, Elytra Slot)
    // are present on servers, causing random item overrides across mods.

    @Inject(method = "quickMove", at = @At("HEAD"), cancellable = true)
    private void handleCrownQuickMove(PlayerEntity player, int index, CallbackInfoReturnable<ItemStack> cir) {
        if (index < 0 || index >= this.slots.size()) {
            return;
        }

        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasStack()) {
            return;
        }

        ItemStack slotStack = slot.getStack();

        // Shift-clicking a Legendary Crown from main inventory/hotbar into the Crown Slot
        if (CrownHelper.isCrown(slotStack)) {
            if (player instanceof CrownHolder holder) {
                if (holder.evecualmc$getCrown().isEmpty()) {
                    ItemStack copy = slotStack.copy();
                    ItemStack equipped = slotStack.split(1);
                    holder.evecualmc$setCrown(equipped);
                    if (slotStack.isEmpty()) {
                        slot.setStack(ItemStack.EMPTY);
                    } else {
                        slot.markDirty();
                    }
                    if (player instanceof ServerPlayerEntity serverPlayer) {
                        CrownHelper.syncCrownToTracking(serverPlayer);
                    }
                    cir.setReturnValue(copy);
                    return;
                }
            }
        }
    }
}
