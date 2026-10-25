package mg.itu.util;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import mg.itu.annotation.Auth;
import mg.itu.mapping.Mapping;
import mg.itu.mapping.UrlMethode;

/**
 * Sprint 10 - Verification de l'authentification avant l'appel d'une action.
 *
 * Configuration (web.xml <context-param>, ou framework.properties) :
 *   auth-session-key  nom de la variable de session qui prouve que
 *                     l'utilisateur est connecte (ex: "utilisateur")
 *   role-session-key  nom de la variable de session qui contient le role
 *                     (ex: "role") - obligatoire seulement si un @Auth
 *                     utilise roles = {...}
 *   login-url         (optionnel) URL ou rediriger un non-connecte (ex: /login)
 *
 * Les noms sont lus UNE fois au demarrage (FrontControllerListener appelle
 * validate) : si une action est @Auth et que la configuration manque, le
 * deploiement echoue tout de suite avec un message clair, au lieu de
 * laisser passer tout le monde en silence.
 *
 * La variable de role peut etre un String, une Collection ou un tableau.
 */
public class AuthChecker {

    public static final String AUTH_KEY_PARAM = "auth-session-key";
    public static final String ROLE_KEY_PARAM = "role-session-key";
    public static final String LOGIN_URL_PARAM = "login-url";

    public enum Resultat {
        AUTORISE,
        NON_AUTHENTIFIE, // -> 401 ou redirection vers login-url
        INTERDIT         // -> 403 (connecte mais mauvais role)
    }

    private AuthChecker() {
    }

    /** Annotation effective : methode d'abord, sinon classe, sinon null. */
    public static Auth findAuth(Class<?> controller, Method method) {

        Auth auth = method.getAnnotation(Auth.class);

        if (auth == null) {
            auth = controller.getAnnotation(Auth.class);
        }

        return auth;
    }

    /** Verification au demarrage de toute la table de mapping. */
    public static void validate(Map<UrlMethode, Mapping> mappings) {

        for (Map.Entry<UrlMethode, Mapping> e : mappings.entrySet()) {

            Mapping m = e.getValue();

            Auth auth = findAuth(m.getControllerClass(), m.getMethod());

            if (auth == null) {
                continue;
            }

            if (isBlank(FrameworkConfig.get(AUTH_KEY_PARAM))) {
                throw new IllegalStateException(
                        "@Auth sur " + m + " mais le parametre '"
                                + AUTH_KEY_PARAM + "' est absent de web.xml");
            }

            if (auth.roles().length > 0
                    && isBlank(FrameworkConfig.get(ROLE_KEY_PARAM))) {
                throw new IllegalStateException(
                        "@Auth(roles) sur " + m + " mais le parametre '"
                                + ROLE_KEY_PARAM + "' est absent de web.xml");
            }
        }
    }

    public static Resultat check(HttpServletRequest request, Mapping mapping) {

        Auth auth = findAuth(
                mapping.getControllerClass(), mapping.getMethod());

        if (auth == null) {
            return Resultat.AUTORISE; // action publique
        }

        HttpSession session = request.getSession(false);

        String cleAuth = FrameworkConfig.get(AUTH_KEY_PARAM);

        if (session == null
                || isBlank(cleAuth)
                || session.getAttribute(cleAuth) == null) {
            return Resultat.NON_AUTHENTIFIE;
        }

        if (auth.roles().length == 0) {
            return Resultat.AUTORISE;
        }

        String cleRole = FrameworkConfig.get(ROLE_KEY_PARAM);

        if (isBlank(cleRole)) {
            return Resultat.INTERDIT; // jamais autorise par erreur
        }

        Object roleSession = session.getAttribute(cleRole);

        for (String requis : auth.roles()) {
            if (possede(roleSession, requis)) {
                return Resultat.AUTORISE;
            }
        }

        return Resultat.INTERDIT;
    }

    private static boolean possede(Object roleSession, String requis) {

        if (roleSession == null) {
            return false;
        }

        if (roleSession instanceof Collection) {
            for (Object o : (Collection<?>) roleSession) {
                if (o != null && requis.equals(o.toString())) {
                    return true;
                }
            }
            return false;
        }

        if (roleSession.getClass().isArray()) {
            int n = Array.getLength(roleSession);
            for (int i = 0; i < n; i++) {
                Object o = Array.get(roleSession, i);
                if (o != null && requis.equals(o.toString())) {
                    return true;
                }
            }
            return false;
        }

        return requis.equals(roleSession.toString());
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

}
