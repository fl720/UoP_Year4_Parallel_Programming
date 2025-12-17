import java.awt.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class Laplace_seq {

    final static int N = 256;
    final static int CELL_SIZE = 2;
    final static int NITER = 100000;
    final static int OUTPUT_FREQ = 100000;

    static float[][] phi = new float[N][N];
    static float[][] newPhi = new float[N][N];

    static Display display = new Display();

    public static void main(String args[]) throws Exception {

        // Make voltage non-zero on left and right edges
        for (int j = 0; j < N; j++) {
            phi[0][j] = 1.0F;
            phi[N - 1][j] = 1.0F;
        }

        display.repaint();

        // --- NEW: Save initial state image ---
        saveImage("laplace_start.png");
        // -------------------------------------

        long startTime = System.currentTimeMillis();

        for (int iter = 0; iter < NITER; iter++) {

            // Calculate new phi
            for (int i = 1; i < N - 1; i++) {
                for (int j = 1; j < N - 1; j++) {
                    newPhi[i][j] = 0.25F * (phi[i][j - 1] + phi[i][j + 1] +
                                             phi[i - 1][j] + phi[i + 1][j]);
                }
            }

            // Update all phi values
            for (int i = 1; i < N - 1; i++) {
                for (int j = 1; j < N - 1; j++) {
                    phi[i][j] = newPhi[i][j];
                }
            }

            if (iter % OUTPUT_FREQ == 0) {
                System.out.println("iter = " + iter);
                display.repaint();
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Calculation completed in " +
                           (endTime - startTime) + " milliseconds");

        display.repaint();

        // --- NEW: Save final state image ---
        saveImage("laplace_end.png");
        // -----------------------------------
    }

    // --- NEW METHOD: save current phi as PNG image ---
    static void saveImage(String filename) {
        int width = N * CELL_SIZE;
        int height = N * CELL_SIZE;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                float f = Math.max(0, Math.min(1, phi[i][j])); // Clamp [0,1]
                Color c = new Color(f, 0.0F, 1.0F - f);
                g2.setColor(c);
                g2.fillRect(CELL_SIZE * i, CELL_SIZE * j, CELL_SIZE, CELL_SIZE);
            }
        }

        g2.dispose();
        try {
            ImageIO.write(img, "png", new File(filename));
            System.out.println("Saved image: " + filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // -------------------------------------------------

    static class Display extends JPanel {
        final static int WINDOW_SIZE = N * CELL_SIZE;

        Display() {
            setPreferredSize(new Dimension(WINDOW_SIZE, WINDOW_SIZE));
            JFrame frame = new JFrame("Laplace");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(this);
            frame.pack();
            frame.setVisible(true);
        }

        public void paintComponent(Graphics g) {
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    float f = phi[i][j];
                    Color c = new Color(f, 0.0F, 1.0F - f);
                    g.setColor(c);
                    g.fillRect(CELL_SIZE * i, CELL_SIZE * j, CELL_SIZE, CELL_SIZE);
                }
            }
        }
    }
}
