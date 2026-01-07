import com.aparapi.Kernel;
import com.aparapi.device.Device;
import com.aparapi.Range;

// 编译和运行命令保持不变
// javac -cp ".;aparapi-2.0.0.jar;aparapi-jni-1.4.3.jar;bcel-6.5.0.jar" AparapiGPUTest_prepro.java
// java -cp ".;aparapi-2.0.0.jar;aparapi-jni-1.4.3.jar;bcel-6.5.0.jar" AparapiGPUTest_prepro

public class AparapiGPUTest_prepro {

    // 配置参数
    private static final int WARMUP_ITERATIONS = 5;  // 预热次数
    private static final int BENCHMARK_ITERATIONS = 5; // 正式测试次数

    public static void main(String[] args) {
        // 测试矩阵大小
        int[] sizes = {512, 1024, 2048,4096};
        // int[] sizes = {3072}; 
        
        System.out.println("Aparapi GPU 矩阵乘法基准测试");
        System.out.println("==================================");
        System.out.println("预热次数: " + WARMUP_ITERATIONS);
        System.out.println("测试次数: " + BENCHMARK_ITERATIONS + " (取平均值)");

        // 显示首选设备信息
        Device device = Device.best();
        System.out.println("首选设备: " + device.toString());
        System.out.println("设备类型: " + device.getType());
        System.out.println("最大工作组大小 (Max Work Group): " + device.getMaxWorkGroupSize());
        System.out.println();

        for (int N : sizes) {
            System.out.println("----------------------------------");
            System.out.println("当前矩阵大小: " + N + "x" + N);
            
            // 1. 数据准备 (在计时循环之外完成，避免GC干扰)
            System.out.println("正在初始化数据...");
            final float[] A = new float[N * N];
            final float[] B = new float[N * N];
            final float[] C = new float[N * N];

            for (int i = 0; i < N * N; i++) {
                A[i] = (float) Math.random();
                B[i] = (float) Math.random();
            }

            // 2. 定义 Kernel
            Kernel kernel = new Kernel() {
                @Override
                public void run() {
                    int i = getGlobalId(0);
                    int j = getGlobalId(1);
                    
                    if (i < N && j < N) {
                        float sum = 0.0f;
                        // 注意：这里访问的是一维数组模拟的二维
                        // A取第i行: A[i*N + k]
                        // B取第j列: B[k*N + j] (这里B没有转置，访存是不连续的，GPU通常能通过高带宽掩盖一部分延迟，但转置B依然会更快)
                        for (int k = 0; k < N; k++) {
                            sum += A[i * N + k] * B[k * N + j];
                        }
                        C[i * N + j] = sum;
                    }
                }
            };

            // 定义执行范围 (2D)
            // 建议显式指定 local width/height，通常 16x16 或 32x32 比较通用
            // 如果不确定，也可以只用 Range.create2D(N, N)，让 Aparapi 自动决定
            Range range = Range.create2D(N, N );

            try {
                // 3. 预热阶段 (Warmup)
                System.out.print("正在预热 (" + WARMUP_ITERATIONS + " 次)... ");
                for (int w = 0; w < WARMUP_ITERATIONS; w++) {
                    kernel.execute(range);
                }
                System.out.println("完成。");

                // 检查实际执行模式 (确保是在 GPU 上跑的)
                System.out.println("实际执行模式: " + kernel.getExecutionMode());
                if (!kernel.getExecutionMode().equals(Kernel.EXECUTION_MODE.GPU)) {
                    System.err.println("警告: 代码未在 GPU 上运行！请检查驱动或 Aparapi 配置。");
                }

                // 4. 正式测试阶段 (Benchmark)
                System.out.println("开始测试 (" + BENCHMARK_ITERATIONS + " 次)...");
                
                double totalTimeSec = 0;

                for (int t = 0; t < BENCHMARK_ITERATIONS; t++) {
                    long t0 = System.nanoTime();
                    kernel.execute(range); // 核心计算
                    long t1 = System.nanoTime();
                    
                    double runTimeSec = (t1 - t0) / 1e6;
                    totalTimeSec += runTimeSec;
                    
                    System.out.printf("  Run %d: %.4f ms\n", (t+1), runTimeSec);
                }

                // 5. 结果计算
                double avgTimeSec = totalTimeSec / BENCHMARK_ITERATIONS;
                double avgTimeMs = avgTimeSec ; 
                
                // 浮点运算量: 2 * N^3
                double operations = 2.0 * Math.pow(N, 3);
                double gflops = (operations / 1e9) / (avgTimeSec/1000);

                System.out.printf("平均执行时间: %.4f ms\n", avgTimeMs);
                System.out.printf("平均性能:     %.2f GFLOPS\n", gflops);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 6. 资源释放
                kernel.dispose();
            }

            // 提示 GC 回收旧的大数组，为下一次可能的循环腾空间
            System.gc();
            System.out.println();
        }
        
        System.out.println("所有测试结束。");
    }
}