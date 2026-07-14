package mg.itu.util;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FindClassesByAnnotation {

    /**
     * Retourne toutes les classes d'un package
     */
    public static List<Class<?>> findClasses(String packageName)
            throws ClassNotFoundException, URISyntaxException {

        List<Class<?>> classes = new ArrayList<>();

        String path = packageName.replace('.', '/');

        ClassLoader classLoader =
                Thread.currentThread().getContextClassLoader();

        URL resource =
                classLoader.getResource(path);

        if(resource == null){
            return classes;
        }

        File directory =
                new File(resource.toURI());

        scanDirectory(
                packageName,
                directory,
                classes,
                classLoader);

        return classes;
    }

    /**
     * Scan récursif des sous-packages
     */
    private static void scanDirectory(
            String packageName,
            File directory,
            List<Class<?>> classes,
            ClassLoader classLoader)
            throws ClassNotFoundException {

        if(directory == null || !directory.exists()){
            return;
        }

        File[] files = directory.listFiles();

        if(files == null){
            return;
        }

        for(File file : files){

            if(file.isDirectory()){

                scanDirectory(
                        packageName + "." + file.getName(),
                        file,
                        classes,
                        classLoader);

            }

            else if(file.getName().endsWith(".class")){

                String className =
                        packageName
                        + "."
                        + file.getName()
                              .replace(".class","");

                Class<?> clazz =
                        classLoader.loadClass(className);

                classes.add(clazz);
            }
        }
    }

}