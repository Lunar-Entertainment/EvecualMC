package com.evecual.evecualmc.item;

import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ElectronicDuperBlockItem extends BlockItem {

    public ElectronicDuperBlockItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("§6💠 High-Tier Quantum Duplication Core"));
        tooltip.add(Text.literal("§7Replicates physical items and resources through"));
        tooltip.add(Text.literal("§7high-frequency electromagnetic resonance."));
        tooltip.add(Text.literal("§eDuplicator Specifications:"));
        tooltip.add(Text.literal(" §f• Energy Buffer: §b10,000 EU §7(Consumes high voltage)"));
        tooltip.add(Text.literal(" §f• Duplication Rate: §a100% molecular fidelity"));
        tooltip.add(Text.literal(" §c⛔ Security Restriction:"));
        tooltip.add(Text.literal("   §7Dupers, weapons, lightning, and vehicles cannot be cloned."));
        tooltip.add(Text.literal("§8Fabricated in the Item Fabricator workstation."));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
