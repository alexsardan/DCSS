/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dcss.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alex
 */
public class TCPServiceThread extends ServiceThread {
    
    private Socket cSock;
    
    public class TCPResponseManager extends ServiceThread.ResponseManager {
        Socket respSocket;
        
        public TCPResponseManager(LinkedBlockingQueue sendQueue, Socket sock) {
            super(sendQueue);
            this.respSocket = sock;
        }

        @Override
        public void run() {
            //TODO: add response logic here
        }
     
    }
    
    public TCPServiceThread(ExecutorService globalThreadPool, int serverid, Socket clientSock) {
        super(globalThreadPool, serverid);
        this.cSock = clientSock;
        this.respManager = new TCPResponseManager(this.responseQueue, cSock);
    }

    @Override
    public void run() {
        try {
            ObjectInputStream in = new ObjectInputStream(this.cSock.getInputStream());
            GenericRequest req;
            
            while (true) {
                try {
                    req = (GenericRequest) in.readObject();
                    this.requestQueue.add(req);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(TCPServiceThread.class.getName()).log(Level.WARNING, null, "Malformed packet received. Skiping...");
                    continue;
                }        
            }
        } catch (IOException ex) {
            Logger.getLogger(TCPServiceThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
