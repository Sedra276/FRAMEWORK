package mg.itu.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import mg.itu.http.HttpMethode;
import mg.itu.mapping.Mapping;
import mg.itu.mapping.UrlMethode;
import mg.itu.view.ModelAndView;

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

        ServletContext context =
                getServletContext();

        HashMap<UrlMethode, Mapping> mappings =
                (HashMap<UrlMethode, Mapping>)
                        context.getAttribute(
                                "mapping");

        if (mappings == null) {

            response.setContentType("text/plain");

            PrintWriter out =
                    response.getWriter();

            out.println(
                    "Aucun mapping trouve. "
                            + "Verifiez le parametre <base-package> dans web.xml");

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

        if (mapping == null) {

            response.setContentType("text/plain");

            PrintWriter out =
                    response.getWriter();

            out.println("URL inconnue : " + key);
            out.println();
            out.println("URLs disponibles :");

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

        Method method =
                mapping.getMethod();

        if (method.getReturnType() != ModelAndView.class) {

            throw new ServletException(
                    "La methode "
                            + method.getName()
                            + " doit retourner un ModelAndView");

        }

        try {

            Object controller =
                    mapping.getControllerClass()
                            .getDeclaredConstructor()
                            .newInstance();

            ModelAndView result =
                    (ModelAndView) method.invoke(controller);

            if (result == null) {

                throw new ServletException(
                        "Le ModelAndView renvoye par "
                                + method.getName()
                                + " est null");

            }

            addAttributesToRequest(
                    request,
                    result.getData());

            String prefix =
                    context.getInitParameter("view-prefix");

            String suffix =
                    context.getInitParameter("view-suffix");

            String viewPath =
                    buildViewPath(
                            prefix,
                            result.getView(),
                            suffix);

            RequestDispatcher dispatcher =
                    request.getRequestDispatcher(viewPath);

            dispatcher.forward(request, response);

        } catch (InstantiationException
                | IllegalAccessException
                | IllegalArgumentException
                | InvocationTargetException
                | NoSuchMethodException e) {

            throw new ServletException(
                    "Erreur (LcsFw) : " + e.getMessage(), e);

        }

    }

    private void addAttributesToRequest(
            HttpServletRequest request,
            Map<String, Object> data) {

        if (data == null) {
            return;
        }

        for (String attribut : data.keySet()) {

            request.setAttribute(
                    attribut,
                    data.get(attribut));

        }

    }

    private String buildViewPath(
            String prefix,
            String view,
            String suffix) {

        StringBuilder path =
                new StringBuilder("/");

        if (prefix != null && !prefix.isEmpty()) {

            path.append(prefix).append("/");

        }

        path.append(view);

        if (suffix != null && !suffix.isEmpty()) {

            path.append(".").append(suffix);

        }

        return path.toString();

    }

}