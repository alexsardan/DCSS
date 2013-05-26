
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
                        db.insertUploadEntry(createFileReqObj.owner, filePathString, createFileReqObj.fileLength);
                        db.con.close();
                        
                        if (createFileReqObj.type.equals("push_data_first") == false)
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
        } catch (SQLException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void uploadFile(GenericRequest qenericRequest)
    {
        RandomAccessFile raf = null;
        try {
            UploadFileRequestObject uploadFileReqObj = (UploadFileRequestObject)qenericRequest;
            
            CreateFileRequestObject cfro = new CreateFileRequestObject("push_data_first", uploadFileReqObj.session_key, 
                                                                        uploadFileReqObj.fileName, uploadFileReqObj.accessType,
                                                                        uploadFileReqObj.fileLength, uploadFileReqObj.owner);
            createFile(cfro);
            
            Database db = new Database(this.idServer);
            
            String filePath = "";
            
            if (uploadFileReqObj.type.equals("push_data") == true)
                filePath = new Database(this.idServer).getUploadEntry(uploadFileReqObj.owner, uploadFileReqObj.fileName);
            else
                filePath = uploadFileReqObj.filePath;
            
            raf = new RandomAccessFile(filePath, "rw");
            raf.seek(0);
            raf.seek(uploadFileReqObj.offsetChunk);
            raf.write(uploadFileReqObj.chunk);
            raf.close();
            
            db.updateUploadEntry(filePath, uploadFileReqObj.chunkLength);
            if (db.isUploadFinished(filePath) == true)
            {
                db.deleteUploadEntry(filePath);
                
                int indexFileNameBegin = filePath.lastIndexOf("/");
                String fileName = filePath.substring(indexFileNameBegin + 1);
                int indexFilePermissionBegin = filePath.lastIndexOf("/", indexFileNameBegin - 1);
                String filePermission = filePath.substring(indexFilePermissionBegin + 1, indexFileNameBegin);
                int permission = filePermission.equals("private") ? 1 : 0;
                        
                int firstUnderscore = fileName.lastIndexOf("_");
                int secondUnderscore = fileName.lastIndexOf("_", firstUnderscore - 1);
                
                int session_key = 0;
                if (uploadFileReqObj.type.equals("push_data"))
                    session_key = new Database(this.idServer).getId(uploadFileReqObj.owner);
                else 
                    session_key = uploadFileReqObj.session_key;
                
                db.addFile(session_key, fileName.substring(0, secondUnderscore), permission, filePath);
                
                if (uploadFileReqObj.type.equals("push_data") == false)
                {
                    ReplyMessage replyMessage = new ReplyMessage("create_user", "client", "ACK");
                    this.responseQueue.add(replyMessage);

                    File f = new File(filePath);
                    
                    int nrChunks = (int) (f.length() / CHUNKSIZE);
                    int remainBytes = (int) (f.length() - nrChunks * CHUNKSIZE);

                    for (int i = 0 ; i < nrChunks ; i++)
                    {
                        RandomAccessFile rf = new RandomAccessFile(filePath, "r");
                        rf.seek(0);
                        rf.seek(i * CHUNKSIZE);
                        byte[] data = new byte[CHUNKSIZE];
                        rf.read(data);
                        rf.close();

                        ReplicaUploadFileResponse replicaUploadFileResp = new ReplicaUploadFileResponse("push_data", "server", uploadFileReqObj.fileName,
                                                                                                        uploadFileReqObj.filePath, uploadFileReqObj.accessType,
                                                                                                        uploadFileReqObj.fileLength, uploadFileReqObj.owner,
                                                                                                        i * CHUNKSIZE, data, CHUNKSIZE);
                        this.responseQueue.add(replicaUploadFileResp);
                    }

                    if (remainBytes > 0)
                    {
                        RandomAccessFile rf = new RandomAccessFile(filePath, "r");
                        rf.seek(0);
                        rf.seek(nrChunks * CHUNKSIZE);
                        byte[] data = new byte[remainBytes];
                        rf.read(data);
                        rf.close();

                        ReplicaUploadFileResponse replicaUploadFileResp = new ReplicaUploadFileResponse("push_data", "server", uploadFileReqObj.fileName,
                                                                                                        uploadFileReqObj.filePath, uploadFileReqObj.accessType,
                                                                                                        uploadFileReqObj.fileLength, uploadFileReqObj.owner,
                                                                                                        nrChunks * CHUNKSIZE, data, remainBytes);
                        this.responseQueue.add(replicaUploadFileResp);
                    }
                }
            }
            
            db.con.close();
        } catch (SQLException ex) {
            
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

            File f = new File(filePath);
                    
            int nrChunks = (int) (f.length() / CHUNKSIZE);
            int remainBytes = (int) (f.length() - nrChunks * CHUNKSIZE);

            for (int i = 0 ; i < nrChunks ; i++)
            {
                RandomAccessFile rf = new RandomAccessFile(filePath, "r");
                rf.seek(0);
                rf.seek(i * CHUNKSIZE);
                byte[] data = new byte[CHUNKSIZE];
                rf.read(data);
                rf.close();

                DownloadFileResponseObject downloadFileRespObj = new DownloadFileResponseObject("download", "client", 
                                                                                                downloadFileReqObj.fileName,
                                                                                                i * CHUNKSIZE, CHUNKSIZE, data, f.length());
                this.responseQueue.add(downloadFileRespObj);
            }

            if (remainBytes > 0)
            {
                RandomAccessFile rf = new RandomAccessFile(filePath, "r");
                rf.seek(0);
                rf.seek(nrChunks * CHUNKSIZE);
                byte[] data = new byte[remainBytes];
                rf.read(data);
                rf.close();

                DownloadFileResponseObject downloadFileRespObj = new DownloadFileResponseObject("download", "client",
                                                                                                downloadFileReqObj.fileName,
                                                                                                nrChunks * CHUNKSIZE, remainBytes, data, f.length());
                this.responseQueue.add(downloadFileRespObj);
            }

            db.con.close();
        } catch (Exception ex) {
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
            
            if (exchangeDatabase.type.equals("exchange"))
            {
                ExchangeDatabaseResponse exData = new ExchangeDatabaseResponse("update", filesList, usersList);
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
            
            db.con.close();
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
