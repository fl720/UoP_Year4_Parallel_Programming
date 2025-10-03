/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */

/**
 *
 * @author up2154598
 */
public class SequentialPi {

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        int numSteps = 10000000 ; 
        double step = 1.0 / (double) numSteps ; 
        double sum = 0.0 ;
        for( int i = 0 ; i < numSteps ; i++ ){
            double x = ( i + 0.5 ) * step ; 
            sum += 4.0 / ( 1.0 + x * x ) ; 
        }
        double pi = step * sum ; 
        long endTime = System.currentTimeMillis();
        System.out.println("Value of pi:" + pi ) ; 
        System.out.println("Calculated in " + (endTime - startTime) + " milliseconds");
    }
}
