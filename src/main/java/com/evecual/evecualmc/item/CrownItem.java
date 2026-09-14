package com.evecual.evecualmc.item;

import com.evecual.evecualmc.util.CrownHolder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public abstract class CrownItem extends Item {
    public CrownItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack heldStack = user.getStackInHand(hand);
        if (user instanceof CrownHolder crownHolder) {
            ItemStack equippedCrown = crownHolder.evecual$getCrown();
            if (equippedCrown.isEmpty()) {
                if (!world.isClient()) {
                    crownHolder.evecual$setCrown(heldStack.copy());
                    heldStack.setCount(0);
                    world.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.ITEM_ARMOR_EQUIP_GOLD, SoundCategory.PLAYERS, 1.0F, 1.0F);
                }
                return TypedActionResult.success(heldStack, world.isClient());
            } else {
                if (!world.isClient()) {
                    ItemStack temp = equippedCrown.copy();
                    crownHolder.evecual$setCrown(heldStack.copy());
                    user.setStackInHand(hand, temp);
                    world.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.ITEM_ARMOR_EQUIP_GOLD, SoundCategory.PLAYERS, 1.0F, 1.0F);
                }
                return TypedActionResult.success(user.getStackInHand(hand), world.isClient());
            }
        }
        return TypedActionResult.pass(heldStack);
    }
}
