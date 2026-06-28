package mg.itu.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FindClassesByAnnotation {

    public static List<Class<?>> find(
            String packageName,
            Class<?> annotation)
            throws Exception {

        List<Class<?>> result =
                new ArrayList<>();

        String root =
                Thread.currentThread()
                        .getContextClassLoader()
                        .getResource("")
                        .getPath();

        String packagePath =
                packageName.replace('.', '/');

        File folder =
                new File(root + packagePath);

        if (!folder.exists()) {
            return result;
        }

        File[] files = folder.listFiles();

        if (files == null) {
            return result;
        }

        for (File file : files) {

            if (!file.getName()
                    .endsWith(".class")) {
                continue;
            }

            String className =
                    packageName
                    + "."
                    + file.getName()
                        .replace(".class", "");

            Class<?> clazz =
                    Class.forName(className);

            if (clazz.isAnnotationPresent(
                    (Class) annotation)) {

                result.add(clazz);
            }
        }

        return result;
    }
}