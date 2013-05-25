/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dcss.server;

import java.io.Serializable;

/**
 *
 * @author Alex
 */
public class GenericRequest implements Serializable {
    public String type;
    public int session_key;

    public GenericRequest(String type, int session_key) {
        this.type = type;
        this.session_key = session_key;
    }
    
}
