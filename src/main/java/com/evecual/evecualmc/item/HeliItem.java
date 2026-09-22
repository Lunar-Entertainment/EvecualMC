package com.evecual.evecualmc.item;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.entity.HeliEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HeliItem extends Item {
    public HeliItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (!world.isClient) {
            BlockPos pos = context.getBlockPos();
            Direction side = context.getSide();
            BlockPos spawnPos = pos.offset(side);

            HeliEntity heli = new HeliEntity(EvecualMC.HELI_ENTITY, world);
            if (context.getStack().hasNbt()) {
                heli.applyItemNbt(context.getStack().getNbt());
            }
            heli.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY() + 0.05, spawnPos.getZ() + 0.5, context.getPlayerYaw(), 0.0f);
            world.spawnEntity(heli);

            world.playSound(null, spawnPos, SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.BLOCKS, 0.6f, 1.2f);

            if (context.getPlayer() != null && !context.getPlayer().isCreative()) {
                context.getStack().decrement(1);
            }
        }
        return ActionResult.success(world.isClient);
    }


    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null) {
            boolean upgraded = nbt.getBoolean("UpgradedEngine");
            int glass = nbt.getInt("GlassColor");
            int color = nbt.getInt("ColorVariant");
            int energy = nbt.contains("Energy") ? nbt.getInt("Energy") : 2000;

            String colorName = switch (color) {
                case 0 -> "Crimson Red";
                case 1 -> "Cyber Blue";
                case 2 -> "Stealth Black";
                case 3 -> "Neon Lime";
                case 4 -> "Pearl White";
                case 5 -> "Racing Yellow";
                default -> "Cyber Blue";
            };

            tooltip.add(Text.literal("§7Body Color: §f" + colorName));
            tooltip.add(Text.literal("§e⚡ Energy: §f" + energy + " EU"));
            tooltip.add(Text.literal("§b💨 Speed: §f12 m/s §7(§b20 m/s Boost§7)"));
            tooltip.add(Text.literal(upgraded ? "§b⚡ Turbine: High-Power Turbine (+50% Capacity)" : "§7⚡ Turbine: Standard Dual-Rotor"));
            if (glass > 0) {
                tooltip.add(Text.literal("§3✨ Canopy: Custom Tinted Glass"));
            }
        } else {
            tooltip.add(Text.literal("§7⚡ High-Speed Electric Helicopter (12 - 20 m/s)"));
        }
        super.appendTooltip(stack, world, tooltip, context);
    }
}
