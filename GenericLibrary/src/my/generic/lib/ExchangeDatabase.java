
package my.generic.lib;

import java.io.Serializable;
import java.util.ArrayList;

public class ExchangeDatabase extends GenericRequest
{
    public String action;
    public ArrayList<UploadFile> filesList;
    public ArrayList<User> usersList;
    
    public ExchangeDatabase(String action, ArrayList<UploadFile> files,
                            ArrayList<User> users)
    {
        super(null, 0);
        
        this.action = action;
        this.filesList = files;
        this.usersList = users;
    }
}
