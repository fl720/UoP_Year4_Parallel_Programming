import com.aparapi.Kernel;
import com.aparapi.ProfileInfo;
import com.aparapi.Range;
import com.aparapi.device.Device ;
import com.aparapi.device.JavaDevice;

// how to execute in the terminal"
// make sure you have aparapi-2.0.0.jar, aparapi-jni-1.4.3.jar, bcel-6.5.0.jar in your classpath
// javac -cp ".;aparapi-2.0.0.jar" AparapiMatmul.java
// put: java -cp ".;aparapi-2.0.0.jar;aparapi-jni-1.4.3.jar;bcel-6.5.0.jar" AparapiMatmul

public class AparapiMatmul {

    public static final int N = 1024 ; 

    public static void main(String [] args) {

        float [] a = new float [N * N], b = new float [N * N] ;
        float [] c = new float [N * N] ;

        for (int i = 0 ; i < N ; i++) {
            for(int j = 0 ; j < N ; j++) {
                a [N * i + j] = i + j ;
                b [N * i + j] = i - j ;
            }
        }

        Kernel kernel = new Kernel() {
            public void run() {
                int tx = getGlobalId(0) ;
                int ty = getGlobalId(1) ;

                float sum = 0 ;
                for(int k = 0 ; k < N ; k++) {
                    sum += a [N * ty + k] * b [N * k + tx] ;
                }
                c [N * ty + tx] = sum ;
            }
        } ;

        long startTime = System.currentTimeMillis() ;

        Device device = Device.best() ;
       
        Range range = device.createRange2D(N, N) ;
        // 强制使用 CPU (Java 线程池模式)
        // kernel.setExecutionMode(Kernel.EXECUTION_MODE.JTP); 
        kernel.setTargetDevice(JavaDevice.THREAD_POOL);        // 加上这行

        kernel.execute(range) ;

        long endTime = System.currentTimeMillis() ;

        System.out.println("Device type = " +
                           device.getType());

        long timeMs = endTime - startTime ;
        System.out.println("Matrix size: "
                + N ) ;
        System.out.println("Matrix multiplication completed in "
                + timeMs + " milliseconds") ;
        System.out.println("Performance = " +
                ((2L * N * N * N) / (1E6 * timeMs)) + " GFLOPS") ;
    }
}