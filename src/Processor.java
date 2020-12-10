import java.awt.image.BufferedImage;
import java.awt.Image;
import javax.imageio.ImageIO;
import java.awt.Color;
import javax.swing.JFrame;
import java.io.*;
import java.util.*;

public class Processor {
    private int[][] kernel;
    private BufferedImage image;
    private BufferedImage newImage;

    public Processor(String ker, String img) {
        try (BufferedReader in = new BufferedReader(new FileReader(ker))) {
            int n = Integer.parseInt(in.readLine());
            kernel = new int[n][n];
            for (int i = 0; i < n; i++) {
                String[] row = in.readLine().split(" ");
                for (int j = 0; j < n; j++) {
                    kernel[i][j] = Integer.parseInt(row[j]);
                }
            }

            image = ImageIO.read(new File(img));
            newImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

            processImage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void processImage() {
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                processPixel(j, i);
            }
        }
    }

    public void processPixel(int col, int row) {
        int[][] sample = new int[kernel.length][kernel.length];

        for (int i = -1 * kernel.length / 2; i <= kernel.length / 2; i++) {
            for (int j = -1 * kernel.length / 2; j <= kernel.length / 2; j++) {
                sample[i + kernel.length / 2][j + kernel.length / 2] = get(col + j, row + i);
            }
        }

        int val = 0;
        set(col, row, val);
    }

    public int get(int col, int row) {
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

    public void set(int col, int row, int val) {
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


    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("Enter your kernel file and then your image file.");
        Processor p = new Processor(in.nextLine(), in.nextLine());
    }
}
