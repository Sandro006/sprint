package framework;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {

    private HashMap<String, String> routes = new HashMap<>();

    // Liste des contrôleurs trouvés au démarrage
    private List<Class<?>> controllers = new ArrayList<>();

    /**
     * Scan d'un package pour trouver les classes annotées @Controller
     */
    private List<Class<?>> scanControllers(String packageName) {
        List<Class<?>> result = new ArrayList<>();

        String basePath = packageName.replace('.', '/');

        try {
            ClassLoader classLoader =
                    Thread.currentThread().getContextClassLoader();

            java.util.Enumeration<java.net.URL> resources =
                    classLoader.getResources(basePath);

            while (resources.hasMoreElements()) {
                java.net.URL url = resources.nextElement();

                if (!"file".equals(url.getProtocol())) {
                    continue;
                }

                File dir = new File(url.toURI());

                if (!dir.exists() || !dir.isDirectory()) {
                    continue;
                }

                scanDir(packageName, dir, result);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private void scanDir(
            String packageName,
            File dir,
            List<Class<?>> acc) {

        File[] files = dir.listFiles();

        if (files == null) {
            return;
        }

        for (File f : files) {

            if (f.isDirectory()) {

                scanDir(
                        packageName + "." + f.getName(),
                        f,
                        acc);

            } else if (f.getName().endsWith(".class")) {

                String className =
                        packageName + "."
                                + f.getName().substring(
                                        0,
                                        f.getName().length() - 6);

                try {

                    Class<?> clazz =
                            Class.forName(className);

                    if (clazz.isAnnotationPresent(
                            Controller.class)) {

                        acc.add(clazz);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Appelé une seule fois au démarrage de l'application
     */
    @Override
    public void init() {

        controllers = scanControllers("controller");

        System.out.println(
                "[FrontController] Contrôleurs trouvés :");

        for (Class<?> c : controllers) {
            System.out.println(
                    " - " + c.getName());
        }
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        try {

            response.getWriter().println(
                    "<html><body>");

            response.getWriter().println(
                    "<h2>Contrôleurs trouvés</h2>");

            if (controllers.isEmpty()) {
                response.getWriter().println(
                        "<p>Aucun contrôleur trouvé.</p>");
            } else {
                for (Class<?> c : controllers) {
                    response.getWriter().println(
                            "<h3>" + c.getSimpleName() + "</h3>");
                    response.getWriter().println("<ul>");

                    Method[] methods = c.getDeclaredMethods();
                    boolean found = false;

                    for (Method m : methods) {
                        // Sprint 1: pas encore d’annotation @Url, donc on liste toutes les méthodes publiques
                        // futures étapes: utilisation d'une annotation @Url sur chaque méthode.
                        if (m.getParameterCount() == 0 && m.getReturnType() == String.class) {
                            found = true;
                            response.getWriter().println(
                                    "<li> @Url -> méthode: " + m.getName() + "()</li>");
                        }
                    }

                    if (!found) {
                        response.getWriter().println(
                                "<li>Aucune méthode candidate (String sans param)</li>");
                    }

                    response.getWriter().println("</ul>");
                }
            }


            String uri = request.getRequestURI();
            String context = request.getContextPath();
            String url = uri.substring(context.length());

            response.getWriter().println(
                    "<p><b>URI :</b> " + uri + "</p>");

            response.getWriter().println(
                    "<p><b>Contexte :</b> "
                            + context + "</p>");

            response.getWriter().println(
                    "<p><b>URL demandée :</b> "
                            + url + "</p>");

            String mapping = routes.get(url);

            if (mapping != null) {

                String[] infos = mapping.split(":");

                String className = infos[0];
                String methodName = infos[1];

                Class<?> clazz =
                        Class.forName(className);

                Object controller =
                        clazz.getDeclaredConstructor()
                                .newInstance();

                Method method =
                        clazz.getMethod(methodName);

                Object result =
                        method.invoke(controller);

                response.getWriter().println(
                        "<h3>Résultat :</h3>");

                response.getWriter().println(
                        result);

            } else {

                response.getWriter().println(
                        "<p>Aucune route trouvée pour "
                                + url + "</p>");
            }

            response.getWriter().println(
                    "</body></html>");

        } catch (Exception e) {

            e.printStackTrace();

            response.getWriter().println(
                    "<p style='color:red'>Erreur : "
                            + e.getMessage()
                            + "</p>");
        }
    }
}