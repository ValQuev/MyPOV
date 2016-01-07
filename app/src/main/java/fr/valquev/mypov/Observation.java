package fr.valquev.mypov;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ValQuev on 17/09/15.
 */
public class Observation implements Parcelable {

    private int id;
    private long publidate;
    private double lat;
    private double lng;
    private double distance;
    private String nom;
    private String description;
    private int nb_commentaires;
    private Observateur observateur;
    private Notes notes;
    private List<ObservationPhoto> photos;

    public Observation(Parcel parcel) {
        id = parcel.readInt();
        publidate = parcel.readLong();
        lat = parcel.readDouble();
        lng = parcel.readDouble();
        distance = parcel.readDouble();
        nom = parcel.readString();
        description = parcel.readString();
        nb_commentaires = parcel.readInt();
        observateur = parcel.readParcelable(Observateur.class.getClassLoader());
        notes = parcel.readParcelable(Notes.class.getClassLoader());
        photos = new ArrayList<>();
        parcel.readList(photos, getClass().getClassLoader());
    }

    public int getId() {
        return id;
    }

    public long getDate() {
        return publidate;
    }

    public String getSuperbDate() {
        return new SimpleDateFormat("dd/MM/yy").format(new Date(getDate() * 1000));
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public double getDistance() {
        return distance;
    }

    public String getSuperbDistance() {
        return String.format("%.1f", getDistance()) + " km";
    }

    public String getNom() {
        return nom;
    }

    public String getDescription() {
        return description;
    }

    public int getNb_commentaires() {
        return nb_commentaires;
    }

    public void setNb_commentaires(int nb_commentaires) {
        this.nb_commentaires = nb_commentaires;
    }

    public Observateur getObservateur() {
        return observateur;
    }

    public List<ObservationPhoto> getPhotos() {
        return photos;
    }

    public Notes getNotes() {
        return notes;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeLong(publidate);
        parcel.writeDouble(lat);
        parcel.writeDouble(lng);
        parcel.writeDouble(distance);
        parcel.writeString(nom);
        parcel.writeString(description);
        parcel.writeInt(nb_commentaires);
        parcel.writeParcelable(observateur, i);
        parcel.writeParcelable(notes, i);
        parcel.writeList(photos);
    }

    public static final Parcelable.Creator<Observation> CREATOR = new Parcelable.Creator<Observation>() {
        @Override
        public Observation createFromParcel(Parcel source) {
            return new Observation(source);
        }

        @Override
        public Observation[] newArray(int size) {
            return new Observation[size];
        }
    };

    public static Parcelable.Creator<Observation> getCreator() {
        return CREATOR;
    }
}
