package util;

import java.util.HashMap;

public class CustomSession {

    private HashMap<String,Object> values=new HashMap<>();

    public CustomSession() {
    }

    public void add(String key,Object value) {
        values.putIfAbsent(key, value);
    }

    public Object get(String key){
        return values.get(key);
    }

    public void update(String key,Object value){
        values.replace(key, value);
    }
    
    public void delete(String key){
        this.values.remove(key);
    }

    public HashMap<String, Object> getValues() {
        return values;
    }

    public void setValues(HashMap<String, Object> values) {
        this.values = values;
    }
}
