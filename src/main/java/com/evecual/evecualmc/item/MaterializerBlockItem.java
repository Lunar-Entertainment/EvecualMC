package com.evecual.evecualmc.item;

import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MaterializerBlockItem extends BlockItem {

    public MaterializerBlockItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("§b⚛ Subatomic Quantum Materializer"));
        tooltip.add(Text.literal("§7Condenses electromagnetic flux into solid physical"));
        tooltip.add(Text.literal("§7matter, rare minerals, and high-tech components."));
        tooltip.add(Text.literal("§eMachine Specifications:"));
        tooltip.add(Text.literal(" §f• Energy Buffer: §b8,000 EU §7(High-voltage input)"));
        tooltip.add(Text.literal(" §f• Process: §dSynthesizes physical matter from pure energy"));
        tooltip.add(Text.literal(" §f• Component: §6Required core for crafting Electronic Duper"));
        tooltip.add(Text.literal("§8Craftable in Item Fabricator or Crafting Table."));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
