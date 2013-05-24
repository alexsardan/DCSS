/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dcss.server;

/**
 *
 * @author Alex
 */
public class GenericResponse {
    public String type;
    public String dest;
    public byte[] data;

        public GenericResponse(String type, String dest, byte[] data) {
        this.type = type;
        this.dest = dest;
        this.data = data;
    }
    
}
