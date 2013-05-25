/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dcss.server;

import my.generic.lib.GenericResponse;

/**
 *
 * @author Alex
 */
public abstract class ServerGroup {
    public abstract void sendAll(GenericResponse resp);
    public abstract void addToGroup(String hostname, int port);
    
}
