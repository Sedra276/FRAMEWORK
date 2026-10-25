package mg.itu.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sprint 10 - Securite : l'action (ou tous les actions du controleur si
 * l'annotation est sur la classe) n'est executable que par un utilisateur
 * authentifie.
 *
 *   @Auth                          -> il suffit d'etre connecte
 *   @Auth(roles = {"ADMIN"})       -> connecte ET role ADMIN
 *   @Auth(roles = {"ADMIN","RH"})  -> connecte ET role ADMIN ou RH
 *
 * Le framework ne decide pas du nom des variables de session : il le lit
 * dans web.xml (voir AuthChecker) :
 *   auth-session-key : variable de session qui existe si on est connecte
 *   role-session-key : variable de session qui contient le role
 *   login-url        : (optionnel) page ou rediriger si non connecte
 *
 * Une annotation sur la methode prime sur celle de la classe.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Auth {

    /** Roles acceptes (OU). Vide = tout utilisateur connecte. */
    String[] roles() default {};

}
