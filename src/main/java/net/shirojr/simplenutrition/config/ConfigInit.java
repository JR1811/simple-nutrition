package net.shirojr.simplenutrition.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;

public class ConfigInit {
    public static SimpleNutritionConfig CONFIG = new SimpleNutritionConfig();

    public static void initialize() {
        AutoConfig.register(SimpleNutritionConfig.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(SimpleNutritionConfig.class).getConfig();
    }
}
