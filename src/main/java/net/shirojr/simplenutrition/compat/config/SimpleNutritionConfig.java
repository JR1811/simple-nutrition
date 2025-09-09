package net.shirojr.simplenutrition.compat.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = "simple-nutrition")
public class SimpleNutritionConfig implements ConfigData {
    @Comment("Adds a new row below the Inventory's ItemStacks to visualize digestion order")
    public boolean loadGui = false;
    @Comment("Defines the digestion time in ticks, which with the players are starting out (will decrease with training)")
    public long baseDigestionTime = 6000L;
    @Comment("Specifies how many items an entity's stomach will remember at the same time")
    public int baseNutritionBufferSize = 10;
}
