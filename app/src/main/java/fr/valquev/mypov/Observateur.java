package fr.valquev.mypov;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ValQuev on 28/09/15.
 */
public class Observateur implements Parcelable {

    private int id_user;
    private String pseudo;

    public Observateur(Parcel parcel) {
        id_user = parcel.readInt();
        pseudo = parcel.readString();
    }

    public int getId_user() {
        return id_user;
    }

    public String getPseudo() {
        return pseudo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id_user);
        parcel.writeString(pseudo);
    }

    public static final Parcelable.Creator<Observateur> CREATOR = new Parcelable.Creator<Observateur>() {
        @Override
        public Observateur createFromParcel(Parcel source) {
            return new Observateur(source);
        }

        @Override
        public Observateur[] newArray(int size) {
            return new Observateur[size];
        }
    };

    public static Parcelable.Creator<Observateur> getCreator() {
        return CREATOR;
    }
}
