
package my.generic.lib;

public class DownloadFileRequestObject extends GenericRequest
{
    public int idFile;
    public String fileName;
    
    public DownloadFileRequestObject(String type, int session_key, int id, String fileName)
    {
        super(type, session_key);
        this.idFile = id;
        this.fileName = fileName;
    }
}
