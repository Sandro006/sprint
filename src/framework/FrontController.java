package framework;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import framework.annotations.Controller;
import framework.annotations.GetMapping;

public class FrontController extends HttpServlet {
    private HashMap<String, String> routes = new HashMap<>(); // Stocke "url" -> "Classe:Methode"
    private List<Class<?>> controllers = new ArrayList<>();

    @Override
    public void init() {
        try {
            // 1. On récupère le dossier "controller" dans les ressources compilées
            URL resource = Thread.currentThread().getContextClassLoader().getResource("controller");
            if (resource == null) return;

            File folder = new File(resource.toURI());

            // 2. On parcourt tous les fichiers du dossier
            for (File file : folder.listFiles()) {
                if (!file.getName().endsWith(".class")) continue;

                // On enlève le ".class" pour avoir juste le nom de la classe
                String className = file.getName().replace(".class", "");
                Class<?> clazz = Class.forName("controller." + className);

                // 3. Si la classe a l'annotation @Controller, on la garde
                if (!clazz.isAnnotationPresent(Controller.class)) continue;

                controllers.add(clazz);

                // 4. Construire les routes à partir de @GetMapping (méthodes)
                for (Method m : clazz.getDeclaredMethods()) {
                    if (m.isAnnotationPresent(GetMapping.class)) {
                        GetMapping gm = m.getAnnotation(GetMapping.class);
                        String url = gm.value();
                        routes.put(url, clazz.getName() + ":" + m.getName());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html;charset=UTF-8");
        var out = res.getWriter();

        // 1. Extraction de l'URL demandée (ex: /mon-app/home -> /home)
        String url = req.getRequestURI().substring(req.getContextPath().length());

        // 2. Affichage HTML des contrôleurs et de leurs méthodes annotées @GetMapping
        out.println("<h2>Contrôleurs trouvés :</h2>");
        for (Class<?> c : controllers) {
            out.println("<h3>" + c.getSimpleName() + "</h3><ul>");

            boolean any = false;
            for (Method m : c.getDeclaredMethods()) {
                if (m.isAnnotationPresent(GetMapping.class)) {
                    any = true;
                    String mapping = m.getAnnotation(GetMapping.class).value();
                    out.println("<li>@GetMapping(\"" + mapping + "\") -> " + m.getName() + "()</li>");
                }
            }

            if (!any) {
                out.println("<li>Aucune méthode annotée avec @GetMapping</li>");
            }
            out.println("</ul>");
        }

        // 3. Routage et exécution (ou affichage de toutes les routes si l'URL n'existe pas)
        out.println("<p><b>URL demandée :</b> " + url + "</p>");

        String mapping = routes.get(url);
        if (mapping != null) {
            try {
                String[] infos = mapping.split(":"); // "controller.MonController" et "maMethode"
                Class<?> clazz = Class.forName(infos[0]);
                Object ctrlInstance = clazz.getDeclaredConstructor().newInstance();
                Method method = clazz.getMethod(infos[1]);

                Object result = method.invoke(ctrlInstance);
                out.println("<h3>Résultat :</h3>" + result);
            } catch (Exception e) {
                out.println("<p style='color:red'>Erreur : " + e.getMessage() + "</p>");
            }
        } else {
            out.println("<p style='color:orange'><b>Aucune route trouvée</b> pour " + url + "</p>");
            out.println("<h3>Routes existantes :</h3><ul>");

            List<String> urls = new ArrayList<>(routes.keySet());
            Collections.sort(urls);

            for (String u : urls) {
                String mp = routes.get(u);
                String[] infos = mp.split(":");
                String controllerClass = infos[0];
                String methodName = infos[1];
                out.println("<li>" + u + " -> " + controllerClass + "#" + methodName + "</li>");
            }

            out.println("</ul>");
        }
    }
}

