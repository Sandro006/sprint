# Gogo Framework — Mini MVC Java

Petit framework MVC Java (type Spring simplifié) basé sur un `FrontController` Servlet `jakarta.*`.

Il scanne les classes annotées `@Controller`, mappe les URLs vers les méthodes via `@Url`, gère les verbes `@Get` / `@Post`, le binding des paramètres, la validation, les vues et les API JSON.

## Fonctionnalités
- Routing : `@Controller` + `@Url("/liste")`
- Verbes HTTP : `@Get`, `@Post`
- Paramètres : `@Param`
- Vues : `ModelView` / `ModelAndView` (`setUrl()` + `addObjet()`)
- API REST : `@RestApi` (JSON maison via `util.JsonUtil`)

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
