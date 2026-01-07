import java.util.Random;

public class SeqMatMul {

    public static void main(String[] args) throws Exception {
    int N = (args.length >= 1) ? Integer.parseInt(args[0]) : 512;
    int threads = (args.length >= 2) ? Integer.parseInt(args[1]) : Runtime.getRuntime().availableProcessors();

    double[][] A = randomMatrix(N, 123);
    double[][] B = randomMatrix(N, 456);

    int trials = 5;

// Warmup
for (int w = 0; w < 3; w++) {
    multiplySequential(A, B);
    multiplyParallel(A, B, threads);
}


// ---- Sequential timings (print all 5) ----
System.out.println("Sequential runs (ms):");
for (int t = 0; t < trials; t++) {
    long t0 = System.nanoTime();
    multiplySequential(A, B);
    long t1 = System.nanoTime();
    double ms = (t1 - t0) / 1_000_000.0;
    System.out.println("  Run " + (t + 1) + ": " + ms);
}

// ---- Parallel timings (print all 5) ----
System.out.println("Parallel runs (ms):");
for (int t = 0; t < trials; t++) {
    long t0 = System.nanoTime();
    multiplyParallel(A, B, threads);
    long t1 = System.nanoTime();
    double ms = (t1 - t0) / 1_000_000.0;
    System.out.println("  Run " + (t + 1) + ": " + ms);
}

// ---- Compute results once for correctness check ----
double[][] Cseq = multiplySequential(A, B);
double[][] Cpar = multiplyParallel(A, B, threads);

// Verify correctness
assertSame(Cseq, Cpar, 1e-9);

System.out.println("N = " + N);
System.out.println("Threads = " + threads);
System.out.println("Checksum: " + checksum(Cpar));
    }

    // Step 2 will fill these in:
    static double[][] randomMatrix(int n, long seed) {
    Random rand = new Random(seed);
    double[][] M = new double[n][n];

    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            M[i][j] = rand.nextDouble(); // random value in [0, 1)
        }
    }
    return M;
}


    static double[][] multiplySequential(double[][] A, double[][] B) {
    int n = A.length;
    double[][] Bt = transpose(B);
    double[][] C = new double[n][n];

    for (int i = 0; i < n; i++) {
        double[] Ai = A[i];
        double[] Ci = C[i];

        for (int j = 0; j < n; j++) {
            double[] Btj = Bt[j];
            double sum = 0.0;

            for (int k = 0; k < n; k++) {
                sum += Ai[k] * Btj[k];
            }
            Ci[j] = sum;
        }
    }
    return C;
}

    static double[][] multiplyParallel(double[][] A, double[][] B, int numThreads) throws InterruptedException {
    int n = A.length;
    double[][] Bt = transpose(B);
    double[][] C = new double[n][n];

    numThreads = Math.max(1, Math.min(numThreads, n)); // no more threads than rows
    Thread[] threads = new Thread[numThreads];

    int rowsPerThread = (n + numThreads - 1) / numThreads;

    for (int t = 0; t < numThreads; t++) {
        int start = t * rowsPerThread;
        int end = Math.min(n, start + rowsPerThread);

        threads[t] = new Thread(() -> {
            for (int i = start; i < end; i++) {
                double[] Ai = A[i];
                double[] Ci = C[i];

                for (int j = 0; j < n; j++) {
                    double[] Btj = Bt[j];
                    double sum = 0.0;

                    for (int k = 0; k < n; k++) {
                        sum += Ai[k] * Btj[k];
                    }
                    Ci[j] = sum;
                }
            }
        });

        threads[t].start();
    }

    for (Thread th : threads) th.join(); // wait for all threads
    return C;
}

     
     static double[][] transpose(double[][] M) {
    int n = M.length;
    double[][] T = new double[n][n];
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            T[j][i] = M[i][j];
        }
    }
    return T;
}
    static double checksum(double[][] C) {
    double sum = 0.0;
    int n = C.length;
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            sum += C[i][j];
        }
    }
    return sum;

}
    static void assertSame(double[][] X, double[][] Y, double eps) {
    int n = X.length;
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            double a = X[i][j];
            double b = Y[i][j];
            double diff = Math.abs(a - b);

            // relative-ish tolerance
            double scale = Math.max(1.0, Math.max(Math.abs(a), Math.abs(b)));
            if (diff > eps * scale) {
                throw new AssertionError("Mismatch at (" + i + "," + j + "): " + a + " vs " + b);
            }
        }
    }
}

}