package net.shirojr.simplenutrition.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
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
                                .executes(NutritionCommands::handleRemoval))));
    }

    private static int handleRemovalSelf(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ((Nutrition) player).simple_nutrition$clear();
        return Command.SINGLE_SUCCESS;
    }

    private static int handleRemoval(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        for (ServerPlayerEntity player : EntityArgumentType.getPlayers(context, "targets")) {
            ((Nutrition) player).simple_nutrition$clear();
        }
        return Command.SINGLE_SUCCESS;
    }


    public static void initialize() {
        CommandRegistrationCallback.EVENT.register(NutritionCommands::register);
    }
}
