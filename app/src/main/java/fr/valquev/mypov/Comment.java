package fr.valquev.mypov;

/**
 * Created by ValQuev on 28/09/15.
 */
public class Comment {

    private int id;
    private long publidate;
    private String texte;
    private Observateur utilisateur;

    public int getId() {
        return id;
    }

    public long getPublidate() {
        return publidate;
    }

    public String getTexte() {
        return texte;
    }

    public Observateur getUtilisateur() {
        return utilisateur;
    }
}
