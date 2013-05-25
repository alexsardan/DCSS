/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dcss.client;

import java.io.BufferedOutputStream;
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
                if (logedId == 0)                
                    System.out.println("Utilizatorul nu a putut fi autentificat");
                else
                {
                    System.out.println("Utilizatorul a fost autentificat cu succes");
                    /*TODO asociate with id*/
                }
                break;
                
            case "list":
                ListFilesResponseObject lfro = (ListFilesResponseObject)req;
                System.out.println(showFiles(lfro.files));                
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
