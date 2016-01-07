package fr.valquev.mypov;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ValQuev on 05/01/2016.
 */
public class Notes implements Parcelable {

    private int nb_affirmations;
    private int nb_infirmations;

    public Notes(Parcel parcel) {
        nb_affirmations = parcel.readInt();
        nb_infirmations = parcel.readInt();
    }

    public int getNbAffirmations() {
        return nb_affirmations;
    }

    public int getNbInfirmations() {
        return nb_infirmations;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(nb_affirmations);
        parcel.writeInt(nb_infirmations);
    }

    public static final Parcelable.Creator<Notes> CREATOR = new Parcelable.Creator<Notes>() {
        @Override
        public Notes createFromParcel(Parcel source) {
            return new Notes(source);
        }

        @Override
        public Notes[] newArray(int size) {
            return new Notes[size];
        }
    };

    public static Parcelable.Creator<Notes> getCreator() {
        return CREATOR;
    }
}
