package scan;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpSession;
import util.CustomSession;

public class SessionScan {
    public static void synchronizeSession(HttpSession httpSession, CustomSession customSession) {
        Map<String, Object> customSessionValues = customSession.getValues();
        Enumeration<String> httpSessionAttributeNames = httpSession.getAttributeNames();
        List<String> attributesToRemove = new ArrayList<>();
 
        while (httpSessionAttributeNames.hasMoreElements()) {
            String attributeName = httpSessionAttributeNames.nextElement();
            if (!customSessionValues.containsKey(attributeName)) {
                attributesToRemove.add(attributeName);
            }
        }

        
        for (String attributeName : attributesToRemove) {
            httpSession.removeAttribute(attributeName);
        } 
        
        for (Map.Entry<String, Object> entry : customSessionValues.entrySet()) {
            httpSession.setAttribute(entry.getKey(), entry.getValue());
        }
    }
}
