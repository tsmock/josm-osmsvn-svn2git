// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.wms;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.IndexColorModel;

import org.openstreetmap.josm.spi.preferences.Config;

public class RasterImageModifier extends ImageModifier {

    private int cadastreBackground = -1; // white

    public static int cadastreBackgroundTransp = 16777215; // original white but transparent

    private boolean transparencyEnabled = false;

    public RasterImageModifier(BufferedImage bi) {
        setBufferedImage(bi);
        transparencyEnabled = Config.getPref().getBoolean("cadastrewms.backgroundTransparent");
        if (transparencyEnabled)
            makeTransparent();
        if (Config.getPref().getBoolean("cadastrewms.invertGrey"))
            invertGrey();
    }

    /**
     * Invert black/white/grey pixels (to change original black characters to white).
     */
    private void invertGrey() {
        int w = bufferedImage.getWidth();
        int h = bufferedImage.getHeight();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int pixel = bufferedImage.getRGB(x, y);
                if ((!transparencyEnabled && pixel != cadastreBackground)
                        || (transparencyEnabled && pixel != cadastreBackgroundTransp)) {
                    bufferedImage.setRGB(x, y, reverseIfGrey(pixel));
                }
            }
        }
    }

    /**
     * Reverse the grey value if the pixel is grey (light grey becomes dark grey)
     * Used for texts.
     */
    private int reverseIfGrey(int pixel) {
        Color col = new Color(pixel);
        int r = col.getRed();
        int g = col.getGreen();
        int b = col.getBlue();
        if ((b == r) && (b == g)) {
            pixel = (0x00 << 24) + ((byte) (255 - r) << 16) + ((byte) (255 - r) << 8) + ((byte) (255 - r));
        }
        return pixel;
    }

    private void makeTransparent() {
        if (bufferedImage.getColorModel() instanceof ComponentColorModel ||
            bufferedImage.getColorModel() instanceof IndexColorModel) {
            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();
            BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            // converting grey scale colors to black/white is configurable (use less resources but is less readable)
            boolean simplifyColors = Config.getPref().getBoolean("cadastrewms.raster2bitsColors", false);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = bufferedImage.getRGB(x, y);
                    Color c = new Color(rgb);
                    int r = c.getRed();
                    int g = c.getGreen();
                    int b = c.getBlue();
                    Color maskedColor;
                    if (rgb == cadastreBackground) {
                        maskedColor = simplifyColors ? new Color(0xff, 0xff, 0xff, 0x00) :
                            new Color(r, g, b, 0x00); // transparent
                    } else {
                        maskedColor = simplifyColors ? new Color(0, 0, 0, 0xFF) :
                            new Color(r, g, b, 0xFF); // opaque
                    }
                    bi.setRGB(x, y, maskedColor.getRGB());
                }
            }
            setBufferedImage(bi);
        }
        return;
    }

    /**
     * Temporary fix for Java6 which doesn't de-serialize correctly cached image on disk.
     * Recreate a new raster image based on what is loaded/serialized from disk cache.
     * @return new image
     */
    public static BufferedImage fixRasterImage(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] rgbArray = new int[width * height];
        img.getRGB(0, 0, width, height, rgbArray, 0, width);
        bi.setRGB(0, 0, width, height, rgbArray, 0, width);
        return bi;
    }

}
