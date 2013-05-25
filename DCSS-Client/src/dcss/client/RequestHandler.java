/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dcss.client;

import my.generic.lib.GenericRequest;

/**
 *
 * @author Alex
 */
public abstract class RequestHandler {
    public abstract void sendRequest(GenericRequest req);
    public abstract void destroyHandler();
}
