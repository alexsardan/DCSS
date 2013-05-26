/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dcss.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import my.generic.lib.*;

/**
 *
 * @author Alex
 */
public abstract class ResponseHandler extends Thread {
    class UploadHandler extends Thread {
        public static final int CHUNKSIZE = 51200;
        DCSSClient cl;
        String filename;

        public UploadHandler(DCSSClient cl, String filename) {
            this.cl = cl;
            this.filename = filename;
        }

        @Override
        public void run() {
            try {
                String tmp = filename.substring(filename.lastIndexOf("/") + 1);
                String fn = tmp.substring(0, tmp.lastIndexOf("_"));
                String fnn = fn.substring(0, fn.lastIndexOf("_"));
                
                int lastSlash = filename.lastIndexOf("/");
                int firstSlash = filename.lastIndexOf("/", lastSlash - 1);
                String accessType = filename.substring(firstSlash + 1, lastSlash);
                File f = new File(filename);
                
                BufferedInputStream buf = new BufferedInputStream(new FileInputStream(fnn));
                byte[] buffer = new byte[CHUNKSIZE];
                int off = 0;
                int bytesRead = 0;
                try {
                    while ((bytesRead = buf.read(buffer)) != -1) {
                        UploadFileRequestObject upf = new UploadFileRequestObject("upload", this.cl.sessionKey, fnn, this.filename, 
                                                                                  accessType, f.length(), this.cl.lastLoginName,
                                                                                  off, buffer,bytesRead);
                        off += bytesRead;
                        this.cl.serverRequest.sendRequest(upf);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ResponseHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ResponseHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }    
    
    DCSSClient client;

    public ResponseHandler(DCSSClient client) {
        this.client = client;
    }
    
    public boolean processResponse(GenericResponse req)
    {
        ReplyMessage rep;
        
        switch(req.type)
        {
            case "create":
                rep = (ReplyMessage)req;
                if (rep.answer.equals("ACK"))
                    System.out.println("Utilizatorul a fost creeat cu succes");
                else
                    System.out.println("Utilizatorul nu a putut fi creeat cu succes");                   
                break;
                
            case "login":
                rep = (ReplyMessage)req;
                int logedId = Integer.parseInt(rep.answer);
                if (logedId == 0) {    
                    System.out.println("Utilizatorul nu a putut fi autentificat");
                    this.client.lastLoginName = null;
                }
                else
                {
                    System.out.println("Utilizatorul a fost autentificat cu succes");
                    client.logedIn = true;
                    client.sessionKey = logedId;
                }
                break;
                
            case "list":
                if (client.logedIn) {
                    ListFilesResponseObject lfro = (ListFilesResponseObject)req;
                    System.out.println(showFiles(lfro.files));
                } else {
                    System.out.println("Trebuie sa te autentifici!");
                }
                break;
                
            case "download":
                DownloadFileResponseObject fragment = (DownloadFileResponseObject)req;
                try {
                    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(fragment.fileName));
                    out.write(fragment.chunk, fragment.offsetChunk, fragment.chunk.length);
                    out.close();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(DCSSClient.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(DCSSClient.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
                
            case "upload":
                rep = (ReplyMessage) req;
                System.out.println("Fisierul " + rep.answer + " a fost incarcat cu succes");
                
                break;
                
            case "upload_first":
                rep = (ReplyMessage) req;
                UploadHandler up = new UploadHandler(client, rep.answer);
                up.start();
                break;
        }
        
        return true;
    }
    
    public String showFiles(ArrayList<UploadFile> files)
    {
        String file = "Id        Nume fisier           Owner            Privat              Data adaugat \n";
        for(UploadFile f:files)
        {
            file += f.id + f.name + f.ownerName +  ((f.privat == 0)?"NU":"DA") + f.dateAdded + "\n";
        }
        
        return file;
    }
}
