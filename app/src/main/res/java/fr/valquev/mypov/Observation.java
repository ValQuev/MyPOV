package java.fr.valquev.mypov;

/**
 * Created by ValQuev on 17/09/15.
 */
public class Observation {

    private int id;
    //private long publidate;
    private double lat;
    private double lng;
    private int id_user;
    private String nom;
    private String text;

    public int getId() {
        return id;
    }

    /*public long getPublidate() {
        return publidate;
    }*/

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public int getId_user() {
        return id_user;
    }

    public String getNom() {
        return nom;
    }

    public String getText() {
        return text;
    }
}
