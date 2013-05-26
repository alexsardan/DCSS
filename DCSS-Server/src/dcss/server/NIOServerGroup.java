/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dcss.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import my.generic.lib.CreateFileRequestObject;
import my.generic.lib.GenericRequest;
import my.generic.lib.GenericResponse;
import my.generic.lib.LoginCreateRequestObject;
import my.generic.lib.ReplicaFileResponseObject;
import my.generic.lib.ReplicaUploadFileResponse;
import my.generic.lib.ReplicaUserResponse;
import my.generic.lib.UploadFileRequestObject;

/**
 *
 * @author Alex
 */
public class NIOServerGroup extends ServerGroup {

    ArrayList<SocketChannel> channels;
    
    public NIOServerGroup() {
        this.channels = new ArrayList<>();
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
    public void addToGroup(String hostname, int port) {
        try {
            SocketChannel sc = SocketChannel.open();
            sc.configureBlocking(false);
            sc.connect(new InetSocketAddress(hostname, port));
            while (!sc.finishConnect());
            this.channels.add(sc);
        } catch (IOException ex) {
            Logger.getLogger(NIOServerGroup.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void sendAll(GenericResponse resp) {
        for (SocketChannel o : this.channels) {
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
                    this.sendObj(o, req);
                }
            } catch (IOException ex) {
                Logger.getLogger(TCPServerGroup.class.getName()).log(Level.WARNING, "Cannot write response to server");
            }
        }
    }
    
    public synchronized void addExistingChannel(SocketChannel chan) {
        this.channels.add(chan);
    }
}
