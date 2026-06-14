package framework;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {

    private HashMap<String, String> routes = new HashMap<>();

    @Override
    public void init() {

        routes.put(
                "/users",
                "controller.UserController:list");

    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        try {
            // Récupérer l'URL demandée
            String uri = request.getRequestURI();

            // Récupérer le contexte de l'application
            String context = request.getContextPath();

            // Extraire la partie de l'URL après le contexte
            String url = uri.substring(context.length());

            response.getWriter().println(
                    "FrontController appelé");

            // Afficher l'URL demandée
            response.getWriter().println(
                    "URL : " + uri);

            response.getWriter().println(
                    "Contexte : " + context);

            response.getWriter().println(
                    "URL demandée : " + url);

            // Trouver la route correspondante
            String mapping = routes.get(url);

            if (mapping == null) {
                response.getWriter().println("404 - Route introuvable");
                return;
            }

            String[] infos = mapping.split(":");

            String className = infos[0];

            String methodName = infos[1];

            Class<?> clazz = Class.forName(className);

            Object controller = clazz.getDeclaredConstructor()
                    .newInstance();

            Method method = clazz.getMethod(methodName);

            Object result = method.invoke(controller);

            response.getWriter()
                    .println(result);

        } catch (Exception e) {
            e.printStackTrace();

            response.getWriter()
                    .println("Erreur : "
                            + e.getMessage());
        }
    }
}