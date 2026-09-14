package com.evecual.evecualmc.mixin;

import com.evecual.evecualmc.EvecualMC;
import com.evecual.evecualmc.util.CrownHolder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements CrownHolder {
    @Unique
    private static final TrackedData<ItemStack> EVECUAL_CROWN = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);

    @Unique
    private final SimpleInventory evecual$crownInventory = new SimpleInventory(1);

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initDataTracker", at = @At("RETURN"))
    private void evecual$initCrownDataTracker(CallbackInfo ci) {
        this.dataTracker.startTracking(EVECUAL_CROWN, ItemStack.EMPTY);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void evecual$crownTick(CallbackInfo ci) {
        if (!this.getWorld().isClient()) {
            if (evecual$hasBorgersCrown()) {
                PlayerEntity player = (PlayerEntity) (Object) this;
                if (this.age % 80 == 0) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 100, 0, true, false));
                    if (player.getHungerManager().isNotFull()) {
                        player.getHungerManager().add(1, 1.0f);
                    }
                }
            }
        } else {
            if (evecual$hasMechanicalCrown()) {
                if (this.age % 10 == 0) {
                    double px = this.getX() + (this.random.nextDouble() - 0.5) * 0.4;
                    double py = this.getY() + this.getEyeHeight(this.getPose()) + 0.32;
                    double pz = this.getZ() + (this.random.nextDouble() - 0.5) * 0.4;
                    this.getWorld().addParticle(ParticleTypes.ELECTRIC_SPARK, px, py, pz, 0, 0.02, 0);
                }
            } else if (evecual$hasBorgersCrown()) {
                if (this.age % 15 == 0) {
                    double px = this.getX() + (this.random.nextDouble() - 0.5) * 0.4;
                    double py = this.getY() + this.getEyeHeight(this.getPose()) + 0.32;
                    double pz = this.getZ() + (this.random.nextDouble() - 0.5) * 0.4;
                    this.getWorld().addParticle(ParticleTypes.WAX_ON, px, py, pz, 0, 0.01, 0);
                }
            }
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void evecual$writeCrownNbt(NbtCompound nbt, CallbackInfo ci) {
        NbtList list = new NbtList();
        ItemStack stack = this.evecual$crownInventory.getStack(0);
        if (!stack.isEmpty()) {
            NbtCompound itemNbt = new NbtCompound();
            stack.writeNbt(itemNbt);
            list.add(itemNbt);
        }
        nbt.put("EvecualCrownInventory", list);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void evecual$readCrownNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("EvecualCrownInventory", NbtElement.LIST_TYPE)) {
            NbtList list = nbt.getList("EvecualCrownInventory", NbtElement.COMPOUND_TYPE);
            if (!list.isEmpty()) {
                ItemStack stack = ItemStack.fromNbt(list.getCompound(0));
                this.evecual$crownInventory.setStack(0, stack);
                this.dataTracker.set(EVECUAL_CROWN, stack.copy());
            } else {
                this.evecual$crownInventory.setStack(0, ItemStack.EMPTY);
                this.dataTracker.set(EVECUAL_CROWN, ItemStack.EMPTY);
            }
        }
    }

    @Inject(method = "dropInventory", at = @At("TAIL"))
    private void evecual$dropCrownOnDeath(CallbackInfo ci) {
        if (!this.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
            ItemStack stack = this.evecual$crownInventory.getStack(0);
            if (!stack.isEmpty()) {
                PlayerEntity player = (PlayerEntity) (Object) this;
                player.dropItem(stack.copy(), true, false);
                this.evecual$crownInventory.setStack(0, ItemStack.EMPTY);
                this.dataTracker.set(EVECUAL_CROWN, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public SimpleInventory evecual$getCrownInventory() {
        return this.evecual$crownInventory;
    }

    @Override
    public ItemStack evecual$getCrown() {
        return this.dataTracker.get(EVECUAL_CROWN);
    }

    @Override
    public void evecual$setCrown(ItemStack crown) {
        this.evecual$crownInventory.setStack(0, crown);
        this.dataTracker.set(EVECUAL_CROWN, crown == null ? ItemStack.EMPTY : crown.copy());
    }

    @Override
    public boolean evecual$hasMechanicalCrown() {
        ItemStack crown = this.dataTracker.get(EVECUAL_CROWN);
        return crown != null && crown.isOf(EvecualMC.MECHANICAL_CROWN);
    }

    @Override
    public boolean evecual$hasBorgersCrown() {
        ItemStack crown = this.dataTracker.get(EVECUAL_CROWN);
        return crown != null && crown.isOf(EvecualMC.BORGERS_CROWN);
    }

    @Override
    public void evecual$syncCrown() {
        ItemStack stack = this.evecual$crownInventory.getStack(0);
        this.dataTracker.set(EVECUAL_CROWN, stack.copy());
    }
}
