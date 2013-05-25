/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dcss.client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import my.generic.lib.GenericRequest;

/**
 *
 * @author Alex
 */
public class TCPRequestHandler extends RequestHandler {

    private Socket sock;
    private ObjectOutputStream os;
    
    public TCPRequestHandler(Socket sk) {
        super();
        this.sock = sk;
        try {
            this.os = new ObjectOutputStream(this.sock.getOutputStream());
        } catch (IOException ex) {
            Logger.getLogger(TCPRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public synchronized void sendRequest(GenericRequest req) {
        try {
            os.writeObject(req);
        } catch (IOException ex) {
            Logger.getLogger(TCPRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void destroyHandler() {
        try {
            this.os.close();
            this.sock.close();
        } catch (IOException ex) {
            Logger.getLogger(TCPRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }      
}
