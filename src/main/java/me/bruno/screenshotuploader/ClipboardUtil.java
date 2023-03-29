package me.bruno.screenshotuploader;

import com.mojang.logging.LogUtils;
import me.bruno.screenshotuploader.mixin.NativeImagePointerAccessor;
import net.minecraft.client.texture.NativeImage;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

public class ClipboardUtil {

    // By Screenshot To Clipboard
    public static void copy(NativeImage img) {
        if (img.getFormat() != NativeImage.Format.RGBA) {
            LogUtils.getLogger().warn("This format isnt allowed.");
            return;
        }

        long imagePointer = ((NativeImagePointerAccessor) (Object) img).getPointer();

        ByteBuffer buf = MemoryUtil.memByteBufferSafe(imagePointer, img.getWidth() * img.getHeight() * 4);
        if (buf == null) {
            throw new RuntimeException("Invalid image");
        }

        byte[] array;
        if (buf.hasArray()) {
            array = buf.array();
        } else {
            array = new byte[img.getHeight() * img.getWidth() * 4];
            buf.get(array);
        }

        new Thread(() -> {
            DataBufferByte dataBuf = new DataBufferByte(array, array.length);
            ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
            // Ignore the alpha channel, due to JDK-8204187
            int[] nBits = {8, 8, 8};
            int[] bOffs = {0, 1, 2}; // is this efficient, no transformation is being done?
            ColorModel cm = new ComponentColorModel(cs, nBits, false, false,
                    Transparency.TRANSLUCENT,
                    DataBuffer.TYPE_BYTE);
            BufferedImage bufImg = new BufferedImage(cm, Raster.createInterleavedRaster(dataBuf,
                    img.getWidth(), img.getHeight(),
                    img.getWidth() * 4, 4,
                    bOffs, null), false, null);

            Transferable trans = getTransferableImage(bufImg);
            Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
            c.setContents(trans, null);
        }, "Copy to clipboard task").start();


    }

    private static Transferable getTransferableImage(final BufferedImage bufferedImage) {
        return new Transferable() {
            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[]{DataFlavor.imageFlavor};
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return DataFlavor.imageFlavor.equals(flavor);
            }

            @Override
            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                if (DataFlavor.imageFlavor.equals(flavor)) {
                    return bufferedImage;
                }
                throw new UnsupportedFlavorException(flavor);
            }
        };
    }
}

