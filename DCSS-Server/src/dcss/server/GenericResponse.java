/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dcss.server;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author Alex
 */
public class GenericResponse implements Serializable{
    public String type;
    public String dest;

    public GenericResponse(String type, String dest) {
        this.type = type;
        this.dest = dest;
    }
}

class ReplyMessage extends GenericResponse
{
    public String answer;
    
    public ReplyMessage(String type, String dest, String answer)
    {
        super(type, dest);
        this.answer = answer;
    }
}

class ReplicaUserResponse extends GenericResponse
{
    public String userName;
    public String password;
    
    public ReplicaUserResponse(String type, String dest, String name, String passwd)
    {
        super(type, dest);
        this.userName = name;
        this.password = passwd;
    }
}

class ListFilesResponseObject extends GenericResponse
{
    ArrayList<UploadFile> files;
    
    public ListFilesResponseObject(String type, String dest, ArrayList<UploadFile> files)
    {
        super(type, dest);
        this.files = files;
    }
}