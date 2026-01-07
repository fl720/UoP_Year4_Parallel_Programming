import com.aparapi.Kernel;
import com.aparapi.Range;
import com.aparapi.device.Device;

/**
 * GPU-accelerated matrix operations using Aparapi
 * Note: LU decomposition on GPU is complex, this shows GPU matrix multiplication
 * 
 * 
 * javac -cp ".;aparapi-2.0.0.jar;aparapi-jni-1.4.3.jar;bcel-6.5.0.jar" ParallelLUAparapi.java
 * java -cp ".;aparapi-2.0.0.jar;aparapi-jni-1.4.3.jar;bcel-6.5.0.jar" ParallelLUAparapi  
 */
public class ParallelLUAparapi {
    
    // Matrix sizes to test
    private static final int[] MATRIX_SIZES = {512, 1024, 2048, 3072, 4096};
    
    /**
     * Main method for GPU-accelerated computation
     */
    public static void main(String[] args) {
        System.out.println("=== GPU-ACCELERATED COMPUTATION (APARAPI) ===");
        System.out.println("Testing matrix multiplication on GPU");
        System.out.println("Matrix sizes: 512, 1024, 2048, 4096, 5120");
        System.out.println();
        
        // Get available GPU device
        Device device = Device.firstGPU();
        // if (device == null) {
        //     System.out.println("No GPU found, using CPU as fallback");
        //     device = Device.firstCPU();
        // }
        
        // Device device = Device.best();
        System.out.println("使用设备: " + device.toString());
        System.out.println("设备类型: " + device.getType());
        System.out.println();
        
        // Test each matrix size
        for (int n : MATRIX_SIZES) {
            System.out.println("Testing N = " + n);
            
            // Generate random matrices
            long startGen = System.currentTimeMillis();
            float[] A = generateRandomMatrixFloat(n);
            float[] B = generateRandomMatrixFloat(n);
            long endGen = System.currentTimeMillis();
            
            System.out.printf("  Matrix generation: %d ms\n", endGen - startGen);
            
            // Allocate result matrix
            float[] C = new float[n * n];
            
            // Create GPU kernel for matrix multiplication
            Kernel kernel = new Kernel() {
                @Override
                public void run() {
                    int i = getGlobalId(0);  // Row index
                    int j = getGlobalId(1);  // Column index
                    
                    if (i < n && j < n) {
                        float sum = 0.0f;
                        for (int k = 0; k < n; k++) {
                            sum += A[i * n + k] * B[k * n + j];
                        }
                        C[i * n + j] = sum;
                    }
                }
            };
            
            // Try to use GPU
            try {
                kernel.setExecutionMode(Kernel.EXECUTION_MODE.GPU);
            } catch (Exception e) {
                System.out.println("  GPU mode not available, using CPU");
                kernel.setExecutionMode(Kernel.EXECUTION_MODE.JTP);
            }
            
            // Warm-up run
            if (n <= 1024) {
                kernel.execute(Range.create2D(n, n, 16, 16));
                kernel.getExecutionMode(); // Ensure completion
            }
            
            // Time GPU computation
            
            long startCompute = System.nanoTime();
            
            kernel.execute(Range.create2D(n, n, 16, 16));
            kernel.getExecutionMode(); // Ensure GPU computation completes
            long endCompute = System.nanoTime();
            
            long computeTime = (endCompute - startCompute);
            double timeMs = computeTime/1e6;
            
            // Calculate GFLOPS for matrix multiplication
            // Matrix multiplication requires 2n^3 floating point operations
            double flops = 2.0 * n * n * n;
            double gflops =  (flops / 1e9) / (computeTime/1e9);
            
            System.out.printf("  GPU computation time: %.6f ms\n", timeMs);
            System.out.printf("  Performance: %.2f GFLOPS\n", gflops);
            
            // Simple validation for small matrices
            if (n <= 512) {
                // Compute one element on CPU for verification
                int testRow = n / 2;
                int testCol = n / 2;
                float cpuResult = 0.0f;
                for (int k = 0; k < n; k++) {
                    cpuResult += A[testRow * n + k] * B[k * n + testCol];
                }
                
                float gpuResult = C[testRow * n + testCol];
                float diff = Math.abs(cpuResult - gpuResult);
                
                System.out.printf("  Validation (single element): diff = %e\n", diff);
            }
            
            System.out.println();
            
            // Cleanup
            kernel.dispose();
            
            // Force garbage collection
            System.gc();
        }
        
        System.out.println("GPU benchmark completed!");
    }
    
    /**
     * Generates random float matrix for GPU computation
     */
    private static float[] generateRandomMatrixFloat(int size) {
        float[] matrix = new float[size * size];
        java.util.Random rand = new java.util.Random(12345);
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i * size + j] = rand.nextFloat();
            }
        }
        
        return matrix;
    }
}