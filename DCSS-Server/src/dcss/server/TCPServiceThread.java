/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dcss.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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
public class TCPServiceThread extends ServiceThread {
    
    private Socket cSock;
    
    public class TCPResponseManager extends ServiceThread.ResponseManager {
        Socket respSocket;
        
        public TCPResponseManager(LinkedBlockingQueue sendQueue, Socket sock, ServerGroup sg) {
            super(sendQueue, sg);
            this.respSocket = sock;
        }

        @Override
        public void run() {
            try {
                ObjectOutputStream out = new ObjectOutputStream(this.respSocket.getOutputStream());
                GenericResponse resp;
                
                while (true) {
                    try {
                        resp = this.sendQueue.take();
                        if (resp.type.equals("server")) {
                            this.sg.sendAll(resp);
                        } else if (resp.type.equals("client")) {
                            out.writeObject(resp);
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(TCPServiceThread.class.getName()).log(Level.WARNING, "The response queue was interrupted");
                        continue;
                    }
                    out.writeObject(resp);
                }
            } catch (IOException ex) {
                Logger.getLogger(TCPServiceThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
     
    }
    
    public TCPServiceThread(ExecutorService globalThreadPool, int serverid, Socket clientSock, ServerGroup sg) {
        super(globalThreadPool, serverid);
        this.cSock = clientSock;
        this.respManager = new TCPResponseManager(this.responseQueue, cSock, sg);
        this.serverGroup = sg;
    }

    @Override
    public void run() {
        this.respManager.start();
        this.processor.start();
        
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
        
        try {
            this.processor.join();
            this.respManager.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(TCPServiceThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
