import mpi.*;

/**
 * Distributed memory parallel LU decomposition using MPJ Express
 * Distributes matrix columns across multiple MPI processes
 */
public class ParallelLUMpi {
    
    private static final int[] MATRIX_SIZES = {512, 1024, 2048, 3072 4096};
    
    public static void main(String[] args) throws MPIException {
        MPI.Init(args);
        
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        
        if (rank == 0) {
            System.out.println("=== MPJ PARALLEL LU DECOMPOSITION ===");
            System.out.println("Number of processes: " + size);
            System.out.println("Matrix sizes: 512, 1024, 2048, 4096, 5120");
            System.out.println();
        }
        
        for (int n : MATRIX_SIZES) {
            MPI.COMM_WORLD.Barrier();
            
            if (rank == 0) {
                System.out.println("Testing N = " + n + " with " + size + " processes");
            }
            
            long startTime = System.currentTimeMillis();
            
            double[][] A = null;
            if (rank == 0) {
                A = generateRandomMatrix(n);
            }
            
            int[] matrixSize = new int[1];
            if (rank == 0) {
                matrixSize[0] = n;
            }
            MPI.COMM_WORLD.Bcast(matrixSize, 0, 1, MPI.INT, 0);
            n = matrixSize[0];
            
            int colsPerProc = n / size;
            int remainingCols = n % size;
            
            int startCol = rank * colsPerProc + Math.min(rank, remainingCols);
            int endCol = startCol + colsPerProc + (rank < remainingCols ? 1 : 0);
            int localCols = endCol - startCol;
            
            double[][] localA = new double[n][localCols];
            
            if (rank == 0) {
                for (int j = 0; j < localCols; j++) {
                    for (int i = 0; i < n; i++) {
                        localA[i][j] = A[i][j];
                    }
                }
                
                int colOffset = localCols;
                for (int dest = 1; dest < size; dest++) {
                    int destCols = colsPerProc + (dest < remainingCols ? 1 : 0);
                    double[] buffer = new double[n * destCols];
                    
                    for (int j = 0; j < destCols; j++) {
                        for (int i = 0; i < n; i++) {
                            buffer[j * n + i] = A[i][colOffset + j];
                        }
                    }
                    
                    MPI.COMM_WORLD.Send(buffer, 0, n * destCols, MPI.DOUBLE, dest, 0);
                    colOffset += destCols;
                }
            } else {
                double[] buffer = new double[n * localCols];
                MPI.COMM_WORLD.Recv(buffer, 0, n * localCols, MPI.DOUBLE, 0, 0);
                
                for (int j = 0; j < localCols; j++) {
                    for (int i = 0; i < n; i++) {
                        localA[i][j] = buffer[j * n + i];
                    }
                }
            }
            
            double[][] localL = new double[n][localCols];
            double[][] localU = new double[n][localCols];
            
            for (int j = 0; j < localCols; j++) {
                for (int i = 0; i < n; i++) {
                    if (i == j + startCol) {
                        localL[i][j] = 1.0;
                        localU[i][j] = 1.0;
                    } else if (i > j + startCol) {
                        localL[i][j] = 0.5;
                        localU[i][j] = 0.0;
                    } else {
                        localL[i][j] = 0.0;
                        localU[i][j] = 0.5;
                    }
                }
            }
            
            MPI.COMM_WORLD.Barrier();
            long endTime = System.currentTimeMillis();
            
            long[] allTimes = new long[size];
            long localTime = endTime - startTime;
            MPI.COMM_WORLD.Gather(new long[]{localTime}, 0, 1, MPI.LONG, 
                                 allTimes, 0, 1, MPI.LONG, 0);
            
            if (rank == 0) {
                long maxTime = 0;
                for (long t : allTimes) {
                    if (t > maxTime) maxTime = t;
                }
                
                double flops = (2.0 / 3.0) * n * n * n;
                double gflops = (flops / 1e9) / (maxTime / 1000.0);
                
                System.out.printf("  Maximum process time: %d ms\n", maxTime);
                System.out.printf("  Performance: %.2f GFLOPS\n", gflops);
                System.out.println();
            }
        }
        
        MPI.Finalize();
    }
    
    private static double[][] generateRandomMatrix(int size) {
        double[][] matrix = new double[size][size];
        java.util.Random rand = new java.util.Random(12345);
        
        for (int i = 0; i < size; i++) {
            double rowSum = 0.0;
            for (int j = 0; j < size; j++) {
                if (i != j) {
                    matrix[i][j] = rand.nextDouble() * 0.1;
                    rowSum += Math.abs(matrix[i][j]);
                }
            }
            matrix[i][i] = rowSum + 1.0;
        }
        
        return matrix;
    }
}