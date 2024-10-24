package net.shirojr.simplenutrition.data;

import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;

import java.util.LinkedHashMap;

public class TrackedDataUtil {
    public static final int NUTRITION_BUFFER_SIZE = 10;

    public static final TrackedDataHandler<LinkedHashMap<ItemStack, Long>> ITEM_QUEUE = new TrackedDataHandler<>() {
        @Override
        public void write(PacketByteBuf buf, LinkedHashMap<ItemStack, Long> value) {
            buf.writeVarInt(value.size());
            for (var entry : value.entrySet()) {
                buf.writeItemStack(entry.getKey());
                buf.writeLong(entry.getValue());
            }
        }

        @Override
        public LinkedHashMap<ItemStack, Long> read(PacketByteBuf buf) {
            LinkedHashMap<ItemStack, Long> stacks = new LinkedHashMap<>();
            int size = buf.readVarInt();
            for (int i = 0; i < size; i++) {
                stacks.put(buf.readItemStack(), buf.readLong());
            }
            return stacks;
        }

        @Override
        public LinkedHashMap<ItemStack, Long> copy(LinkedHashMap<ItemStack, Long> value) {
            return value;
        }
    };

    public static final TrackedDataHandler<Long> LONG = new TrackedDataHandler<>() {
        @Override
        public void write(PacketByteBuf buf, Long value) {
            buf.writeLong(value);
        }

        @Override
        public Long read(PacketByteBuf buf) {
            return buf.readLong();
        }

        @Override
        public Long copy(Long value) {
            return value;
        }
    };
}
