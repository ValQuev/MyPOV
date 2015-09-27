package fr.valquev.mypov;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ValQuev on 17/09/15.
 */
public class Observation implements Parcelable {

    private int id;
    private long publidate;
    private double lat;
    private double lng;
    private String nom;
    private String text;
    private Observateur observateur;

    public Observation(Parcel parcel) {
        id = parcel.readInt();
        publidate = parcel.readLong();
        lat = parcel.readLong();
        lng = parcel.readLong();
        nom = parcel.readString();
        text = parcel.readString();
        observateur = parcel.readParcelable(Observateur.class.getClassLoader());
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

    public String getNom() {
        return nom;
    }

    public String getText() {
        return text;
    }

    public Observateur getObservateur() {
        return observateur;
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
        parcel.writeString(nom);
        parcel.writeString(text);
        parcel.writeParcelable(observateur, PARCELABLE_WRITE_RETURN_VALUE);
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
