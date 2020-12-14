import com.github.sarxos.webcam.Webcam;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Applies image processing to the user's webcam feed and displays the resultant stream
 * in a JFrame. The kind of image processing is selectable by the user, using a panel of
 * buttons.
 *
 * The input kernel file must be titled "kernel.txt."
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
    private BufferedImage image;    // input image
    private BufferedImage newImage; // output processed image
    private final Webcam webcam;    // user's webcam
    private Mode mode;

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

            mode = Mode.KERNEL;
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

        // menu bar
        JMenuBar menuBar = new JMenuBar();
        // drop-down menu
        JMenu menu = new JMenu("File");
        // menu item that lets the user save their capture as a .png
        JMenuItem save = new JMenuItem(" Save as .png");
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileDialog chooser = new FileDialog(frame, "Use a .png extension", FileDialog.SAVE);
                chooser.setVisible(true);
                String filename = chooser.getFile();
                if (filename != null) {
                    // desired image format
                    String format = chooser.getFile().substring(filename.lastIndexOf('.') + 1);

                    // only works if the user desires .png
                    if (format.equalsIgnoreCase("png")) {
                        try {
                            ImageIO.write(newImage, format, new File(String.format("%s%s%s",
                                    chooser.getDirectory(), File.separator, chooser.getFile())));
                        }
                        catch (IOException exception) {
                            exception.printStackTrace();
                        }
                        chooser.dispose();
                    }
                }
            }
        });
        menu.add(save);
        menuBar.add(menu);
        frame.setJMenuBar(menuBar);

        // adds the processing mode button panel to the bottom of the screen
        Container content = frame.getContentPane();
        content.setLayout(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        JButton halftoneButton = new JButton("Halftone Processing");
        JButton kernelButton = new JButton("Kernel Processing");
        JButton grayscaleButton = new JButton("Grayscaling");
        ActionListener buttonListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource().equals(halftoneButton)) {
                    mode = Mode.HALFTONE;
                } else if (e.getSource().equals(kernelButton)) {
                    mode = Mode.KERNEL;
                } else if (e.getSource().equals(grayscaleButton)) {
                    mode = Mode.GRAYSCALE;
                }
            }
        };
        halftoneButton.addActionListener(buttonListener);
        kernelButton.addActionListener(buttonListener);
        grayscaleButton.addActionListener(buttonListener);
        buttonPanel.add(halftoneButton);
        buttonPanel.add(kernelButton);
        buttonPanel.add(grayscaleButton);
        content.add(buttonPanel, BorderLayout.SOUTH);

        // timer for updating the frame with webcam feed
        Timer timer = new Timer(1, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // reads image, processes the new image, and repaints the frame
                image = webcam.getImage();
                switch (mode) {
                    case HALFTONE -> newImage = Halftoner.processImage(image, 10);
                    case KERNEL -> newImage = Kernelizer.processImage(image, kernel);
                    case GRAYSCALE -> newImage = Grayscaler.processImage(image);
                }
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

        // prepares the panel with the output image
        frame.add(new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(newImage, 0, 0, null);
            }
        });

        frame.setSize((int) webcam.getViewSize().getWidth(), (int) webcam.getViewSize().getHeight());
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setTitle("It's you!");
        frame.validate();
        frame.setVisible(true);
    }

    /**
     * Processing mode
     */
    enum Mode {
        HALFTONE, KERNEL, GRAYSCALE
    }

    /**
     * Program's main method, starts the processor
     *
     * @param args, command-line args
     */
    public static void main(String[] args) {
        WebcamProcessor p = new WebcamProcessor("kernel.txt");
        SwingUtilities.invokeLater(p);
    }
}
