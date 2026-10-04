package util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Mini sérialiseur JSON maison (remplace Gson).
 * Simple, sans dépendance externe.
 * Gère : null, String, Number, Boolean, Enum, Date/Temporal,
 * Map, Collection, tableau, et POJO via réflexion.
 */
public final class JsonUtil {

    private JsonUtil() {
    }

    public static String toJson(Object obj) {
        return toJson(obj, new IdentityHashMap<>());
    }

        private static String quote(String s) {
        return "\"" + s + "\"";
    }

    private static String escape(String s) {
        return s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\f", "\\f");
    }
    private static String toJson(Object obj, IdentityHashMap<Object, Boolean> seen) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof String s) {
            return quote(escape(s));
        }
        if (obj instanceof Character c) {
            return quote(escape(c.toString()));
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        if (obj instanceof Enum<?> e) {
            return quote(escape(e.name()));
        }
        // Dates -> string ISO simple
        if (obj instanceof Date d) {
            return quote(new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(d));
        }
        if (obj instanceof Temporal t) {
            return quote(t.toString());
        }
        if (obj instanceof LocalDate || obj instanceof LocalDateTime) {
            return quote(obj.toString());
        }

        // Anti boucle infinie : objet déjà vu
        if (seen.containsKey(obj)) {
            return "null";
        }
        seen.put(obj, Boolean.TRUE);

        try {
            if (obj instanceof Map<?, ?> map) {
                return mapToJson(map, seen);
            }
            if (obj instanceof Collection<?> col) {
                return collectionToJson(col, seen);
            }
            if (obj.getClass().isArray()) {
                return arrayToJson(obj, seen);
            }
            return pojoToJson(obj, seen);
        } finally {
            seen.remove(obj);
        }
    }

    private static String mapToJson(Map<?, ?> map, IdentityHashMap<Object, Boolean> seen) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<?, ?> e : map.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            String key = e.getKey() == null ? "null" : e.getKey().toString();
            sb.append(quote(escape(key)));
            sb.append(":");
            sb.append(toJson(e.getValue(), seen));
        }
        sb.append("}");
        return sb.toString();
    }

    private static String collectionToJson(Collection<?> col, IdentityHashMap<Object, Boolean> seen) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Object item : col) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append(toJson(item, seen));
        }
        sb.append("]");
        return sb.toString();
    }

    private static String arrayToJson(Object array, IdentityHashMap<Object, Boolean> seen) {
        StringBuilder sb = new StringBuilder("[");
        int len = Array.getLength(array);
        for (int i = 0; i < len; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(toJson(Array.get(array, i), seen));
        }
        sb.append("]");
        return sb.toString();
    }

    private static String pojoToJson(Object obj, IdentityHashMap<Object, Boolean> seen) {
        StringBuilder sb = new StringBuilder("{");
        Field[] fields = obj.getClass().getDeclaredFields();
        boolean first = true;
        for (Field f : fields) {
            int mod = f.getModifiers();
            if (Modifier.isStatic(mod) || Modifier.isTransient(mod)) {
                continue;
            }
            f.setAccessible(true);
            Object value;
            try {
                value = f.get(obj);
            } catch (IllegalAccessException e) {
                continue;
            }
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append(quote(escape(f.getName())));
            sb.append(":");
            sb.append(toJson(value, seen));
        }
        sb.append("}");
        return sb.toString();
    }


}
