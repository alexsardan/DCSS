/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dcss.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import my.generic.lib.GenericRequest;
import my.generic.lib.GenericResponse;

/**
 *
 * @author Alex
 */
public class NIOResponseHandler extends ResponseHandler {
    
    public SocketChannel chan;

    public NIOResponseHandler(DCSSClient client, SocketChannel ch) {
        super(client);
        this.chan = ch;
    }
    
    private final ByteBuffer lengthByteBuffer = ByteBuffer.wrap(new byte[4]);
    private ByteBuffer dataByteBuffer = null;
    private boolean readLength = true;

    public GenericResponse recv(SocketChannel socket) throws IOException, ClassNotFoundException {
        if (readLength) {
            socket.read(lengthByteBuffer);
            if (lengthByteBuffer.remaining() == 0) {
                readLength = false;
                dataByteBuffer = ByteBuffer.allocate(lengthByteBuffer.getInt(0));
                lengthByteBuffer.clear();
            }
        } else {
            socket.read(dataByteBuffer);
            if (dataByteBuffer.remaining() == 0) {
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(dataByteBuffer.array()));
                final GenericResponse ret = (GenericResponse) ois.readObject();
                dataByteBuffer = null;
                readLength = true;
                return ret;
            }
        }
        return null;
    }

    @Override
    public void run() {
        GenericResponse resp = null;
        while (true) {
            try {
                resp = this.recv(this.chan);
                if (resp == null)
                    continue;
                super.processResponse(resp);
            } catch (IOException ex) {
                Logger.getLogger(NIOResponseHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(NIOResponseHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
