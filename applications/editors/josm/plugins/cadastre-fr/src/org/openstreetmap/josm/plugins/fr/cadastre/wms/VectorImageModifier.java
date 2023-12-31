// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.wms;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.ColorHelper;

public class VectorImageModifier extends ImageModifier {

    private int cadastreBackground = -1; // white

    public static int cadastreBackgroundTransp = 1; // original white but transparent

    private int backgroundPixel = 0;

    public VectorImageModifier() {
        super();
    }

    public VectorImageModifier(BufferedImage bi, boolean monocolor) {
        setBufferedImage(bi);
        if (Config.getPref().getBoolean("cadastrewms.backgroundTransparent"))
            makeTransparent();
        else if (Config.getPref().getBoolean("cadastrewms.alterColors"))
            replaceBackground();
        if (Config.getPref().getBoolean("cadastrewms.invertGrey"))
            invertGrey();
        if (monocolor)
            setBufferedImage(convert8(convert4(bufferedImage)));
    }

    /**
     * Replace the background color by the josm color.background color.
     */
    private void replaceBackground() {
        int w = bufferedImage.getWidth();
        int h = bufferedImage.getHeight();
        int josmBackgroundColor = ColorHelper.html2color(Config.getPref().get("color.background", "#000000")).getRGB();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int pixel = bufferedImage.getRGB(x, y);
                if (pixel == cadastreBackground) {
                    bufferedImage.setRGB(x, y, josmBackgroundColor);
                }
            }
        }
        // The cadastre has now a new background (for invertGrey())
        cadastreBackground = josmBackgroundColor;
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
                if (pixel != cadastreBackground) {
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
            pixel = ((byte) col.getAlpha() << 24) + ((byte) (255 - r) << 16) + ((byte) (255 - r) << 8) + ((byte) (255 - r));
        }
        // Maybe we should try to find a better formula to avoid discontinuity when text is drawn on a colored item
        // (building, ...). One could use the conversion to and from HSB to only change the brightness not the color.
        // Note: the color palette does not have the inverse colors so it may be very weird!
        return pixel;
    }

    private void makeTransparent() {
        ColorModel colorModel = bufferedImage.getColorModel();
        if (bufferedImage.getColorModel() instanceof IndexColorModel) {
            // vector image (IndexColorModel)
            IndexColorModel icm = (IndexColorModel) colorModel;
            WritableRaster raster = bufferedImage.getRaster();
            // pixel is offset in ICM's palette
            backgroundPixel = 1; // default Cadastre background sample
            int size = icm.getMapSize();
            byte[] reds = new byte[size];
            byte[] greens = new byte[size];
            byte[] blues = new byte[size];
            icm.getReds(reds);
            icm.getGreens(greens);
            icm.getBlues(blues);
            // The cadastre background has now an alpha to 0 (for invertGrey())
            cadastreBackground = 0x00ffffff;
            IndexColorModel icm2 = new IndexColorModel(colorModel.getPixelSize(), size, reds, greens, blues,
                    backgroundPixel);
            setBufferedImage(new BufferedImage(icm2, raster, bufferedImage.isAlphaPremultiplied(), null));
        }
        return;
    }
}
