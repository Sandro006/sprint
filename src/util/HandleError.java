package util;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

public class HandleError {
    private HttpSession session;

    public HandleError(HttpSession session) {
        this.session = session;
    }


    public String getFieldValue(String key) {
        Map<String, String> errors = getErrors();
        return errors != null && errors.containsKey(key) ? errors.get(key) : "";
    }

    public String getErrorMessage(String key) {
        Map<String, String> errors = getErrors();
        return errors != null && errors.containsKey(key) ? errors.get(key) : "";
    }

    public void clearErrors() {
        session.removeAttribute("error");
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getErrors() {
        return (Map<String, String>) session.getAttribute("error");
    }
}