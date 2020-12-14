import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Image processor for gray-scaling.
 *
 * @author Evan Wang
 * @version 11 December 2020
 */
public class Grayscaler {
    /**
     * Iterates through each of the image's pixels and calls processPixel() on each.
     *
     * @param image, the image to be processed
     * @return new processed BufferedImage
     */
    public static BufferedImage processImage(BufferedImage image) {
        for (int col = 0; col < image.getWidth(); col++) {
            for (int row = 0; row < image.getHeight(); row++) {
                processPixel(col, row, image, image);
            }
        }
        return image;
    }

    /**
     * Collects an nxn array of surrounding pixels to the specified pixel, applies
     * dot product with kernel, then sets the corresponding pixel in the output
     * image equal to that dot product.
     *
     * @param col, the x coordinate of the pixel in the image
     * @param row, the y coordinate of the pixel in the image
     * @param image, the image to be processed
     * @param newImage, the image to be output
     */
    private static void processPixel(int col, int row, BufferedImage image, BufferedImage newImage) {
        set(col, row, image, toGray(new Color(get(col, row, image))).getRGB());
    }

    private static double intensity(Color color) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        if (r == g && r == b) return r;   // to avoid floating-point issues
        return 0.299*r + 0.587*g + 0.114*b;
    }

    private static Color toGray(Color color) {
        int y = (int) (Math.round(intensity(color)));   // round to nearest int
        Color gray = new Color(y, y, y);
        return gray;
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
