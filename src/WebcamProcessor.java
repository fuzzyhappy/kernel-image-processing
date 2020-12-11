import com.github.sarxos.webcam.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Applies image processing to the user's webcam feed and displays the resultant stream
 * in a JFrame. The input kernel file must be titled "kernel.txt."
 *
 * The text file containing the kernel should be formatted (for an example nxn matrix as
 * our kernel), the integer n on its own line, followed by the matrix, with each float
 * from the matrix being separated by spaces and each row being separated by a newline.
 *
 * @author Evan Wang
 * @version 11 December 2020
 */
public class WebcamProcessor implements Runnable {
    private float[][] kernel;       // the kernel
    private JPanel imagePanel;      // JPanel containing the processed image
    private BufferedImage image;    // input image
    private BufferedImage newImage; // output processed image
    private Webcam webcam;          // user's webcam

    /**
     * Constructor forWebcamProcessor.
     *
     * @param ker, pathname for the kernel text file
     */
    public WebcamProcessor(String ker) {
        // preps webcam for being read
        webcam = Webcam.getDefault();
        webcam.setViewSize(new Dimension(640, 480));
        webcam.open();
        
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

            // prepares the panel with the output image
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
     * Run() method, starts the frame.
     */
    @Override
    public void run() {
        JFrame frame = new JFrame();

        // timer for updating the frame with webcam feed
        Timer timer = new Timer(1, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // reads image, processes the new image, and repaints the frame
                image = webcam.getImage();
                newImage = KernelProcessor.processImage(image, kernel);
                frame.repaint();
            }
        });
        timer.setRepeats(true);
        timer.start();

        // ends the program's processes on close
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                frame.dispose();
                webcam.close();
                timer.stop();
            }
        });
        
        frame.add(imagePanel);
        frame.setSize((int)webcam.getViewSize().getWidth(), (int)webcam.getViewSize().getHeight());
        frame.setTitle("It's you!");
        frame.validate();
        frame.setVisible(true);
    }

    /**
     * Program's main method, starts the processor
     *
     * @param args, command-line args
     */
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        WebcamProcessor p = new WebcamProcessor("kernel.txt");
        SwingUtilities.invokeLater(p);
    }
}
