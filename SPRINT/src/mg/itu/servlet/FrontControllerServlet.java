package mg.itu.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import mg.itu.annotation.Controller;
import mg.itu.util.FindClassesByAnnotation;

public class FrontControllerServlet extends HttpServlet {

    private List<String> controllers = new ArrayList<>();

    @Override
    public void init() throws ServletException {
        super.init();
        String basePackage = getInitParameter("base-package");
        try {
            if (basePackage != null && !basePackage.trim().isEmpty()) {
                List<Class<?>> classes = FindClassesByAnnotation.find(basePackage, Controller.class);
                for (Class<?> c : classes) {
                    controllers.add(c.getName());
                }
            }
        } catch (Exception e) {
            throw new ServletException("Error while scanning package: " + basePackage, e);
        }
    }

    protected void processRequest(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        out.println("<html><head><title>Front Controller</title></head><body>");
        out.println("<h1>Manakory Jiaby - Sprint 1</h1>");
        out.println("<p>URL : " + request.getRequestURI() + "</p>");

        out.println("<h2>Controllers :</h2>");
        if (controllers.isEmpty()) {
            out.println("<p>No controllers found.</p>");
        } else {
            out.println("<ul>");
            for (String controller : controllers) {
                out.println("<li>" + controller + "</li>");
            }
            out.println("</ul>");
        }

        out.println("</body></html>");
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}