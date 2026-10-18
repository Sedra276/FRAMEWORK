package mg.itu.util;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import mg.itu.http.MapFile;

/**
 * Sprint 8 - Binding de la requete vers les parametres d'une action.
 *
 * Conventions de nommage des champs du formulaire :
 *   objet            : emp.nom, emp.adresse.ville
 *   liste d'objets   : emp.adresses[0].ville, emp.adresses[1].ville
 *   liste de simples : emp.tags=a&emp.tags=b   ou   emp.notes[0]=15
 *   fichier          : emp.rectoCin  (input type="file" name="emp.rectoCin")
 *   liste de fichiers: emp.docs (input multiple) ou emp.docs[0], emp.docs[1]
 *
 * Un champ ou parametre de type MapFile est rempli depuis la Map des
 * fichiers recus ; il n'est jamais traite comme un objet ordinaire.
 *
 * Cette classe ne depend pas de la servlet : elle travaille sur des Map
 * (request.getParameterMap() et les fichiers deja extraits), donc elle
 * est testable sans Tomcat.
 */
public class ObjectBinder {

    private ObjectBinder() {
    }

    /** Donnees de la requete, passees d'un appel recursif a l'autre. */
    private static final class Ctx {

        final Map<String, String[]> params;
        final Map<String, List<MapFile>> files;

        Ctx(Map<String, String[]> params, Map<String, List<MapFile>> files) {
            this.params = params;
            this.files = files == null
                    ? Collections.<String, List<MapFile>>emptyMap()
                    : files;
        }
    }

    public static Object bind(
            Map<String, String[]> params,
            String nom,
            Class<?> type) {

        return bind(params, null, nom, type, type);
    }

    public static Object bind(
            Map<String, String[]> params,
            String nom,
            Class<?> type,
            Type genericType) {

        return bind(params, null, nom, type, genericType);
    }

    /**
     * Point d'entree. genericType permet de connaitre le type des
     * elements d'une liste (List<Adresse>), que Class<?> seul ne donne pas.
     */
    public static Object bind(
            Map<String, String[]> params,
            Map<String, List<MapFile>> files,
            String nom,
            Class<?> type,
            Type genericType) {

        // premier niveau : un objet est toujours instancie, une liste
        // est toujours renvoyee (vide si rien recu)
        return resolve(new Ctx(params, files), nom, type, genericType, true);
    }

    /**
     * Construit la valeur de "cle" selon son type.
     * force = true  : renvoie toujours quelque chose (premier niveau)
     * force = false : renvoie null si le formulaire ne contient rien
     *                 pour cette cle (le champ garde alors sa valeur par defaut)
     */
    private static Object resolve(
            Ctx ctx,
            String cle,
            Class<?> type,
            Type genericType,
            boolean force) {

        // 1. fichier : verifie AVANT les objets, sinon MapFile serait
        //    instancie comme un POJO avec ses champs filename/extension
        if (type == MapFile.class) {
            return firstFile(ctx, cle);
        }

        // 2. valeur simple
        if (isSimple(type)) {

            if (!force && !ctx.params.containsKey(cle)) {
                return null;
            }

            return convertValue(first(ctx.params, cle), type);
        }

        // 3. collection : List, Set, Collection
        if (Collection.class.isAssignableFrom(type)) {

            Class<?> elementType = elementTypeOf(genericType);

            if (elementType == null) {
                return null; // List sans type generique : non gere
            }

            List<Object> elements = buildElements(ctx, cle, elementType);

            if (elements == null) {
                return force ? newCollection(type, new ArrayList<>()) : null;
            }

            return newCollection(type, elements);
        }

        // 4. tableau : String[], int[], Adresse[], MapFile[]
        if (type.isArray()) {

            Class<?> elementType = type.getComponentType();

            List<Object> elements = buildElements(ctx, cle, elementType);

            if (elements == null) {
                return force ? Array.newInstance(elementType, 0) : null;
            }

            Object tableau = Array.newInstance(elementType, elements.size());

            for (int i = 0; i < elements.size(); i++) {
                Array.set(tableau, i, elements.get(i));
            }

            return tableau;
        }

        // 5. interface / abstract (Map, etc.) : non gere
        if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
            return null;
        }

        // 6. objet
        return bindObject(ctx, cle, type, force);
    }

    /**
     * Construit les elements d'une liste ou d'un tableau.
     * Renvoie null si le formulaire ne contient rien pour cette cle.
     */
    private static List<Object> buildElements(
            Ctx ctx,
            String cle,
            Class<?> elementType) {

        List<Object> elements = new ArrayList<>();

        // cas A : valeurs simples repetees (emp.tags=a&emp.tags=b)
        if (isSimple(elementType) && ctx.params.containsKey(cle)) {

            for (String valeur : ctx.params.get(cle)) {
                elements.add(convertValue(valeur, elementType));
            }

            return elements;
        }

        // cas A bis : plusieurs fichiers sous le meme nom (input multiple)
        if (elementType == MapFile.class && ctx.files.containsKey(cle)) {

            elements.addAll(ctx.files.get(cle));

            return elements;
        }

        // cas B : elements indexes (cle[0], cle[1].champ ...)
        TreeSet<Integer> indices = findIndices(ctx, cle);

        if (indices.isEmpty()) {
            return null;
        }

        // les indices sont tries ; un trou (0, 2) est simplement compacte
        for (int i : indices) {

            Object element = resolve(
                    ctx,
                    cle + "[" + i + "]",
                    elementType,
                    elementType,
                    true);

            if (element != null) {
                elements.add(element);
            }
        }

        return elements;
    }

    /** Cherche tous les indices i pour lesquels une cle commence par "cle[i]". */
    private static TreeSet<Integer> findIndices(Ctx ctx, String cle) {

        TreeSet<Integer> indices = new TreeSet<>();

        collectIndices(ctx.params.keySet(), cle, indices);
        collectIndices(ctx.files.keySet(), cle, indices);

        return indices;
    }

    private static void collectIndices(
            Iterable<String> cles,
            String cle,
            TreeSet<Integer> indices) {

        String debut = cle + "[";

        for (String k : cles) {

            if (!k.startsWith(debut)) {
                continue;
            }

            int fin = k.indexOf(']', debut.length());

            if (fin < 0) {
                continue;
            }

            try {
                indices.add(
                        Integer.parseInt(
                                k.substring(debut.length(), fin)));
            } catch (NumberFormatException e) {
                // indice non numerique : ignore
            }
        }
    }

    /** Extrait T de List<T> ; null si le type n'est pas parametre. */
    private static Class<?> elementTypeOf(Type genericType) {

        if (genericType instanceof ParameterizedType) {

            Type[] args =
                    ((ParameterizedType) genericType)
                            .getActualTypeArguments();

            if (args.length == 1 && args[0] instanceof Class) {
                return (Class<?>) args[0];
            }
        }

        return null;
    }

    /** Cree la bonne collection selon le type declare (List, Set...). */
    @SuppressWarnings("unchecked")
    private static Object newCollection(Class<?> type, List<Object> elements) {

        if (type.isAssignableFrom(ArrayList.class)) {
            return elements; // List, Collection, ArrayList
        }

        if (type.isAssignableFrom(LinkedHashSet.class)) {
            return new LinkedHashSet<>(elements); // Set
        }

        // classe concrete particuliere (LinkedList, TreeSet...)
        try {
            Collection<Object> c =
                    (Collection<Object>) type.getDeclaredConstructor()
                            .newInstance();
            c.addAll(elements);
            return c;
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(
                    "Collection non supportee : " + type.getName(), e);
        }
    }

    private static Object bindObject(
            Ctx ctx,
            String prefix,
            Class<?> type,
            boolean force) {

        // objet imbrique : on ne l'instancie que si le formulaire
        // contient au moins un champ ou fichier qui le concerne
        if (!force && !hasPrefix(ctx, prefix)) {
            return null;
        }

        Object obj;

        try {
            Constructor<?> ctor = type.getDeclaredConstructor();
            ctor.setAccessible(true);
            obj = ctor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(
                    "Impossible d'instancier " + type.getName()
                            + " (constructeur sans argument requis)", e);
        }

        // parcours des champs de l'objet : pour chacun, resolve() decide
        // s'il s'agit d'un MapFile, d'une valeur simple, d'une liste...
        for (Field champ : allFields(type)) {

            Object valeur = resolve(
                    ctx,
                    prefix + "." + champ.getName(),
                    champ.getType(),
                    champ.getGenericType(),
                    false);

            // rien recu pour ce champ : on garde la valeur par defaut
            if (valeur == null) {
                continue;
            }

            setValue(obj, champ, valeur);
        }

        return obj;
    }

    /** Setter s'il existe (setNom), sinon acces direct au champ. */
    private static void setValue(Object obj, Field champ, Object valeur) {

        String nom = champ.getName();

        String setter = "set"
                + Character.toUpperCase(nom.charAt(0))
                + nom.substring(1);

        try {

            Method m = obj.getClass().getMethod(setter, champ.getType());
            m.invoke(obj, valeur);

        } catch (NoSuchMethodException e) {

            try {
                champ.setAccessible(true);
                champ.set(obj, valeur);
            } catch (IllegalAccessException ex) {
                throw new IllegalArgumentException(
                        "Impossible d'ecrire le champ " + nom, ex);
            }

        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(
                    "Erreur sur " + setter, e);
        }
    }

    /** Champs de la classe + ceux des classes parentes (hors static/final). */
    private static List<Field> allFields(Class<?> type) {

        List<Field> champs = new ArrayList<>();

        for (Class<?> c = type;
             c != null && c != Object.class;
             c = c.getSuperclass()) {

            for (Field f : c.getDeclaredFields()) {

                int mod = f.getModifiers();

                if (Modifier.isStatic(mod) || Modifier.isFinal(mod)
                        || f.isSynthetic()) {
                    continue;
                }

                champs.add(f);
            }
        }

        return champs;
    }

    private static boolean hasPrefix(Ctx ctx, String prefix) {

        String debutPoint = prefix + ".";
        String debutIndex = prefix + "[";

        for (String cle : ctx.params.keySet()) {
            if (cle.startsWith(debutPoint) || cle.startsWith(debutIndex)) {
                return true;
            }
        }

        for (String cle : ctx.files.keySet()) {
            if (cle.startsWith(debutPoint) || cle.startsWith(debutIndex)) {
                return true;
            }
        }

        return false;
    }

    private static MapFile firstFile(Ctx ctx, String cle) {

        List<MapFile> liste = ctx.files.get(cle);

        if (liste == null || liste.isEmpty()) {
            return null;
        }

        return liste.get(0);
    }

    private static String first(Map<String, String[]> params, String cle) {

        String[] valeurs = params.get(cle);

        if (valeurs == null || valeurs.length == 0) {
            return null;
        }

        return valeurs[0];
    }

    public static boolean isSimple(Class<?> type) {

        return type.isPrimitive()
                || type == String.class
                || type == Integer.class
                || type == Long.class
                || type == Double.class
                || type == Float.class
                || type == Short.class
                || type == Byte.class
                || type == Boolean.class
                || type == Character.class
                || type.isEnum();
    }

    /** Conversion String -> type declare. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Object convertValue(String valeur, Class<?> type) {

        if (type == String.class) {
            return valeur;
        }

        if (valeur == null || valeur.isEmpty()) {

            if (type == int.class) return 0;
            if (type == long.class) return 0L;
            if (type == double.class) return 0.0;
            if (type == float.class) return 0f;
            if (type == short.class) return (short) 0;
            if (type == byte.class) return (byte) 0;
            if (type == boolean.class) return false;
            if (type == char.class) return '\0';

            return null;
        }

        if (type == int.class || type == Integer.class)
            return Integer.parseInt(valeur);
        if (type == long.class || type == Long.class)
            return Long.parseLong(valeur);
        if (type == double.class || type == Double.class)
            return Double.parseDouble(valeur);
        if (type == float.class || type == Float.class)
            return Float.parseFloat(valeur);
        if (type == short.class || type == Short.class)
            return Short.parseShort(valeur);
        if (type == byte.class || type == Byte.class)
            return Byte.parseByte(valeur);
        if (type == boolean.class || type == Boolean.class)
            return Boolean.parseBoolean(valeur);
        if (type == char.class || type == Character.class)
            return valeur.charAt(0);
        if (type.isEnum())
            return Enum.valueOf((Class<Enum>) type, valeur);

        return valeur;
    }
}
