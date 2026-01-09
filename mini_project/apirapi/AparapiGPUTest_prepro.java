import com.aparapi.Kernel;
import com.aparapi.device.Device;
import com.aparapi.Range;

// javac -cp ".;aparapi-2.0.0.jar;aparapi-jni-1.4.3.jar;bcel-6.5.0.jar" AparapiGPUTest_prepro.java
// java -cp ".;aparapi-2.0.0.jar;aparapi-jni-1.4.3.jar;bcel-6.5.0.jar" AparapiGPUTest_prepro

public class AparapiGPUTest_prepro {

    private static final int WARMUP_ITERATIONS = 5;  // number of warm up 
    private static final int BENCHMARK_ITERATIONS = 5; // number of benchmark runs

    public static void main(String[] args) {
        int[] sizes = {512, 1024, 2048,3072, 4096};
        
        Device device = Device.best();
        System.out.println("first choice device: " + device.toString());
        System.out.println("Device type: " + device.getType());
        System.out.println("Max Work Group: " + device.getMaxWorkGroupSize());
        System.out.println();

        for (int N : sizes) {
            System.out.println("----------------------------------");
            System.out.println("Matrix size: " + N + "x" + N);
            
            final float[] A = new float[N * N];
            final float[] B = new float[N * N];
            final float[] C = new float[N * N];

            for (int i = 0; i < N * N; i++) {
                A[i] = (float) Math.random();
                B[i] = (float) Math.random();
            }

            Kernel kernel = new Kernel() {
                @Override
                public void run() {
                    int i = getGlobalId(0);
                    int j = getGlobalId(1);
                    
                    if (i < N && j < N) {
                        float sum = 0.0f;
                        for (int k = 0; k < N; k++) {
                            sum += A[i * N + k] * B[k * N + j];
                        }
                        C[i * N + j] = sum;
                    }
                }
            };

            Range range = Range.create2D(N, N );

            try {
                for (int w = 0; w < WARMUP_ITERATIONS; w++) {
                    kernel.execute(range);
                }

                System.out.println("Actuall mode: " + kernel.getExecutionMode());
                if (!kernel.getExecutionMode().equals(Kernel.EXECUTION_MODE.GPU)) {
                    System.err.println("NOT running on GPU! Skipping benchmark!");
                }

                System.out.println("Attempt (" + BENCHMARK_ITERATIONS + " )...");
                
                double totalTimeSec = 0;

                for (int t = 0; t < BENCHMARK_ITERATIONS; t++) {
                    long t0 = System.nanoTime();
                    kernel.execute(range); 
                    long t1 = System.nanoTime();
                    
                    double runTimeSec = (t1 - t0) / 1e6;
                    totalTimeSec += runTimeSec;
                    
                    System.out.printf("  Run %d: %.4f ms\n", (t+1), runTimeSec);
                }

                double avgTimeSec = totalTimeSec / BENCHMARK_ITERATIONS;
                double avgTimeMs = avgTimeSec ; 
                
                double operations = 2.0 * Math.pow(N, 3);
                double gflops = (operations / 1e9) / (avgTimeSec/1000);

                System.out.printf("Average time: %.4f ms\n", avgTimeMs);
                System.out.printf("Average performance:     %.2f GFLOPS\n", gflops);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                kernel.dispose();
            }
            System.gc();
            System.out.println();
        }
        
    }
}