package net.shirojr.simplenutrition.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.world.World;
import net.shirojr.simplenutrition.compat.cca.components.NutritionComponent;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class NutritionCommands implements net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(literal("nutrition")
                .requires(source -> source.hasPermissionLevel(2))
                .then(literal("clear")
                        .executes(NutritionCommands::handleRemovalSelf)
                        .then(argument("targets", EntityArgumentType.players())
                                .executes(NutritionCommands::handleRemoval)))
                .then(literal("of")
                        .then(argument("targets", EntityArgumentType.players())
                                .executes(NutritionCommands::handlePrint)))
                .then(literal("digestion")
                        .then(argument("targets", EntityArgumentType.players())
                                .executes(NutritionCommands::handleDigestionPrint)
                                .then(argument("time", LongArgumentType.longArg())
                                        .executes(NutritionCommands::handleSetDigestionTime))))
        );
    }

    private static int handleDigestionPrint(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var players = EntityArgumentType.getPlayers(context, "targets");
        for (ServerPlayerEntity player : players) {
            NutritionComponent nutritionComponent = NutritionComponent.get(player);
            Pair<String, Integer> time = getFormattedTime(nutritionComponent.getDigestionDuration());
            context.getSource().sendFeedback(() -> Text.literal("%s - %s %s"
                            .formatted(player.getName().getString(), time.getRight(), time.getLeft())),
                    false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int handleSetDigestionTime(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        StringBuilder sb = new StringBuilder("Set digestion time for");
        var players = EntityArgumentType.getPlayers(context, "targets");
        long ticks = LongArgumentType.getLong(context, "time");
        Pair<String, Integer> time = getFormattedTime(ticks);
        for (ServerPlayerEntity player : players) {
            NutritionComponent nutritionComponent = NutritionComponent.get(player);
            nutritionComponent.setDigestionDuration(ticks, true);
            sb.append(" [%s]".formatted(player.getName().getString()));
        }
        sb.append(" to %s %s".formatted(time.getRight(), time.getLeft()));
        context.getSource().sendFeedback(() -> Text.literal(sb.toString()), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int handlePrint(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        for (ServerPlayerEntity player : EntityArgumentType.getPlayers(context, "targets")) {
            NutritionComponent nutritionComponent = NutritionComponent.get(player);

            var nutritionEntries = nutritionComponent.getNutritionBuffer().entrySet();
            long digestionDuration = nutritionComponent.getDigestionDuration();
            Pair<String, Integer> formattedTime = getFormattedTime(digestionDuration);
            context.getSource().sendFeedback(() ->
                    Text.literal("---- [ %s | digestion duration: %s %s ] ---".formatted(
                            player.getName().getString(), formattedTime.getLeft(), formattedTime.getRight())), false);

            for (var entry : nutritionEntries) {
                World world = player.getWorld();
                ItemStack nutritionStack = entry.getKey().copy();
                Pair<String, Integer> time = getFormattedTime(world.getTime() - entry.getValue());

                context.getSource().sendFeedback(() ->
                                Text.literal("%sx %s (%s %s ago)".formatted(nutritionStack.getCount(),
                                        nutritionStack.getItem().getName().getString(), time.getRight(), time.getLeft())),
                        false);
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int handleRemovalSelf(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();
        }
        NutritionComponent nutritionComponent = NutritionComponent.get(player);
        nutritionComponent.clear();
        context.getSource().getPlayer().sendMessage(Text.literal("cleared digested item list"), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int handleRemoval(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        for (ServerPlayerEntity player : EntityArgumentType.getPlayers(context, "targets")) {
            NutritionComponent nutritionComponent = NutritionComponent.get(player);
            nutritionComponent.clear();
            context.getSource().sendFeedback(() -> Text.literal("cleared digested item list for " + player.getName().getString()), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static Pair<String, Integer> getFormattedTime(long ticks) {
        long time = ticks;
        String timeUnit = "Ticks";
        if (time >= 20) {
            time /= 20;
            timeUnit = "Seconds";
            if (time >= 60) {
                time /= 60;
                timeUnit = "Minutes";
                if (time >= 60) {
                    time /= 60;
                    timeUnit = "Hours";
                    if (time >= 24) {
                        time /= 24;
                        timeUnit = "Days";
                    }
                }
            }
        }
        return new Pair<>(timeUnit, (int) time);
    }

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register(new NutritionCommands());
    }
}
