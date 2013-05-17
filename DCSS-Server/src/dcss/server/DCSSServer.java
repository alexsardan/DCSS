/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dcss.server;

/**
 *
 * @author Alex
 */
public class DCSSServer {
    public static final int PMODULE_SIZE = 16;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ProcessingModule requestProcessor = new ProcessingModule(PMODULE_SIZE);
        requestProcessor.finishProcessingAndDestroy();
    }
}
