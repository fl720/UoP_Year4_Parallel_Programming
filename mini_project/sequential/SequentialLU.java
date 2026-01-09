import java.util.Random;

/**
 * Sequential LU Decomposition (Doolittle algorithm) without pivoting.
 * This class performs LU decomposition and measures execution time for different matrix sizes.
 */
public class SequentialLU {

    // Matrix size to test
    private static final int[] MATRIX_SIZES = {512, 1024, 2048, 3072, 4096};

    private static double[][] generateRandomMatrix(int size) {
        Random rand = new Random();
        double[][] matrix = new double[size][size]; 
        for (int i = 0; i < size; i++) {
            double rowSum = 0.0;
            for (int j = 0; j < size; j++) {
                if (i != j) {
                    matrix[i][j] = rand.nextDouble() * 0.1; // small values
                    rowSum += Math.abs(matrix[i][j]);
                }
            }
            matrix[i][i] = rowSum + 1.0; 
        }
        return matrix;
    }

    /**
     * Perform LU decomposition of matrix A.
     * Returns two matrices L and U such that A = L * U.
     * L is lower triangular with 1 on diagonal, U is upper triangular.
     */
    private static LUResult decompose(double[][] A) {
        int n = A.length;
        double[][] L = new double[n][n];
        double[][] U = new double[n][n];

        for (int i = 0; i < n; i++) {
            L[i][i] = 1.0;
        }

        for (int k = 0; k < n; k++) {
            for (int j = k; j < n; j++) {
                double sum = 0.0;
                for (int p = 0; p < k; p++) {
                    sum += L[k][p] * U[p][j];
                }
                U[k][j] = A[k][j] - sum;
            }

            for (int i = k + 1; i < n; i++) {
                double sum = 0.0;
                for (int p = 0; p < k; p++) {
                    sum += L[i][p] * U[p][k];
                }
                L[i][k] = (A[i][k] - sum) / U[k][k];
            }
        }

        return new LUResult(L, U);
    }
    
    private static class LUResult {
        double[][] L;
        double[][] U;

        LUResult(double[][] L, double[][] U) {
            this.L = L;
            this.U = U;
        }
    }

    public static void main(String[] args) {

        for (int n : MATRIX_SIZES) {
            System.out.println("Testing N = " + n);

            double[][] A = generateRandomMatrix(n);

            // Using warm-up 
            if (n == 512) {
                decompose(A);
            }

            long startTime = System.nanoTime();
            decompose(A);
            long endTime = System.nanoTime();
            
            double time = (endTime - startTime) / 1e6 ; 
            System.out.printf("Time: %.4f s\n", time);

            double flops = (2.0 / 3.0) * n * n * n;
            double gflops = (flops / 1e9) / (time / 1000.0);

            System.out.printf("  Performance: %.2f GFLOPS\n", gflops);
            System.out.println();
        }
    }
}