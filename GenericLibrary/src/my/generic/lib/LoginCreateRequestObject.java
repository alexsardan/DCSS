package my.generic.lib;

public class LoginCreateRequestObject extends GenericRequest
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
