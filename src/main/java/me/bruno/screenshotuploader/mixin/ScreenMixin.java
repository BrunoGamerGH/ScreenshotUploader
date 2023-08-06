package me.bruno.screenshotuploader.mixin;

import me.bruno.screenshotuploader.ScreenshotSaveClickEvent;
import me.bruno.screenshotuploader.ScreenshotUploader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Inject(at = @At("HEAD"), method = "handleTextClick")
    public void handleTextClickWithCustomEvent(Style style, CallbackInfoReturnable<Boolean> cir) {
        if (style != null) {
            if (style.getClickEvent() instanceof ScreenshotSaveClickEvent) {
                String value = style.getClickEvent().getValue();
                try {
                    if (MinecraftClient.getInstance().player != null) {
                        if (ScreenshotUploader.getInstance().hasImage(value)) {
                            ScreenshotUploader.getInstance().copyImageToClipboard(MinecraftClient.getInstance().player, value);
                        } else {
                            MinecraftClient.getInstance().player.sendMessage(Text.literal("Screenshot name doesn't exist"));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


}