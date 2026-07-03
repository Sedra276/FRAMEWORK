package mg.itu.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import mg.itu.annotation.Controller;
import mg.itu.annotation.UrlMapping;
import mg.itu.http.HttpMethode;
import mg.itu.mapping.Mapping;
import mg.itu.util.FindClassesByAnnotation;

public class FrontControllerServlet extends HttpServlet {

    private List<String> controllers = new ArrayList<>();
    private HashMap<String, Mapping> mappings = new HashMap<>();
    private HashMap<String, String> duplicateErrors = new HashMap<>();

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

                            if (mappings.containsKey(key) || duplicateErrors.containsKey(key)) {
                                String msg = "URL dupliquee : \"" + annotation.url()
                                        + "\" avec la methode " + annotation.method()
                                        + " est mappee plusieurs fois (conflit detecte sur "
                                        + c.getSimpleName() + "." + m.getName() + ")";

                                duplicateErrors.put(key, msg);
                                mappings.remove(key);
                                getServletContext().log("[FrontController] " + msg);
                                continue;
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
        } catch (Throwable e) {
           
            getServletContext().log("[FrontController] ERREUR CRITIQUE au demarrage : " + e, e);
            e.printStackTrace();
        }
    }

    private String buildKey(String url, HttpMethode method) {
        return url + "_" + method;
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getRequestURI().substring(request.getContextPath().length());
        String httpMethod = request.getMethod();
        String key = path + "_" + httpMethod;

        response.setContentType("text/plain; charset=UTF-8");

       
        if (duplicateErrors.containsKey(key)) {
            String msg = duplicateErrors.get(key);
            getServletContext().log("[FrontController] Requete bloquee : " + msg);

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            PrintWriter out = response.getWriter();
            out.println("ERREUR - URL DUPLIQUEE");
            out.println("--------------------------------");
            out.println(msg);
            return;
        }

        Mapping map = mappings.get(key);
        PrintWriter out = response.getWriter();

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