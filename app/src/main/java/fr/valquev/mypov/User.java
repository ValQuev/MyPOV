package fr.valquev.mypov;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

/**
 * Created by ValQuev on 28/09/15.
 */
public class User {

    private final String DEFAULT = "NULL";
    private final String USER_SP = "UserSP";

    private SharedPreferences mSharedPreferences;

    private int id_user;
    private String pseudo;
    private String mail;
    private String password;
    private long inscridate;

    public User(Context context) {
        mSharedPreferences = context.getSharedPreferences(MyPOV.class.getSimpleName(), Context.MODE_PRIVATE);
        if(isLogged()) {
            User user = new Gson().fromJson(mSharedPreferences.getString(USER_SP, DEFAULT), User.class);
            id_user = user.getId_user();
            pseudo = user.getPseudo();
            mail = user.getMail();
            password = user.getPassword();
            inscridate = user.getInscridate();
        }
    }

    public boolean isLogged() {
        return !mSharedPreferences.getString(USER_SP, DEFAULT).equals(DEFAULT);
    }

    public void regist(User user) {
        if (user == null) {
            mSharedPreferences.edit().putString(USER_SP, DEFAULT).apply();
        } else {
            mSharedPreferences.edit().putString(USER_SP, new Gson().toJson(user)).apply();
        }
    }

    public void logout() {
        regist(null);
    }

    public int getId_user() {
        return id_user;
    }

    public String getPseudo() {
        return pseudo;
    }

    public String getMail() {
        return mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getInscridate() {
        return inscridate;
    }
}
