package net.shirojr.simplenutrition.mixin.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.shirojr.simplenutrition.SimpleNutrition;
import net.shirojr.simplenutrition.compat.NutritionData;
import net.shirojr.simplenutrition.nutrition.Nutrition;
import net.shirojr.simplenutrition.util.LinkedHashMapUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractInventoryScreen<PlayerScreenHandler> implements RecipeBookProvider {
    @Unique
    private static final Identifier TEXTURE = new Identifier(SimpleNutrition.MOD_ID, "textures/gui/slots.png");

    public InventoryScreenMixin(PlayerScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderNutritionItems(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!NutritionData.shouldLoadGui()) return;
        if (this.client == null || this.client.player == null) return;
        Nutrition nutrition = (Nutrition) this.client.player;
        var nutritionEntries = nutrition.simple_nutrition$getNutritionStacks();
        int x = this.client.getWindow().getScaledWidth() / 2 - 80;
        int y = this.client.getWindow().getScaledHeight() / 2 + 86;
        int gap = 18;

        for (int i = 0; i < nutritionEntries.size(); i++) {
            var entry = LinkedHashMapUtil.get(nutritionEntries, i);
            if (entry == null) continue;
            ItemStack stack = entry.getKey().copy();
            context.drawItem(stack, x + gap * i, y + 20);
        }
    }

    @Inject(method = "drawForeground", at = @At("TAIL"))
    private void renderNutritionSlotsTitle(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        if (!NutritionData.shouldLoadGui()) return;
        if (this.client == null || this.client.player == null) return;
        Nutrition nutrition = (Nutrition) this.client.player;
        if (nutrition.simple_nutrition$getNutritionStacks().isEmpty()) return;
        int x = 7, y = 177;
        context.drawText(this.textRenderer, Text.translatable("gui.simple-nutrition.nutrition_list"), x, y, 4210752, false);
    }

    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void renderNutritionSlots(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        if (!NutritionData.shouldLoadGui()) return;
        if (this.client == null || this.client.player == null) return;
        Nutrition nutrition = (Nutrition) this.client.player;
        var nutritionEntries = nutrition.simple_nutrition$getNutritionStacks();
        if (nutritionEntries.isEmpty()) return;

        drawNutritionSlots(context, nutritionEntries.size(),
                this.client.getWindow().getScaledWidth() / 2 - 87,
                this.client.getWindow().getScaledHeight() / 2 + 90);
    }

    @Unique
    @SuppressWarnings("SameParameterValue")
    private void drawNutritionSlots(DrawContext context, int slotAmount, int x, int y) {
        if (!NutritionData.shouldLoadGui()) return;
        int slotCount = Math.max(slotAmount, 3);
        if (slotCount == 3) {
            context.drawTexture(TEXTURE, x, y, 1, 1, 65, 38, 128, 128);
        } else {
            int startWith = 24, startHeight = 38, startU = 1, startV = 1;
            int midWith = 18, midHeight = 38, midU = 25, midV = 1;
            int endWith = 24, endHeight = 38, endU = 43, endV = 1;

            slotCount -= 2; // start and end piece are separate

            // start piece
            context.drawTexture(TEXTURE, x, y, startU, startV, startWith, startHeight, 128, 128);
            // mid piece(s)
            for (int i = 0; i < slotCount; i++) {
                context.drawTexture(TEXTURE, x + startWith + (i * midWith), y, midU, midV, midWith, midHeight, 128, 128);
            }
            // end piece
            context.drawTexture(TEXTURE, x + startWith + (slotCount * midWith), y, endU, endV, endWith, endHeight, 128, 128);
        }
    }
}
