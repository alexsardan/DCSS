package my.generic.lib;

public class ReplicaUserResponse extends GenericResponse
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
