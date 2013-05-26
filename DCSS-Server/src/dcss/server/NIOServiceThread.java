/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dcss.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
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
public class NIOServiceThread extends ServiceThread {
    
    ServerSocketChannel niossc;
    Selector sel;
    
    class NIOResponseManager extends ResponseManager {

        public NIOResponseManager(LinkedBlockingQueue sendQueue, ServerGroup sg) {
            super(sendQueue, sg);
        }
        
    }
    
    class AttachmentContainer {
        LinkedBlockingQueue<GenericRequest> reqq;
        LinkedBlockingQueue<GenericResponse> respq;
        ProcessingModule proc;

        public AttachmentContainer(LinkedBlockingQueue<GenericRequest> reqq, LinkedBlockingQueue<GenericResponse> respq, ProcessingModule proc) {
            this.reqq = reqq;
            this.respq = respq;
            this.proc = proc;
        }
        
    }
    
    public NIOServiceThread(ExecutorService globalThreadPool, int serverid, ServerSocketChannel niossc) {
        super(globalThreadPool, serverid, false);
        this.niossc = niossc;
        try {
            this.sel = Selector.open();
        } catch (IOException ex) {
            Logger.getLogger(NIOServiceThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendObj(SocketChannel socket, GenericResponse serializable) throws IOException {
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
    
    private final ByteBuffer lengthByteBuffer = ByteBuffer.wrap(new byte[4]);
    private ByteBuffer dataByteBuffer = null;
    private boolean readLength = true;

    public GenericRequest recv(SocketChannel socket) throws IOException, ClassNotFoundException {
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
                final GenericRequest ret = (GenericRequest) ois.readObject();
                dataByteBuffer = null;
                readLength = true;
                return ret;
            }
        }
        return null;
    }

    @Override
    public void run() {
            try {
                niossc.register(sel, SelectionKey.OP_ACCEPT);
                while (true) {
                    sel.select();
                    for (Iterator<SelectionKey> i = sel.selectedKeys().iterator(); i.hasNext();) {
                        SelectionKey key = i.next();
                        i.remove();
                        if (key.isConnectable()) { 
				((SocketChannel)key.channel()).finishConnect(); 
			} 
			if (key.isAcceptable()) { 
				// accept connection 
				SocketChannel client = niossc.accept(); 
				client.configureBlocking(false); 
				client.socket().setTcpNoDelay(true); 
				client.register(sel, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                                LinkedBlockingQueue<GenericRequest> reqq = new LinkedBlockingQueue<>();
                                LinkedBlockingQueue<GenericResponse> respq = new LinkedBlockingQueue<>();
                                ProcessingModule proc = new ProcessingModule(this.globalThreadPool, reqq, respq, this.serverid);
                                AttachmentContainer ct = new AttachmentContainer(reqq, respq, proc);
                                key.attach(ct);
                                proc.start();
			} 
			if (key.isReadable()) {
                            try {
                                GenericRequest req = null;
                                if (( req = this.recv((SocketChannel) key.channel())) == null) {
                                    req = this.recv((SocketChannel) key.channel());
                                }
                                if (req == null) {
                                    System.out.println("This is just wrong!!");
                                   
                                } else {
                                    if (req.type.equals("new_server")) {
                                        Logger.getLogger(NIOServiceThread.class.getName()).log(Level.INFO, "This client is actually a server. Adding it to server group.");
                                        ((NIOServerGroup)this.serverGroup).addExistingChannel((SocketChannel) key.channel());
                                    } else {
                                        ((AttachmentContainer)key.attachment()).reqq.add(req);
                                    }
                                }
                            } catch (ClassNotFoundException ex) {
                                Logger.getLogger(NIOServiceThread.class.getName()).log(Level.SEVERE, null, ex);
                            }
			}
                        if (key.isWritable()) {
                            if (key.attachment() == null)
                                continue;
                            GenericResponse resp = ((AttachmentContainer)key.attachment()).respq.poll();
                            if (resp != null) {
                                if (resp.dest.equals("client")) {
                                    this.sendObj((SocketChannel) key.channel(), resp);
                                } else if (resp.dest.equals("server")) {
                                    this.serverGroup.sendAll(resp);
                                }
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(NIOServiceThread.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    sel.close();
                    niossc.socket().close();
                    niossc.close();
                } catch (Exception ex) {
                    Logger.getLogger(NIOServiceThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
    }
    
}
