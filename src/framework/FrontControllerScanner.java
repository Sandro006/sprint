package framework;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import framework.annotations.Controller;
import framework.annotations.GetMapping;

/**
 * Sépare la logique de scan/reflexion du FrontController.
 */
public final class FrontControllerScanner {
    private FrontControllerScanner() {}

    public static File resolveControllerFolder() throws Exception {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("controller");
        if (resource == null) return null;
        return new File(resource.toURI());
    }

    public static void scanControllersAndBuildRoutes(File folder, HashMap<String, String> routes, List<Class<?>> controllers)
            throws Exception {
        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (!file.getName().endsWith(".class")) continue;

            String className = file.getName().replace(".class", "");
            Class<?> clazz = Class.forName("controller." + className);

            if (!clazz.isAnnotationPresent(Controller.class)) continue;

            controllers.add(clazz);
            buildRoutesForController(clazz, routes);
        }
    }

    private static void buildRoutesForController(Class<?> clazz, HashMap<String, String> routes) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (!m.isAnnotationPresent(GetMapping.class)) continue;

            GetMapping gm = m.getAnnotation(GetMapping.class);
            String url = gm.value();
            routes.put(url, clazz.getName() + ":" + m.getName());
        }
    }
}

