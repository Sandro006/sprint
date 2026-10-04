package controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import scan.ControllerScan;
import scan.MethodScan;
import scan.SessionScan;
import util.CustomSession;
import util.JsonUtil;
import util.Mapping;
import util.ModelAndView;
import annotation.RestApi;
import java.util.List;
import java.util.Map;

import java.util.ArrayList;
import java.util.HashMap;

@MultipartConfig
@WebServlet(urlPatterns = "/")
public class FrontController extends HttpServlet {
    private List<String> controllerList = new ArrayList<>();
    private Map<String, Mapping> urlMappings = new HashMap<>();
    private Map<String, String> handleError = new HashMap<>();
    private boolean initialized = false;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        synchronized (this) {
            if (!initialized) {
                try {
                    ControllerScan controllerScan = new ControllerScan(urlMappings, controllerList, getServletContext(),
                            request);
                    controllerScan.initControllers();
                    initialized = true;
                } catch (Exception e) {
                    try (PrintWriter out = response.getWriter()) {
                        out.println("Initialization error: " + e.getMessage());
                    }
                    return;
                }
            }
        }

        try (PrintWriter out = response.getWriter()) {
            StringBuilder sb = new StringBuilder();

            String requestURL = request.getRequestURL().toString();
            String requestMethod = request.getMethod();
            String baseUrl = getBaseUrl(request);

            sb.append("<!DOCTYPE html>");
            sb.append("<html lang='fr'>");
            sb.append("<meta charset=\"UTF-8\">");
            sb.append("<head>");
            sb.append("<title>FrontController</title>");
            sb.append("<style>.error-message {\n" + //
                    "    color: red;\n" + //
                    "    font-weight: bold;\n" + //
                    "}\n" + // 22,k
                    "\n" + //
                    ".stack-trace {\n" + //
                    "    \n" + //
                    "    margin-top: 10px;\n" + //
                    "    padding: 10px;\n" + //
                    "    \n" + //
                    "    border: 1px solid #ccc;\n" + //
                    "}\n" + //
                    "</style>");
            sb.append("</head>");
            sb.append("<body>");
            sb.append("<p><b>URL:</b> " + requestURL + "</p>");
            sb.append("<p><b>Methode HTTP:</b> " + requestMethod + "</p>");

            sb.append("<p><b2>Controleurs Disponibles:</b2></p>");
            sb.append("<ul>");
            for (String controller : controllerList) {
                sb.append("<li>" + controller + "</li>");
            }
            sb.append("</ul>");
            handleError.clear();
            String mappedURL = requestURL.replace(baseUrl, "");
            if (!mappedURL.startsWith("/")) {
                mappedURL = "/" + mappedURL;
            }
            if (!urlMappings.containsKey(mappedURL)) {
                // sb.append("L'URL demandee est introuvable.");
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "L'URL demandee est introuvable.");
                return;
            }

            if (urlMappings.containsKey(mappedURL)) {
                try {

                    Mapping map = urlMappings.get(mappedURL);
                    if (!map.getVerbAction().testVerbAction(request.getMethod(), mappedURL)) {
                        throw new Exception("Vous essayez d'utiliser une requette avec la methode "
                                + request.getMethod() + " au lieu de " + map.getVerbAction().getVerb());
                    }
                    sb.append("<b>Classe du Controleur:</b> " + map.getControlleur() + "<br>");
                    sb.append("<b>Methode Associee:</b> " + map.getMethode() + "<br>");

                    Class<?> clazz = Class.forName(map.getControlleur());
                    Method[] methods = clazz.getDeclaredMethods();
                    Method method = null;

                    for (Method method1 : methods) {
                        if (method1.getName().equals(map.getMethode())) {
                            method = method1;
                            break;
                        }
                    }

                    if (method == null) {
                        throw new Exception("Methode non trouvee : " + map.getMethode());
                    }

                    try {
                        Object controllerInstance = clazz.getDeclaredConstructor().newInstance();
                        Field[] fields = clazz.getDeclaredFields();
                        for (Field field : fields) {
                            if (field.getType() == CustomSession.class) {
                                field.setAccessible(true);
                                field.set(controllerInstance, new CustomSession());
                            }
                        }

                        MethodScan methodScan = new MethodScan(handleError, method, request);

                        methodScan.authentification();
                        Object[] methodParam = methodScan.getMethodParameters();
                        Object result = method.invoke(controllerInstance, methodParam);

                        if (!handleError.isEmpty()) {
                            String referer = request.getHeader("Referer");
                            if (referer != null) {
                                request.setAttribute("error", copyMap(handleError));
                                // handleError.clear();
                                System.out.println("ATO EHHH, misy erreur");
                                String relativePath = referer.replaceFirst(baseUrl, "");

                                if (relativePath.isEmpty() || relativePath.equals("/")) {

                                    response.sendRedirect(request.getContextPath() + "/index.jsp");
                                } else {

                                    response.sendRedirect(request.getContextPath() + "/" + relativePath);
                                }
                                return;
                            }

                        }
                        handleError = new HashMap<>();

                        for (Object param : methodParam) {
                            if (param instanceof CustomSession) {
                                SessionScan.synchronizeSession(request.getSession(), (CustomSession) param);
                            }
                        }
                        response.setContentType("text/html;charset=UTF-8");
                        if (result instanceof String) {
                            sb.append("<br>Resultat de l'invocation de methode : " + result);
                        } else if (result instanceof ModelAndView) {

                            ModelAndView modelAndView = (ModelAndView) result;
                            if (method.isAnnotationPresent(RestApi.class)) {

                                response.setContentType("application/json;charset=UTF-8");
                                sb.setLength(0);

                                out.println(JsonUtil.toJson(modelAndView.getData()));

                                return;
                            }
                            for (String key : modelAndView.getData().keySet()) {
                                request.setAttribute(key, modelAndView.getData().get(key));
                            }
                            request.getRequestDispatcher(modelAndView.getUrl()).forward(request, response);
                            return;
                        } else {
                            if (method.isAnnotationPresent(RestApi.class)) {
                                response.setContentType("application/json;charset=UTF-8");
                                if (result instanceof String) {
                                    sb.append(result);
                                } else {
                                    sb.append(JsonUtil.toJson(result));
                                }
                            }
                        }
                    } catch (InvocationTargetException e) {
                        Throwable cause = e.getCause();
                        if (cause instanceof Exception) {
                            throw (Exception) cause;
                        } else {
                            throw new Exception("Erreur lors de l'invocation : " + cause.getMessage());
                        }
                    }

                } catch (Exception e) {

                    sb.append("<div class='error-message'>Erreur lors de l'invocation : " + e.getMessage() + "</div>");
                    sb.append("<div class='stack-trace'>Trace de la pile :");
                    for (StackTraceElement element : e.getStackTrace()) {
                        sb.append("<div>" + element.toString() + "</div>");
                    }
                    sb.append("</div>");
                    e.printStackTrace();
                }
            } else {
                sb.append("<p class='error-message' >Aucune methode associee à cet URL.</p>");
            }

            sb.append("</body>");
            sb.append("</html>");
            out.println(sb.toString());
        }
    }

    private String getBaseUrl(HttpServletRequest request) {
        String requestURL = request.getRequestURL().toString();
        String requestURI = request.getRequestURI();
        return requestURL.substring(0, requestURL.length() - requestURI.length()) + request.getContextPath() + "/";
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception e) {
            handleException(e, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception e) {
            handleException(e, response);
        }
    }

    private void handleException(Exception e, HttpServletResponse response) throws IOException {
        e.printStackTrace();
        try (PrintWriter out = response.getWriter()) {
            out.println("Error: " + e.getMessage());
            for (StackTraceElement element : e.getStackTrace()) {
                out.println(element.toString());
            }
        }
    }

    private Map<String, String> copyMap(Map<String, String> source) {
        return new HashMap<>(source);
    }
}
