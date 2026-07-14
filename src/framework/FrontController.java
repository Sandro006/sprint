package framework;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import framework.annotations.GetMapping;
import framework.annotations.PostMapping;

public class FrontController extends HttpServlet {
    private final HashMap<String, String> routes = new HashMap<>(); // Stocke "METHOD:/url" -> "Classe:Methode"
    private final List<Class<?>> controllers = new ArrayList<>();
    private final HashMap<Class<?>, Object> controllerInstances = new HashMap<>();

    private String viewPrefix = "/views/";
    private String viewSuffix = ".jsp";



    private static final boolean CONSOLE_DEBUG = true;

    private static void fwLog(String msg) {
        if (!CONSOLE_DEBUG) return;
        System.out.println("[frameworkDeDro] " + msg);
    }


    
    @Override
    public void init() {
        try {
            File folder = FrontControllerScanner.resolveControllerFolder();
            if (folder == null) return;

            FrontControllerScanner.scanControllersAndBuildRoutes(folder, routes, controllers);
            instantiateControllersOnce();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void instantiateControllersOnce() throws Exception {
        for (Class<?> controllerClass : controllers) {
            Object instance = controllerClass.getDeclaredConstructor().newInstance();
            controllerInstances.put(controllerClass, instance);
            fwLog("controller instance est pret XO: " + controllerClass.getName());
        }
    }


    private void handle(HttpServletRequest req, HttpServletResponse res, String httpMethod) throws ServletException, IOException {
        res.setContentType("text/html;charset=UTF-8");
        var out = res.getWriter();

        String url = req.getRequestURI().substring(req.getContextPath().length());
        fwLog("do" + httpMethod + " url=" + url);

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
                if (m.isAnnotationPresent(PostMapping.class)) {
                    any = true;
                    String mapping = m.getAnnotation(PostMapping.class).value();
                    out.println("<li>@PostMapping(\"" + mapping + "\") -> " + m.getName() + "()</li>");
                }
            }

            if (!any) {
                out.println("<li>Aucune méthode annotée avec @GetMapping/@PostMapping</li>");
            }
            out.println("</ul>");
        }

        out.println("<p><b>URL demandée :</b> " + url + " (" + httpMethod + ")</p>");

        String mapping = routes.get(httpMethod + ":" + url);
        if (mapping != null) {
            try {
                String[] infos = mapping.split(":");
                Class<?> clazz = Class.forName(infos[0]);
                Object ctrlInstance = controllerInstances.get(clazz);
                if (ctrlInstance == null) {
                    ctrlInstance = clazz.getDeclaredConstructor().newInstance();
                    controllerInstances.put(clazz, ctrlInstance);
                    fwLog("controller instance created lazily: " + clazz.getName());
                }
                Method method = clazz.getMethod(infos[1]);

                Object result = method.invoke(ctrlInstance);

                if (result instanceof framework.ModelAndView) {
                    framework.ModelAndView mv = (framework.ModelAndView) result;

                    // Prefix / suffix depuis context.xml (ou valeurs par défaut)
                    String prefix = viewPrefix;
                    String suffix = viewSuffix;
                    try {
                        javax.servlet.ServletContext sc = getServletContext();
                        String p = sc.getInitParameter("viewPrefix");
                        String s = sc.getInitParameter("viewSuffix");
                        if (p != null) prefix = p;
                        if (s != null) suffix = s;
                    } catch (Exception ignore) {
                        // garder valeurs par défaut
                    }

                    String viewPath = prefix + mv.getViewName() + suffix;
                    for (java.util.Map.Entry<String, Object> entry : mv.getModel().entrySet()) {
                        req.setAttribute(entry.getKey(), entry.getValue());
                    }
                    req.getRequestDispatcher(viewPath).forward(req, res);
                    return;
                }

                out.println("<h3>Résultat :</h3>" + result);


            } catch (Exception e) {
                out.println("<p style='color:red'>Erreur : " + e.getMessage() + "</p>");
            }
        } else {
            out.println("<p style='color:orange'><b>Aucune route trouvée</b> pour " + httpMethod + ":" + url + "</p>");

            //============================== Test POST ==================================

            out.println("</ul>");
            out.println("<hr>");
            out.println("<h3>Test POST</h3>");
            out.println("<form method='post' action='" + req.getContextPath() + "/test/get'>");
            out.println("<button type='submit'>Appeler /test/get en POST</button>");
            out.println("</form>");
            out.println("<h3>Routes existantes :</h3><ul>");

            // ===========================================================================

            List<String> keys = new ArrayList<>(routes.keySet());
            
            Collections.sort(keys);

            for (String key : keys) {
                String mp = routes.get(key);
                String[] infos = mp.split(":");
                String controllerClass = infos[0];
                String methodName = infos[1];
                out.println("<li>" + key + " -> " + controllerClass + "#" + methodName + "</li>");
            }

        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        handle(req, res, "GET");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        handle(req, res, "POST");
    }
}


