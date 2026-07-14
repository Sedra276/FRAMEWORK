package mg.itu.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import mg.itu.annotation.Controller;
import mg.itu.annotation.UrlMapping;
import mg.itu.mapping.Mapping;
import mg.itu.mapping.UrlMethode;

public class ScanAnnotation {

    /**
     * Génère automatiquement la table de mapping
     */
    public static HashMap<UrlMethode, Mapping> generateMap(String packageName)
            throws Exception {

        HashMap<UrlMethode, Mapping> mappings =
                new HashMap<>();

        List<Class<?>> classes =
                FindClassesByAnnotation.findClasses(packageName);

        for (Class<?> clazz : classes) {

            // Vérifie si la classe est un Controller
            if (!clazz.isAnnotationPresent(Controller.class)) {
                continue;
            }

            // Parcourt toutes les méthodes
            for (Method method : clazz.getDeclaredMethods()) {

                if (!method.isAnnotationPresent(UrlMapping.class)) {
                    continue;
                }

                UrlMapping annotation =
                        method.getAnnotation(UrlMapping.class);

                UrlMethode key =
                        new UrlMethode(
                                annotation.url(),
                                annotation.method());

                // Vérification de duplication
                if (mappings.containsKey(key)) {

                    throw new Exception(
                            "URL déjà utilisée : "
                                    + annotation.method()
                                    + " "
                                    + annotation.url());
                }

                Mapping value =
                        new Mapping(
                                clazz,
                                method);

                mappings.put(key, value);
            }
        }

        return mappings;
    }

}