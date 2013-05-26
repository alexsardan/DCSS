/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dcss.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import my.generic.lib.CreateFileRequestObject;
import my.generic.lib.GenericRequest;
import my.generic.lib.GenericResponse;
import my.generic.lib.LoginCreateRequestObject;
import my.generic.lib.ReplicaUploadFileResponse;
import my.generic.lib.ReplicaUserResponse;
import my.generic.lib.UploadFileRequestObject;

/**
 *
 * @author Alex
 */
public class TCPServerGroup extends ServerGroup {

    ArrayList<ObjectOutputStream> servers;
    ArrayList<Integer> exchange;
    ArrayList<Socket> sockets;
    
    public TCPServerGroup() {
        servers = new ArrayList<>();
        sockets = new ArrayList<>();
        this.exchange = new ArrayList<>();
    }

    @Override
    public synchronized void sendAll(GenericResponse resp) {
        for (ObjectOutputStream o : this.servers) {
            try {
                GenericRequest req = null;
                switch (resp.type) {
                    case "create":
                        req = new LoginCreateRequestObject("push_user", 0, ((ReplicaUserResponse)resp).userName, ((ReplicaUserResponse)resp).password);
                        break;
                    case "push_data":
                        req = new UploadFileRequestObject("push_data", 0, ((ReplicaUploadFileResponse)resp).fileName, ((ReplicaUploadFileResponse)resp).filePath,
                                                          ((ReplicaUploadFileResponse)resp).accessType, ((ReplicaUploadFileResponse)resp).fileLength,
                                                          ((ReplicaUploadFileResponse)resp).owner, ((ReplicaUploadFileResponse)resp).offsetChunk,
                                                          ((ReplicaUploadFileResponse)resp).chunk, ((ReplicaUploadFileResponse)resp).chunkLength);
                        break;
                }
                if (req != null) {
                    o.writeObject(req);
                }
            } catch (IOException ex) {
                Logger.getLogger(TCPServerGroup.class.getName()).log(Level.WARNING, "Cannot write response to server");
            }
        }
    }

    @Override
    public synchronized void addToGroup(String hostname, int port) {
        try {
            Socket newSocket = new Socket(hostname, port);
            ObjectOutputStream os = new ObjectOutputStream(newSocket.getOutputStream());
            this.servers.add(os);
            this.exchange.add(new Integer(0));
            this.sockets.add(newSocket);
            GenericRequest srvReq = new GenericRequest("new_server", 0);
            os.writeObject(srvReq);
        } catch (UnknownHostException ex) {
            Logger.getLogger(TCPServerGroup.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TCPServerGroup.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public synchronized void addExistingStream(ObjectOutputStream str) {
        this.servers.add(str);
        this.exchange.add(new Integer(0));
    }
    
    public Socket getLastSocket() {
        return this.sockets.get(this.sockets.size() - 1);
    }
}
