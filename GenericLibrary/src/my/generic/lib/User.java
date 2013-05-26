package my.generic.lib;

import java.io.Serializable;

public class User implements Serializable
{
    
    public String name;
    public String password;
    
    public User(String name, String password)
    {
        this.name = name;
        this.password = password;
    }
    
    @Override
    public boolean equals(Object other)
    {
        User cmp = (User)other;
        if ((this.name.equals(cmp.name)) && (this.password.equals(cmp.password)))
            return false;
        
        return true;
    }
}
