package com.evecual.evecualmc.item;

import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ElectricGrinderBlockItem extends BlockItem {

    public ElectricGrinderBlockItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("§7⚙ Industrial Material Pulverizer"));
        tooltip.add(Text.literal("§7Crushes raw ores, ingots, and minerals into"));
        tooltip.add(Text.literal("§7fine dusts and secondary materials."));
        tooltip.add(Text.literal("§eMachine Specifications:"));
        tooltip.add(Text.literal(" §f• Energy Buffer: §b2,000 EU §7(4 EU/t operating draw)"));
        tooltip.add(Text.literal(" §f• Processing: §aDoubles ore yield into powdered dusts"));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
