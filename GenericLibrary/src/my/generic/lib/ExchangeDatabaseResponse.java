package my.generic.lib;

import java.io.Serializable;
import java.util.ArrayList;

public class ExchangeDatabaseResponse extends GenericResponse
{
    public ArrayList<UploadFile> filesList;
    public ArrayList<User> usersList;
    
    public ExchangeDatabaseResponse(String action, ArrayList<UploadFile> files,
                            ArrayList<User> users)
    {
        super(action, null);

        this.filesList = new ArrayList<UploadFile>(files);
        this.usersList = new ArrayList<User>(users);
    }
}
