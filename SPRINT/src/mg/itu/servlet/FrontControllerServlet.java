package mg.itu.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import mg.itu.http.HttpMethode;
import mg.itu.mapping.Mapping;
import mg.itu.mapping.UrlMethode;

public class FrontControllerServlet
        extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        processRequest(req, resp);
    }

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        processRequest(req, resp);
    }

    protected void processRequest(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain");

        PrintWriter out =
                response.getWriter();

        ServletContext context =
                getServletContext();

        HashMap<UrlMethode, Mapping> mappings =
                (HashMap<UrlMethode, Mapping>)
                        context.getAttribute(
                                "mapping");

        if (mappings == null) {

            out.println("Aucun mapping.");

            return;

        }

        String path =
                request.getRequestURI()
                        .substring(
                                request.getContextPath()
                                        .length());

        HttpMethode httpMethod =
                HttpMethode.valueOf(
                        request.getMethod());

        UrlMethode key =
                new UrlMethode(
                        path,
                        httpMethod);

        Mapping mapping =
                mappings.get(key);
                out.println("========== DEBUG ==========");
out.println("Méthode HTTP : " + request.getMethod());
out.println("Path : " + path);
out.println("Clé recherchée : " + key);
out.println();

out.println("Toutes les clés :");
for (UrlMethode u : mappings.keySet()) {
    out.println(u);
}
out.println("===========================");

        if (mapping == null) {

            out.println("URL inconnue\n");

            out.println("URLs disponibles :\n");

            for (UrlMethode url :
                    mappings.keySet()) {

                Mapping m =
                        mappings.get(url);

                out.println(
                        url.getMethode()
                                + " "
                                + url.getUrl()
                                + " -> "
                                + m);

            }

            return;

        }

        try {

            Object controller =
                    mapping.getControllerClass()
                            .getDeclaredConstructor()
                            .newInstance();

            Method method =
                    mapping.getMethod();

            Object result =
                    method.invoke(controller);

            if (result != null) {

                out.println(result);

            }

        } catch (Exception e) {

            throw new ServletException(e);

        }

    }

}