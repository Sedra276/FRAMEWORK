package mg.itu.http;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Sprint 8 - Fichier recu par upload (input type="file").
 *
 * Le framework en cree une instance pour chaque champ de type MapFile
 * (parametre de methode ou attribut d'un objet) dont le champ de
 * formulaire contient un fichier.
 *
 *   filename  : nom sans extension      (cin_recto)
 *   extension : extension sans le point (jpg)
 *   bytes     : contenu du fichier
 *
 * Aucune dependance vers l'API servlet : la classe est testable seule.
 */
public class MapFile {

    private String filename;
    private String extension;
    private byte[] bytes;

    public MapFile() {
    }

    /** nomOriginal = nom envoye par le navigateur (avec extension). */
    public MapFile(String nomOriginal, byte[] bytes) {

        String nom = nomOriginal == null ? "" : nomOriginal;

        // certains navigateurs envoient le chemin complet : on le retire
        int sep = Math.max(nom.lastIndexOf('/'), nom.lastIndexOf('\\'));

        if (sep >= 0) {
            nom = nom.substring(sep + 1);
        }

        int point = nom.lastIndexOf('.');

        // pas d'extension : "readme", ".htaccess" ou "fichier."
        if (point <= 0 || point == nom.length() - 1) {
            this.filename = nom;
            this.extension = "";
        } else {
            this.filename = nom.substring(0, point);
            this.extension = nom.substring(point + 1);
        }

        this.bytes = bytes;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    /** Nom complet : filename + "." + extension. */
    public String getFullName() {
        return extension == null || extension.isEmpty()
                ? filename
                : filename + "." + extension;
    }

    public long getSize() {
        return bytes == null ? 0 : bytes.length;
    }

    /**
     * Sauvegarde sous le nom d'origine (nettoye) dans le dossier donne.
     * Un fichier existant du meme nom est ecrase : si plusieurs
     * utilisateurs peuvent envoyer "cin.jpg", utiliser saveTo(dossier, nom).
     */
    public Path saveTo(String dossier) throws IOException {
        return saveTo(dossier, filename);
    }

    /**
     * Sauvegarde sous nomBase + "." + extension (ex: "emp12_recto" ->
     * emp12_recto.jpg). Le dossier est cree s'il n'existe pas.
     * Retourne le chemin complet ecrit : a stocker en base si besoin.
     */
    public Path saveTo(String dossier, String nomBase) throws IOException {

        if (dossier == null || dossier.isEmpty()) {
            throw new IllegalArgumentException(
                    "Dossier de sauvegarde non defini");
        }

        if (bytes == null) {
            throw new IllegalStateException("Aucun contenu a sauvegarder");
        }

        String nom = sanitize(nomBase);
        String ext = extension == null
                ? ""
                : extension.replaceAll("[^A-Za-z0-9]", "");

        if (!ext.isEmpty()) {
            nom = nom + "." + ext;
        }

        Path base = Paths.get(dossier).toAbsolutePath().normalize();
        Path cible = base.resolve(nom).normalize();

        // ceinture et bretelles : le fichier doit rester dans le dossier
        if (!cible.startsWith(base)) {
            throw new SecurityException("Nom de fichier refuse : " + nom);
        }

        Files.createDirectories(base);
        Files.write(cible, bytes);

        return cible;
    }

    /** Le nom vient du navigateur : on ne garde que des caracteres sains. */
    private static String sanitize(String nom) {

        String s = nom == null ? "" : nom;

        int sep = Math.max(s.lastIndexOf('/'), s.lastIndexOf('\\'));

        if (sep >= 0) {
            s = s.substring(sep + 1);
        }

        s = s.replaceAll("[^A-Za-z0-9._-]", "_");
        s = s.replaceAll("^\\.+", ""); // pas de ".." ni de fichier cache

        return s.isEmpty() ? "file" : s;
    }

    @Override
    public String toString() {
        return getFullName() + " (" + getSize() + " octets)";
    }
}
