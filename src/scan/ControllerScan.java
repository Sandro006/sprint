package scan;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import annotation.Controller;
import annotation.Post;
import annotation.Url;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import util.Mapping;
import util.ModelAndView;
import util.VerbAction;

@SuppressWarnings("unused")
public class ControllerScan {

    private final Map<String, Mapping> urlMappings;
    private final List<String> controllerList;
    
    private final ServletContext context;
    private final HttpServletRequest request;
    private final String packageName;

    // Constructeur pour initialiser les attributs
    public ControllerScan(Map<String, Mapping> urlMappings, List<String> controllerList,
                          ServletContext context, HttpServletRequest request) throws Exception {
        this.urlMappings = urlMappings;
        this.controllerList = controllerList;
        this.context = context;
        this.request = request;

        // Récupération du paramètre de package à scanner depuis le ServletContext
        this.packageName = context.getInitParameter("package-to-scan");

        if (this.packageName == null) {
            throw new Exception("Aucune package-to-scan trouvé dans le context.");
        }
    }

    // Méthode principale pour initialiser les contrôleurs
    public void initControllers() throws Exception {
        boolean packageEmpty = true;

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources(packageName.replace('.', '/'));

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            if (resource.getProtocol().equals("file")) {
                File directory = new File(resource.toURI());
                if (directory.exists() && isDirectoryNotEmpty(directory)) {
                    packageEmpty = false;
                    scanControllers(directory, packageName);
                }
            }
        }

        if (packageEmpty) {
            throw new Exception("Le package " + packageName + " est vide.");
        }

        if (controllerList.isEmpty()) {
            throw new Exception("Aucune classe annotée par @Controller trouvée dans : " + packageName);
        }
    }

    // Méthode pour vérifier si un répertoire contient des fichiers
    private boolean isDirectoryNotEmpty(File directory) {
        File[] files = directory.listFiles();
        return files != null && files.length > 0;
    }

    // Méthode pour scanner les contrôleurs dans un répertoire donné
    private void scanControllers(File directory, String packageName) throws Exception {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                scanControllers(file, packageName + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                try {
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(Controller.class)) {
                        controllerList.add(className);
                        Method[] methods = clazz.getDeclaredMethods();
                        for (Method method : methods) {
                            if (method.isAnnotationPresent(Url.class)) {
                                validateAndRegisterMethod(clazz, method);
                            }
                        }
                    }
                } catch (ClassNotFoundException e) {
                    throw new Exception("Class non trouvée : " + className, e);
                }
            }
        }
    }

    // Méthode pour valider et enregistrer la méthode dans les mappings
    private void validateAndRegisterMethod(Class<?> clazz, Method method) throws Exception {
        if (method.getReturnType().equals(String.class) || method.getReturnType().equals(ModelAndView.class)) {
            String urlName = null;
            if (method.isAnnotationPresent(Url.class)) {
                Url urlAnnotation = method.getAnnotation(Url.class);
                urlName = urlAnnotation.url();
            }

            if (urlName != null && urlMappings.containsKey(urlName)) {
                throw new Exception("URL " + urlName + " est déjà définie.");
            } else if (urlName != null) {
                if (method.isAnnotationPresent(Post.class)) {
                    urlMappings.put(urlName, new Mapping(clazz.getName(), method.getName(), new VerbAction("POST", urlName)));
                } else {
                    urlMappings.put(urlName, new Mapping(clazz.getName(), method.getName(), new VerbAction("GET", urlName)));
                }
            }
        } else {
            throw new Exception("Les méthodes doivent retourner soit String soit ModelAndView.");
        }
    }
}
