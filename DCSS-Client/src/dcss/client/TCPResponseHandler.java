/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dcss.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import my.generic.lib.GenericResponse;

/**
 *
 * @author Alex
 */
public class TCPResponseHandler extends ResponseHandler {
    private Socket sock;
    private ObjectInputStream is;

    public TCPResponseHandler(Socket sock) {
        this.sock = sock;
        try {
            is = new ObjectInputStream(this.sock.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(TCPResponseHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        GenericResponse resp = null;
        while (true) {
            try {
                resp = (GenericResponse) this.is.readObject();
                //TODO: process resp;
            } catch (IOException ex) {
                Logger.getLogger(TCPResponseHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(TCPResponseHandler.class.getName()).log(Level.WARNING, "Malformed Packet received");
                continue;
            }
        }
    }
    
}