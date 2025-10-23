  import java.awt.Color ;
  import java.awt.image.BufferedImage ;
  
  import javax.imageio.ImageIO;
  
  import java.io.File ;
  
  public class ParallelMandelbrot_vert_range extends Thread {
  
      final static int N = 4096 ;
      final static int CUTOFF = 100 ; 
  
      static int [] [] set = new int [N] [N] ;
  
      public static void main(String [] args) throws Exception {
  
          // Calculate set
  
          long startTime = System.currentTimeMillis();
  
          ParallelMandelbrot_vert_range thread0 = new ParallelMandelbrot_vert_range(0) ;
          ParallelMandelbrot_vert_range thread1 = new ParallelMandelbrot_vert_range(1) ;
  
          thread0.start() ;
          thread1.start() ;
  
          thread0.join() ;
          thread1.join() ;
  
          long endTime = System.currentTimeMillis();
  
          System.out.println("Calculation completed in " +
                             (endTime - startTime) + " milliseconds");
  
          // Plot image
  
          BufferedImage img = new BufferedImage(N, N,
                                                BufferedImage.TYPE_INT_ARGB) ;
  
          // Draw pixels
  
          for (int i = 0 ; i < N ; i++) {
              for (int j = 0 ; j < N ; j++) {
  
                  int k = set [i] [j] ;
  
                  float level ;
                  if(k < CUTOFF) {
                      level = (float) k / CUTOFF ;
                  }
                  else {
                      level = 0 ;
                  }
                  Color c = new Color(0, level, level) ;  // Cyan
                  img.setRGB(i, j, c.getRGB()) ;
              }
          }
      
  
          // Print file
  
          ImageIO.write(img, "PNG", new File("Mandelbrot_para_verRange.png"));
      }
  
      int me ;
  
      public ParallelMandelbrot_vert_range(int me) {
          this.me = me ;
      }
  
      public void run() {
  
          int begin, end ;

          if (me == 0) {
            begin = 0 ;
            end = N/2 ;
            for(int i = 0 ; i < N ; i++) {
                for(int j = begin ; j < end ; j++) {

                    double cr = (4.0 * i - 2 * N) / N ;
                    double ci = (4.0 * j - 2 * N) / N ;

                    double zr = cr, zi = ci ;

                    int k = 0 ;
                    while (k < CUTOFF && zr * zr + zi * zi < 4.0) {

                        // z = c + z * z

                        double newr = cr + zr * zr - zi * zi ;
                        double newi = ci + 2 * zr * zi ;

                        zr = newr ;
                        zi = newi ;

                        k++ ;
                    }

                    set [i] [j] = k ;
                }
            }
          }
          else {  // me == 1
            begin = N/2 ;
            end = N ;              
            for(int i = 0 ; i < N ; i++) {
                for(int j = begin ; j < end ; j++) {

                    double cr = (4.0 * i - 2 * N) / N ;
                    double ci = (4.0 * j - 2 * N) / N ;

                    double zr = cr, zi = ci ;

                    int k = 0 ;
                    while (k < CUTOFF && zr * zr + zi * zi < 4.0) {

                        // z = c + z * z

                        double newr = cr + zr * zr - zi * zi ;
                        double newi = ci + 2 * zr * zi ;

                        zr = newr ;
                        zi = newi ;

                        k++ ;
                    }

                    set [i] [j] = k ;
                }
            }
          }
      }
  
  }