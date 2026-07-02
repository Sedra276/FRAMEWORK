package mg.itu.mapping;

import java.util.Objects;

import mg.itu.http.HttpMethode;

public class UrlMethode {

    private String url;

    private HttpMethode methode;

    public UrlMethode() {
    }

    public UrlMethode(String url,
                      HttpMethode methode) {

        this.url = url;
        this.methode = methode;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HttpMethode getMethode() {
        return methode;
    }

    public void setMethode(HttpMethode methode) {
        this.methode = methode;
    }

    @Override
    public boolean equals(Object obj) {

        if(this == obj)
            return true;

        if(obj == null)
            return false;

        if(getClass() != obj.getClass())
            return false;

        UrlMethode other =
                (UrlMethode) obj;

        return Objects.equals(url, other.url)
                &&
                methode == other.methode;
    }

    @Override
    public int hashCode() {

        return Objects.hash(
                url,
                methode
        );

    }

    @Override
    public String toString() {

        return methode
                + " "
                + url;

    }

}