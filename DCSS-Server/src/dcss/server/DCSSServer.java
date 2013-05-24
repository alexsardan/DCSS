/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dcss.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alex
 */
public class DCSSServer {
    ExecutorService processingService;
    
    public static final int PMODULE_SIZE = 16;
    
    public void initWorkpool(int size)
    {
        this.processingService = Executors.newFixedThreadPool(size);
    }
    
    public void finishProcessingAndDestroy() {
        this.processingService.shutdown();
        try {
            this.processingService.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            Logger.getLogger(ProcessingModule.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public void main(String[] args) {
        int sizeWorkpool = new Integer(args[0]).intValue();
        initWorkpool(sizeWorkpool);

        finishProcessingAndDestroy();
    }
}
