package my.generic.lib;

public class ReplyMessage extends GenericResponse
{
    public String answer;
    
    public ReplyMessage(String type, String dest, String answer)
    {
        super(type, dest);
        this.answer = answer;
    }
}
