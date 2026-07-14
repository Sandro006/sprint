package framework;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ModelAndView {
    private final String viewName;
    private final Map<String, Object> model;

    public ModelAndView(String viewName) {
        this(viewName, new HashMap<>());
    }

    public ModelAndView(String viewName, Map<String, Object> model) {
        this.viewName = viewName;
        this.model = (model == null) ? new HashMap<>() : new HashMap<>(model);
    }

    public String getViewName() {
        return viewName;
    }

    public Map<String, Object> getModel() {
        return Collections.unmodifiableMap(model);
    }

    public ModelAndView addObject(String key, Object value) {
        model.put(key, value);
        return this;
    }
}

