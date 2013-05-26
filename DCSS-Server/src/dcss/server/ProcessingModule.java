
package dcss.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
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
                case "create":
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
                case "update":
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
                ReplyMessage replyMessage = new ReplyMessage("login", "client", idClient + "");
                this.responseQueue.add(replyMessage);
            }
            else
            {
                ReplyMessage replyMessage = new ReplyMessage("login", "client", 0 + "");
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
            if ((answerCreateUser == true) && (lcro.type.equals("push_user") == false))
            {
                ReplyMessage replyMessage = new ReplyMessage("create", "client", "ACK");
                this.responseQueue.add(replyMessage);
                
                ReplicaUserResponse replicaUserResponse = new ReplicaUserResponse("create", "server", lcro.userName, lcro.password);
                this.responseQueue.add(replicaUserResponse);
            }
            else if (answerCreateUser == false)
            {
                ReplyMessage replyMessage = new ReplyMessage("create", "client", "NACK");
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
        try {
            CreateFileRequestObject createFileReqObj = (CreateFileRequestObject)qenericRequest;
            
            int session_key = 0;
            if (createFileReqObj.type.equals("push_data_first"))
                session_key = new Database(this.idServer).getId(createFileReqObj.owner);
            else 
                session_key = createFileReqObj.session_key;
            
            String userHome = System.getProperty("user.home");
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String filePathString = userHome + "/SERVER" + this.idServer + "/" + 
                                    createFileReqObj.accessType + "/" + createFileReqObj.fileName +
                                    "_" + (session_key + "") + "_" + dateFormat.format(new Date());
            
            File f = new File(filePathString);
            File folder = new File(filePathString.substring(0, filePathString.lastIndexOf("/")));
            if (folder.exists() == false)
                folder.mkdirs();
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
                        
                        if (createFileReqObj.equals("push_data_first") == false)
                        {
                            ReplyMessage replyMessage = new ReplyMessage("upload_first", "client", filePathString);
                            this.responseQueue.add(replyMessage);
                        }
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
        } catch (SQLException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void uploadFile(GenericRequest qenericRequest)
    {
        RandomAccessFile raf = null;
        try {
            UploadFileRequestObject uploadFileReqObj = (UploadFileRequestObject)qenericRequest;
            
            Database db = new Database(this.idServer);
            
            raf = new RandomAccessFile(uploadFileReqObj.filePath, "rw");
           //raf.seek((long)uploadFileReqObj.offsetChunk);
            raf.write(uploadFileReqObj.chunk, uploadFileReqObj.offsetChunk, uploadFileReqObj.chunkLength);
            raf.seek(0);
            raf.close();
            
            db.updateUploadEntry(uploadFileReqObj.filePath, uploadFileReqObj.chunkLength);
            if (db.isUploadFinished(uploadFileReqObj.filePath) == true)
            {
                db.deleteUploadEntry(uploadFileReqObj.filePath);
                
                int indexFileNameBegin = uploadFileReqObj.filePath.lastIndexOf("/");
                String fileName = uploadFileReqObj.filePath.substring(indexFileNameBegin + 1);
                int indexFilePermissionBegin = uploadFileReqObj.filePath.lastIndexOf("/", indexFileNameBegin - 1);
                String filePermission = uploadFileReqObj.filePath.substring(indexFilePermissionBegin, indexFileNameBegin);
                int permission = filePermission.equals("private") ? 1 : 0;
                        
                int firstUnderscore = fileName.lastIndexOf("_");
                int secondUnderscore = fileName.lastIndexOf("_", firstUnderscore - 1);
                
                int session_key = 0;
                if (uploadFileReqObj.type.equals("push_data_first"))
                    session_key = new Database(this.idServer).getId(uploadFileReqObj.owner);
                else 
                    session_key = uploadFileReqObj.session_key;
                
                db.addFile(session_key, fileName.substring(0, secondUnderscore), permission, uploadFileReqObj.filePath);
                
                if (uploadFileReqObj.type.equals("push_data") == false)
                {
                    ReplyMessage replyMessage = new ReplyMessage("create_user", "client", "ACK");
                    this.responseQueue.add(replyMessage);

                    File f = new File(uploadFileReqObj.filePath);
                    ReplicaFileResponseObject createFileRespObj = new ReplicaFileResponseObject("push_data_first", "server",
                                                                                              fileName, filePermission, f.length(), uploadFileReqObj.owner);
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
                                                                                                        fileName, i * CHUNKSIZE,
                                                                                                        CHUNKSIZE, data, uploadFileReqObj.owner);
                        this.responseQueue.add(replicaUploadFileResp);
                    }

                    if (remainBytes > 0)
                    {
                        byte[] data = new byte[remainBytes];
                        bis.read(data, nrChunks * CHUNKSIZE, remainBytes);

                        ReplicaUploadFileResponse replicaUploadFileResp = new ReplicaUploadFileResponse("push_data", "server", 
                                                                                                        fileName, nrChunks * CHUNKSIZE,
                                                                                                        remainBytes, data, uploadFileReqObj.owner);
                        this.responseQueue.add(replicaUploadFileResp);
                    }

                    bis.close();      
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void downloadFile(GenericRequest qenericRequest)
    {
        FileInputStream fileInputStream = null;
        try {
            DownloadFileRequestObject downloadFileReqObj = (DownloadFileRequestObject)qenericRequest;
            
            Database db = new Database(this.idServer);
            String filePath = db.getPath(downloadFileReqObj.idFile,
                                         downloadFileReqObj.fileName,
                                         downloadFileReqObj.session_key);
            if (filePath.equals(""))
            {
                ReplyMessage replyMessage = new ReplyMessage("create_user", "client", "NACK");
                this.responseQueue.add(replyMessage);
                return;
            }
            fileInputStream = new FileInputStream(filePath);
            BufferedInputStream bis = new BufferedInputStream(fileInputStream);

            File f = new File(filePath);
            CreateFileRequestObject createFileReqObj = new CreateFileRequestObject("create_file",
                                                       downloadFileReqObj.session_key, downloadFileReqObj.fileName,
                                                       null, f.length(), db.getNameAfterID(downloadFileReqObj.session_key));
            this.responseQueue.add(createFileReqObj);
            
            int nrChunks = (int) (f.length() / CHUNKSIZE);
            int remainBytes = (int) (f.length() - nrChunks * CHUNKSIZE);
            for (int i = 0 ; i < nrChunks ; i++)
            {
                byte[] data = new byte[CHUNKSIZE];
                bis.read(data, i * CHUNKSIZE, CHUNKSIZE);

                DownloadFileResponseObject downloadFileRespObj = new DownloadFileResponseObject("download", "client", 
                                                                                                downloadFileReqObj.fileName,
                                                                                                i * CHUNKSIZE, CHUNKSIZE, data);
                this.responseQueue.add(downloadFileRespObj);
            }

            if (remainBytes > 0)
            {
                byte[] data = new byte[remainBytes];
                bis.read(data, nrChunks * CHUNKSIZE, remainBytes);

                DownloadFileResponseObject downloadFileRespObj = new DownloadFileResponseObject("download", "client",
                                                                                                downloadFileReqObj.fileName,
                                                                                                nrChunks * CHUNKSIZE, CHUNKSIZE, data);
                this.responseQueue.add(downloadFileRespObj);
            }

            bis.close();      
        } catch (IOException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fileInputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void exchangeDatabase(GenericRequest qenericRequest)
    {
        try {
            ExchangeDatabase exchangeDatabase = (ExchangeDatabase)qenericRequest;
            
            Database db = new Database(this.idServer);
            ArrayList<UploadFile> filesList = db.getFiles();
            ArrayList<User> usersList = db.getUsers();
            
            if (exchangeDatabase.action.equals("exchange"))
            {
                ExchangeDatabase exData = new ExchangeDatabase("update", filesList, usersList);
                this.responseQueue.add(exData);
            }
            
            for(UploadFile file : exchangeDatabase.filesList)
            {
                if (filesList.indexOf(file) == -1)
                    db.addFile(file.owner, file.name, file.privat, file.path);
            }
            
            for(User user : exchangeDatabase.usersList)
            {
                if (usersList.indexOf(user) == -1)
                    db.addUser(user.name, user.password);
            }
        } catch (SQLException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
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
