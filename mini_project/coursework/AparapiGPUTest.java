import com.aparapi.Kernel;
import com.aparapi.device.Device;
import com.aparapi.Range;

// javac -cp ".;aparapi-2.0.0.jar;aparapi-jni-1.4.3.jar;bcel-6.5.0.jar" AparapiGPUTest.java
// java -cp ".;aparapi-2.0.0.jar;aparapi-jni-1.4.3.jar;bcel-6.5.0.jar" AparapiGPUTest  

public class AparapiGPUTest {
    
    public static void main(String[] args) {
        int[] sizes = {512, 1024, 2048,3072, 4096};
        
        
        Device device = Device.best();
        System.out.println("Using device: " + device.toString());
        System.out.println("Device type: " + device.getType());
        System.out.println();
        
        for ( int N : sizes) {
            System.out.println("Testing matrix size: " + N + "x" + N);
            
            final float[] A = new float[N * N];
            final float[] B = new float[N * N];
            final float[] C = new float[N * N];
            
            for (int i = 0; i < N * N; i++) {
                A[i] = (float)Math.random();
                B[i] = (float)Math.random();
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
            kernel.execute(Range.create2D(N, N, 16, 16));
            
            long startTime = System.nanoTime();
            kernel.execute(Range.create2D(N, N, 16, 16));
            long endTime = System.nanoTime();
            
            kernel.getExecutionMode();
            
            double timeSec = (endTime - startTime) / 1e9;
            double timeMs = timeSec * 1000;
            
            double operations = 2.0 * N * N * N; 
            double gflops = (operations / 1e9) / timeSec;
            
            System.out.printf("Time: %.2f ms\n", timeMs);
            System.out.printf("Performance: %.2f GFLOPS\n", gflops);
            System.out.println();
            
            kernel.dispose();
            System.gc();
        }
    }
}