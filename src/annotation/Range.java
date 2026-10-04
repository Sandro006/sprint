package annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Range {
    double min();
    double max();
    String message() default "La valeur est hors de la plage permise.";
}

