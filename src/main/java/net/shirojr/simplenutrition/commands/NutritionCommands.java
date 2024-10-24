package net.shirojr.simplenutrition.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Pair;
import net.minecraft.world.World;
import net.shirojr.simplenutrition.nutrition.Nutrition;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class NutritionCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean b) {
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
            Nutrition nutrition = (Nutrition) player;
            Pair<String, Integer> time = getFormattedTime(nutrition.simple_nutrition$getDigestionDuration());
            context.getSource().getPlayer().sendMessage(new LiteralText("%s - %s %s"
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
            ((Nutrition) player).simple_nutrition$setDigestionDuration(ticks);
            sb.append(" [%s]".formatted(player.getName().getString()));
        }
        sb.append(" to %s %s".formatted(time.getRight(), time.getLeft()));
        context.getSource().getPlayer().sendMessage(new LiteralText(sb.toString()), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int handlePrint(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        for (ServerPlayerEntity player : EntityArgumentType.getPlayers(context, "targets")) {
            context.getSource().getPlayer().sendMessage(
                    new LiteralText("---- [ %s | digestion duration: %s ] ---".formatted(
                            player.getName().getString(),
                            ((Nutrition) player).simple_nutrition$getDigestionDuration())),
                    false);
            for (var entry : ((Nutrition) player).simple_nutrition$getNutritionStacks().entrySet()) {
                World world = player.getWorld();
                ItemStack nutritionStack = entry.getKey().copy();
                Pair<String, Integer> time = getFormattedTime(world.getTime() - entry.getValue());

                context.getSource().getPlayer().sendMessage(
                        new LiteralText("%sx %s (%s %s ago)".formatted(nutritionStack.getCount(),
                                nutritionStack.getItem().getName().getString(), time.getRight(), time.getLeft())),
                        false);
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int handleRemovalSelf(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ((Nutrition) player).simple_nutrition$clear();
        context.getSource().getPlayer().sendMessage(new LiteralText("cleared digested item list"), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int handleRemoval(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        for (ServerPlayerEntity player : EntityArgumentType.getPlayers(context, "targets")) {
            ((Nutrition) player).simple_nutrition$clear();
            context.getSource().getPlayer().sendMessage(new LiteralText("cleared digested item list for " + player.getName().getString()), false);
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
        CommandRegistrationCallback.EVENT.register(NutritionCommands::register);
    }
}
