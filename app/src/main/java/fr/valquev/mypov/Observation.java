package fr.valquev.mypov;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
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
    private Observateur observateur;
    private List<ObservationPhoto> photos;

    public Observation(Parcel parcel) {
        id = parcel.readInt();
        publidate = parcel.readLong();
        lat = parcel.readDouble();
        lng = parcel.readDouble();
        distance = parcel.readDouble();
        nom = parcel.readString();
        description = parcel.readString();
        observateur = parcel.readParcelable(Observateur.class.getClassLoader());
        photos = new ArrayList<>();
        parcel.readList(photos, getClass().getClassLoader());
    }

    public int getId() {
        return id;
    }

    public long getPublidate() {
        return publidate;
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
        return String.format("%.1f", distance) + " km";
    }

    public String getNom() {
        return nom;
    }

    public String getDescription() {
        return description;
    }

    public Observateur getObservateur() {
        return observateur;
    }

    public List<ObservationPhoto> getPhotos() {
        return photos;
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
        parcel.writeParcelable(observateur, PARCELABLE_WRITE_RETURN_VALUE);
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
