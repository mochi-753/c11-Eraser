package com.test.eraser.network.packets;

import com.test.eraser.gui.BagMenu;
import com.test.eraser.gui.ClientBagGui;
import com.test.eraser.utils.ClientBagCache;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.mojang.text2speech.Narrator.LOGGER;

public class SyncBagPagesPacket {
    private final UUID bagId;
    private final int currentPage;
    private final List<ItemStack> prev;
    private final List<ItemStack> current;
    private final List<ItemStack> next;

    public SyncBagPagesPacket(UUID bagId, int currentPage,
                              List<ItemStack> prev, List<ItemStack> current, List<ItemStack> next) {
        this.bagId = bagId;
        this.currentPage = currentPage;
        this.prev = prev;
        this.current = current;
        this.next = next;
    }

    public static void encode(SyncBagPagesPacket pkt, FriendlyByteBuf buf) {//shityyyyyyyyyyyyy writeitem :(
        buf.writeUUID(pkt.bagId);
        buf.writeInt(pkt.currentPage);
        buf.writeCollection(pkt.prev, (b, stack) -> writeItemStackAsLongCount(b, stack));
        buf.writeCollection(pkt.current, (b, stack) -> writeItemStackAsLongCount(b, stack));
        buf.writeCollection(pkt.next, (b, stack) -> writeItemStackAsLongCount(b, stack));
    }

    public static SyncBagPagesPacket decode(FriendlyByteBuf buf) {
        UUID bagId = buf.readUUID();
        int page = buf.readInt();
        List<ItemStack> prev = buf.readList(b -> readItemStackAsLongCount(b));
        List<ItemStack> current = buf.readList(b -> readItemStackAsLongCount(b));
        List<ItemStack> next = buf.readList(b -> readItemStackAsLongCount(b));
        return new SyncBagPagesPacket(bagId, page, prev, current, next);
    }

    private static void writeItemStackAsLongCount(FriendlyByteBuf buf, ItemStack stack) {
        if (stack.isEmpty()) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeId(BuiltInRegistries.ITEM, stack.getItem());
            buf.writeVarLong(stack.getCount());
            CompoundTag tag = stack.getTag();
            buf.writeNbt(tag);
        }
    }

    private static ItemStack readItemStackAsLongCount(FriendlyByteBuf buf) {
        if (!buf.readBoolean()) {
            return ItemStack.EMPTY;
        } else {
            Item item = buf.readById(BuiltInRegistries.ITEM);
            long countFromNetwork = buf.readVarLong();
            int finalCount = (int) Math.max(0, Math.min(countFromNetwork, Integer.MAX_VALUE));
            CompoundTag tag = buf.readNbt();
            ItemStack stack = new ItemStack(item, finalCount);
            stack.setTag(tag);
            return stack;
        }
    }

    public static void handle(SyncBagPagesPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof ClientBagGui gui) {
                gui.update(pkt.bagId, pkt.currentPage, pkt.prev, pkt.current, pkt.next);

                BagMenu menu = gui.getMenu();
                menu.updatePageState(pkt.currentPage, pkt.current);
            }
            ClientBagCache.put(pkt.bagId, pkt.currentPage, pkt.prev, pkt.current, pkt.next);

        });
        ctx.get().setPacketHandled(true);
    }
}