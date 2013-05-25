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
import my.generic.lib.GenericResponse;

/**
 *
 * @author Alex
 */
public class TCPServerGroup extends ServerGroup {

    ArrayList<ObjectOutputStream> servers;
    
    public TCPServerGroup() {
        servers = new ArrayList<>();
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
        } catch (UnknownHostException ex) {
            Logger.getLogger(TCPServerGroup.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TCPServerGroup.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    
}
