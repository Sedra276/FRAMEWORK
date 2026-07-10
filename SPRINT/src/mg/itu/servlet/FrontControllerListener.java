package mg.itu.servlet;

import java.util.HashMap;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import mg.itu.mapping.Mapping;
import mg.itu.mapping.UrlMethode;
import mg.itu.util.ScanAnnotation;

public class FrontControllerListener
        implements ServletContextListener {

    @Override
    public void contextInitialized(
            ServletContextEvent sce) {

        ServletContext context =
                sce.getServletContext();

        String basePackage =
                context.getInitParameter(
                        "base-package");

        try {

            HashMap<UrlMethode, Mapping> mappings =
                    ScanAnnotation.generateMap(
                            basePackage);

            context.setAttribute(
                    "mapping",
                    mappings);

            System.out.println(
                    "Framework initialized.");

        } catch (Exception e) {

            throw new RuntimeException(e);

        }

    }

    @Override
    public void contextDestroyed(
            ServletContextEvent sce) {

    }

}