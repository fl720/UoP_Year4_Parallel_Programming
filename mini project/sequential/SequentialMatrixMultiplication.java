package sequential;

public class SequentialMatrixMultiplication {
    
    public static double[][] multiply(double[][] A, double[][] B) {
        int n = A.length;
        int m = B[0].length;
        int p = B.length;
        
        double[][] C = new double[n][m];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                double sum = 0.0;
                for (int k = 0; k < p; k++) {
                    sum += A[i][k] * B[k][j];
                }
                C[i][j] = sum;
            }
        }
        
        return C;
    }
    
    public static void main(String[] args) {
        // 测试不同规模的矩阵
        int[] sizes = {512, 1024, 2048};
        
        for (int n : sizes) {
            System.out.println("\n测试矩阵大小: " + n + "x" + n);
            
            // 生成随机矩阵
            double[][] A = generateRandomMatrix(n, n);
            double[][] B = generateRandomMatrix(n, n);
            
            // 预热
            multiply(A, B);
            
            // 正式计时
            long startTime = System.nanoTime();
            double[][] C = multiply(A, B);
            long endTime = System.nanoTime();
            
            double time = (endTime - startTime) / 1e9;
            System.out.printf("执行时间: %.4f 秒\n", time);
            System.out.printf("性能: %.2f GFLOPS\n", 
                2.0 * n * n * n / time / 1e9);
        }
    }
    
    private static double[][] generateRandomMatrix(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = Math.random();
            }
        }
        return matrix;
    }
}