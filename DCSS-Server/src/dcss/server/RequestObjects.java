/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dcss.server;

/**
 *
 * @author Alex
 */
class LoginCreateRequestObject
{
    String name;
    String password;
    
    public LoginCreateRequestObject(String name, String password)
    {
        this.name = name;
        this.password = password;
    }
}
