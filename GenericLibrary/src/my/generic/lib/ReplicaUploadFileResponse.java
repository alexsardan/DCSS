
package my.generic.lib;

public class ReplicaUploadFileResponse extends GenericResponse
{
    public String fileName;
    public int offsetChunk;
    public long lengthChunk;
    public byte[] chunk;
    public String owner;
    
    public ReplicaUploadFileResponse(String type, 
                                   String dest,
                                   String fileName,
                                   int offset,
                                   long length,
                                   byte[] data,
                                   String owner)
    {
        super(type, dest);
        this.fileName = fileName;
        this.offsetChunk = offset;
        this.lengthChunk = length;
        this.chunk = new byte[(int)length];
        System.arraycopy(data, 0, chunk, 0, (int)length);
        this.owner = owner;
    }
}
