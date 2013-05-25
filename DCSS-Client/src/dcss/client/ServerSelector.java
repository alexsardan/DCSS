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

/**
 *
 * @author Alex
 */
public class ServerSelector {
    private String host;
    private int port;
    private ArrayList<SockDescr> alternatives;
    private int sockTry;
    
    private class SockDescr {
        public String host;
        public int port;

        public SockDescr(String host, int port) {
            this.host = host;
            this.port = port;
        }
    }

    public ServerSelector() {
        this.sockTry = 0;
    }
    
    public void addHosts(String hostname, int port) {
        this.alternatives.add(new SockDescr(hostname, port));
    }

    public void selectServer() {
        SockDescr sk = this.alternatives.get(this.sockTry);
        this.sockTry = (this.sockTry + 1)%alternatives.size();
        this.host = sk.host;
        this.port = sk.port;
        /*
 */               
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
    
}
