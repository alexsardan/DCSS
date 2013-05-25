package my.generic.lib;

import java.util.ArrayList;

public class ListFilesResponseObject extends GenericResponse
{
    ArrayList<UploadFile> files;
    
    public ListFilesResponseObject(String type, String dest, ArrayList<UploadFile> files)
    {
        super(type, dest);
        this.files = files;
    }
}
