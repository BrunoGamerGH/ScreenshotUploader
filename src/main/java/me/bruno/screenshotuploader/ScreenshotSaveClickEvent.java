package me.bruno.screenshotuploader;

import net.minecraft.text.ClickEvent;

import java.io.File;

public class ScreenshotSaveClickEvent extends ClickEvent {

    File value;
    public ScreenshotSaveClickEvent(File value) {
        super(null, null);
        this.value = value;
    }

    public File getFile() {
        return value;
    }
}
