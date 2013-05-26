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
        public ObjectOutputStream out;
        boolean useOs;
        
        public TCPResponseManager(LinkedBlockingQueue sendQueue, Socket sock, ServerGroup sg, boolean useOs) {
            super(sendQueue, sg);
            this.respSocket = sock;
            this.useOs = useOs;
        }

        @Override
        public void run() {
            Logger.getLogger(TCPServiceThread.class.getName()).log(Level.INFO, "Service thread started for client");
            try {
                //if (this.useOs) {
                    this.out = new ObjectOutputStream(this.respSocket.getOutputStream());
                //}
                GenericResponse resp;
                
                while (true) {
                    try {
                        resp = this.sendQueue.take();
                        if ((resp.dest != null && resp.dest.equals("server")) || resp.type.equals("update")) {
                            this.sg.sendAll(resp);
                        } else if (resp.dest.equals("client")) {
                            this.out.writeObject(resp);
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(TCPServiceThread.class.getName()).log(Level.WARNING, "The response queue was interrupted");
                        continue;
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(TCPServiceThread.class.getName()).log(Level.SEVERE, null, ex);
            } 
                //if (this.useOs)
                try {
                    this.out.close();
                } catch (IOException ex) {
                    Logger.getLogger(TCPServiceThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            
        }
     
    }
    
    public TCPServiceThread(ExecutorService globalThreadPool, int serverid, Socket clientSock, ServerGroup sg, boolean useOs) {
        super(globalThreadPool, serverid, true);
        this.cSock = clientSock;
        this.respManager = new TCPResponseManager(this.responseQueue, cSock, sg, useOs);
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
                    if (req.type.equals("new_server")) {
                        Logger.getLogger(TCPServiceThread.class.getName()).log(Level.INFO, "This client is actually a server. Adding it to server group.");
                        ((TCPServerGroup)this.serverGroup).addExistingStream(((TCPResponseManager) this.respManager).out);
                    } else {
                        this.requestQueue.add(req);
                    }
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(TCPServiceThread.class.getName()).log(Level.WARNING, null, "Malformed packet received. Skiping...");
                    continue;
                }        
            }
        } catch (IOException ex) {
            Logger.getLogger(TCPServiceThread.class.getName()).log(Level.INFO, "Client exited");
        } finally {        
            try {
                this.processor.join();
                this.respManager.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(TCPServiceThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
