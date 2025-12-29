// package sequential;

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
        int[] sizes = {500, 1000, 2500, 5000 }; // matrix sizes to test
        
        for (int n : sizes) {
            System.out.println("\n Matrices Size: " + n + "x" + n);
            
            double[][] A = generateRandomMatrix(n, n);
            double[][] B = generateRandomMatrix(n, n);

            multiply(A, B);
            
            long startTime = System.nanoTime();
            double[][] C = multiply(A, B);
            long endTime = System.nanoTime();
            
            double time = (endTime - startTime) / 1e9;
            System.out.printf("Time: %.4f s\n", time);
            System.out.printf("GFLOPS: %.2f \n", 
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