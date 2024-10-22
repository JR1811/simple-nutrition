package net.shirojr.simplenutrition.data;

import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;

import java.util.ArrayList;
import java.util.List;

public class TrackedDataUtil {
    public static final int NUTRITION_BUFFER_SIZE = 10;

    public static final TrackedDataHandler<List<ItemStack>> ITEM_QUEUE = new TrackedDataHandler<>() {
        @Override
        public void write(PacketByteBuf buf, List<ItemStack> value) {
            buf.writeVarInt(value.size());
            for (ItemStack entry : value) {
                buf.writeItemStack(entry);
            }
        }

        @Override
        public List<ItemStack> read(PacketByteBuf buf) {
            List<ItemStack> stacks = new ArrayList<>();
            int size = buf.readVarInt();
            for (int i = 0; i < size; i++) {
                stacks.add(buf.readItemStack());
            }
            return stacks;
        }

        @Override
        public List<ItemStack> copy(List<ItemStack> value) {
            return value;
        }
    };
}
