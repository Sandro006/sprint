package util;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView {
    String url;
    Map<String,Object> data;
    
    public ModelAndView(String url){
        this.url=url;
        this.data=new HashMap<>();
    }

    public void setAttribute(String nom,Object object)
    {
        this.data.put(nom, object);
    }

    
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    

}
