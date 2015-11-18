package fr.valquev.mypov;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ValQuev on 18/11/2015.
 */
public class ObservationPhoto implements Parcelable {

    private int id;
    private long publidate;
    private Observateur utilisateur;
    private String format;

    public ObservationPhoto(Parcel parcel) {
        id = parcel.readInt();
        publidate = parcel.readLong();
        utilisateur = parcel.readParcelable(Observateur.class.getClassLoader());
        format = parcel.readString();
    }

    public int getId() {
        return id;
    }

    public long getPublidate() {
        return publidate;
    }

    public Observateur getUtilisateur() {
        return utilisateur;
    }

    public String getFormat() {
        return format;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeLong(publidate);
        parcel.writeParcelable(utilisateur, PARCELABLE_WRITE_RETURN_VALUE);
        parcel.writeString(format);
    }

    public static final Parcelable.Creator<ObservationPhoto> CREATOR = new Parcelable.Creator<ObservationPhoto>() {
        @Override
        public ObservationPhoto createFromParcel(Parcel source) {
            return new ObservationPhoto(source);
        }

        @Override
        public ObservationPhoto[] newArray(int size) {
            return new ObservationPhoto[size];
        }
    };

    public static Parcelable.Creator<ObservationPhoto> getCreator() {
        return CREATOR;
    }
}
