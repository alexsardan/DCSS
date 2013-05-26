
package my.generic.lib;

public class ReplicaUploadFileResponse extends GenericResponse
{
    public String fileName;
    public String filePath;
    public String accessType;
    public long fileLength;
    public String owner;
    public int offsetChunk;
    public byte[] chunk;
    public int chunkLength;
    
    public ReplicaUploadFileResponse(String type, 
                                   String dest,
                                   String fileName,
                                   String filePath,
                                   String access,
                                   long length,
                                   String owner,
                                   int offset,
                                   byte[] data,
                                   int chunkLength)
    {
        super(type, dest);
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
