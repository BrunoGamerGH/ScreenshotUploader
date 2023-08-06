package me.bruno.screenshotuploader;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ScreenshotUploader implements ClientModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("screenshotuploader");
	private static ScreenshotUploader instance;

	private Map<String, NativeImage> imageMap;

	public static ScreenshotUploader getInstance() {
		return instance;
	}

	public void addImage(String name, NativeImage image) {
		imageMap.put(name, image);
	}

	public NativeImage getImage(String name) {
		return imageMap.getOrDefault(name, null);
	}

	public void copyImageToClipboard(ClientPlayerEntity source, String name) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player != null || client.isInSingleplayer() || client.isIntegratedServerRunning() || client.player.getServer() != null) {
			if (hasImage(name)) {
				NativeImage image = getImage(name);
				ClipboardUtil.copy(image);
				source.sendMessage(Text.literal("Copied screenshot."));
			}
		}
	}

	public boolean hasImage(String name) {
		return imageMap.containsKey(name);
	}

	@Override
	public void onInitializeClient() {
		instance = this;
		imageMap = new HashMap<>();
		try {
			Toolkit.getDefaultToolkit().getSystemClipboard();
		} catch (HeadlessException e) {
			LOGGER.warn("java.awt.headless property was not set properly!");
		}
	}
}


