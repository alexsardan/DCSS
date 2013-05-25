package dcss.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import my.generic.lib.*;

public class DCSSClient {
    
    private int id;
    public boolean logedIn;
    
    public DCSSClient(int id)
    {
        this.id = id;
        this.logedIn = false;
    }

    public String processRequest(String req)
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
                    
                    LoginCreateRequestObject lcro = new LoginCreateRequestObject("login", this.id, name, pass);
                                        
                    break;
                }
                else
                    return "Apel incorect! \n client [nume] [parola] \n";
            case "login":
                if (st.countTokens() == 2)
                {
                    String name = st.nextToken();
                    String pass = st.nextToken();
                    
                    LoginCreateRequestObject lcro = new LoginCreateRequestObject("create_user", this.id, name, pass);
                    
                    break;
                }
                return "Apel incorect! \n login [nume] [parola] \n";
                
            case "list":
                if (st.countTokens() > 0)
                    return "Comanda 'list' nu necesita parametri suplimentari";
                else
                {
                    ListFilesRequestObject list = new ListFilesRequestObject("list", this.id);
                }                   
                break;
            case "download":
                if (st.countTokens() == 2)
                {
                    try
                    {
                        int idFile = Integer.parseInt(st.nextToken());
                        String name = st.nextToken();
                       /*TODO*/
                        
                    } catch(NumberFormatException ex)
                    {
                        return "Parametrul [id_fisier] in format incorect";
                    }
                    break;
                }
                
            case "upload":
                if (st.countTokens() == 3)
                {
                    /*TODO*/
                }
                break;
            default:
                return "Comanda incorecta! [create, login, list, download, upload]";
        }       
        
        return "";
    }
    
    public static void main(String[] args) 
    {
        DCSSClient client = new DCSSClient(1);
        try {
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            String command;
            
            System.out.print("> ");
            while ((command = console.readLine()) != null) 
            {
                if (command.equals("exit")) {
                    return;
                } else {
                  /* this.id = 1;
                   
                   System.out.println(processRequest(command));*/
                }
                System.out.print("> ");
            }
        } catch (Exception ex) {
            Logger.getLogger(DCSSClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
