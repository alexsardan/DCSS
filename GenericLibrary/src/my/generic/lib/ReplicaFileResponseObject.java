
package my.generic.lib;

public class ReplicaFileResponseObject extends GenericResponse
{
    public String fileName;
    public String accessType;
    public long fileLength;
    public String owner;
    
    public ReplicaFileResponseObject(String type, 
                                   String dest,
                                   String fileName,
                                   String access,
                                   long length,
                                   String owner)
    {
        super(type, dest);
        this.fileName = fileName;
        this.accessType = access;
        this.fileLength = length;
        this.owner = owner;
    }
}
