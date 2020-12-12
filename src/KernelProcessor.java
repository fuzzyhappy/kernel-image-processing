import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Class filled with static utility methods to process a given image using a given nxn kernel matrix of floats.
 *
 * @author Evan Wang
 * @version 11 December 2020
 */
public class KernelProcessor {
    /**
     * Iterates through each of the image's pixels and calls processPixel() on each.
     *
     * @param image, the image to be processed
     * @param kernel, the kernel to be applied to the image.
     * @return new processed BufferedImage
     */
    public static BufferedImage processImage(BufferedImage image, float[][] kernel) {
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int col = 0; col < image.getWidth(); col++) {
            for (int row = 0; row < image.getHeight(); row++) {
                processPixel(col, row, image, newImage, kernel);
            }
        }
        return newImage;
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
     * @param kernel, the kernel to be applied to the pixel
     */
    private static void processPixel(int col, int row, BufferedImage image, BufferedImage newImage, float[][] kernel) {
        // collects nxn sample of surrounding pixels
        int[][] sample = new int[kernel.length][kernel.length];

        for (int x = -1 * kernel.length / 2; x <= kernel.length / 2; x++) {
            for (int y = -1 * kernel.length / 2; y <= kernel.length / 2; y++) {
                sample[x + kernel.length / 2][y + kernel.length / 2] = get(col + x, row + y, image);
            }
        }

        double red = 0;
        double green = 0;
        double blue = 0;

        // applies dot product
        for (int i = 0; i < kernel.length; i ++) {
            for (int j = 0; j < kernel.length; j++) {
                red += ((sample[i][j] >> 16) & 0xff) * kernel[i][j];
                green += ((sample[i][j] >> 8) & 0xff) * kernel[i][j];
                blue += ((sample[i][j]) & 0xff) * kernel[i][j];
            }
        }

        // accounts for overflow of the byte value
        red = Math.min(Math.max((int)(red), 0), 255);
        green = Math.min(Math.max((int)(green), 0), 255);
        blue = Math.min(Math.max((int)(blue), 0), 255);

        // sets corresponding pixel to the dot product
        int val = new Color((int)red, (int)green, (int)blue).getRGB();
        set(col, row, newImage, val);
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
