package mg.itu.mapping;

import mg.itu.http.HttpMethode;

public class Mapping {

    private String url;
    private String controller;
    private String method;
    private HttpMethode httpMethode;

    public Mapping() {
    }

    public Mapping(String url, String controller, String method, HttpMethode httpMethode) {
        this.url = url;
        this.controller = controller;
        this.method = method;
        this.httpMethode = httpMethode;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getController() {
        return controller;
    }

    public void setController(String controller) {
        this.controller = controller;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public HttpMethode getHttpMethode() {
        return httpMethode;
    }

    public void setHttpMethode(HttpMethode httpMethode) {
        this.httpMethode = httpMethode;
    }
}