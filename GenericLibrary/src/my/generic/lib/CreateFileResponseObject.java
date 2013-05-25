
package my.generic.lib;

public class CreateFileResponseObject extends GenericResponse
{
    public String fileName;
    public String accessType;
    public long fileLength;
    
    public CreateFileResponseObject(String type, 
                                   String dest,
                                   String fileName,
                                   String access,
                                   long length)
    {
        super(type, dest);
        this.fileName = fileName;
        this.accessType = access;
        this.fileLength = length;
    }
}
