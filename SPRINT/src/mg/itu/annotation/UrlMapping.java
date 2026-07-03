package mg.itu.annotation;

import java.lang.annotation.*;
import mg.itu.http.HttpMethode;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UrlMapping {
    String url();
    HttpMethode method() default HttpMethode.GET;
}