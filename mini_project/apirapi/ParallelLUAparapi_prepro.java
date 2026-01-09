import com.aparapi.Kernel;
import com.aparapi.Range;
import com.aparapi.device.Device;

/**
 * GPU-accelerated matrix operations using Aparapi
 * Improved Benchmark Version: Warmup + Averaging + Correct Timing
 * * javac -cp ".;aparapi-2.0.0.jar;aparapi-jni-1.4.3.jar;bcel-6.5.0.jar" ParallelLUAparapi_prepro.java
 * java -cp ".;aparapi-2.0.0.jar;aparapi-jni-1.4.3.jar;bcel-6.5.0.jar" ParallelLUAparapi_prepro  
 */
public class ParallelLUAparapi_prepro {
    
    private static final int[] MATRIX_SIZES = {512, 1024, 2048, 3072, 4096};

    private static final int WARMUP_ITERATIONS = 5;     // Number of warmup runs
    private static final int BENCHMARK_ITERATIONS = 5;  // Number of benchmark runs
    
    public static void main(String[] args) {
        System.out.println("=== GPU-ACCELERATED COMPUTATION (APARAPI) ===");
        System.out.println("Testing matrix multiplication kernel on GPU");
        System.out.println("Warmup Rounds: " + WARMUP_ITERATIONS);
        System.out.println("Benchmark Rounds: " + BENCHMARK_ITERATIONS);
        System.out.println();
        
        Device device = Device.best();
        System.out.println("Using device: " + device.toString());
        System.out.println("Device type: " + device.getType());
        System.out.println("Max work group size: " + device.getMaxWorkGroupSize());
        System.out.println();
        
        for (int n : MATRIX_SIZES) {
            System.out.println("Testing N = " + n + " (" + n + "x" + n + ")");
            
            System.out.print("  Generating data... ");
            float[] A = generateRandomMatrixFloat(n);
            float[] B = generateRandomMatrixFloat(n);
            float[] C = new float[n * n];
            System.out.println("Done.");
            
            final float[] kernelA = A;
            final float[] kernelB = B;
            final float[] kernelC = C;
            
            Kernel kernel = new Kernel() {
                @Override
                public void run() {
                    int i = getGlobalId(0); 
                    int j = getGlobalId(1); 
                    
                    if (i < n && j < n) {
                        float sum = 0.0f;
                        for (int k = 0; k < n; k++) {
                            sum += kernelA[i * n + k] * kernelB[k * n + j];
                        }
                        kernelC[i * n + j] = sum;
                    }
                }
            };
            
            Range range = Range.create2D(n, n);

            try {
                kernel.setExecutionMode(Kernel.EXECUTION_MODE.GPU);
                
                System.out.print("  Warming up (" + WARMUP_ITERATIONS + " runs)... ");
                for (int w = 0; w < WARMUP_ITERATIONS; w++) {
                    kernel.execute(range);
                }
                System.out.println("Done.");
                
                System.out.println("  Actual Execution Mode: " + kernel.getExecutionMode());
                if (!kernel.getExecutionMode().equals(Kernel.EXECUTION_MODE.GPU)) {
                    System.err.println("  WARNING: Code is running on CPU! Performance will be low.");
                }

                System.out.print("  Benchmarking (" + BENCHMARK_ITERATIONS + " runs)... ");
                double totalTimeNSec = 0;

                for (int t = 0; t < BENCHMARK_ITERATIONS; t++) {
                    long t0 = System.nanoTime();
                    kernel.execute(range); 
                    long t1 = System.nanoTime();
                    
                    double runTimeNSec = (t1 - t0);
                    totalTimeNSec += runTimeNSec;
                    System.out.printf("  Run %d: %.4f ms\n", (t+1), runTimeNSec/1e6);
                }
                System.out.println("Done.");

                double avgTimeNSec = totalTimeNSec / BENCHMARK_ITERATIONS;
                double avgTimeMs = avgTimeNSec / 1e6;
                
                double operations = 2.0 * Math.pow(n, 3);
                double gflops = (operations / 1e9) / (avgTimeNSec/1e9);
                
                System.out.printf("  Avg Computation Time: %.4f ms\n", avgTimeMs);
                System.out.printf("  Avg Performance:      %.2f GFLOPS\n", gflops);
                
            } catch (Exception e) {
                System.err.println("Error during execution:");
                e.printStackTrace();
            } finally {
                kernel.dispose();
            }
            
            A = null; B = null; C = null;
            System.gc();
            try { Thread.sleep(200); } catch (InterruptedException e) {}
        }
        
        System.out.println("GPU benchmark completed!");
    }
    
    private static float[] generateRandomMatrixFloat(int size) {
        float[] matrix = new float[size * size];
        java.util.Random rand = new java.util.Random(12345);
        
        for (int i = 0; i < size * size; i++) {
            matrix[i] = rand.nextFloat();
        }
        
        return matrix;
    }
}