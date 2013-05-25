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
import my.generic.lib.GenericRequest;
import my.generic.lib.GenericResponse;

/**
 *
 * @author Alex
 */
public class TCPServerGroup extends ServerGroup {

    ArrayList<ObjectOutputStream> servers;
    ArrayList<Socket> sockets;
    
    public TCPServerGroup() {
        servers = new ArrayList<>();
        sockets = new ArrayList<>();
    }

    @Override
    public synchronized void sendAll(GenericResponse resp) {
        for (ObjectOutputStream o : this.servers) {
            try {
                o.writeObject(resp);
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
            servers.add(os);
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
    }
    
    public Socket getLastSocket() {
        return this.sockets.get(this.sockets.size() - 1);
    }
}
