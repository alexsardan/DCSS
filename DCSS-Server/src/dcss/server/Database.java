package dcss.server;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Connection;
import java.util.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import my.generic.lib.*;


public class Database {
    
    Connection con;
    PreparedStatement st;
    ResultSet rs;
    
    public Database (int id) throws SQLException
    {
        String url = "jdbc:mysql://localhost/fileserver_" + id;
        this.con = DriverManager.getConnection(url, "root", "root");  
    }    
    
    public boolean addUser(String name, String pass)
    {
        if (this.isUser(name))
            return false;
        
        try {            
            
            MessageDigest m=MessageDigest.getInstance("MD5");
            m.update(pass.getBytes(),0,pass.length());           
            String md5Pass = new BigInteger(1,m.digest()).toString(16);            
            
            this.st = this.con.prepareStatement("INSERT INTO Users (Name, Password) VALUES (?, ?);");
            this.st.setString(1, name);
            this.st.setString(2, md5Pass);
            this.st.executeUpdate();
            
        } catch (Exception ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
        return true;
    }
    
    public boolean isUser(String name)
    {
        try {
            this.st = this.con.prepareStatement("SELECT * FROM Users WHERE Name=?;");
            this.st.setString(1, name);
            this.rs = this.st.executeQuery();
            
            if (rs.next())
                return true;       
            
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);            
        }
        
        return false;        
    }
    
    public int logIn(String name, String pass)
    {
        try {
            
            MessageDigest m=MessageDigest.getInstance("MD5");
            m.update(pass.getBytes(),0,pass.length());           
            String md5Pass = new BigInteger(1,m.digest()).toString(16);
            
            this.st = this.con.prepareStatement("SELECT * FROM Users WHERE Name=? AND Password=?;");
            this.st.setString(1, name);
            this.st.setString(2, md5Pass);
            this.rs = this.st.executeQuery();
            
            if (this.rs.next())
                return rs.getInt("id");
            
        } catch (Exception ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);            
        }
        
        return 0;
    }
    
    public boolean addFile(int clientId, String name, int privat, String path)
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();  
        
        try {            
            this.st = this.con.prepareStatement("INSERT INTO Files (Name, Owner, Private, Path, DateAdded) VALUES (?, ?, ?, ?, ?);");
            this.st.setString(1, name);
            this.st.setInt(2, clientId);
            this.st.setInt(3, privat);
            this.st.setString(4, path);
            this.st.setString(5, dateFormat.format(date));
            this.st.executeUpdate();
            
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;        
    }
    
    public ArrayList<UploadFile> browseFiles(int clientId)
    {
        ArrayList<UploadFile> files = new ArrayList();
        
        try {
            this.st = this.con.prepareStatement("SELECT * from Files INNER JOIN Users ON Files.Owner = Users.Id WHERE Owner = ? OR Private = 0;");
            this.st.setInt(1, clientId);
            this.rs = this.st.executeQuery();
            
            while(this.rs.next())
            {
                files.add(
                            new UploadFile
                            (
                                this.rs.getInt("id"),
                                this.rs.getString("Name"),
                                this.rs.getInt("Owner"),
                                this.rs.getInt("Private"),
                                this.rs.getString("Path"),
                                this.rs.getDate("DateAdded"),
                                this.rs.getString("Users.Name")    
                            )
                        );
            }
            return files;
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    public String getPath(int id, String name, int clientId)
    {
        try {
            this.st = this.con.prepareStatement("SELECT * from Files WHERE Id = ? AND Name = ?;");
            this.st.setInt(1, id);
            this.st.setString(2, name);
            this.rs = this.st.executeQuery();
            
            if (this.rs.next())
            {
                if (this.rs.getInt("Private") == 0 || this.rs.getInt("Owner") == clientId)
                    return this.rs.getString("Path");
            }            
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
    
    public boolean insertUploadEntry(String path, long totalSize)
    {
        try 
        {
            this.st = this.con.prepareStatement("INSERT INTO active_uploads (FilePath, TotalSize) VALUES (?, ?);");
            this.st.setString(1, path);
            this.st.setLong(2, totalSize);
            this.st.executeUpdate();
            
        } catch (Exception ex)
        {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
    
    public boolean isUploadFinished (String path)
    {        
        try {
            this.st = this.con.prepareStatement("SELECT * FROM active_Uploads WHERE FilePath=?;");            
            this.st.setString(1, path);
            this.rs = this.st.executeQuery();
            
            if (rs.next())
            {
                if(rs.getFloat("LocalSize") == rs.getFloat("TotalSize"))
                    return true;
            }
            
            return false;
            
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }        
    }
    
    public boolean updateUploadEntry(String path, float chunkSize)
    {
        try {
            this.st = this.con.prepareStatement("UPDATE active_Uploads SET LocalSize = LocalSize + ? WHERE FilePath = ?;");
            this.st.setFloat(1, chunkSize);
            this.st.setString(2, path);
            this.st.executeUpdate();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }        
        return false;
    }
    
    public boolean deleteUploadEntry(String path)
    {
        try 
        {
            this.st = this.con.prepareStatement("DELETE FROM active_Uploads WHERE FilePath = ?;");
            this.st.setString(1, path);
            this.st.executeUpdate();
            
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }
    
    public ArrayList<UploadFile> getFiles()
    {
        ArrayList<UploadFile> files = new ArrayList();
        
        try {
            this.st = this.con.prepareStatement("SELECT * from Files");           
            this.rs = this.st.executeQuery();
            
            while(this.rs.next())
            {
                files.add(
                            new UploadFile
                            (
                                this.rs.getInt("id"),
                                this.rs.getString("Name"),
                                this.rs.getInt("Owner"),
                                this.rs.getInt("Private"),
                                this.rs.getString("Path"),
                                this.rs.getDate("DateAdded"),
                                this.rs.getString("Users.Name")    
                            )
                        );
            }
            return files;
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    public ArrayList<User> getUsers()
    {
        ArrayList<User> users = new ArrayList();
        
        try {
            this.st = this.con.prepareStatement("SELECT * FROM Users;");
            
            this.rs = this.st.executeQuery();
            
            while(this.rs.next())
            {
                users.add(
                            new User
                            (
                                this.rs.getString("Name"),
                                this.rs.getString("Password")
                            )
                        );
            }
            return users;
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    
    public int getId (String name)
    {
        try {
            this.st = this.con.prepareStatement("SELECT Id FROM Users WHERE Name=?;");
            this.st.setString(1, name);
            this.rs = this.st.executeQuery();
            
            if (rs.next())
            {
                return rs.getInt("Id");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    public String getNameAfterID(int ID)
    {
        try {
            this.st = this.con.prepareStatement("SELECT Name FROM Users WHERE Id=?;");
            this.st.setInt(1, ID);
            this.rs = this.st.executeQuery();
            
            if (rs.next())
            {
                return rs.getString("Name");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
        public int getFileID(String fileName, int idOwner)
    {
        try {
            this.st = this.con.prepareStatement("SELECT Id FROM files WHERE Name=? AND Owner=?;");
            this.st.setString(1, fileName);
            this.st.setInt(2, idOwner);
            this.rs = this.st.executeQuery();
            
            if (rs.next())
            {
                return rs.getInt("Id");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }
}
