package my.generic.lib;

import java.io.Serializable;
import java.util.Date;

public class UploadFile implements Serializable
{
    public int id;
    public String name;
    public int owner;
    public String ownerName;
    public int privat;
    public String path;
    public Date dateAdded;
    
    public UploadFile (int id, String name, int owner, int privat, String path, Date dateAdded, String ownerName)
    {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.privat = privat;
        this.path = path;
        this.dateAdded = dateAdded;
        this.ownerName = ownerName;
    }
    
    @Override
    public String toString()
    {
        return id + " " + name +" " + ownerName + " " + privat + " " + path +" "+ dateAdded;
    }
    
    @Override
    public boolean equals(Object other)
    {
        UploadFile cmp = (UploadFile)other;
        if ((this.id == cmp.id) && (this.name.equals(cmp.name)) && (this.owner == cmp.owner) &&
             (this.ownerName.equals(cmp.ownerName)) && (this.privat == cmp.privat) &&
             (this.path.equals(cmp.path)) && (this.dateAdded.equals(cmp.dateAdded)))
            return true;
        
        return false;
    }
}
