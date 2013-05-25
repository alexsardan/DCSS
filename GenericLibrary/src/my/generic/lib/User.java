package my.generic.lib;

public class User 
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
            return true;
        
        return false;
    }
}
