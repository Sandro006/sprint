# Gogo Framework — Mini MVC Java

Petit framework MVC Java (type Spring simplifié) basé sur un `FrontController` Servlet `jakarta.*`.

Il scanne les classes annotées `@Controller`, mappe les URLs vers les méthodes via `@Url`, gère les verbes `@Get` / `@Post`, le binding des paramètres, la validation, les vues et les API JSON.

## Fonctionnalités
- Routing : `@Controller` + `@Url("/liste")`
- Verbes HTTP : `@Get`, `@Post`
- Paramètres : `@Param`
- Vues : `ModelView` / `ModelAndView` (`setUrl()` + `addObjet()`)
- API REST : `@RestApi` (JSON maison via `util.JsonUtil`)
- Base générique : `util.DBUtil` + `dao.BaseDAO` (JDBC, `@Table`/`@Column`/`@Id`)
- Contrôleurs **singleton** par défaut (`controllers.singleton=false` pour revenir au prototype)

## Authentification
- `@Authentification(name = "ADMIN,MANAGER")` sur une méthode : exige `authenticated=true` + un rôle autorisé en session.
- `@Role(name = "ADMIN")` sur un **champ** : *vérifie* le rôle de la session et lie le champ. Ne modifie jamais la session.
- Le login doit établir la session lui-même :
  ```java
  session.add("user", username);
  session.add("role", role);
  session.add("authenticated", true);
  ```
- `CustomSession.add()` écrase la valeur (comportement `HttpSession.setAttribute`), donc un re-login change bien le rôle.

## Prérequis
- JDK (avec `javac`, `jar` dans le PATH)
- Apache Tomcat 10.1 (framework en `jakarta.servlet`, incompatible Tomcat 9)
- Aucune dépendance externe (JSON maison intégré)

## Build
```bat
script-framework.bat
```
1. Compile `src/*.java`
2. Crée `gogo.jar`
3. Le synchronise vers `FrontController-Test` (`lib/` + `WEB-INF/lib/`)
4. Ensuite lance `deploy.bat` dans `FrontController-Test`

## Exemple rapide
```java
@Controller
public class MonController {

    @Url(url = "/hello")
    @Get
    public ModelAndView hello(@Param("nom") String nom) {
        ModelAndView mv = new ModelAndView();
        mv.setUrl("/hello.jsp");
        mv.addObjet("nom", nom);
        return mv;
    }

    @Url(url = "/api/hello")
    @Get
    @RestApi
    public String helloJson(@Param("nom") String nom) {
        return "{\"message\":\"Bonjour \"}";
    }
}
```

## Base de données (générique, testé MySQL)
`web.xml` :
```xml
<context-param><param-name>db.driver</param-name><param-value>com.mysql.cj.jdbc.Driver</param-value></context-param>
<context-param><param-name>db.url</param-name><param-value>jdbc:mysql://localhost:3306/test?useSSL=false&amp;serverTimezone=UTC</param-value></context-param>
<context-param><param-name>db.user</param-name><param-value>root</param-value></context-param>
<context-param><param-name>db.password</param-name><param-value></param-value></context-param>
```
Mettre `mysql-connector-j.jar` dans `WEB-INF/lib` (runtime seul, framework = `java.sql` seul).
```java
@Table(name="etudiant")
public class Etudiant {
  @Id private int id;
  @Column(name="nom") private String nom;
  // ctor vide + getters/setters
}

try (Connection c = DBUtil.getConnection(getServletContext())) {
  BaseDAO<Etudiant> dao = new BaseDAO<>(Etudiant.class);
  List<Etudiant> all = dao.findAll(c);
}
```
