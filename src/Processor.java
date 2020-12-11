import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

/**
 * Image processor, takes in the name of a text file containing the relevant kernel and
 * the path to the desired image using a Scanner, in that order.
 *
 * The text file containing the kernel should be formatted (for an example nxn matrix as
 * our kernel), the integer n on its own line, followed by the matrix, with each float
 * from the matrix being separated by spaces and each row being separated by a newline.
 *
 * @author Evan Wang
 * @version 10 December 2020
 */
public class Processor implements Runnable {
    private float[][] kernel;       // the kernel
    private JPanel imagePanel;      // JPanel containing the processed image
    private BufferedImage image;    // input image
    private BufferedImage newImage; // output processed image

    /**
     * Constructor for Processor.
     *
     * @param ker, pathname for the kernel text file
     * @param img, pathname for the image text file
     */
    public Processor(String ker, String img) {
        try (BufferedReader in = new BufferedReader(new FileReader(ker))) {
            // initializes kernel based off of text file data
            int n = Integer.parseInt(in.readLine());
            kernel = new float[n][n];
            for (int i = 0; i < n; i++) {
                String[] row = in.readLine().split(" ");
                for (int j = 0; j < n; j++) {
                    kernel[i][j] = Float.parseFloat(row[j]);
                }
            }

            // reads image and prepares a blank similar image for output
            image = ImageIO.read(new File(img));
            newImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

            // prepares the panel with the output image
            processImage();
            imagePanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.drawImage(newImage, 0, 0, null);
                }
            };

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Iterates through each of the image's pixels and calls processPixel() on each.
     */
    private void processImage() {
        for (int col = 0; col < image.getWidth(); col++) {
            for (int row = 0; row < image.getHeight(); row++) {
                processPixel(col, row);
            }
        }
    }

    /**
     * Collects an nxn array of surrounding pixels to the specified pixel, applies
     * dot product with kernel, then sets the corresponding pixel in the output
     * image equal to that dot product.
     *
     * @param col, the x coordinate of the pixel in the image
     * @param row, the y coordinate of the pixel in the image
     */
    private void processPixel(int col, int row) {
        // collects nxn sample of surrounding pixels
        int[][] sample = new int[kernel.length][kernel.length];

        for (int x = -1 * kernel.length / 2; x <= kernel.length / 2; x++) {
            for (int y = -1 * kernel.length / 2; y <= kernel.length / 2; y++) {
                sample[x + kernel.length / 2][y + kernel.length / 2] = get(col + x, row + y);
            }
        }

        float red = 0;
        float green = 0;
        float blue = 0;

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
        set(col, row, val);
    }

    /**
     * Gets the 24-bit RGB value of the specified pixel. Accounts for edge
     * case pixels by extending their values to adjacent non-existent pixels.
     *
     * @param col, the x coordinate of the pixel in the image
     * @param row, the y coordinate of the pixel in the image
     * @return 24-bit RGB
     */
    private int get(int col, int row) {
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
     * @param val, the new RGB value of the specified pixel
     */
    private void set(int col, int row, int val) {
        newImage.setRGB(col, row, val);
    }

    /**
     * Overrides the void run() method in Runnable.
     * Creates the JFrame which displays the processed image.
     */
    @Override
    public void run() {
        JFrame frame = new JFrame();
        frame.setSize(newImage.getWidth(), newImage.getHeight());
        frame.add(imagePanel);
        frame.setTitle("Your new image!");
        frame.repaint();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    /**
     * Program's main method, prompts user for pathnames to their kernel
     * and image files.
     *
     * @param args, command-line args
     */
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("Enter your kernel file and then your image file.");
        Processor p = new Processor(in.nextLine(), in.nextLine());
        SwingUtilities.invokeLater(p);
    }
}
