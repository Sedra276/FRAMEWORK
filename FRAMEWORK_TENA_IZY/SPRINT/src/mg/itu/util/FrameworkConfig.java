package mg.itu.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sprint 8 - Configuration du framework, lisible depuis les controleurs.
 *
 * Sources, de la moins prioritaire a la plus prioritaire :
 *   1. framework.properties (a la racine du classpath, optionnel)
 *   2. les <context-param> de web.xml
 *
 * Exemple web.xml :
 *   <context-param>
 *     <param-name>upload-path</param-name>
 *     <param-value>/var/data/employes</param-value>
 *   </context-param>
 *
 * Usage dans un controleur :
 *   String dossier = FrameworkConfig.getRequired("upload-path");
 */
public class FrameworkConfig {

    public static final String PROPERTIES_FILE = "framework.properties";

    private static final Map<String, String> VALEURS =
            new ConcurrentHashMap<>();

    private FrameworkConfig() {
    }

    /** Appele une fois par le listener au demarrage. */
    public static void init(Map<String, String> paramsWebXml) {

        VALEURS.clear();

        loadProperties();

        if (paramsWebXml != null) {
            for (Map.Entry<String, String> e : paramsWebXml.entrySet()) {
                if (e.getKey() != null && e.getValue() != null) {
                    VALEURS.put(e.getKey(), e.getValue());
                }
            }
        }
    }

    private static void loadProperties() {

        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        if (cl == null) {
            cl = FrameworkConfig.class.getClassLoader();
        }

        try (InputStream in = cl.getResourceAsStream(PROPERTIES_FILE)) {

            if (in == null) {
                return;
            }

            Properties p = new Properties();
            p.load(in);

            for (String cle : p.stringPropertyNames()) {
                VALEURS.put(cle, p.getProperty(cle).trim());
            }

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Lecture de " + PROPERTIES_FILE + " impossible", e);
        }
    }

    public static String get(String cle) {
        return VALEURS.get(cle);
    }

    public static String get(String cle, String defaut) {

        String v = VALEURS.get(cle);

        return v == null ? defaut : v;
    }

    /** Comme get, mais message clair si la cle n'est pas configuree. */
    public static String getRequired(String cle) {

        String v = VALEURS.get(cle);

        if (v == null || v.isEmpty()) {
            throw new IllegalStateException(
                    "Parametre '" + cle + "' manquant : l'ajouter dans web.xml "
                            + "(<context-param>) ou dans " + PROPERTIES_FILE);
        }

        return v;
    }
}
