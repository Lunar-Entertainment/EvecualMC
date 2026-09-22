package com.evecual.evecualmc.item;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.entity.CarEntity;
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

public class CarItem extends Item {
    public CarItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (!world.isClient) {
            BlockPos pos = context.getBlockPos();
            Direction side = context.getSide();
            BlockPos spawnPos = pos.offset(side);

            CarEntity car = new CarEntity(EvecualMC.CAR_ENTITY, world);
            if (context.getStack().hasNbt()) {
                car.applyItemNbt(context.getStack().getNbt());
            }
            car.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, context.getPlayerYaw(), 0.0f);
            world.spawnEntity(car);

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
            int trunkTier = nbt.getInt("TrunkTier");
            int glass = nbt.getInt("GlassColor");
            int color = nbt.getInt("ColorVariant");

            String colorName = switch (color) {
                case 1 -> "Cyber Blue";
                case 2 -> "Stealth Black";
                case 3 -> "Neon Lime";
                case 4 -> "Pearl White";
                case 5 -> "Racing Yellow";
                default -> "Crimson Red";
            };

            tooltip.add(Text.literal("§7Body Color: §f" + colorName));
            tooltip.add(Text.literal(upgraded ? "§b⚡ Engine: Advanced Turbo (+40% Boost)" : "§7⚡ Engine: Standard Electric"));
            tooltip.add(Text.literal(trunkTier == 1 ? "§6📦 Trunk: Expanded (27 Slots)" : "§7📦 Trunk: Standard (18 Slots)"));
            if (glass > 0) {
                tooltip.add(Text.literal("§3✨ Canopy: Custom Tinted Glass"));
            }
        } else {
            tooltip.add(Text.literal("§7⚡ Standard Electric Vehicle"));
        }
        super.appendTooltip(stack, world, tooltip, context);
    }
}
