import java.awt.*;
import javax.swing.*;
import java.util.concurrent.CyclicBarrier;

public class ParallelLaplace extends Thread {
    final static int N = 256;
    final static int P = 4; // Number of threads
    final static int CELL_SIZE = 2;
    final static int NITER = 100000;
    final static int OUTPUT_FREQ = 1000;
    
    static float[][] phi = new float[N][N];
    static float[][] newPhi = new float[N][N];
    static Display display = new Display();
    static CyclicBarrier barrier = new CyclicBarrier(P);
    
    public static void main(String args[]) throws Exception {
        // Make voltage non-zero on left and right edges
        for(int j = 0; j < N; j++) {
            phi[0][j] = 1.0F;
            phi[N-1][j] = 1.0F;
        }
        display.repaint();
        
        long startTime = System.currentTimeMillis();
        
        ParallelLaplace[] threads = new ParallelLaplace[P];
        for(int me = 0; me < P; me++) {
            threads[me] = new ParallelLaplace(me);
            threads[me].start();
        }
        for(int me = 0; me < P; me++) {
            threads[me].join();
        }
        
        long endTime = System.currentTimeMillis();
        System.out.println("Calculation completed in " + (endTime - startTime) + " milliseconds");
    }
    
    int me;
    ParallelLaplace(int me) {
        this.me = me;
    }
    
    final static int B = N / P; // block size
    
    public void run() {
        // Modify domain for boundary conditions as suggested in lab document
        int begin = me * B;
        int end = begin + B;
        if(me == 0) begin = 1;
        if(me == P-1) end = N - 1;
        
        for(int iter = 0; iter < NITER; iter++) {
            // Calculate new phi
            for(int i = begin; i < end; i++) {
                for(int j = 1; j < N - 1; j++) {
                    newPhi[i][j] = 0.25F * (phi[i][j-1] + phi[i][j+1] + 
                                           phi[i-1][j] + phi[i+1][j]);
                }
            }
            
            synch(); // Barrier 1: Wait for all threads to finish calculating newPhi
            
            // Update phi values
            for(int i = begin; i < end; i++) {
                for(int j = 1; j < N - 1; j++) {
                    phi[i][j] = newPhi[i][j];
                }
            }
            
            synch(); // Barrier 2: Wait for all threads to finish updating phi
            
            if(me == 0 && iter % OUTPUT_FREQ == 0) {
                System.out.println("iter = " + iter);
                display.repaint();
            }
        }
    }
    
    static void synch() {
        try {
            barrier.await();
        }
        catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    static class Display extends JPanel {
        final static int WINDOW_SIZE = N * CELL_SIZE;
        
        Display() {
            setPreferredSize(new Dimension(WINDOW_SIZE, WINDOW_SIZE));
            JFrame frame = new JFrame("Parallel Laplace");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(this);
            frame.pack();
            frame.setVisible(true);
        }
        
        public void paintComponent(Graphics g) {
            for(int i = 0; i < N; i++) {
                for(int j = 0; j < N; j++) {
                    float f = phi[i][j];
                    Color c = new Color(f, 0.0F, 1.0F - f);
                    g.setColor(c);
                    g.fillRect(CELL_SIZE * i, CELL_SIZE * j, CELL_SIZE, CELL_SIZE);
                }
            }
        }
    }
}