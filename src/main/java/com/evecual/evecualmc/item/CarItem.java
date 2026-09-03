package com.evecual.evecualmc.item;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.entity.CarEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

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
            car.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, context.getPlayerYaw(), 0.0f);
            world.spawnEntity(car);

            world.playSound(null, spawnPos, SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.BLOCKS, 0.6f, 1.2f);

            if (context.getPlayer() != null && !context.getPlayer().isCreative()) {
                context.getStack().decrement(1);
            }
        }
        return ActionResult.success(world.isClient);
    }
}
