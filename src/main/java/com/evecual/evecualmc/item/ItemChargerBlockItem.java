package com.evecual.evecualmc.item;

import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemChargerBlockItem extends BlockItem {

    public ItemChargerBlockItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("§3⚡ Inductive Tool Charging Station"));
        tooltip.add(Text.literal("§7Rapidly recharges energy-powered devices, weapons,"));
        tooltip.add(Text.literal("§7and battery items placed in its chamber."));
        tooltip.add(Text.literal("§eStation Specifications:"));
        tooltip.add(Text.literal(" §f• Energy Buffer: §b5,000 EU §7(Fast inductive recharge)"));
        tooltip.add(Text.literal(" §f• Supported Gear: §fZapper, Railgun, Controllers, Batteries"));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
