package wmsplugin;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.imageio.ImageIO;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.gui.NavigatableComponent;

public class GeorefImage implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum State { IMAGE, NOT_IN_CACHE, FAILED};

    private final WMSLayer layer;
    private State state;

    private BufferedImage image;
    private BufferedImage reImg = null;
    private int xIndex;
    private int yIndex;


    public EastNorth getMin() {
        return layer.getEastNorth(xIndex, yIndex);
    }

    public EastNorth getMax() {
        return layer.getEastNorth(xIndex+1, yIndex+1);
    }


    public GeorefImage(WMSLayer layer) {
        this.layer = layer;
    }

    public void changePosition(int xIndex, int yIndex) {
        if (!equalPosition(xIndex, yIndex)) {
            this.xIndex = xIndex;
            this.yIndex = yIndex;
            this.image = null;
            this.reImg = null;
        }
    }

    public boolean equalPosition(int xIndex, int yIndex) {
        return this.xIndex == xIndex && this.yIndex == yIndex;
    }

    public void changeImage(State state, BufferedImage image) {
        this.image = image;
        this.reImg = null;
        this.state = state;

        switch (state) {
        case FAILED:
        {
            BufferedImage img = createImage();
            Graphics g = img.getGraphics();
            g.setColor(Color.RED);
            g.fillRect(0, 0, img.getWidth(), img.getHeight());
            g.setFont(g.getFont().deriveFont(Font.PLAIN).deriveFont(36.0f));
            g.setColor(Color.BLACK);
            g.drawString(tr("Exception occurred"), 10, img.getHeight()/2);
            this.image = img;
            break;
        }
        case NOT_IN_CACHE:
        {
            BufferedImage img = createImage();
            Graphics g = img.getGraphics();
            g.setColor(Color.GRAY);
            g.fillRect(0, 0, img.getWidth(), img.getHeight());
            Font font = g.getFont();
            Font tempFont = font.deriveFont(Font.PLAIN).deriveFont(36.0f);
            g.setFont(tempFont);
            g.setColor(Color.BLACK);
            g.drawString(tr("Not in cache"), 10, img.getHeight()/2);
            g.setFont(font);
            this.image = img;
            break;
        }
        default:
            break;
        }
    }

    private BufferedImage createImage() {
        int left = layer.getImageX(xIndex);
        int bottom = layer.getImageY(yIndex);
        int width = layer.getImageX(xIndex + 1) - left;
        int height = layer.getImageY(yIndex + 1) - bottom;

        return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    public boolean paint(Graphics g, NavigatableComponent nc, int xIndex, int yIndex, int leftEdge, int bottomEdge) {
        if (image == null)
            return false;

        if(!(this.xIndex == xIndex && this.yIndex == yIndex)){
            return false;
        }

        int left = layer.getImageX(xIndex);
        int bottom = layer.getImageY(yIndex);
        int width = layer.getImageX(xIndex + 1) - left;
        int height = layer.getImageY(yIndex + 1) - bottom;

        int x = left - leftEdge;
        int y = nc.getHeight() - (bottom - bottomEdge) - height;

        // This happens if you zoom outside the world
        if(width == 0 || height == 0)
            return false;

        if(reImg != null && reImg.getWidth() == width && reImg.getHeight() == height) {
            g.drawImage(reImg, x, y, null);
            return true;
        }

        boolean alphaChannel = WMSLayer.PROP_ALPHA_CHANNEL.get() && getImage().getTransparency() != Transparency.OPAQUE;

        try {
            if(reImg != null) reImg.flush();
            long freeMem = Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory();
            //System.out.println("Free Memory:           "+ (freeMem/1024/1024) +" MB");
            // Notice that this value can get negative due to integer overflows
            //System.out.println("Img Size:              "+ (width*height*3/1024/1024) +" MB");

            int multipl = alphaChannel ? 4 : 3;
            // This happens when requesting images while zoomed out and then zooming in
            // Storing images this large in memory will certainly hang up JOSM. Luckily
            // traditional rendering is as fast at these zoom levels, so it's no loss.
            // Also prevent caching if we're out of memory soon
            if(width > 2000 || height > 2000 || width*height*multipl > freeMem) {
                fallbackDraw(g, getImage(), x, y, width, height);
            } else {
                // We haven't got a saved resized copy, so resize and cache it
                reImg = new BufferedImage(width, height, alphaChannel?BufferedImage.TYPE_INT_ARGB:BufferedImage.TYPE_3BYTE_BGR);
                reImg.getGraphics().drawImage(getImage(),
                        0, 0, width, height, // dest
                        0, 0, getImage().getWidth(null), getImage().getHeight(null), // src
                        null);
                reImg.getGraphics().dispose();
                g.drawImage(reImg, x, y, null);
            }
        } catch(Exception e) {
            fallbackDraw(g, getImage(), x, y, width, height);
        }
        return true;
    }

    private void fallbackDraw(Graphics g, Image img, int x, int y, int width, int height) {
        if(reImg != null) {
            reImg.flush();
            reImg = null;
        }
        g.drawImage(
                img, x, y, x + width, y + height,
                0, 0, img.getWidth(null), img.getHeight(null),
                null);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        boolean hasImage = in.readBoolean();
        if (hasImage)
            image = (ImageIO.read(ImageIO.createImageInputStream(in)));
        else {
            in.readObject(); // read null from input stream
            image = null;
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        if(getImage() == null) {
            out.writeBoolean(false);
            out.writeObject(null);
        } else {
            out.writeBoolean(true);
            ImageIO.write(getImage(), "png", ImageIO.createImageOutputStream(out));
        }
    }

    public void flushedResizedCachedInstance() {
        reImg = null;
    }


    public BufferedImage getImage() {
        return image;
    }

    public State getState() {
        return state;
    }

    public int getXIndex() {
        return xIndex;
    }

    public int getYIndex() {
        return yIndex;
    }
}
