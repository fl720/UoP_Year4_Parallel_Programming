# UoP_Year4_Parallel_Programming
This is Year4 Parallel Programming practice and coursework source code. Using Java to study different behaviour and properties of different parallel programming approches. 

From : 
* Multicore processors
* Computer Cluster Mode using MPJ(Java message passing library)
* GPU (Using Aparapi)

Folder **PP** contains all practices after each lecture. Folder **mini project** contains all the materials for the coursework.

## PP
Exercise in various topics to discuss and dicover the ability and limitation in each parallel programming methods. 

* lecture 01: calculating π uisng numerical approch in both sequential and parallel (multi-threads) to work out the performace of calculating under different thread numbers(1,2,4,8).
* lecture 02: calculating Mandelbrot set and plot them. Discussed how different decomposition methods (workload) will affect the efficiency.
* lecture 03: simulation of Convey Game of Life. Discovered approches in updating data and calculating data. Introduced method to hold threads before updating all the data.
* lecture 04: using Laplace equation, to further discuss the effects of hold threads before updating and idea of boundry.
* lecture 05: introduce to MPJ cluster mode to solve calculation of π uisng numerical approch in lecture 01. 
* lecture 06: using cluster mode to solve Lapalace equation and to discover the limitation (overhead in communication) of it.
* lecture 07: introduce to Working Farm to solve Mandelbrot set, where a head thread will be in charge of distruibuting the work to slave threads.
Each threads will take the next avaliable work after they have solve their problem. 
* Lecutre 08 & 09 & 10: introduce to GPU. Solving multiplication of Matrices using GPU platform (Aparapi).

## Mini Project
A parallel version of matrix multiplication using Java threads or MPJ Express, that achieves good parallel speedups. 
It generates two random matrices of size N by filling them with random numbers, and multiply them together by parallel and sequential algorithms.
Compares the result of the parallel algorithm with the result of the sequential version, element by element. Compare timings of both algorithms for parallel speedup. 

There is also a parallel version of solution of linear equations by the LU decompostion method. To simplified, "pivotting" is not discussed. 
AparapiAPI is also used to discover ability of GPU in parallel programming. 
