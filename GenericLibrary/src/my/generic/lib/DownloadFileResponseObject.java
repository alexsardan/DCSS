
package my.generic.lib;

public class DownloadFileResponseObject extends GenericResponse
{
    public String fileName;
    public int offsetChunk;
    public long lengthChunk;
    public byte[] chunk;
    public long fileLength;
    
    public DownloadFileResponseObject(String type, 
                                   String dest,
                                   String fileName,
                                   int offset,
                                   long length,
                                   byte[] data,
                                   long lengthFile)
    {
        super(type, dest);
        this.fileName = fileName;
        this.offsetChunk = offset;
        this.lengthChunk = length;
        this.chunk = new byte[(int)length];
        System.arraycopy(data, 0, chunk, 0, (int)length);
        this.fileLength = lengthFile;
    }
}
