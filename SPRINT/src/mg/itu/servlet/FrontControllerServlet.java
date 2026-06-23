package mg.itu.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

import mg.itu.annotation.Controller;
import mg.itu.annotation.Url;
import mg.itu.mapping.Mapping;
import mg.itu.util.FindClassesByAnnotation;

public class FrontControllerServlet extends HttpServlet {

    private List<String> controllers = new ArrayList<>();

    private HashMap<String, Mapping> mappings =
            new HashMap<>();

    @Override
    public void init() throws ServletException {

        super.init();

        String basePackage =
                getInitParameter("base-package");

        try {

            if(basePackage != null &&
                    !basePackage.trim().isEmpty()) {

                List<Class<?>> classes =
                        FindClassesByAnnotation.find(
                                basePackage,
                                Controller.class);

                for(Class<?> c : classes) {

                    // Sprint 1
                    controllers.add(c.getName());

                    // Sprint 2
                    Method[] methods =
                            c.getDeclaredMethods();

                    for(Method m : methods) {

                        if(m.isAnnotationPresent(
                                Url.class)) {

                            Url url =
                                    m.getAnnotation(
                                            Url.class);

                            mappings.put(
                                    url.value(),
                                    new Mapping(
                                            c.getName(),
                                            m.getName()
                                    )
                            );
                        }
                    }
                }
            }

        } catch(Exception e) {

            throw new ServletException(
                    "Error while scanning package : "
                            + basePackage,
                    e);
        }
    }

    protected void processRequest(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");

        PrintWriter out =
                response.getWriter();

        String context =
                request.getContextPath();

        String uri =
                request.getRequestURI();

        String path =
                uri.substring(context.length());

        out.println("<html>");
        out.println("<head>");
        out.println("<title>Front Controller</title>");
        out.println("</head>");
        out.println("<body>");

        out.println("<h1>Manakory Jiaby</h1>");

        out.println("<p>URL : "
                + uri
                + "</p>");

        // Sprint 1

        out.println("<h2>Controllers :</h2>");

        if(controllers.isEmpty()) {

            out.println("<p>No controllers found.</p>");

        } else {

            out.println("<ul>");

            for(String controller
                    : controllers) {

                out.println(
                        "<li>"
                                + controller
                                + "</li>");
            }

            out.println("</ul>");
        }

        // Sprint 2

        out.println("<hr>");

        out.println(
                "<h2>Recherche URL</h2>");

        Mapping mapping =
                mappings.get(path);

        if(mapping != null) {

            out.println(
                    "<h3>URL connue</h3>");

            out.println(
                    "<p>Classe : "
                            + mapping.getClassName()
                            + "</p>");

            out.println(
                    "<p>Methode : "
                            + mapping.getMethodName()
                            + "</p>");

        } else {

            out.println(
                    "<h3>Je ne connais pas cette URL</h3>");

            out.println(
                    "<h4>URLs connues :</h4>");

            out.println("<ul>");

            for(String url :
                    mappings.keySet()) {

                Mapping m =
                        mappings.get(url);

                out.println(
                        "<li>"
                                + url
                                + " -> "
                                + m.getClassName()
                                + "."
                                + m.getMethodName()
                                + "()"
                                + "</li>");
            }

            out.println("</ul>");
        }

        out.println("</body>");
        out.println("</html>");
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        processRequest(
                request,
                response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        processRequest(
                request,
                response);
    }
}