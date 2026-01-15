import com.aparapi.Kernel;
import com.aparapi.ProfileInfo;
import com.aparapi.Range;
import com.aparapi.device.Device ;
import com.aparapi.device.JavaDevice;

// how to execute in the terminal"
// make sure you have aparapi-2.0.0.jar, aparapi-jni-1.4.3.jar, bcel-6.5.0.jar in your classpath

// javac -cp ".;aparapi-2.0.0.jar;aparapi-jni-1.4.3.jar;bcel-6.5.0.jar" AparapiMatmul.java

// java -cp ".;aparapi-2.0.0.jar;aparapi-jni-1.4.3.jar;bcel-6.5.0.jar" AparapiMatmul
import com.aparapi.Kernel;
import com.aparapi.Range;
import com.aparapi.device.Device;
import com.aparapi.device.JavaDevice;
import com.aparapi.device.OpenCLDevice;

public class AparapiMatmul {
    // 512 , 1024, 2048, 4096, 5120
    public static final int N = 5120; 
    public static void main(String[] args) {

        System.out.println("正在初始化数据 (N=" + N + ")...");
        
        final float[] a = new float[N * N];
        final float[] b = new float[N * N];
        final float[] c = new float[N * N];

        // 初始化数据
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                a[N * i + j] = i + j;
                b[N * i + j] = i - j;
            }
        }

        Kernel kernel = new Kernel() {
            @Override
            public void run() {
                int tx = getGlobalId(0);
                int ty = getGlobalId(1);
                float sum = 0;
                for (int k = 0; k < N; k++) {
                    sum += a[N * ty + k] * b[N * k + tx];
                }
                c[N * ty + tx] = sum;
            }
        };

        // ==========================================
        // 1. 在这里选择你想测试的设备 (取消注释其中一行)
        // ==========================================
        
        // 选项 A: 自动选择最佳设备 (通常是 GPU)
        Device device = Device.best(); 

        // 选项 B: 强制使用 CPU 多核 (Java Thread Pool) -> 你的 42 GFLOPS 成绩就是这个
        // Device device = Device.first(Device.TYPE.JTP);

        // 选项 C: 强制使用 CPU 单核 (Sequential) -> 会非常慢，用来对比
        // Device device = Device.first(Device.TYPE.SEQ);

        // ==========================================
        // 2. 打印设备详情
        // ==========================================
        String typeStr = "未知";
        Device.TYPE type = device.getType();

        if (type == Device.TYPE.GPU) {
            typeStr = "GPU (OpenCL 显卡加速)";
        } else if (type == Device.TYPE.CPU) {
            typeStr = "OpenCL CPU (OpenCL 驱动)";
        } else if (type == Device.TYPE.JTP) {
            typeStr = "CPU 多核并行 (Java 线程池)";
        } else if (type == Device.TYPE.SEQ) {
            typeStr = "CPU 单核 (Java 顺序执行)";
        }

        System.out.println("============================================");
        System.out.println("当前运行模式: " + typeStr);
        System.out.println("设备具体信息: " + device.toString());
        System.out.println("============================================");

        // 3. 运行计算
        long startTime = System.currentTimeMillis();

        Range range = device.createRange2D(N, N);
        kernel.execute(range);

        long endTime = System.currentTimeMillis();
        long timeMs = endTime - startTime;

        System.out.println("计算完成!");
        System.out.println("矩阵大小: " + N + " x " + N);
        System.out.println("耗时: " + timeMs + " ms");
        
        if (timeMs > 0) {
            System.out.println("性能: " + ((2L * N * N * N) / (1E6 * timeMs)) + " GFLOPS");
        }
    }
}