package mg.itu.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import mg.itu.annotation.RestAPI;
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

        boolean isRestAPI =
                method.isAnnotationPresent(RestAPI.class);

        try {

            Object controller =
                    mapping.getControllerClass()
                            .getDeclaredConstructor()
                            .newInstance();

            Object result =
                    method.invoke(controller);

            // Cas 1 : @RestAPI -> JSON, quel que soit le type retourne
            if (isRestAPI) {

                response.setContentType(
                        "application/json;charset=UTF-8");

                Gson gson =
                        new Gson();

                String json =
                        gson.toJson(result);

                PrintWriter out =
                        response.getWriter();

                out.print(json);
                out.flush();

                return;

            }

            // Cas 2 : ModelAndView -> forward vers la JSP
            if (result instanceof ModelAndView) {

                ModelAndView mv =
                        (ModelAndView) result;

                addAttributesToRequest(
                        request,
                        mv.getData());

                String prefix =
                        context.getInitParameter("view-prefix");

                String suffix =
                        context.getInitParameter("view-suffix");

                String viewPath =
                        buildViewPath(
                                prefix,
                                mv.getView(),
                                suffix);

                RequestDispatcher dispatcher =
                        request.getRequestDispatcher(viewPath);

                dispatcher.forward(request, response);

                return;

            }

            // Cas 3 : compatibilite avec les anciens controleurs (String, void, etc.)
            response.setContentType(
                    "text/plain;charset=UTF-8");

            PrintWriter out =
                    response.getWriter();

            out.print(
                    result == null ? "" : result.toString());

            out.flush();

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
