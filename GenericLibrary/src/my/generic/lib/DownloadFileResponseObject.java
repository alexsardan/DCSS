
package my.generic.lib;

public class DownloadFileResponseObject extends GenericResponse
{
    public String fileName;
    public int offsetChunk;
    public long lengthChunk;
    public byte[] chunk;
    
    public DownloadFileResponseObject(String type, 
                                   String dest,
                                   String fileName,
                                   int offset,
                                   long length,
                                   byte[] data)
    {
        super(type, dest);
        this.fileName = fileName;
        this.offsetChunk = offset;
        this.lengthChunk = length;
        this.chunk = data;
    }
}
