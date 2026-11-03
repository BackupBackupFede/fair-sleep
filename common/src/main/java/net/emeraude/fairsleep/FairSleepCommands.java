package net.emeraude.fairsleep;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.emeraude.fairsleep.compat.Compat;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** {@code /fairsleep settings} and {@code /fairsleep reload}, both for operators. */
public final class FairSleepCommands {

    private static final int OPERATOR = 2;

    private FairSleepCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // The Commands constructor runs more than once (startup validation, /reload).
        if (dispatcher.getRoot().getChild(FairSleep.MOD_ID) != null) return;
        dispatcher.register(Commands.literal(FairSleep.MOD_ID)
            .requires(source -> Compat.hasPermission(source, OPERATOR))
            .then(Commands.literal("settings").executes(FairSleepCommands::settings))
            .then(Commands.literal("reload").executes(FairSleepCommands::reload)));
    }

    private static int settings(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendSystemMessage(error("Open this as a player, or edit config/fairsleep.json."));
            return 0;
        }
        SettingsMenu.open(player);
        return 1;
    }

    private static int reload(CommandContext<CommandSourceStack> ctx) {
        String failure = FairSleep.reloadConfig();
        if (failure == null && !FairSleep.config().aggroAllowed) Sleepers.forgetAll(ctx.getSource().getServer().getAllLevels());
        ctx.getSource().sendSystemMessage(failure == null
            ? info("Fair Sleep config reloaded.")
            : error("Config not reloaded: " + failure));
        return failure == null ? 1 : 0;
    }

    private static Component info(String text) {
        return Component.literal(text).withStyle(ChatFormatting.GRAY);
    }

    private static Component error(String text) {
        return Component.literal(text).withStyle(ChatFormatting.RED);
    }
}
