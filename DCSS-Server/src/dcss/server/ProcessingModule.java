/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dcss.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import my.generic.lib.*;

class RequestHandler implements Runnable
{
    GenericRequest qenericRequest;
    LinkedBlockingQueue responseQueue;
    int idServer = 0;
    
    private static final int CHUNKSIZE = 51200;
    
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
        LoginCreateRequestObject lcro = (LoginCreateRequestObject)qenericRequest;
        
        try {
            int idClient = new Database(this.idServer).logIn(lcro.userName, lcro.password);
            if (idClient != 0)
            {
                ReplyMessage replyMessage = new ReplyMessage("login", "client", "ACK");
                this.responseQueue.add(replyMessage);
            }
            else
            {
                ReplyMessage replyMessage = new ReplyMessage("login", "client", "NACK");
                this.responseQueue.add(replyMessage);
            }
        } catch (SQLException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void createUser(GenericRequest qenericRequest)
    {
        try {
            LoginCreateRequestObject lcro = (LoginCreateRequestObject)qenericRequest;
            
            boolean answerCreateUser = new Database(this.idServer).addUser(lcro.userName, lcro.password);
            if (answerCreateUser == true)
            {
                ReplyMessage replyMessage = new ReplyMessage("create_user", "client", "ACK");
                this.responseQueue.add(replyMessage);
                
                ReplicaUserResponse replicaUserResponse = new ReplicaUserResponse("create_user", "server", lcro.userName, lcro.password);
                this.responseQueue.add(replicaUserResponse);
            }
            else if (answerCreateUser == false)
            {
                ReplyMessage replyMessage = new ReplyMessage("create_user", "client", "NACK");
                this.responseQueue.add(replyMessage);
            }
        } catch (SQLException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void listFiles(GenericRequest qenericRequest)
    {
        try {
            ListFilesRequestObject listFileReqObj = (ListFilesRequestObject)qenericRequest;
            
            ArrayList<UploadFile> files = new Database(this.idServer).browseFiles(listFileReqObj.session_key); 
            
            ListFilesResponseObject listFilesRespObj = new ListFilesResponseObject("list", "client", files);
            this.responseQueue.add(listFilesRespObj);
        } catch (SQLException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void createFile(GenericRequest qenericRequest)
    {
        CreateFileRequestObject createFileReqObj = (CreateFileRequestObject)qenericRequest;
        
        String userHome = System.getProperty("user.home");
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
        String filePathString = userHome + "/SERVER" + this.idServer + "/" + 
                                createFileReqObj.accessType + "/" + createFileReqObj.fileName +
                                "_" + createFileReqObj.session_key + "_" + dateFormat.format(new Date());
        
        File f = new File(filePathString);
        if(f.exists() == false)
        {
            try {
                boolean checkCreation = f.createNewFile();
                if (checkCreation == true)
                {
                    RandomAccessFile raf = new RandomAccessFile(f, "rw");  
                    try  
                    {  
                        raf.setLength(createFileReqObj.fileLength);  
                    }  
                    finally  
                    {  
                        raf.close();  
                    }  
                    
                    Database db = new Database(this.idServer);
                    db.insertUploadEntry(filePathString, createFileReqObj.fileLength);
                    
                    ReplyMessage replyMessage = new ReplyMessage("upload_first", "client", filePathString);
                    this.responseQueue.add(replyMessage);
                }
                else
                {
                    ReplyMessage replyMessage = new ReplyMessage("upload_first", "client", "NACK");
                    this.responseQueue.add(replyMessage);
                }
            } catch (SQLException ex) {
                Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else
        {
            ReplyMessage replyMessage = new ReplyMessage("upload_first", "client", "NACK");
            this.responseQueue.add(replyMessage);
        }
    }
    
    public void uploadFile(GenericRequest qenericRequest)
    {
        FileOutputStream  fileOutputStream = null;
        try {
            UploadFileRequestObject uploadFileReqObj = (UploadFileRequestObject)qenericRequest;
            
            fileOutputStream = new FileOutputStream(uploadFileReqObj.filePath);
            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
            bos.write(uploadFileReqObj.chunk, uploadFileReqObj.offsetChunk, uploadFileReqObj.chunk.length);
            bos.close();
            
            Database db = new Database(this.idServer);
            db.updateUploadEntry(uploadFileReqObj.filePath, uploadFileReqObj.chunk.length);
            if (db.isUploadFinished(uploadFileReqObj.filePath) == true)
            {
                db.deleteUploadEntry(uploadFileReqObj.filePath);
                
                int indexFileNameBegin = uploadFileReqObj.filePath.lastIndexOf("/");
                String fileName = uploadFileReqObj.filePath.substring(indexFileNameBegin);
                int indexFilePermissionBegin = uploadFileReqObj.filePath.lastIndexOf("/", indexFileNameBegin - 1);
                String filePermission = uploadFileReqObj.filePath.substring(indexFilePermissionBegin, indexFileNameBegin);
                int permission = filePermission.equals("private") ? 1 : 0;
                        
                db.addFile(uploadFileReqObj.session_key, fileName, permission, uploadFileReqObj.filePath);
                
                ReplyMessage replyMessage = new ReplyMessage("create_user", "client", "ACK");
                this.responseQueue.add(replyMessage);

                File f = new File(uploadFileReqObj.filePath);
                CreateFileResponseObject createFileRespObj = new CreateFileResponseObject("push_data_first", "server",
                                                                                          fileName, filePermission, f.length());
                this.responseQueue.add(createFileRespObj);
                            
                FileInputStream fileInputStream = new FileInputStream(uploadFileReqObj.filePath);
                BufferedInputStream bis = new BufferedInputStream(fileInputStream);

                int nrChunks = (int) (f.length() / CHUNKSIZE);
                int remainBytes = (int) (f.length() - nrChunks * CHUNKSIZE);

                for (int i = 0 ; i < nrChunks ; i++)
                {
                    byte[] data = new byte[CHUNKSIZE];
                    bis.read(data, i * CHUNKSIZE, CHUNKSIZE);

                    ReplicaUploadFileResponse replicaUploadFileResp = new ReplicaUploadFileResponse("push_data", "server", 
                                                                                                    fileName, i * CHUNKSIZE, data);
                    this.responseQueue.add(replicaUploadFileResp);
                }

                if (remainBytes > 0)
                {
                    byte[] data = new byte[remainBytes];
                    bis.read(data, nrChunks * CHUNKSIZE, remainBytes);

                    ReplicaUploadFileResponse replicaUploadFileResp = new ReplicaUploadFileResponse("push_data", "server", 
                                                                                                    fileName, nrChunks * CHUNKSIZE, data);
                    this.responseQueue.add(replicaUploadFileResp);
                }

                bis.close();      
            }
        } catch (SQLException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

public class ProcessingModule extends Thread
{
    ExecutorService processingService;
    LinkedBlockingQueue<GenericRequest> requestQueue;
    LinkedBlockingQueue<GenericResponse> responseQueue;
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
