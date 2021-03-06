
package my.generic.lib;

import java.io.Serializable;
import java.util.ArrayList;

public class ExchangeDatabase extends GenericRequest
{
    public ArrayList<UploadFile> filesList;
    public ArrayList<User> usersList;
    
    public ExchangeDatabase(String action, ArrayList<UploadFile> files,
                            ArrayList<User> users)
    {
        super(action, 0);

        this.filesList = new ArrayList<UploadFile>(files);
        this.usersList = new ArrayList<User>(users);
    }
}
