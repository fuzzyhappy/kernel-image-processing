import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.*;
import java.util.*;

public class Processor implements Runnable {
    private float[][] kernel;
    private JPanel imagePanel;
    private BufferedImage image;
    private BufferedImage newImage;

    public Processor(String ker, String img) {
        try (BufferedReader in = new BufferedReader(new FileReader(ker))) {
            int n = Integer.parseInt(in.readLine());
            kernel = new float[n][n];
            for (int i = 0; i < n; i++) {
                String[] row = in.readLine().split(" ");
                for (int j = 0; j < n; j++) {
                    kernel[i][j] = Float.parseFloat(row[j]);
                }
            }

            image = ImageIO.read(new File(img));
            newImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

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

    private void processImage() {
        for (int col = 0; col < image.getWidth(); col++) {
            for (int row = 0; row < image.getHeight(); row++) {
                processPixel(col, row);
            }
        }
    }

    private void processPixel(int col, int row) {
        int[][] sample = new int[kernel.length][kernel.length];

        for (int x = -1 * kernel.length / 2; x <= kernel.length / 2; x++) {
            for (int y = -1 * kernel.length / 2; y <= kernel.length / 2; y++) {
                sample[x + kernel.length / 2][y + kernel.length / 2] = get(col + x, row + y);
            }
        }

        float red = 0;
        float green = 0;
        float blue = 0;

        for (int i = 0; i < kernel.length; i ++) {
            for (int j = 0; j < kernel.length; j++) {
                red += ((sample[i][j] >> 16) & 0xff) * kernel[i][j];
                green += ((sample[i][j] >> 8) & 0xff) * kernel[i][j];
                blue += ((sample[i][j]) & 0xff) * kernel[i][j];
            }
        }
        red = Math.min(Math.max((int)(red), 0), 255);
        green = Math.min(Math.max((int)(green), 0), 255);
        blue = Math.min(Math.max((int)(blue), 0), 255);

        int val = new Color((int)red, (int)green, (int)blue).getRGB();
        set(col, row, val);
    }

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

    private void set(int col, int row, int val) {
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
        newImage.setRGB(col, row, val);
    }

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

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("Enter your kernel file and then your image file.");
        Processor p = new Processor(in.nextLine(), in.nextLine());
        SwingUtilities.invokeLater(p);
    }
}
