/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dcss.server;

import java.io.Serializable;

/**
 *
 * @author Alex
 */
public class GenericRequest implements Serializable {
    public String type;
    public int session_key;

    public GenericRequest(String type, int session_key) {
        this.type = type;
        this.session_key = session_key;
    }
}

class LoginCreateRequestObject extends GenericRequest
{
    public String userName;
    public String password;
    
    public LoginCreateRequestObject(String type, int session_key, String name, String passwd)
    {
        super(type, session_key);
        this.userName = name;
        this.password = passwd;
    }
}

class ListFilesRequestObject extends GenericRequest
{
    public ListFilesRequestObject(String type, int session_key)
    {
        super(type, session_key);
    }
}

class CreateFileRequestObject extends GenericRequest
{
    public String fileName;
    public String accessType;
    
    public CreateFileRequestObject(String type, int session_key, String fileName, String access)
    {
        super(type, session_key);
        this.fileName = fileName;
        this.accessType = access;
    }
}