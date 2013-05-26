package my.generic.lib;

public class UploadFileRequestObject extends GenericRequest
{
    public String fileName;
    public String filePath;
    public String accessType;
    public long fileLength;
    public String owner;
    public int offsetChunk;
    public byte[] chunk;
    public int chunkLength;
    
    public UploadFileRequestObject(String type, 
                                   int session_key,
                                   String fileName,
                                   String filePath,
                                   String access,
                                   long length,
                                   String owner,
                                   int offset,
                                   byte[] data,
                                   int chunkLength)
    {
        super(type, session_key);
        this.fileName = fileName;
        this.filePath = filePath;
        this.accessType = access;
        this.fileLength = length;
        this.owner = owner;
        this.offsetChunk = offset;
        this.chunk = new byte[chunkLength];
        System.arraycopy(data, 0, chunk, 0, chunkLength);
        this.chunkLength = chunkLength;
    }
}
