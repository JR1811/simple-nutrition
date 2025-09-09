package net.shirojr.simplenutrition.compat.cca;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import net.shirojr.simplenutrition.compat.cca.components.NutritionComponent;
import net.shirojr.simplenutrition.compat.cca.impl.NutritionComponentImpl;

public class SimpleNutritionComponents implements EntityComponentInitializer {
    public static final ComponentKey<NutritionComponent> NUTRITION =
            ComponentRegistry.getOrCreate(NutritionComponent.KEY, NutritionComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(NUTRITION, NutritionComponentImpl::new, NutritionComponentImpl::onRespawn);
    }
}
