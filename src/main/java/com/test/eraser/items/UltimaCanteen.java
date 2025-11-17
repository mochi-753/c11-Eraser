package com.test.eraser.items;

import com.test.eraser.gui.BagMenu;
import com.test.eraser.mixin.eraser.LivingEntityAccessor;
import com.test.eraser.network.PacketHandler;
import com.test.eraser.network.packets.SyncBagPagesPacket;
import com.test.eraser.utils.BagSavedData;
import com.test.eraser.utils.SynchedEntityDataUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class UltimaCanteen extends Item {
    private static final float SATURATION_TARGET = 7.0f;
    private static final int FOOD_LEVEL_TARGET = 20;

    public UltimaCanteen(Properties props) {
        super(props);
    }

    /*@Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (world.isClientSide()) {
            player.swing(hand, true);
            return InteractionResultHolder.consume(stack);
        }
        else {
            float maxHp = player.getMaxHealth();
            float healAmount = maxHp * 0.4f;
            float newHp = Math.min(player.getHealth() + healAmount, maxHp);
            player.setHealth(newHp);

            try {
                int currentFood = player.getFoodData().getFoodLevel();
                if (currentFood < FOOD_LEVEL_TARGET) {
                    int addFood = FOOD_LEVEL_TARGET - currentFood;
                    player.getFoodData().eat(addFood, SATURATION_TARGET);
                }

                float currentSat = player.getFoodData().getSaturationLevel();
                if (currentSat < SATURATION_TARGET) {
                    float addSat = SATURATION_TARGET - currentSat;
                    player.getFoodData().eat(0, addSat);
                }

            } catch (Throwable t) {
                t.printStackTrace();
            }

            world.playSound(null, player.blockPosition(), SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 1.0f, 1.0f);
            player.getCooldowns().addCooldown(this, 70);
        }

        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
    }*/

    @Override
    public Component getName(ItemStack stack) {
        String text = Component.literal("Canteen").getString();
        var result = Component.empty();
        long time = System.currentTimeMillis() / 50;

        for (int i = 0; i < text.length(); i++) {
            int color = waveGrayWhiteColor(time, i, 5.0);
            result = result.append(Component.literal(String.valueOf(text.charAt(i)))
                    .withStyle(style -> style.withColor(color)));
        }
        return result;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            ItemStack stack = player.getItemInHand(hand);
            ServerPlayer serverPlayer = (ServerPlayer) player;

            UUID bagId = getOrCreateBagId(stack);

            int totalPages = Integer.MAX_VALUE;
            int currentPage = 0;

            BagSavedData data = BagSavedData.get(level);
            List<ItemStack> prevPage = (currentPage > 0) ? data.getPage(bagId, currentPage - 1) : Collections.emptyList();
            List<ItemStack> currentPageItems = data.getPage(bagId, currentPage);
            List<ItemStack> nextPage = (currentPage < data.getTotalPages(bagId) - 1) ? data.getPage(bagId, currentPage + 1) : Collections.emptyList();

            PacketHandler.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new SyncBagPagesPacket(bagId, currentPage, prevPage, currentPageItems, nextPage)
            );

            NetworkHooks.openScreen(serverPlayer, new BagMenuProvider(bagId, currentPage, totalPages), buf -> {
                buf.writeUUID(bagId);
                buf.writeInt(currentPage);
                buf.writeInt(totalPages);
            });
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    private UUID getOrCreateBagId(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().hasUUID("bag_id")) {
            return stack.getTag().getUUID("bag_id");
        } else {
            UUID uuid = UUID.randomUUID();
            stack.getOrCreateTag().putUUID("bag_id", uuid);
            return uuid;
        }
    }

    public static class BagMenuProvider implements MenuProvider {
        private final UUID bagId;
        private final int page;
        private final int totalPages;

        public BagMenuProvider(UUID bagId, int page, int totalPages) {
            this.bagId = bagId;
            this.page = page;
            this.totalPages = totalPages;
        }

        @Override
        public Component getDisplayName() {
            return Component.literal("Canteen");
        }

        @Override
        public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
            return new BagMenu(windowId, inv, bagId, page, totalPages);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        long gameTime = (level != null) ? level.getGameTime() : 0;

        String special = "Solve food problem!";
        String[] partsSpecial = special.split(" ");
        var waveLineSpecial = Component.empty();

        int gold = 0xFFD700;
        int Gold2 = 0xD4AF37;

        for (int i = 0; i < partsSpecial.length; i++) {
            double wave = 0.5 + 0.5 * Math.sin((gameTime / 6.5) + i);
            int r = (int) (((gold >> 16) & 0xFF) * wave + ((Gold2 >> 16) & 0xFF) * (1 - wave));
            int g = (int) (((gold >> 8) & 0xFF) * wave + ((Gold2 >> 8) & 0xFF) * (1 - wave));
            int b = (int) ((gold & 0xFF) * wave + (Gold2 & 0xFF) * (1 - wave));
            int blended = (r << 16) | (g << 8) | b;

            waveLineSpecial = waveLineSpecial.append(
                    Component.literal(partsSpecial[i])
                            .withStyle(s -> s.withColor(blended))
            );
            if (i < partsSpecial.length - 1) {
                waveLineSpecial = waveLineSpecial.append(Component.literal(" "));
            }
        }
        tooltip.add(1, waveLineSpecial);
    }

    private static int waveGrayWhiteColor(long time, int index, double speed) {
        double wave = (Math.sin((time / speed) + index) + 1.0) / 2.0;
        int gray = 0xAAAAAA;
        int white = 0xFFFFFF;
        int r = (int) (((gray >> 16) & 0xFF) * (1 - wave) + ((white >> 16) & 0xFF) * wave);
        int g = (int) (((gray >> 8) & 0xFF) * (1 - wave) + ((white >> 8) & 0xFF) * wave);
        int b = (int) ((gray & 0xFF) * (1 - wave) + (white & 0xFF) * wave);

        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }
}