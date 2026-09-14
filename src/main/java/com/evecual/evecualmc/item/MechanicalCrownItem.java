package com.evecual.evecualmc.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MechanicalCrownItem extends CrownItem {
    public MechanicalCrownItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("§6👑 Crown Accessory"));
        tooltip.add(Text.literal("§7Slot: §eCrown Accessory Slot"));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§b⚡ Mechanical Surge:"));
        tooltip.add(Text.literal("§7 Overclocks the §eLightning Item §7into a"));
        tooltip.add(Text.literal("§7 hypercharged soul-lightning catalyst:"));
        tooltip.add(Text.literal("§b  • §f150 Total Uses §7(Overclocked Durability)"));
        tooltip.add(Text.literal("§b  • §fSoul Fire Ignition §7(Burns Persistent Soul Fire)"));
        tooltip.add(Text.literal("§b  • §fMassive Soul Shockwave §7(High Bonus AoE Damage)"));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§8[Right-click in hand or place in Crown Slot to equip]"));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
