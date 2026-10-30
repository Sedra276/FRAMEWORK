package mg.itu.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import mg.itu.annotation.Singleton;

/**
 * Sprint 11 - Fournit l'instance du controleur pour une requete.
 *
 *   classe sans @Singleton : nouvelle instance a chaque appel (comportement
 *                            des sprints precedents)
 *   classe avec @Singleton : toujours la meme instance, creee au premier
 *                            appel (get est synchronise : meme si deux
 *                            requetes arrivent ensemble, une seule
 *                            instance est creee)
 */
public class ControllerFactory {

    private static final Map<Class<?>, Object> SINGLETONS =
            new HashMap<>();

    private ControllerFactory() {
    }

    public static Object get(Class<?> type)
            throws InstantiationException,
            IllegalAccessException,
            InvocationTargetException,
            NoSuchMethodException {

        if (!type.isAnnotationPresent(Singleton.class)) {
            return create(type);
        }

        synchronized (SINGLETONS) {

            Object instance = SINGLETONS.get(type);

            if (instance == null) {
                instance = create(type);
                SINGLETONS.put(type, instance);
            }

            return instance;
        }
    }

    /** Appele a l'arret de l'application (contextDestroyed). */
    public static void clear() {

        synchronized (SINGLETONS) {
            SINGLETONS.clear();
        }
    }

    private static Object create(Class<?> type)
            throws InstantiationException,
            IllegalAccessException,
            InvocationTargetException,
            NoSuchMethodException {

        Constructor<?> ctor = type.getDeclaredConstructor();
        ctor.setAccessible(true);

        return ctor.newInstance();
    }

}
