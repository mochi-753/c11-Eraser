package com.test.eraser.additional;

import com.test.eraser.logic.ILivingEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

public class SnackArmor {

    public static final ArmorMaterial SNACK_MATERIAL = new ArmorMaterial() {
        @Override
        public int getDurabilityForType(Type type) {
            return 9999;
        }

        @Override
        public int getDefenseForType(Type type) {
            return 1919114514;
        }

        @Override
        public int getEnchantmentValue() {
            return 30;
        }

        @Override
        public net.minecraft.sounds.SoundEvent getEquipSound() {
            return SoundEvents.ARMOR_EQUIP_LEATHER;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(Items.COOKIE);
        }

        @Override
        public @NotNull String getName() {
            return "snack_protector";
        }

        @Override
        public float getToughness() {
            return 0.0F;
        }

        @Override
        public float getKnockbackResistance() {
            return 0.0F;
        }
    };

    public static class SnackHelmet extends ArmorItem {
        public SnackHelmet() {
            super(SNACK_MATERIAL, Type.HELMET, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
        }
    }

    public static class SnackChestplate extends ArmorItem {
        public SnackChestplate() {
            super(SNACK_MATERIAL, Type.CHESTPLATE, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
        }
    }

    public static class SnackLeggings extends ArmorItem {
        public SnackLeggings() {
            super(SNACK_MATERIAL, Type.LEGGINGS, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
        }
    }

    public static class SnackBoots extends ArmorItem {
        public SnackBoots() {
            super(SNACK_MATERIAL, Type.BOOTS, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
        }
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class SnackProtector {

        public static boolean isFullSet(Player player) {
            ItemStack head = player.getInventory().armor.get(3);
            ItemStack chest = player.getInventory().armor.get(2);
            ItemStack legs = player.getInventory().armor.get(1);
            ItemStack feet = player.getInventory().armor.get(0);

            return head.getItem() == ModItems.SNACK_HELMET.get()
                    && chest.getItem() == ModItems.SNACK_CHESTPLATE.get()
                    && legs.getItem() == ModItems.SNACK_LEGGINGS.get()
                    && feet.getItem() == ModItems.SNACK_BOOTS.get();
        }

        public static boolean hasSnackProtector(Player player) {
            ItemStack head = player.getInventory().armor.get(3);
            ItemStack chest = player.getInventory().armor.get(2);
            ItemStack legs = player.getInventory().armor.get(1);
            ItemStack feet = player.getInventory().armor.get(0);

            return head.getItem() == ModItems.SNACK_HELMET.get()
                    || chest.getItem() == ModItems.SNACK_CHESTPLATE.get()
                    || legs.getItem() == ModItems.SNACK_LEGGINGS.get()
                    || feet.getItem() == ModItems.SNACK_BOOTS.get();
        }

        @SubscribeEvent
        public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
            if (!(event.getEntity() instanceof Player player)) return;

            if (isFullSet(player)) {
                applyAbilities(player);
            } else if (player instanceof ILivingEntity Iliving) {
                if (Iliving.wasFullset()) resetAbilities(player);
            }
        }

        @SubscribeEvent
        public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
            Player player = event.getEntity();
            if (!player.level().isClientSide && isFullSet(player)) {
                applyAbilities(player);
            }
        }

        @SubscribeEvent
        public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
            Player player = event.getEntity();
            if (!player.level().isClientSide && isFullSet(player)) {
                applyAbilities(player);
            }
        }

        @SubscribeEvent
        public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
            Player player = event.getEntity();
            if (!player.level().isClientSide && isFullSet(player)) {
                applyAbilities(player);
            }
        }

        /*@SubscribeEvent
        public static void onProjectileImpact(ProjectileImpactEvent event) {
            if (!(event.getEntity() instanceof AbstractArrow arrow)) return;

            if (event.getRayTraceResult() instanceof EntityHitResult hit) {
                if (hit.getEntity() instanceof ServerPlayer player) {
                    if (SnackProtector.isEnabled(player)) {
                        arrow.discard();
                        event.setCanceled(true);

                        Vec3 pos = arrow.position();
                        PacketHandler.sendToPlayer(new ShieldEffectPacket(pos), player);
                    }
                }
            }
        }*/

        private static void applyAbilities(Player player) {
            if (player.getAbilities().mayfly && player.getAbilities().invulnerable) return;
            player.getAbilities().mayfly = true;
            if (!player.onGround()) player.getAbilities().flying = true;
            player.getAbilities().invulnerable = true;
            player.onUpdateAbilities();
            player.getFoodData().setFoodLevel(20);
            player.getFoodData().setSaturation(20.0F);
            player.setHealth(player.getMaxHealth());
            if (player instanceof ILivingEntity Iliving) Iliving.setwassFullset(true);
        }

        private static void resetAbilities(Player player) {
            player.getAbilities().mayfly = false;
            player.getAbilities().invulnerable = false;
            player.onUpdateAbilities();
            if (player instanceof ILivingEntity Iliving) Iliving.setwassFullset(false);
        }

        @SubscribeEvent
        public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
            if (!(event.getNewTarget() instanceof Player player)) return;

            if (SnackProtector.isFullSet(player)) {
                event.setCanceled(true);
            }
        }

    }
}
