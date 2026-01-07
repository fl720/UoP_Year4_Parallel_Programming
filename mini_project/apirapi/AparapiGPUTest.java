import com.aparapi.Kernel;
import com.aparapi.device.Device;
import com.aparapi.Range;

// javac -cp ".;aparapi-2.0.0.jar;aparapi-jni-1.4.3.jar;bcel-6.5.0.jar" AparapiGPUTest.java
// java -cp ".;aparapi-2.0.0.jar;aparapi-jni-1.4.3.jar;bcel-6.5.0.jar" AparapiGPUTest  

public class AparapiGPUTest {
    
    public static void main(String[] args) {
        // 测试矩阵大小
        // int[] sizes = {512, 1024, 2048,4096, 5120};
        int[] sizes = {3072};
        
        System.out.println("Aparapi GPU 矩阵乘法测试");
        System.out.println("========================");
        
        // 显示设备信息
        Device device = Device.best();
        System.out.println("使用设备: " + device.toString());
        System.out.println("设备类型: " + device.getType());
        System.out.println();
        
        // 测试每个矩阵大小
        for ( int N : sizes) {
            System.out.println("测试矩阵大小: " + N + "x" + N);
            
            // 准备矩阵数据
            // final int N = 1024;
            final float[] A = new float[N * N];
            final float[] B = new float[N * N];
            final float[] C = new float[N * N];
            
            // 简单初始化（非零值）
            for (int i = 0; i < N * N; i++) {
                A[i] = (float)Math.random();
                B[i] = (float)Math.random();
            }
            
            // 创建Kernel
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
            
            // 设置设备
            // kernel.setTargetDevice(device);
            
            // 预热运行
            kernel.execute(Range.create2D(N, N, 16, 16));
            
            // 计时运行
            long startTime = System.nanoTime();
            kernel.execute(Range.create2D(N, N, 16, 16));
            long endTime = System.nanoTime();
            
            // 确保GPU计算完成
            kernel.getExecutionMode();
            
            // 计算时间
            double timeSec = (endTime - startTime) / 1e9;
            double timeMs = timeSec * 1000;
            
            // 计算GFLOPS
            double operations = 2.0 * N * N * N; // 乘法和加法各N^3次
            double gflops = (operations / 1e9) / timeSec;
            
            // 输出结果
            System.out.printf("执行时间: %.2f ms\n", timeMs);
            System.out.printf("性能: %.2f GFLOPS\n", gflops);
            System.out.println();
            
            kernel.dispose();
            
            // 清理内存以避免OutOfMemoryError
            // A = null;
            // B = null;
            // C = null;
            System.gc();
        }
        
        System.out.println("测试完成!");
    }
}