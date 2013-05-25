
package my.generic.lib;

public class ReplicaFileResponseObject extends GenericResponse
{
    public String fileName;
    public String accessType;
    public long fileLength;
    
    public ReplicaFileResponseObject(String type, 
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
