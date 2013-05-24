/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dcss.server;

/**
 *
 * @author Alex
 */
public class GenericRequest {
    public String type;
    public int session_key;
    public byte[] data;

    public GenericRequest(String type, int session_key, byte[] data) {
        this.type = type;
        this.session_key = session_key;
        this.data = data;
    }
    
}
