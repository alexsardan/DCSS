/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dcss.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import my.generic.lib.*;

/**
 *
 * @author Alex
 */
public class DCSSClient {

    public static String processRequest(String req)
    {
        StringTokenizer st = new StringTokenizer(req);
        
        if (st.countTokens() < 1)
            return "Numar insificient de parametri";
        
        String command = st.nextToken();
        
        switch(command)
        {
            case "create":
                if (st.countTokens() == 2)
                {
                    String name = st.nextToken();
                    String pass = st.nextToken();
                    
                    break;
                }
                else
                    return "Apel incorect! \n client [nume] [parola] \n";
            case "login":
                if (st.countTokens() == 2)
                {
                    String name = st.nextToken();
                    String pass = st.nextToken();
                    
                    break;
                }
                return "Apel incorect! \n login [nume] [parola] \n";
                
            case "list":
                if (st.countTokens() > 0)
                    return "Comanda 'list' nu necesita parametri suplimentari";
                else
                    ;                    
                break;
            case "download":
                if (st.countTokens() == 2)
                {
                    try
                    {
                        int id = Integer.parseInt(st.nextToken());
                    } catch(NumberFormatException ex)
                    {
                        return "Parametrul [id_fisier] in format incorect";
                    }
                    
                    String name = st.nextToken();
                    break;
                }
                
            case "upload":
                if (st.countTokens() == 3)
                {
                    
                }
                break;
            default:
                return "Comanda incorecta! [create, login, list, download, upload]";
        }       
        
        return "";
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        try {
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            String command;
            //
            System.out.print("> ");
            while ((command = console.readLine()) != null) 
            {
                if (command.equals("exit")) {
                    return;
                } else {
                   System.out.println(processRequest(command));
                }
                System.out.print("> ");
            }
        } catch (Exception ex) {
            Logger.getLogger(DCSSClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
