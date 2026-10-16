package dao;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import annotation.Column;
import annotation.Id;
import annotation.Table;

/**
 * DAO générique maison (aucune lib, 100% JDBC).
 * Marche avec MySQL / Postgres / Oracle / etc.
 * Ex : BaseDAO<Etudiant> dao = new BaseDAO<>(Etudiant.class);
 */
public class BaseDAO<T> {

    private final Class<T> type;
    private final String table;
    private final List<FieldMeta> fields = new ArrayList<>();
    private FieldMeta idField;

    private static class FieldMeta {
        Field field;
        String column;
        boolean id;
    }

    public BaseDAO(Class<T> type) throws Exception {
        this.type = type;
        Table t = type.getAnnotation(Table.class);
        if (t != null && !t.name().isBlank()) {
            this.table = t.name();
        } else {
            this.table = type.getSimpleName().toLowerCase();
        }
        for (Field f : type.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers()) || Modifier.isTransient(f.getModifiers())) {
                continue;
            }
            FieldMeta m = new FieldMeta();
            m.field = f;
            Column c = f.getAnnotation(Column.class);
            m.column = (c != null && !c.name().isBlank()) ? c.name() : f.getName();
            m.id = f.isAnnotationPresent(Id.class);
            // Fallback : champ "id" = PK si aucun @Id
            if (!m.id && f.getName().equalsIgnoreCase("id") && idField == null) {
                m.id = true;
            }
            fields.add(m);
            if (m.id && idField == null) {
                idField = m;
            }
        }
        if (fields.isEmpty()) {
            throw new Exception("DAO: aucun champ mappable dans " + type.getName());
        }
    }

    public List<T> findAll(Connection c) throws Exception {
        return query(c, "SELECT * FROM " + table);
    }

    public T findById(Connection c, Object id) throws Exception {
        if (idField == null) {
            throw new Exception("DAO: aucun @Id défini pour " + type.getName());
        }
        List<T> list = query(c, "SELECT * FROM " + table + " WHERE " + idField.column + " = ?", id);
        return list.isEmpty() ? null : list.get(0);
    }

    public int deleteById(Connection c, Object id) throws Exception {
        if (idField == null) {
            throw new Exception("DAO: aucun @Id défini pour " + type.getName());
        }
        return execute(c, "DELETE FROM " + table + " WHERE " + idField.column + " = ?", id);
    }

    /** INSERT : ignore l'@Id si 0/null (AUTO_INCREMENT MySQL). */
    public int save(Connection c, T obj) throws Exception {
        List<FieldMeta> toInsert = new ArrayList<>();
        for (FieldMeta m : fields) {
            if (m.id && isEmptyId(obj, m)) {
                continue;
            }
            toInsert.add(m);
        }
        StringBuilder cols = new StringBuilder();
        StringBuilder vals = new StringBuilder();
        for (int i = 0; i < toInsert.size(); i++) {
            if (i > 0) {
                cols.append(",");
                vals.append(",");
            }
            cols.append(toInsert.get(i).column);
            vals.append("?");
        }
        String sql = "INSERT INTO " + table + " (" + cols + ") VALUES (" + vals + ")";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < toInsert.size(); i++) {
                toInsert.get(i).field.setAccessible(true);
                ps.setObject(i + 1, toInsert.get(i).field.get(obj));
            }
            return ps.executeUpdate();
        }
    }

    public int update(Connection c, T obj) throws Exception {
        if (idField == null) {
            throw new Exception("DAO: update nécessite un @Id.");
        }
        StringBuilder set = new StringBuilder();
        List<FieldMeta> toSet = new ArrayList<>();
        for (FieldMeta m : fields) {
            if (m.id) {
                continue;
            }
            if (!toSet.isEmpty()) {
                set.append(",");
            }
            set.append(m.column).append("=?");
            toSet.add(m);
        }
        String sql = "UPDATE " + table + " SET " + set + " WHERE " + idField.column + " = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            for (FieldMeta m : toSet) {
                m.field.setAccessible(true);
                ps.setObject(i++, m.field.get(obj));
            }
            idField.field.setAccessible(true);
            ps.setObject(i, idField.field.get(obj));
            return ps.executeUpdate();
        }
    }

    public List<T> query(Connection c, String sql, Object... params) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<T> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        }
    }

    public int execute(Connection c, String sql, Object... params) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps.executeUpdate();
        }
    }

    private boolean isEmptyId(T obj, FieldMeta m) throws Exception {
        m.field.setAccessible(true);
        Object v = m.field.get(obj);
        if (v == null) {
            return true;
        }
        if (v instanceof Number n) {
            return n.longValue() == 0L;
        }
        if (v instanceof String s) {
            return s.isEmpty();
        }
        return false;
    }

    private T mapRow(ResultSet rs) throws Exception {
        T obj = type.getDeclaredConstructor().newInstance();
        for (FieldMeta m : fields) {
            Object val;
            try {
                val = rs.getObject(m.column);
            } catch (Exception e) {
                continue; // colonne absente -> ignore
            }
            if (val == null) {
                continue;
            }
            m.field.setAccessible(true);
            Class<?> ft = m.field.getType();
            if (ft == String.class) {
                m.field.set(obj, val.toString());
            } else if (ft == int.class || ft == Integer.class) {
                m.field.set(obj, ((Number) val).intValue());
            } else if (ft == long.class || ft == Long.class) {
                m.field.set(obj, ((Number) val).longValue());
            } else if (ft == double.class || ft == Double.class) {
                m.field.set(obj, ((Number) val).doubleValue());
            } else if (ft == boolean.class || ft == Boolean.class) {
                if (val instanceof Boolean b) {
                    m.field.set(obj, b);
                } else if (val instanceof Number n) {
                    m.field.set(obj, n.intValue() != 0);
                } else {
                    m.field.set(obj, Boolean.parseBoolean(val.toString()));
                }
            } else if (ft == java.sql.Date.class) {
                if (val instanceof java.sql.Date d) {
                    m.field.set(obj, d);
                } else {
                    m.field.set(obj, java.sql.Date.valueOf(val.toString().substring(0, 10)));
                }
            } else {
                m.field.set(obj, val);
            }
        }
        return obj;
    }
}
