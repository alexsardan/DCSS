package my.generic.lib;

import java.io.Serializable;

public class GenericResponse implements Serializable{
    public String type;
    public String dest;

    public GenericResponse(String type, String dest) {
        this.type = type;
        this.dest = dest;
    }
}
