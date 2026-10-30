package mg.itu.servlet;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import mg.itu.mapping.Mapping;
import mg.itu.mapping.UrlMethode;
import mg.itu.util.AuthChecker;
import mg.itu.util.ControllerFactory;
import mg.itu.util.FrameworkConfig;
import mg.itu.util.ScanAnnotation;

public class FrontControllerListener
        implements ServletContextListener {

    @Override
    public void contextInitialized(
            ServletContextEvent sce) {

        ServletContext context =
                sce.getServletContext();

        // Sprint 8 : configuration (web.xml + framework.properties)
        // lisible dans les controleurs via FrameworkConfig
        Map<String, String> initParams =
                new HashMap<>();

        Enumeration<String> noms =
                context.getInitParameterNames();

        while (noms.hasMoreElements()) {

            String nom =
                    noms.nextElement();

            initParams.put(
                    nom,
                    context.getInitParameter(nom));

        }

        FrameworkConfig.init(initParams);

        String basePackage =
                context.getInitParameter(
                        "base-package");

        try {

            HashMap<UrlMethode, Mapping> mappings =
                    ScanAnnotation.generateMap(
                            basePackage);

            // Sprint 10 : si une action est @Auth, la config d'auth
            // (auth-session-key, role-session-key) doit exister
            AuthChecker.validate(mappings);

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

        // Sprint 11 : liberer les controleurs singleton
        ControllerFactory.clear();

    }

}