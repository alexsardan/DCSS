/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dcss.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import my.generic.lib.CreateFileRequestObject;
import my.generic.lib.ExchangeDatabase;
import my.generic.lib.ExchangeDatabaseResponse;
import my.generic.lib.GenericRequest;
import my.generic.lib.GenericResponse;
import my.generic.lib.LoginCreateRequestObject;
import my.generic.lib.ReplicaUploadFileResponse;
import my.generic.lib.ReplicaUserResponse;
import my.generic.lib.UploadFile;
import my.generic.lib.UploadFileRequestObject;
import my.generic.lib.User;

/**
 *
 * @author Alex
 */
public class TCPServerGroup extends ServerGroup {

    ArrayList<ObjectOutputStream> servers;
    ArrayList<Socket> sockets;
    int idServer;
    
    public TCPServerGroup(int idServer) {
        servers = new ArrayList<>();
        sockets = new ArrayList<>();
        this.idServer = idServer;
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
                    case "update":
                        req = new ExchangeDatabase("update", ((ExchangeDatabaseResponse)resp).filesList, ((ExchangeDatabaseResponse)resp).usersList);
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
            this.sockets.add(newSocket);
            GenericRequest srvReq = new GenericRequest("new_server", 0);
            os.writeObject(srvReq);
            
            Database db = new Database(this.idServer);
            ArrayList<UploadFile> filesList = db.getFiles();
            ArrayList<User> usersList = db.getUsers();
            ExchangeDatabase exchangeDatabase = new ExchangeDatabase("exchange", filesList, usersList);
            os.writeObject(exchangeDatabase);
        } catch (SQLException ex) {
            Logger.getLogger(TCPServerGroup.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            Logger.getLogger(TCPServerGroup.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TCPServerGroup.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public synchronized void addExistingStream(ObjectOutputStream str) {
        try {
            this.servers.add(str);
            
            Database db = new Database(this.idServer);
            ArrayList<UploadFile> filesList = db.getFiles();
            ArrayList<User> usersList = db.getUsers();
            ExchangeDatabase exchangeDatabase = new ExchangeDatabase("exchange", filesList, usersList);
            str.writeObject(exchangeDatabase);
        } catch (IOException ex) {
            Logger.getLogger(TCPServerGroup.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(TCPServerGroup.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public Socket getLastSocket() {
        return this.sockets.get(this.sockets.size() - 1);
    }
}
