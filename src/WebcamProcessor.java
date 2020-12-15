import com.github.sarxos.webcam.Webcam;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Applies image processing to the user's webcam feed and displays the resultant stream
 * in a JFrame. The kind of image processing is selectable by the user, using a panel of
 * buttons. The user can also customize image processing settings using pop-out GUIs.
 *
 * @author Evan Wang
 * @version 11 December 2020
 */
public class WebcamProcessor implements Runnable {
    private BufferedImage image;    // input image
    private BufferedImage newImage; // output processed image
    private final Webcam webcam;    // user's webcam
    private JFrame frame;

    private Mode mode;
    private float[][] kernel;       // the kernel
    private int rad;
    private Color[] halftonePalette;


    /**
     * Constructor for WebcamProcessor.
     *
     * @param ker, pathname for the kernel text file
     */
    public WebcamProcessor(String ker) {
        // preps webcam for being read
        webcam = Webcam.getDefault();
        webcam.setViewSize(new Dimension(640, 480));
        webcam.open();

        kernel = new float[][]{{1}};
        mode = Mode.GRAYSCALE;
        rad = 7;
        halftonePalette = new Color[]{new Color(0, 0, 0), new Color(255, 255, 255)};

    }

    /**
     * Run() method, starts the frame.
     */
    @Override
    public void run() {
        frame = new JFrame();

        frame.setJMenuBar(createMenuBar());

        // adds the processing mode button panel to the bottom of the screen
        Container content = frame.getContentPane();
        content.setLayout(new BorderLayout());
        content.add(createButtonPanel(), BorderLayout.SOUTH);

        // timer for updating the frame with webcam feed
        Timer timer = new Timer(1, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // reads image, processes the new image, and repaints the frame
                image = webcam.getImage();
                switch (mode) {
                    case HALFTONE -> newImage = Halftoner.processImage(image, rad,
                            halftonePalette[0], halftonePalette[1]);
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
        }, BorderLayout.CENTER);

        frame.setSize((int) webcam.getViewSize().getWidth(), (int) webcam.getViewSize().getHeight());
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setTitle("It's you!");
        frame.validate();
        frame.setVisible(true);
    }

    /**
     * Creates the menu bar which allows the user to save their capture.
     *
     * @return the JFrame's menu bar
     */
    public JMenuBar createMenuBar() {
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

        return menuBar;
    }

    /**
     * Creates a panel of buttons for the user to select their image processing.
     */
    public JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        // mode buttons
        JButton halftoneButton = new JButton("Halftone Processing");
        JButton kernelButton = new JButton("Kernel Processing");
        JButton grayscaleButton = new JButton("Grayscaling");
        ActionListener buttonListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource().equals(halftoneButton)) {
                    mode = Mode.HALFTONE;
                    createHalftoneCustomizer();
                } else if (e.getSource().equals(kernelButton)) {
                    mode = Mode.KERNEL;
                    createKernelCustomizer();
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

        return buttonPanel;
    }

    /**
     * Creates a frame which allows the user to change half-tone processing's half-tone
     * radius, background color, and foreground color.
     */
    public void createHalftoneCustomizer() {
        JFrame customizer = new JFrame();
        customizer.setTitle("Customize your halftone settings!");
        customizer.setLocationRelativeTo(null);
        customizer.setSize(new Dimension(400, 150));
        customizer.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        Container content = customizer.getContentPane();
        content.setLayout(new BorderLayout());

        // panel for setting labels and textboxes
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridLayout(3, 2, 2, 2));

        JLabel radLabel = new JLabel("Halftone radius:");
        JTextField radText = new JTextField(rad + "", 10);
        JLabel bgLabel = new JLabel("Background Hex Color:");
        JTextField bgText = new JTextField(String.format("#%02x%02x%02x",
                halftonePalette[0].getRed(), halftonePalette[0].getGreen(), halftonePalette[0].getBlue()), 10);
        JLabel fgLabel = new JLabel("Foreground Hex Color:");
        JTextField fgText = new JTextField(String.format("#%02x%02x%02x",
                halftonePalette[1].getRed(), halftonePalette[1].getGreen(), halftonePalette[1].getBlue()), 10);

        // button for confirming settings
        JButton confirmButton = new JButton("Enter");
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rad = Integer.parseInt(radText.getText());
                halftonePalette[0] = Color.decode(bgText.getText());
                halftonePalette[1] = Color.decode(fgText.getText());
                customizer.dispose();
            }
        });
        settingsPanel.add(radLabel);
        settingsPanel.add(radText);
        settingsPanel.add(bgLabel);
        settingsPanel.add(bgText);
        settingsPanel.add(fgLabel);
        settingsPanel.add(fgText);

        customizer.add(settingsPanel, BorderLayout.CENTER);
        customizer.add(confirmButton, BorderLayout.SOUTH);

        customizer.setVisible(true);
    }

    /**
     * Creates a frame which allows the user to customize the kernel.
     */
    public void createKernelCustomizer() {
        JFrame customizer = new JFrame();
        customizer.setTitle("Customize your kernel!");
        customizer.setLocationRelativeTo(null);
        customizer.setSize(new Dimension(400, 200));
        customizer.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        Container content = customizer.getContentPane();
        content.setLayout(new BorderLayout());

        // panel containing the textfield and button for the kernel's dimension
        JPanel dimPanel = new JPanel();
        JLabel dimLabel = new JLabel("n:");
        JTextField dimText = new JTextField("1", 3);
        JButton dimButton = new JButton("Enter");
        // clicking the button creates a new panel containing a textfield for each
        // kernel item.
        dimButton.addActionListener(new ActionListener() {
            JPanel panel;

            @Override
            public void actionPerformed(ActionEvent e) {
                panel = new JPanel();
                int n = Integer.parseInt(dimText.getText());
                // corresponding kernel input textfields
                JTextField[][] kernelText = new JTextField[n][n];

                panel = new JPanel();
                panel.setLayout(new BorderLayout());

                JPanel kernelPanel = new JPanel();
                kernelPanel.setLayout(new GridLayout(n, n, 2, 2));

                // adds textfields to GUI
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        kernelText[i][j] = new JTextField("0", 5);
                        kernelPanel.add(kernelText[i][j]);
                    }
                }
                kernelPanel.validate();

                // button for updating the kernel array based on textfields
                JButton confirmKernelButton = new JButton("Confirm Kernel");
                confirmKernelButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        kernel = new float[n][n];
                        for (int i = 0; i < n; i++) {
                            for (int j = 0; j < n; j++) {
                                kernel[i][j] = Float.parseFloat(kernelText[i][j].getText());
                                customizer.dispose();
                            }
                        }
                    }
                });

                panel.add(kernelPanel, BorderLayout.CENTER);
                panel.add(confirmKernelButton, BorderLayout.SOUTH);
                panel.validate();
                customizer.add(panel);
                customizer.validate();
                customizer.repaint();
            }
        });
        dimPanel.add(dimLabel);
        dimPanel.add(dimText);
        dimPanel.add(dimButton);

        customizer.add(dimPanel, BorderLayout.NORTH);
        customizer.validate();
        customizer.repaint();

        customizer.setVisible(true);
    }

    /**
     * Enumerator for processing mode.
     */
    enum Mode {
        HALFTONE, KERNEL, GRAYSCALE
    }

    /**
     * Program's main method, starts the processor.
     *
     * @param args, command-line args
     */
    public static void main(String[] args) {
        WebcamProcessor p = new WebcamProcessor("kernel.txt");
        SwingUtilities.invokeLater(p);
    }
}
