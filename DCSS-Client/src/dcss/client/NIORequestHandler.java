/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dcss.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import my.generic.lib.GenericRequest;

/**
 *
 * @author Alex
 */
public class NIORequestHandler extends RequestHandler {
    public SocketChannel chan;
    
    public NIORequestHandler(SocketChannel ch) {
        this.chan = ch;
    }

    @Override
    public void destroyHandler() {
        try {
            this.chan.close();
        } catch (IOException ex) {
            Logger.getLogger(NIORequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendObj(SocketChannel socket, GenericRequest serializable) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for(int i=0;i<4;i++) {
            baos.write(0);
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(serializable);
        }
        final ByteBuffer wrap = ByteBuffer.wrap(baos.toByteArray());
        wrap.putInt(0, baos.size()-4);
        socket.write(wrap);
    }

    @Override
    public void sendRequest(GenericRequest req) {
        try {
            this.sendObj(chan, req);
        } catch (IOException ex) {
            Logger.getLogger(NIORequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}
