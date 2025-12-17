import java.awt.*;
import javax.swing.*;
import java.util.concurrent.CyclicBarrier;

public class LaplaceParallel_cg {

    final static int N = 256;
    final static int CELL_SIZE = 2;
    final static int NITER = 100000;
    final static int OUTPUT_FREQ = 1000;

    static float[][] phi = new float[N][N];
    static float[][] newPhi = new float[N][N];

    static Display display = new Display();

    static int P; // Number of threads
    static CyclicBarrier barrier; // Barrier for synchronization

    public static void main(String[] args) throws Exception {

        // Number of threads - could also parse from command line
        P = 4;   // Try 2, 4, 8, etc.

        barrier = new CyclicBarrier(P);

        // Initialise boundary conditions
        for (int j = 0; j < N; j++) {
            phi[0][j] = 1.0f;
            phi[N - 1][j] = 1.0f;
        }

        display.repaint();

        Worker[] workers = new Worker[P];
        Thread[] threads = new Thread[P];

        long startTime = System.currentTimeMillis();

        for (int t = 0; t < P; t++) {
            workers[t] = new Worker(t);
            threads[t] = new Thread(workers[t]);
            threads[t].start();
        }

        for (int t = 0; t < P; t++) {
            threads[t].join();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Parallel calculation completed in " + (endTime - startTime) + " ms");

        display.repaint();
    }

    static class Worker implements Runnable {

        int me; // thread index

        Worker(int me) {
            this.me = me;
        }

        @Override
        public void run() {
            int B = N / P;     // Block size per thread
            int begin = me * B;
            int end = begin + B;

            // Adjust boundaries to avoid overwriting fixed edges
            if (me == 0) begin = 1;
            if (me == P - 1) end = N - 1;

            try {
                for (int iter = 0; iter < NITER; iter++) {

                    // Compute new values
                    for (int i = begin; i < end; i++) {
                        for (int j = 1; j < N - 1; j++) {
                            newPhi[i][j] = 0.25f * (phi[i][j - 1] + phi[i][j + 1] +
                                                    phi[i - 1][j] + phi[i + 1][j]);
                        }
                    }

                    barrier.await(); // Wait for all threads to finish computing

                    // Swap values into phi
                    for (int i = begin; i < end; i++) {
                        for (int j = 1; j < N - 1; j++) {
                            phi[i][j] = newPhi[i][j];
                        }
                    }

                    barrier.await(); // Ensure all update before next iteration

                    // Only one thread should output
                    if (me == 0 && iter % OUTPUT_FREQ == 0) {
                        System.out.println("iter = " + iter);
                        display.repaint();
                    }

                    barrier.await(); // Sync before next iteration
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class Display extends JPanel {
        final static int WINDOW_SIZE = N * CELL_SIZE;

        Display() {
            setPreferredSize(new Dimension(WINDOW_SIZE, WINDOW_SIZE));
            JFrame frame = new JFrame("Laplace Parallel");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(this);
            frame.pack();
            frame.setVisible(true);
        }

        public void paintComponent(Graphics g) {
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    float f = phi[i][j];
                    Color c = new Color(f, 0f, 1f - f);
                    g.setColor(c);
                    g.fillRect(CELL_SIZE * i, CELL_SIZE * j, CELL_SIZE, CELL_SIZE);
                }
            }
        }
    }
}
