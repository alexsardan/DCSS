
package my.generic.lib;

public class ReplicaUploadFileResponse extends GenericResponse
{
    public String fileName;
    public int offsetChunk;
    public byte[] chunk;
    
    public ReplicaUploadFileResponse(String type, 
                                   String dest,
                                   String fileName,
                                   int offset,
                                   byte[] data)
    {
        super(type, dest);
        this.fileName = fileName;
        this.offsetChunk = offset;
        this.chunk = data;
    }
}
