package me.bruno.screenshotuploader.mixin;

import com.mojang.logging.LogUtils;
import me.bruno.screenshotuploader.ScreenshotSaveClickEvent;
import me.bruno.screenshotuploader.ScreenshotUploader;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;

@Mixin(ScreenshotRecorder.class)
public class ScreeenShotRecorderMixin {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
    private static File getScreenshotFilename(File directory) {
        String string = DATE_FORMAT.format(new Date());
        int i = 1;

        while(true) {
            File file = new File(directory, string + (i == 1 ? "" : "_" + i) + ".png");
            if (!file.exists()) {
                return file;
            }

            ++i;
        }
    }

        @Inject(at = @At("HEAD"), method = "saveScreenshotInner", cancellable = true)
    private static void saveScreenshotInnerHead(File gameDirectory, String fileName, Framebuffer framebuffer, Consumer<Text> messageReceiver, CallbackInfo ci) {

            NativeImage nativeImage = ScreenshotRecorder.takeScreenshot(framebuffer);
            File file = new File(gameDirectory, "screenshots");
            file.mkdir();
            File file2;
            if (fileName == null) {
                file2 = getScreenshotFilename(file);
            } else {
                file2 = new File(file, fileName);
            }
            ScreenshotUploader.getInstance().addImage(file2.getName(), nativeImage);

            Util.getIoWorkerExecutor().execute(() -> {
                try {
                    nativeImage.writeTo(file2);
                    MutableText main = MutableText.of(new LiteralTextContent(""));
                    Text saved = Text.literal("Saved Screenshot").formatted(Formatting.UNDERLINE);
                    main.append(saved);
                    main.append(" ");
                    Text open = Text.literal("[OPEN]").formatted(Formatting.BOLD, Formatting.GOLD).styled(style -> style
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file2.getAbsolutePath()))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(file2.getName()))));
                    main.append(open);
                    main.append(" ");
                    Text copy = Text.literal("[COPY]").formatted(Formatting.BOLD, Formatting.BLUE).styled(style -> style
                            .withClickEvent(new ScreenshotSaveClickEvent(file2.getName()))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Copy the screenshot"))));
                    main.append(copy);
                    messageReceiver.accept(main);
                } catch (Exception var7) {
                    LOGGER.warn("Couldn't save screenshot", var7);
                    messageReceiver.accept(Text.translatable("screenshot.failure", var7.getMessage()));
                } finally {
                    ci.cancel();
                }

            });
            ci.cancel();
    }
}
