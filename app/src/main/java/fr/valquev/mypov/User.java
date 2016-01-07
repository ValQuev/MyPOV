package fr.valquev.mypov;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ValQuev on 28/09/15.
 */
public class User {

    private final String DEFAULT = "NULL";
    private final String USER_SP = "UserSP";
    private final String USER_TRI = "UserTRI";
    private final String USER_FILTER = "UserFILTER";
    private final String USER_POSITION = "UserPOSITION";
    private final String USER_ZOOM = "UserZOOM";

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

    public void updatePassword(String password) {
        if(isLogged()) {
            try {
                mSharedPreferences.edit().putString(USER_SP, String.valueOf(new JSONObject(mSharedPreferences.getString(USER_SP, DEFAULT)).put("password", password))).apply();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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

    public void setPreferedTri(String tri) {
        mSharedPreferences.edit().putString(USER_TRI, tri).apply();
    }

    public void setPreferedFilter(boolean me) {
        mSharedPreferences.edit().putBoolean(USER_FILTER, me).apply();
    }

    public String getPreferedTri() {
        return mSharedPreferences.getString(USER_TRI, "distance");
    }

    public boolean getPreferedFilter() {
        return mSharedPreferences.getBoolean(USER_FILTER, false);
    }

    public LatLng getLastLatLng() {
        return new LatLng(Double.parseDouble(mSharedPreferences.getString(USER_POSITION, "0::0").split("::")[0]), Double.parseDouble(mSharedPreferences.getString(USER_POSITION, "0::0").split("::")[1]));
    }

    public void setLastLatLng(LatLng position) {
        mSharedPreferences.edit().putString(USER_POSITION, position.latitude + "::" + position.longitude).apply();
    }

    public float getZoom() {
        return mSharedPreferences.getFloat(USER_ZOOM, 15);
    }

    public void setZoom(float zoom) {
        mSharedPreferences.edit().putFloat(USER_ZOOM, zoom).apply();
    }
}
