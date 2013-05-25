package my.generic.lib;

public class CreateFileRequestObject extends GenericRequest
{
    public String fileName;
    public String accessType;
    public int fileLength;
    
    public CreateFileRequestObject(String type, 
                                   int session_key,
                                   String fileName,
                                   String access,
                                   int length)
    {
        super(type, session_key);
        this.fileName = fileName;
        this.accessType = access;
        this.fileLength = length;
    }
}
