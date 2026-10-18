package mg.itu.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.*;

import mg.itu.http.HttpMethode;
import mg.itu.http.MapFile;
import mg.itu.mapping.Mapping;
import mg.itu.mapping.UrlMethode;
import mg.itu.util.ObjectBinder;
import mg.itu.view.ModelAndView;
import com.google.gson.Gson;
import mg.itu.annotation.RestAPI;

// Sprint 8 : sans cette annotation, le conteneur ne decode pas les
// requetes multipart/form-data (upload) et getParts() est inutilisable.
// Limites modifiables dans web.xml avec <multipart-config>.
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 10L * 1024 * 1024,
        maxRequestSize = 50L * 1024 * 1024)
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

        String path =
                request.getRequestURI()
                        .substring(
                                request.getContextPath()
                                        .length());

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

            // Sprint 7/8 : on construit les arguments a partir des
            // parametres de la requete (formulaire, query string, fichiers)
            Object[] arguments =
                    buildArguments(
                            request,
                            method);

            Object result =
                    method.invoke(controller, arguments);

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

    /**
     * Sprint 8 - Binding : chaque parametre de la methode du controleur
     * est rempli soit directement (String, int, MapFile...), soit par un
     * objet construit a partir des champs "nomParametre.champ" du
     * formulaire, soit par une liste (nomParametre[0].champ).
     * getParameterizedType() donne le type des elements d'une List<T>.
     * Necessite javac -parameters (voir jar.sh) pour avoir les vrais
     * noms de parametres.
     */
    private Object[] buildArguments(
            HttpServletRequest request,
            Method method)
            throws IOException, ServletException {

        Parameter[] parametres =
                method.getParameters();

        Object[] arguments =
                new Object[parametres.length];

        Map<String, String[]> params =
                request.getParameterMap();

        Map<String, List<MapFile>> files =
                extractFiles(request);

        for (int i = 0; i < parametres.length; i++) {

            arguments[i] =
                    ObjectBinder.bind(
                            params,
                            files,
                            parametres[i].getName(),
                            parametres[i].getType(),
                            parametres[i].getParameterizedType());

        }

        return arguments;

    }

    /**
     * Sprint 8 - Upload : transforme les parties "fichier" d'une requete
     * multipart en MapFile, rangees par nom de champ. Les champs texte
     * restent dans request.getParameterMap().
     */
    private Map<String, List<MapFile>> extractFiles(
            HttpServletRequest request)
            throws IOException, ServletException {

        Map<String, List<MapFile>> files =
                new HashMap<>();

        String contentType =
                request.getContentType();

        if (contentType == null
                || !contentType.toLowerCase().startsWith("multipart/")) {
            return files;
        }

        for (Part part : request.getParts()) {

            String nomFichier =
                    part.getSubmittedFileName();

            // null : champ texte ; vide : input file sans fichier choisi
            if (nomFichier == null || nomFichier.isEmpty()) {
                continue;
            }

            MapFile fichier =
                    new MapFile(
                            nomFichier,
                            readBytes(part));

            List<MapFile> liste =
                    files.get(part.getName());

            if (liste == null) {
                liste = new ArrayList<>();
                files.put(part.getName(), liste);
            }

            liste.add(fichier);

        }

        return files;

    }

    private byte[] readBytes(Part part)
            throws IOException {

        try (InputStream in = part.getInputStream()) {

            ByteArrayOutputStream out =
                    new ByteArrayOutputStream();

            byte[] buffer =
                    new byte[8192];

            int lu;

            while ((lu = in.read(buffer)) != -1) {
                out.write(buffer, 0, lu);
            }

            return out.toByteArray();

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
