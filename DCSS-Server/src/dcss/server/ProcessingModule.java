/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dcss.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Alex
 */

class RequestHandler implements Runnable
{
    GenericRequest qenericRequest;
    LinkedBlockingQueue responseQueue;
    int idServer = 0;
    
    public RequestHandler(GenericRequest qenReq,
                          LinkedBlockingQueue respQueue,
                          int idServer)
    {
        this.qenericRequest = qenReq;
        this.responseQueue = respQueue;
        this.idServer = idServer;
    }
    
    @Override
    public void run()
    {
            switch (this.qenericRequest.type)
            {
                case "login":
                    loginClient(this.qenericRequest);
                    break;
                case "create_user":
                    createUser(this.qenericRequest);
                    break;
                case "list":
                    listFiles(this.qenericRequest);
                    break;
                case "upload_first":
                    createFile(this.qenericRequest);
                    break;
                case "upload":
                    uploadFile(this.qenericRequest);
                    break;
                case "download":
                    downloadFile(this.qenericRequest);
                    break;
                case "push_data_first":
                    createFile(this.qenericRequest);
                    break;
                case "push_data":
                    uploadFile(this.qenericRequest);
                    break;
                case "push_user":
                    createUser(this.qenericRequest);
                    break;
                case "exchange":
                    exchangeDatabase(this.qenericRequest);
                    break;
            }
    }
    
    public void loginClient(GenericRequest qenericRequest)
    {
        ObjectInputStream objectInputStream = null;
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(qenericRequest.data);
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            LoginCreateRequestObject lcro = (LoginCreateRequestObject)objectInputStream.readObject();
            
            int idClient = new Database(this.idServer).logIn(lcro.name, lcro.password);
            if (idClient != 0)
            {
                GenericResponse genericResponse = new GenericResponse("login", "client", new String("ACK").getBytes());
                this.responseQueue.add(genericResponse);
            }
            else
            {
                GenericResponse genericResponse = new GenericResponse("login", "client", new String("NACK").getBytes());
                responseQueue.add(genericResponse);
            }
        } catch (SQLException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                objectInputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void createUser(GenericRequest qenericRequest)
    {
        ObjectInputStream objectInputStream = null;
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(qenericRequest.data);
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            LoginCreateRequestObject lcro = (LoginCreateRequestObject)objectInputStream.readObject();
            
            boolean answerCreateUser = new Database(this.idServer).addUser(lcro.name, lcro.password);
            if (answerCreateUser == true)
            {
                GenericResponse genericResponse = new GenericResponse("create_user", "client", new String("ACK").getBytes());
                this.responseQueue.add(genericResponse);
                
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                objectOutputStream.writeObject(lcro);
                genericResponse = new GenericResponse("create_user", "server", byteArrayOutputStream.toByteArray());
                this.responseQueue.add(genericResponse);
            }
            else if (answerCreateUser == false)
            {
                GenericResponse genericResponse = new GenericResponse("create_user", "client", new String("NACK").getBytes());
                this.responseQueue.add(genericResponse);
            }
        } catch (SQLException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                objectInputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void listFiles(GenericRequest qenericRequest)
    {
        
    }
}

public class ProcessingModule extends Thread
{
    ExecutorService processingService;
    LinkedBlockingQueue requestQueue;
    LinkedBlockingQueue responseQueue;
    int idServer = 0;
    
    public ProcessingModule(ExecutorService ps, 
                            LinkedBlockingQueue reqQueue,
                            LinkedBlockingQueue respQueue,
                            int idServer)
    {
        this.processingService = ps;
        this.requestQueue = reqQueue;
        this.responseQueue = respQueue;
        this.idServer = idServer;
    }
    
    @Override
    public void run()
    {
        while(true)
        {
            try {
                GenericRequest qenReq = (GenericRequest)this.requestQueue.take();
                processingService.submit(new RequestHandler(qenReq, this.responseQueue, this.idServer));
            } catch (InterruptedException ex) {
                Logger.getLogger(ProcessingModule.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
