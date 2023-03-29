package me.bruno.screenshotuploader.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import me.bruno.screenshotuploader.ScreenshotUploader;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CopyScreenshotCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
            dispatcher.register(literal("copylatestscreenshot")
                    .requires(ServerCommandSource::isExecutedByPlayer)
                    .then(argument("name", StringArgumentType.greedyString())
                            .executes(context -> copyScreenshot(context.getSource(), StringArgumentType.getString(context, "name"))))
            );
    }

    private static int copyScreenshot(ServerCommandSource source, String name) {
        try {
            if (ScreenshotUploader.getInstance().hasImage(name)) {
                ScreenshotUploader.getInstance().copyImageToClipboard(source, name);
                return 1;
            } else {
                source.getPlayer().sendMessage(Text.literal("Screenshot name doesn't exist"));
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
