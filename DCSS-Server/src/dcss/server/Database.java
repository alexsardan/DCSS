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
import my.generic.lib.UploadFile;


public class Database {
    
    Connection con;
    PreparedStatement st;
    ResultSet rs;
    
    public Database (int id) throws SQLException
    {
        String url = "jdbc:mysql://localhost:3306/fileserver_" + id;
        this.con = DriverManager.getConnection(url, "root", "student");  
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
}
