/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dcss.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alex
 */
public class DCSSClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            String command;
            
            System.out.print("> ");
            while ((command = console.readLine()) != null) {
                if (command.equals("exit")) {
                    return;
                } else {
                    System.out.println(command);
                }
                System.out.print("> ");
            }
        } catch (IOException ex) {
            Logger.getLogger(DCSSClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
