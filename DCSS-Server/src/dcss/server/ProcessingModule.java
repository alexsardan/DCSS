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
public class ProcessingModule {
    
    ExecutorService processingService;

    public ProcessingModule(int size) {
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
}
