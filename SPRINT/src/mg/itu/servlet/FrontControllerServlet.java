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

              for(Class<?> c : classes){

    controllers.add(c.getName());

    for(Method m : c.getDeclaredMethods()){

        if(m.isAnnotationPresent(Url.class)){

            Url annotation = m.getAnnotation(Url.class);

            Mapping map = new Mapping();

            map.setUrl(annotation.value());
            map.setController(c.getSimpleName());
            map.setMethod(m.getName());

            mappings.put(annotation.value(), map);
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

   protected void processRequest(HttpServletRequest request,
                              HttpServletResponse response)
        throws ServletException, IOException {

    response.setContentType("text/plain");

    PrintWriter out = response.getWriter();

    String path = request.getRequestURI()
            .substring(request.getContextPath().length());

    Mapping map = mappings.get(path);

    if(map != null){

        out.println("URL supportee");
        out.println("--------------------");
        out.println("URL        : " + map.getUrl());
        out.println("Controller : " + map.getController());
        out.println("Methode    : " + map.getMethod());

    }else{

        out.println("Je ne connais pas cette URL");
        out.println();

        out.println("Liste des URLs supportees");

        for(Mapping m : mappings.values()){

            out.println(
                    m.getUrl()
                    +" --> "
                    +m.getController()
                    +" --> "
                    +m.getMethod()
            );
        }

    }

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