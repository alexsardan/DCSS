/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dcss.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import my.generic.lib.GenericRequest;
import my.generic.lib.GenericResponse;

/**
 *
 * @author Alex
 */
public abstract class ServiceThread extends Thread {
    
    public class ResponseManager extends Thread {
        
        LinkedBlockingQueue<GenericResponse> sendQueue;
        ServerGroup sg;
        
        public ResponseManager(LinkedBlockingQueue sendQueue, ServerGroup sg) {
            this.sendQueue = sendQueue;
            this.sg = sg;
        }

        @Override
        public void run() {
            super.run();
        }
        
    }
    
    ExecutorService globalThreadPool;
    LinkedBlockingQueue<GenericRequest> requestQueue;
    LinkedBlockingQueue<GenericResponse> responseQueue;
    ProcessingModule processor;
    ResponseManager respManager;
    ServerGroup serverGroup;
    
    public ServiceThread(ExecutorService globalThreadPool, int serverid) {
        this.globalThreadPool = globalThreadPool;
        this.requestQueue = new LinkedBlockingQueue<>();
        this.responseQueue = new LinkedBlockingQueue<>();
        this.processor = new ProcessingModule(globalThreadPool, requestQueue, responseQueue, serverid);
    }

    @Override
    public void run() {
        Logger.getLogger(ProcessingModule.class.getName()).log(Level.WARNING, "Generic Service Thread cannot handle requests");
    }
    
}
