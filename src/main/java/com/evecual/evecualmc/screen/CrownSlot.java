package com.evecual.evecualmc.screen;

import com.evecual.evecualmc.item.CrownItem;
import com.evecual.evecualmc.util.CrownHolder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class CrownSlot extends Slot {
    private final PlayerEntity owner;

    public CrownSlot(Inventory inventory, int index, int x, int y, PlayerEntity owner) {
        super(inventory, index, x, y);
        this.owner = owner;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return stack.getItem() instanceof CrownItem;
    }

    @Override
    public int getMaxItemCount() {
        return 1;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (this.owner instanceof CrownHolder holder) {
            holder.evecual$syncCrown();
        }
    }
}
