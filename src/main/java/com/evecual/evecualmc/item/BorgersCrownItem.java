package com.evecual.evecualmc.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BorgersCrownItem extends CrownItem {
    public BorgersCrownItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("§6👑 Crown Accessory"));
        tooltip.add(Text.literal("§7Slot: §eCrown Accessory Slot"));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§e🍔 Royal Feast:"));
        tooltip.add(Text.literal("§7 Infused with the regal culinary majesty of the Borger."));
        tooltip.add(Text.literal("§e  • §fRegal Fortitude §7(Grants continuous Resistance)"));
        tooltip.add(Text.literal("§e  • §fPassive Nourishment §7(Slowly replenishes hunger & saturation)"));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§8[Right-click in hand or place in Crown Slot to equip]"));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
