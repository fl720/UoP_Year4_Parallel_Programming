import java.awt.*;
import javax.swing.*;
import java.util.concurrent.CyclicBarrier;

// DS GENERATED CODE
public class ParallelLifeBenchmark extends Thread {
    // 测试配置 - 通过修改这些常量进行控制变量测试
    final static int N = 1024;        // 板大小 - 测试时改为 256, 512, 1024, 2048
    final static int CELL_SIZE = 1;   // 显示粒度 - 测试时改为 1, 2, 4
    final static int DELAY = 0;       // 延迟(ms) - 性能测试时保持为0
    final static int P = 4;           // 线程数 - 测试时改为 1, 2, 4, 8, 16
    final static int MAX_ITERATIONS = 100; // 最大迭代次数，用于性能测试
    
    static int[][] state = new int[N][N];
    static int[][] sums = new int[N][N];
    static Display display = new Display();
    static CyclicBarrier barrier = new CyclicBarrier(P);
    
    // 性能统计
    static long totalCalculationTime = 0;
    static int completedIterations = 0;
    
    public static void main(String args[]) throws Exception {
        System.out.println("=== 生命游戏性能测试 ===");
        System.out.println("配置: N=" + N + ", P=" + P + ", CELL_SIZE=" + CELL_SIZE);
        
        // 定义初始状态
        initializeBoard();
        
        if (CELL_SIZE > 0) {
            display.repaint();
            pause();
        }
        
        // 性能测试开始
        long startTime = System.currentTimeMillis();
        
        // 创建并启动线程
        ParallelLifeBenchmark[] threads = new ParallelLifeBenchmark[P];
        for(int me = 0; me < P; me++) {
            threads[me] = new ParallelLifeBenchmark(me);
            threads[me].start();
        }
        
        // 等待所有线程完成
        for(int me = 0; me < P; me++) {
            threads[me].join();
        }
        
        // 性能测试结束
        long endTime = System.currentTimeMillis();
        
        // 输出性能报告
        printPerformanceReport(startTime, endTime);
    }
    
    static void initializeBoard() {
        for(int i = 0; i < N; i++) {
            for(int j = 0; j < N; j++) {
                state[i][j] = Math.random() > 0.5 ? 1 : 0;
            }
        }
    }
    
    static void printPerformanceReport(long startTime, long endTime) {
        System.out.println("\n=== 性能报告 ===");
        System.out.println("总执行时间: " + (endTime - startTime) + " ms");
        System.out.println("完成的迭代次数: " + completedIterations);
        System.out.println("总计算时间: " + totalCalculationTime + " ms");
        System.out.println("平均每代计算时间: " + (totalCalculationTime / (double)completedIterations) + " ms");
        System.out.println("细胞总数: " + (N * N));
        System.out.println("每毫秒处理的细胞数: " + (N * N * completedIterations / (double)totalCalculationTime));
        
        if (P > 1) {
            // 估算单线程性能用于计算加速比
            double singleThreadEstimate = totalCalculationTime * P * 0.85; // 考虑并行开销
            double speedup = singleThreadEstimate / (endTime - startTime);
            System.out.println("并行加速比(估算): " + String.format("%.2f", speedup));
        }
    }
    
    int me;
    
    public ParallelLifeBenchmark(int me) {
        this.me = me;
    }
    
    public void run() {
        // 块分解
        int b = N / P;
        int begin = me * b;
        int end = (me == P-1) ? N : begin + b;
        
        try {
            for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
                long iterStartTime = System.currentTimeMillis();
                
                // 只有线程0输出进度（每10代输出一次以减少IO影响）
                if(me == 0 && iter % 10 == 0) {
                    System.out.println("迭代: " + iter + "/" + MAX_ITERATIONS);
                }
                
                // 计算邻居和
                for(int i = begin; i < end; i++) {
                    for(int j = 0; j < N; j++) {
                        int ip = (i + 1) % N;
                        int im = (i - 1 + N) % N;
                        int jp = (j + 1) % N;
                        int jm = (j - 1 + N) % N;
                        
                        sums[i][j] =
                            state[im][jm] + state[im][j] + state[im][jp] +
                            state[i][jm] + state[i][jp] +
                            state[ip][jm] + state[ip][j] + state[ip][jp];
                    }
                }
                
                barrier.await();
                
                // 更新状态
                for(int i = begin; i < end; i++) {
                    for(int j = 0; j < N; j++) {
                        switch (sums[i][j]) {
                            case 2: break;
                            case 3: state[i][j] = 1; break;
                            default: state[i][j] = 0; break;
                        }
                    }
                }
                
                barrier.await();
                
                long iterEndTime = System.currentTimeMillis();
                long iterTime = iterEndTime - iterStartTime;
                
                // 线程0负责统计性能数据
                if (me == 0) {
                    totalCalculationTime += iterTime;
                    completedIterations++;
                    
                    // 更新显示（如果启用）
                    if (CELL_SIZE > 0) {
                        display.repaint();
                        pause();
                    }
                }
                
                barrier.await();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    static class Display extends JPanel {
        final static int WINDOW_SIZE = N * CELL_SIZE;
        
        Display() {
            if (CELL_SIZE > 0) {
                setPreferredSize(new Dimension(WINDOW_SIZE, WINDOW_SIZE));
                JFrame frame = new JFrame("Life Benchmark - N=" + N + ", P=" + P);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setContentPane(this);
                frame.pack();
                frame.setVisible(true);
            }
        }
        
        public void paintComponent(Graphics g) {
            if (CELL_SIZE > 0) {
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, WINDOW_SIZE, WINDOW_SIZE);
                g.setColor(Color.WHITE);
                for(int i = 0; i < N; i++) {
                    for(int j = 0; j < N; j++) {
                        if(state[i][j] == 1) {
                            g.fillRect(CELL_SIZE * i, CELL_SIZE * j, 
                                      CELL_SIZE, CELL_SIZE);
                        }
                    }
                }
            }
        }
    }
    
    static void pause() {
        try {
            Thread.sleep(DELAY);
        } catch(InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}