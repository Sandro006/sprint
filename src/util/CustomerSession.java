package util;

import java.util.HashMap;

public class CustomerSession {
    private HashMap<String, Object> values = new HashMap<>();

    public HashMap<String, Object> getValues() {
        return values;
    }

    public void setValues(HashMap<String, Object> values) {
        this.values = values;
    }

    public void add(String cle, Object valeur){
        values.putIfAbsent(cle, valeur);
    }

    public Object get(String cle) {
        return values.get(cle);
    }

    public void update(String cle, Object valeur){
        values.replace(cle, valeur);
    }

    public void delete(String cle) {
        values.remove(cle);
    }
}
