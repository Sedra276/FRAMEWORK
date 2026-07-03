package mg.itu.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import mg.itu.annotation.Controller;
import mg.itu.annotation.UrlMapping;
import mg.itu.exception.DuplicateMappingException;
import mg.itu.http.HttpMethode;
import mg.itu.mapping.Mapping;
import mg.itu.util.FindClassesByAnnotation;

public class FrontControllerServlet extends HttpServlet {

    private List<String> controllers = new ArrayList<>();
    private HashMap<String, Mapping> mappings = new HashMap<>();

    @Override
    public void init() throws ServletException {
        super.init();
        String basePackage = getInitParameter("base-package");
        try {
            if (basePackage != null && !basePackage.trim().isEmpty()) {
                List<Class<?>> classes = FindClassesByAnnotation.find(basePackage, Controller.class);

                for (Class<?> c : classes) {
                    controllers.add(c.getName());

                    for (Method m : c.getDeclaredMethods()) {
                        if (m.isAnnotationPresent(UrlMapping.class)) {
                            UrlMapping annotation = m.getAnnotation(UrlMapping.class);

                            String key = buildKey(annotation.url(), annotation.method());

                            if (mappings.containsKey(key)) {
                                Mapping existing = mappings.get(key);
                                throw new DuplicateMappingException(
                                    "URL dupliquee : \"" + annotation.url() + "\" avec la methode "
                                    + annotation.method() + " est deja mappee sur "
                                    + existing.getController() + "." + existing.getMethod()
                                    + " (conflit avec " + c.getSimpleName() + "." + m.getName() + ")"
                                );
                            }

                            Mapping map = new Mapping();
                            map.setUrl(annotation.url());
                            map.setController(c.getSimpleName());
                            map.setMethod(m.getName());
                            map.setHttpMethode(annotation.method());
                            mappings.put(key, map);
                        }
                    }
                }
            }
        } catch (DuplicateMappingException e) {
            // On relance directement pour garder un message clair au demarrage
            throw new ServletException(e.getMessage(), e);
        } catch (Exception e) {
            throw new ServletException("Error while scanning package : " + basePackage, e);
        }
    }

    private String buildKey(String url, HttpMethode method) {
        return url + "_" + method;
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

        String path = request.getRequestURI().substring(request.getContextPath().length());
        String httpMethod = request.getMethod(); // "GET" ou "POST"

        String key = path + "_" + httpMethod;
        Mapping map = mappings.get(key);

        if (map != null) {
            out.println("URL supportee");
            out.println("--------------------");
            out.println("URL        : " + map.getUrl());
            out.println("Methode    : " + map.getHttpMethode());
            out.println("Controller : " + map.getController());
            out.println("Methode J. : " + map.getMethod());
        } else {
            out.println("Je ne connais pas cette URL (ou cette methode HTTP)");
            out.println();
            out.println("Liste des URLs supportees");
            for (Mapping m : mappings.values()) {
                out.println(m.getHttpMethode() + " " + m.getUrl() + " --> " + m.getController() + " --> " + m.getMethod());
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}