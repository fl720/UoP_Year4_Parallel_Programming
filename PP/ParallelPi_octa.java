/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */

/**
 *
 * @author up2154598
 */
public class ParallelPi_octa extends Thread {
      public static void main(String[] args) throws Exception {

          long startTime = System.nanoTime();

          ParallelPi_octa thread1 = new ParallelPi_octa();
          thread1.begin = 0 ;
          thread1.end = numSteps / 8 ;

          ParallelPi_octa thread2 = new ParallelPi_octa();
          thread2.begin = numSteps / 8 ;
          thread2.end = numSteps / 4 ;

          ParallelPi_octa thread3 = new ParallelPi_octa();
          thread3.begin = numSteps / 4 ;
          thread3.end = numSteps / 8 * 3 ;

          ParallelPi_octa thread4 = new ParallelPi_octa();
          thread4.begin = numSteps / 8 * 3 ;
          thread4.end = numSteps / 2 ;

          ParallelPi_octa thread5 = new ParallelPi_octa();
          thread5.begin = numSteps / 2 ;
          thread5.end = numSteps / 8 * 5;

          ParallelPi_octa thread6 = new ParallelPi_octa();
          thread6.begin = numSteps / 8 * 5 ;
          thread6.end = numSteps / 4 * 3 ;

          ParallelPi_octa thread7 = new ParallelPi_octa();
          thread7.begin = numSteps / 4 * 3;
          thread7.end = numSteps / 8 * 7 ;

          ParallelPi_octa thread8 = new ParallelPi_octa();
          thread8.begin = numSteps / 8 * 7 ;
          thread8.end = numSteps  ;

          thread1.start();
          thread2.start();
          thread3.start();
          thread4.start();
          thread5.start();
          thread6.start();
          thread7.start();
          thread8.start();
        
          thread1.join();
          thread2.join();
          thread3.join();
          thread4.join();
          thread5.join();
          thread6.join();
          thread7.join();
          thread8.join();

          long endTime = System.nanoTime();

          double pi = step * (thread1.sum + thread2.sum + thread3.sum + thread4.sum + thread7.sum + thread5.sum + thread6.sum + thread8.sum) ;
        
          System.out.println("Value of pi: " + pi);

          System.out.println("Calculated in " +
                             (endTime - startTime) + " milliseconds");
      }

      static int numSteps = 10000000 ;
    
      static double step = 1.0 / (double) numSteps;

      double sum ;  
      int begin, end ;

      public void run() {

          sum = 0.0 ;

          for(int i = begin ; i < end ; i++){
              double x = (i + 0.5) * step ;
              sum += 4.0 / (1.0 + x * x);
          }
      }
  }