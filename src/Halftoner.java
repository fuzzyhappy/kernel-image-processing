import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 *
 *
 * @author Evan Wang
 * @version 11 December 2020
 */
public class Halftoner {
    /**
     * Iterates through each of the image's pixels and calls processPixel() on each.
     *
     * @param image, the image to be processed
     * @return new processed BufferedImage
     */
    public static BufferedImage processImage(BufferedImage image, int rad) {
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Grayscaler.processImage(image);
        Graphics2D out = newImage.createGraphics();
        out.setColor(new Color(0, 0, 0));
        out.fill(new Rectangle2D.Double(0, 0, image.getWidth(), image.getHeight()));
        out.setColor(new Color(255, 255, 255));
        for (int col = 0; col < image.getWidth(); col += rad) {
            for (int row = 0; row < image.getHeight(); row+= rad) {
                processPixel(col, row, image, out, rad);
            }
        }
        return newImage;
    }

    /**
     *
     *
     * @param col
     * @param row
     * @param image
     * @param out
     * @param rad
     */
    private static void processPixel(int col, int row, BufferedImage image, Graphics2D out, int rad) {
        double value = 0;
        for (int x = -1 * rad; x <= rad; x++) {
            for (int y = -1 * rad; y <= rad; y++) {
                value += (get(col + x, row + y, image)) & 0xff;
            }
        }
        value /= (4 * rad * rad * 255);
        out.fill(new Ellipse2D.Double(col, row, 2 * rad * value, 2 * rad * value));
    }

    /**
     * Gets the 24-bit RGB value of the specified pixel. Accounts for edge
     * case pixels by extending their values to adjacent non-existent pixels.
     *
     * @param col, the x coordinate of the pixel in the image
     * @param row, the y coordinate of the pixel in the image
     * @return 24-bit RGB
     */
    private static int get(int col, int row, BufferedImage image) {
        if (col < 0) {
            col = 0;
        }
        if (row < 0) {
            row = 0;
        }
        if (col >= image.getWidth()) {
            col = image.getWidth() - 1;
        }
        if (row >= image.getHeight()) {
            row = image.getHeight() - 1;
        }
        return image.getRGB(col, row);
    }

    /**
     * Sets the specified pixel's value to the given value.
     *
     * @param col, the x coordinate of the pixel in the image
     * @param row, the y coordinate of the pixel in the image
     * @param newImage, the image to be output
     * @param val, the new RGB value of the specified pixel
     */
    private static void set(int col, int row, BufferedImage newImage, int val) {
        newImage.setRGB(col, row, val);
    }
}
