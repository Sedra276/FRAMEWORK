package mg.itu.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sprint 11 - Controleur singleton.
 *
 * Par defaut le framework cree un NOUVEAU controleur a chaque requete
 * (les attributs d'instance repartent de zero). Avec @Singleton, UNE SEULE
 * instance de la classe est creee (a la premiere requete) puis reutilisee
 * pour toutes les requetes et tous les utilisateurs.
 *
 *   @Controller
 *   @Singleton
 *   public class CompteurController { private int compteur; ... }
 *
 * Attention : l'instance est partagee entre tous les navigateurs et
 * plusieurs requetes peuvent l'utiliser en meme temps. Les donnees propres
 * a un utilisateur vont dans MapSession, pas dans un attribut du
 * controleur ; un attribut modifie doit etre protege (synchronized,
 * AtomicInteger...).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Singleton {

}
