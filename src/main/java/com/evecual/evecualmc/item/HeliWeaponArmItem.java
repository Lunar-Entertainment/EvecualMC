package com.evecual.evecualmc.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HeliWeaponArmItem extends Item {
    public HeliWeaponArmItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("§7Install onto EV Heli wing hardpoint."));
        tooltip.add(Text.literal("§c🚀 High-Voltage Plasma Blaster"));
        tooltip.add(Text.literal("§b⚡ Fires rapid electric energy bolts (LMB / Space)."));
    }
}
