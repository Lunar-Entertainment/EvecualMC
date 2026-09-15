package com.evecual.evecualmc.item;

import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemFabricatorBlockItem extends BlockItem {

    public ItemFabricatorBlockItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("§b⚡ High-Precision Cybernetic Fabricator"));
        tooltip.add(Text.literal("§7Constructs advanced tools, military railguns,"));
        tooltip.add(Text.literal("§7and quantum components using intense EU power."));
        tooltip.add(Text.literal("§eWorkstation Specifications:"));
        tooltip.add(Text.literal(" §f• Energy Buffer: §b4,000 EU §7(Input: Wire / Cable / Battery)"));
        tooltip.add(Text.literal(" §f• Blueprints: §aZapper§7, §dRailgun§7, §bMaterializer§7, §6Duper"));
        tooltip.add(Text.literal("§8Right-click block to open fabrication terminal."));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
