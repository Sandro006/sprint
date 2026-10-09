package scan;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import annotation.Authentification;
import annotation.DateFormat;
import annotation.FieldAnnotation;
import annotation.Numeric;
import annotation.Param;
import annotation.ParamObject;
import annotation.Range;
import annotation.Required;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import util.CustomPart;
import util.CustomSession;

public class MethodScan {

    private Map<String, String> handleError;
    private Method method;
    private HttpServletRequest request;

    // authentification
    public void authentification() throws Exception {
        if (method.isAnnotationPresent(Authentification.class)) {
            Authentification auth = method.getAnnotation(Authentification.class);
            HttpSession session = request.getSession(false);

            if (session != null && Boolean.TRUE.equals(session.getAttribute("authenticated"))) {
                String rolesInSession = (String) session.getAttribute("role"); 
                if (rolesInSession != null && !rolesInSession.isBlank()) {

                    List<String> userRoles = Arrays.asList(rolesInSession.split(","));


                    if (!auth.name().isBlank()) {
                        String[] authorizedRoles = auth.name().split(",");
                        boolean isAuthorized = Arrays.stream(authorizedRoles)
                                                    .map(String::trim)
                                                    .anyMatch(userRoles::contains);

                        if (!isAuthorized) {
                            throw new Exception("Accès interdit : rôle insuffisant pour accéder à cette méthode.");
                        }
                    }
                    return;  
                }
                throw new Exception("Accès interdit : aucun rôle défini dans la session.");
            } else {
                throw new Exception("Accès interdit : l'utilisateur n'est pas authentifié.");
            }
        }
    }

    // Constructor to initialize the required attributes
    public MethodScan(Map<String, String> handleError, Method method, HttpServletRequest request) {
        this.handleError = handleError;
        this.method = method;
        this.request = request;
    }

    public Object[] getMethodParameters() throws Exception {
        Parameter[] parameters = method.getParameters();
        Object[] paramValues = new Object[parameters.length];
    
        for (int i = 0; i < parameters.length; i++) {
            Param requestParam = parameters[i].getAnnotation(Param.class);
            ParamObject objectParam = parameters[i].getAnnotation(ParamObject.class);

            if (requestParam != null) {
                String paramName = requestParam.name();
                if (parameters[i].getType() == CustomPart.class) {
                    Part part = request.getPart(paramName);
                    paramValues[i] = new CustomPart(part);
                } else {
                    String paramValue = request.getParameter(paramName);
                    paramValues[i] = convertParameterType(paramValue, parameters[i].getType());
                }
            } else if (objectParam != null) {
                paramValues[i] = handleObjectParam(parameters[i].getType(), objectParam.name());
            } else if (parameters[i].getType() == CustomSession.class) {
                paramValues[i] = handleCustomSession();
            } else if (isPojo(parameters[i].getType())) {
                // Auto-bind type Spring @ModelAttribute : sans annotation,
                // mappe ?nom=...&age=... directement sur les fields.
                paramValues[i] = handleObjectParam(parameters[i].getType(), "");
            } else {
                throw new Exception("<b>ETU004168</b>  les parametres doivent etre annoter par @Param ou @ParamObject");
            }
        }
        return paramValues;
    }

    private boolean isPojo(Class<?> type) {
        if (type.isPrimitive() || type.isEnum() || type.isArray()) {
            return false;
        }
        if (type == String.class || type == CustomSession.class || type == CustomPart.class) {
            return false;
        }
        String name = type.getName();
        if (name.startsWith("java.") || name.startsWith("jakarta.")) {
            return false;
        }
        if (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)) {
            return false;
        }
        if (Number.class.isAssignableFrom(type) || type == Boolean.class || type == Character.class) {
            return false;
        }
        try {
            type.getDeclaredConstructor();
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
    
    private Object handleObjectParam(Class<?> paramType, String objectName) throws Exception {
        Object paramObject = paramType.getDeclaredConstructor().newInstance();
        String prefix = objectName == null ? "" : objectName.trim();
        boolean hasPrefix = !prefix.isEmpty();

        for (Field field : paramType.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                    || java.lang.reflect.Modifier.isTransient(field.getModifiers())) {
                continue;
            }

            // Support @Role historique (set session)
            if (field.isAnnotationPresent(annotation.Role.class)) {
                validateRole(paramObject, field);
                continue;
            }

            String fieldName = field.getName();
            String alias = null;
            if (field.isAnnotationPresent(FieldAnnotation.class)) {
                alias = field.getAnnotation(FieldAnnotation.class).name();
            }

            // 1) Priorité au format préfixé : etudiant.nom (compat ancien code)
            // 2) Fallback flat : nom (ce que tu veux pour /bind-simple)
            String value = null;
            if (hasPrefix) {
                value = request.getParameter(prefix + "." + fieldName);
                if (value == null && alias != null && !alias.isBlank()) {
                    value = request.getParameter(prefix + "." + alias);
                }
            }
            if (value == null) {
                value = request.getParameter(fieldName);
            }
            if (value == null && alias != null && !alias.isBlank()) {
                value = request.getParameter(alias);
            }

            String errorObject = hasPrefix ? prefix : "";
            // Valide même si absent (pour @Required)
            validateField(field, errorObject, fieldName, value);

            if (value != null) {
                field.setAccessible(true);
                field.set(paramObject, convertParameterType(value, field.getType()));
            }
        }

        return paramObject;
    }

    private CustomSession handleCustomSession() {
        HttpSession session = request.getSession();
        CustomSession customSession = new CustomSession();
        Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            customSession.add(attributeName, session.getAttribute(attributeName));
        }
        return customSession;
    }

    private void validateField(Field field, String objectName, String fieldName, String value) throws Exception {
        if (field.isAnnotationPresent(Required.class) && (value == null || value.isEmpty())) {
            addError(objectName, fieldName, field.getAnnotation(Required.class).message(), value);
        }

        // Si absent/vide : pas de contrôle Numeric/Date/Range (Required a déjà parlé)
        if (value == null || value.isEmpty()) {
            return;
        }

        if (field.isAnnotationPresent(Numeric.class)) {
            try {
                Double.parseDouble(value);
            } catch (NumberFormatException e) {
                addError(objectName, fieldName, field.getAnnotation(Numeric.class).message(), value);
            }
        }

        if (field.isAnnotationPresent(annotation.DateFormat.class)) {
            annotation.DateFormat dateFormat = field.getAnnotation(annotation.DateFormat.class);
            try {
                new SimpleDateFormat(dateFormat.format()).parse(value);
            } catch (ParseException e) {
                addError(objectName, fieldName, field.getAnnotation(DateFormat.class).message(), value);
            }
        }

        if (field.isAnnotationPresent(annotation.Range.class)) {
            annotation.Range range = field.getAnnotation(annotation.Range.class);
            try {
                double numericValue = Double.parseDouble(value);
                if (numericValue < range.min() || numericValue > range.max()) {
                    String key = buildErrorKey(objectName, fieldName);
                    handleError.put(key, value);
                    handleError.put(key + ".err", field.getAnnotation(annotation.Range.class).message());
                }
            } catch (NumberFormatException e) {
                addError(objectName, fieldName, field.getAnnotation(Range.class).message(), value);
            }
        }
    }

    public void validateRole(Object object,Field field) throws Exception{
        if (field.isAnnotationPresent(annotation.Role.class)) {
            annotation.Role roleAnnotation = field.getAnnotation(annotation.Role.class);
            String roleName = roleAnnotation.name();  
            HttpSession session = request.getSession();  
            if (roleName != null && !roleName.isBlank()) {
                field.setAccessible(true);
                field.set(object,roleName);
                String existingRoles = (String) session.getAttribute("role");
                if (existingRoles == null || existingRoles.isBlank()) {

                    session.setAttribute("role", roleName);
                } else {

                    List<String> roleList = new ArrayList<>(Arrays.asList(existingRoles.split(",")));
                    if (!roleList.contains(roleName)) {
                        roleList.add(roleName);
                        session.setAttribute("role", String.join(",", roleList));
                    }
                }
            }


            session.setAttribute("authenticated", true);
        }
    }

    private void addError(String objectName, String fieldName, String message, String value) {
        String key = buildErrorKey(objectName, fieldName);
        String errorKey = key + ".err";


        handleError.put(key, value);


        if (handleError.containsKey(errorKey)) {
            String existingMessage = handleError.get(errorKey);


            if (!existingMessage.contains(message)) {
                handleError.replace(errorKey, existingMessage + "," + message);
            }

        } else {

            handleError.put(errorKey, message);
        }
    }

    private String buildErrorKey(String objectName, String fieldName) {
        if (objectName == null || objectName.isBlank()) {
            return fieldName;
        }
        return objectName + "." + fieldName;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object convertParameterType(String paramValue, Class<?> paramType) throws Exception {
        if (paramValue == null || paramValue.isEmpty()) {
            if (paramType == int.class) return 0;
            if (paramType == long.class) return 0L;
            if (paramType == double.class) return 0d;
            if (paramType == boolean.class) return false;
            return null;
        }

        if (paramType == String.class) {
            return paramValue;
        } else if (paramType == int.class || paramType == Integer.class) {
            return Integer.parseInt(paramValue);
        } else if (paramType == long.class || paramType == Long.class) {
            return Long.parseLong(paramValue);
        } else if (paramType == double.class || paramType == Double.class) {
            return Double.parseDouble(paramValue);
        } else if (paramType == boolean.class || paramType == Boolean.class) {
            return Boolean.parseBoolean(paramValue);
        } else if (paramType == Date.class) {
            return Date.valueOf(paramValue);
        } else if (paramType.isEnum()) {
            return Enum.valueOf((Class<Enum>) paramType, paramValue);
        } else {
            Constructor<?> constructor = paramType.getConstructor();
            return constructor.newInstance(paramValue);
        }
    }
}
