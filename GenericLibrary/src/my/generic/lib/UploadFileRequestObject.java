package my.generic.lib;

public class UploadFileRequestObject extends GenericRequest
{
    public String filePath;
    public int offsetChunk;
    public byte[] chunk;
    
    public UploadFileRequestObject(String type, 
                                   int session_key,
                                   String filePath,
                                   int offset,
                                   byte[] data)
    {
        super(type, session_key);
        this.filePath = filePath;
        this.offsetChunk = offset;
        this.chunk = data;
    }
}
