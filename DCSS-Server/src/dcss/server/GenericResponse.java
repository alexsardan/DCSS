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
    public int dest;
    public byte[] data;

    public GenericResponse(String type, int dest, byte[] data) {
        this.type = type;
        this.dest = dest;
        this.data = data;
    }
    
}
