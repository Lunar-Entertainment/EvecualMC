package com.evecual.evecualmc.screen;

import com.evecual.evecualmc.util.CrownHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Dedicated inventory slot strictly for the three Legendary Crowns.
 * Positioned on top of the head armor slot and revealed when hovering over the head.
 */
public class CrownSlot extends Slot {
    private final PlayerEntity player;
    private boolean revealed = false;

    public CrownSlot(Inventory inventory, int index, int x, int y, PlayerEntity player) {
        super(inventory, index, x, y);
        this.player = player;
    }

    /**
     * Nothing else can be in there except the three legendary crowns.
     */
    @Override
    public boolean canInsert(ItemStack stack) {
        return CrownHelper.isCrown(stack);
    }

    @Override
    public int getMaxItemCount() {
        return 1;
    }

    @Override
    public boolean isEnabled() {
        // On server, always enabled to process incoming slot clicks.
        // On client, enabled whenever revealed by mouse hover over the head, or while a crown is equipped.
        if (this.player.getWorld().isClient()) {
            return this.revealed || this.hasStack();
        }
        return true;
    }

    public void setRevealed(boolean revealed) {
        this.revealed = revealed;
    }

    public boolean isRevealed() {
        return this.revealed;
    }

    @Override
    public void setStack(ItemStack stack) {
        super.setStack(stack);
        if (this.player instanceof ServerPlayerEntity serverPlayer) {
            CrownHelper.syncCrownToTracking(serverPlayer);
        }
    }

    @Override
    public void onTakeItem(PlayerEntity player, ItemStack stack) {
        super.onTakeItem(player, stack);
        if (player instanceof ServerPlayerEntity serverPlayer) {
            CrownHelper.syncCrownToTracking(serverPlayer);
        }
    }
}
