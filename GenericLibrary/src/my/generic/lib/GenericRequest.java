package my.generic.lib;

import java.io.Serializable;

public class GenericRequest implements Serializable {
    public String type;
    public int session_key;

    public GenericRequest(String type, int session_key) {
        this.type = type;
        this.session_key = session_key;
    }
}