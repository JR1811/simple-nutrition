package net.shirojr.simplenutrition.util;

import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class LinkedHashMapUtil {
    public static <T, U> void removeLastEntry(LinkedHashMap<T, U> map) {
        if (map.isEmpty()) return;

        Map.Entry<T, U> lastEntry = null;
        for (var entry : map.entrySet()) {
            lastEntry = entry;
        }
        if (lastEntry == null) return;
        map.remove(lastEntry.getKey());
    }

    @Nullable
    public static <T, U> Map.Entry<T, U> get(LinkedHashMap<T, U> map, int index) {
        if (index > map.size() - 1) return null;
        int i = 0;
        for (var entry : map.entrySet()) {
            if (i == index) {
                return entry;
            }
            i++;
        }
        return null;
    }

    @Nullable
    public static <T, U> Map.Entry<T, U> getLast(LinkedHashMap<T, U> map) {
        if (map.isEmpty()) return null;
        int index = map.size() - 1;
        return get(map, index);
    }
}
