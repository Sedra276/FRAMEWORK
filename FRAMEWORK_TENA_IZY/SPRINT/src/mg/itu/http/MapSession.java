package mg.itu.http;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Sprint 9 - Session utilisateur.
 *
 * Une methode de controleur qui declare un parametre de type MapSession
 * le recoit automatiquement du framework (rien a lire dans le formulaire) :
 *
 *   @UrlMapping(url = "/login", method = HttpMethode.POST)
 *   public ModelAndView login(String login, MapSession session) {
 *       session.set("utilisateur", login);
 *       ...
 *   }
 *
 * Chaque navigateur a son propre cookie JSESSIONID, donc sa propre
 * session : deux navigateurs (ou une fenetre privee) = deux utilisateurs
 * independants.
 *
 * Fonctionnement "ecriture directe" : set/remove agissent tout de suite
 * sur la vraie HttpSession, il n'y a rien a recopier apres l'appel.
 * La session n'est creee que si on y ecrit (get/has n'en creent pas),
 * ce qui evite de creer une session pour chaque visiteur anonyme.
 */
public class MapSession {

    private final HttpServletRequest request;

    public MapSession(HttpServletRequest request) {
        this.request = request;
    }

    /** Valeur de la cle, ou null (cle absente ou pas de session). */
    public Object get(String cle) {

        HttpSession session = request.getSession(false);

        return session == null ? null : session.getAttribute(cle);
    }

    /** Comme get(cle) avec cast ; null si absent ou d'un autre type. */
    public <T> T get(String cle, Class<T> type) {

        Object valeur = get(cle);

        return type.isInstance(valeur) ? type.cast(valeur) : null;
    }

    /** Ecrit une valeur (cree la session si besoin). null = supprime la cle. */
    public void set(String cle, Object valeur) {

        if (valeur == null) {
            remove(cle);
            return;
        }

        request.getSession(true).setAttribute(cle, valeur);
    }

    public void remove(String cle) {

        HttpSession session = request.getSession(false);

        if (session != null) {
            session.removeAttribute(cle);
        }
    }

    public boolean has(String cle) {
        return get(cle) != null;
    }

    /** Copie des attributs actuels (modifier la copie n'a aucun effet). */
    public Map<String, Object> asMap() {

        Map<String, Object> copie = new LinkedHashMap<>();

        HttpSession session = request.getSession(false);

        if (session == null) {
            return copie;
        }

        Enumeration<String> noms = session.getAttributeNames();

        while (noms.hasMoreElements()) {
            String nom = noms.nextElement();
            copie.put(nom, session.getAttribute(nom));
        }

        return Collections.unmodifiableMap(copie);
    }

    /** Identifiant de session (JSESSIONID) ; null s'il n'y a pas de session. */
    public String getId() {

        HttpSession session = request.getSession(false);

        return session == null ? null : session.getId();
    }

    /** Detruit la session (deconnexion). Sans effet s'il n'y en a pas. */
    public void invalidate() {

        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }
    }

    @Override
    public String toString() {
        return "MapSession{id=" + getId() + ", data=" + asMap() + "}";
    }

}
