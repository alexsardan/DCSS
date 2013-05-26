package my.generic.lib;

public class UploadFileRequestObject extends GenericRequest
{
    public String fileName;
    public int offsetChunk;
    public byte[] chunk;
    public String owner;
    
    public UploadFileRequestObject(String type, 
                                   int session_key,
                                   String fileName,
                                   int offset,
                                   byte[] data,
                                   String owner)
    {
        super(type, session_key);
        this.fileName = fileName;
        this.offsetChunk = offset;
        this.chunk = data;
        this.owner = owner;
    }
}
