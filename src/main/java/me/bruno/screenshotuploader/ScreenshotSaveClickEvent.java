package me.bruno.screenshotuploader;

import net.minecraft.text.ClickEvent;

public class ScreenshotSaveClickEvent extends ClickEvent {
    public ScreenshotSaveClickEvent(String value) {
        super(null, value);
    }
}
