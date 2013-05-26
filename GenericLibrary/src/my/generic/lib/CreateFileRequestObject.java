package my.generic.lib;

public class CreateFileRequestObject extends GenericRequest
{
    public String fileName;
    public String accessType;
    public long fileLength;
    public String owner;
    
    public CreateFileRequestObject(String type, 
                                   int session_key,
                                   String fileName,
                                   String access,
                                   long length,
                                   String owner)
    {
        super(type, session_key);
        this.fileName = fileName;
        this.accessType = access;
        this.fileLength = length;
        this.owner = owner;
    }
}
