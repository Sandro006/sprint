package util;

import java.sql.Connection;
import java.sql.DriverManager;

import jakarta.servlet.ServletContext;

/**
 * Connexion JDBC maison, 100% java.sql (marche avec toute base).
 * Driver à fournir au runtime dans WEB-INF/lib :
 *  - MySQL : mysql-connector-j.jar (com.mysql.cj.jdbc.Driver)
 *  - Postgres : postgresql.jar (org.postgresql.Driver)
 *
 * Config dans web.xml :
 *  <context-param><param-name>db.driver</param-name><param-value>com.mysql.cj.jdbc.Driver</param-value></context-param>
 *  <context-param><param-name>db.url</param-name><param-value>jdbc:mysql://localhost:3306/test?useSSL=false&amp;serverTimezone=UTC</param-value></context-param>
 *  <context-param><param-name>db.user</param-name><param-value>root</param-value></context-param>
 *  <context-param><param-name>db.password</param-name><param-value></param-value></context-param>
 */
public final class DBUtil {

    private static String driver;
    private static String url;
    private static String user;
    private static String password;
    private static boolean initialized = false;

    private DBUtil() {
    }

    public static synchronized void configure(String drv, String u, String usr, String pwd) throws Exception {
        driver = drv;
        url = u;
        user = usr;
        password = pwd;
        if (driver != null && !driver.isBlank()) {
            Class.forName(driver);
        }
        initialized = true;
    }

    public static synchronized void init(ServletContext ctx) throws Exception {
        if (initialized) {
            return;
        }
        String drv = ctx.getInitParameter("db.driver");
        String u = ctx.getInitParameter("db.url");
        String usr = ctx.getInitParameter("db.user");
        String pwd = ctx.getInitParameter("db.password");
        if (u == null || u.isBlank()) {
            throw new Exception("DB non configurée : ajoute db.url (et db.user/password/driver) dans web.xml.");
        }
        configure(drv, u, usr == null ? "" : usr, pwd == null ? "" : pwd);
    }

    public static Connection getConnection() throws Exception {
        if (!initialized) {
            throw new Exception("DBUtil.init(ctx) non appelé. Configure web.xml d'abord.");
        }
        return DriverManager.getConnection(url, user, password);
    }

    public static Connection getConnection(ServletContext ctx) throws Exception {
        init(ctx);
        return getConnection();
    }
}
