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

    private static final boolean CONSOLE_DEBUG = true;

    private static void log(String msg) {
        if (!CONSOLE_DEBUG) return;
        System.out.println("[framework] " + msg);
    }


    public static File resolveControllerFolder() throws Exception {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("controller");
        if (resource == null) return null;
        return new File(resource.toURI());
    }

    public static void scanControllersAndBuildRoutes(File folder, HashMap<String, String> routes, List<Class<?>> controllers)
            throws Exception {
        log("scanControllersAndBuildRoutes: folder=" + (folder == null ? "null" : folder.getAbsolutePath()));

        File[] files = folder.listFiles();
        if (files == null) return;

        int controllerCountBefore = controllers.size();

        for (File file : files) {
            if (!file.getName().endsWith(".class")) continue;

            String className = file.getName().replace(".class", "");
            Class<?> clazz = Class.forName("controller." + className);

            if (!clazz.isAnnotationPresent(Controller.class)) continue;

            controllers.add(clazz);
            log("controller found: " + clazz.getName());
            buildRoutesForController(clazz, routes);
        }

        int controllerCountAfter = controllers.size();
        log("scan complete: controllers=" + (controllerCountAfter - controllerCountBefore) + ", routes=" + routes.size());
    }


    private static void buildRoutesForController(Class<?> clazz, HashMap<String, String> routes) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (!m.isAnnotationPresent(GetMapping.class)) continue;

            GetMapping gm = m.getAnnotation(GetMapping.class);
            String url = gm.value();
            String mapping = clazz.getName() + ":" + m.getName();
            routes.put(url, mapping);
            log("route mapped: GET " + url + " -> " + mapping);
        }
    }

}

