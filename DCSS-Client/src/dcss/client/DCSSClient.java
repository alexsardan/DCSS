package dcss.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import my.generic.lib.*;

public class DCSSClient {
    
    private int id;
    public boolean logedIn;
    public int sessionKey;
    public int port;
    public String type;
    public RequestHandler serverRequest;
    public ServerSelector serverSelector;
    public ResponseHandler serverResponse;
    public String lastLoginName;
    
    public DCSSClient(int id)
    {
        this.id = id;
        this.logedIn = false;
        this.serverSelector = new ServerSelector();
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
                    
                    LoginCreateRequestObject lcro = new LoginCreateRequestObject("create", this.id, name, pass);
                    this.serverRequest.sendRequest(lcro);            
                    break;
                }
                else
                    return "Apel incorect! \n client [nume] [parola] \n";
            case "login":
                if (st.countTokens() == 2)
                {
                    String name = st.nextToken();
                    this.lastLoginName = name;
                    String pass = st.nextToken();
                    
                    LoginCreateRequestObject lcro = new LoginCreateRequestObject("login", this.id, name, pass);
                    this.serverRequest.sendRequest(lcro);
                    
                    break;
                }
                return "Apel incorect! \n login [nume] [parola] \n";
                
            case "list":
                if (st.countTokens() > 0)
                    return "Comanda 'list' nu necesita parametri suplimentari";
                else {
                    ListFilesRequestObject list = new ListFilesRequestObject("list", this.id);
                    this.serverRequest.sendRequest(list);
                }                   
                break;
            case "download":
                if (st.countTokens() == 2)
                {
                    try
                    {
                        int idFile = Integer.parseInt(st.nextToken());
                        String name = st.nextToken();
                        DownloadFileRequestObject dfro = new DownloadFileRequestObject("download", this.sessionKey, idFile, name);
                        this.serverRequest.sendRequest(dfro);
                        
                    } catch(NumberFormatException ex)
                    {
                        return "Parametrul [id_fisier] in format incorect";
                    }
                    break;
                }
                
            case "upload":
                if (st.countTokens() == 2) {
                    String filename = st.nextToken();
                    String access = st.nextToken();
                    if ((!access.equals("public")) && (!access.equals("private")))
                        return "Access modifiers should be 'public' or 'private'";
                    File f = new File(filename);
                    CreateFileRequestObject newFileCreateReq = new CreateFileRequestObject("upload_first", this.sessionKey, filename, access, f.length(), this.lastLoginName);
                    this.serverRequest.sendRequest(newFileCreateReq);
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
            BufferedReader cfgIn = new BufferedReader(new FileReader(args[0]));
            
            String cfgLine = cfgIn.readLine();
            while (cfgLine != null) {
                String[] parts = cfgLine.split(":");
                switch(parts[0]) {
                    case "port":
                        client.port = Integer.parseInt(parts[1]);
                        break;
                    case "type":
                        client.type = parts[1];
                        switch(parts[1]) {
                            case "TCP":
                                client.serverSelector.selectServer();
                                Socket sock = new Socket(client.serverSelector.getHost(), client.serverSelector.getPort());
                                client.serverRequest = new TCPRequestHandler(sock);
                                client.serverResponse = new TCPResponseHandler(client, sock);
                                break;
                            case "NIO":
                                client.serverSelector.selectServer();
                                SocketChannel sc = SocketChannel.open();
                                sc.configureBlocking(false);
                                sc.connect(new InetSocketAddress(client.serverSelector.getHost(), client.serverSelector.getPort()));
                                while (!sc.finishConnect());
                                client.serverRequest = new NIORequestHandler(sc);
                                client.serverResponse = new NIOResponseHandler(client, sc);
                                break;
                            default :
                                Logger.getLogger(DCSSClient.class.getName()).log(Level.SEVERE, "This type of connection is not supported: {0}", parts[1]);
                                break;
                        }
                        break;
                    case "s":
                        client.serverSelector.addHosts(parts[1], Integer.parseInt(parts[2]));
                        break;
                }
                cfgLine = cfgIn.readLine();
            }
            cfgIn.close();
            
            client.serverResponse.start();
            String command;
            
            System.out.print("> ");
            while ((command = console.readLine()) != null) 
            {
                if (command.equals("exit")) {
                    return;
                } else {
                    System.out.println(client.processRequest(command));
                }
                System.out.print("> ");
            }
        } catch (Exception ex) {
            Logger.getLogger(DCSSClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
