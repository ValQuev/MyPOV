package fr.valquev.mypov;

import com.google.gson.JsonElement;
import com.squareup.okhttp.RequestBody;

import java.util.List;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Query;

/**
 * Created by ValQuev on 17/09/15.
 */
public interface MyPOVClient {

    String TEST = "_test";
    String BASE_URL = "https://mypov.fr/api" + TEST + "/";
    String LOGIN = "connexion.php";
    String REGISTER = "inscription.php";
    String DEL_ACCOUNT = "delUser.php";
    String UPDATE_PASSWORD = "setPassword.php";
    String GET_OBSERVATIONS = "getObservations.php";
    String GET_LISTE_OBSERVATIONS = "getListeObservations.php";
    String GET_COMMENTAIRES = "getCommentaires.php";
    String ADD_COMMENTAIRE = "addCommentaire.php";
    String ADD_OBSERVATION = "addObservation.php";
    String SET_OBSERVATION = "setObservation.php";
    String DELETE_OBSERVATION = "delObservation.php";
    String ADD_PHOTO_OBSERVATION = "addPhotoObservation.php";
    String GET_NOTE_OBSERVATION = "getNoteObservation.php";
    String SET_NOTE_OBSERVATION = "setNote.php";
    String ADD_NOTE_OBSERVATION = "addNote.php";
    String DEL_NOTE_OBSERVATION = "delNote.php";
    String DEL_PHOTO_OBSERVATION = "delPhotoObservation.php";

    MyPOVClient client = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build().create(MyPOVClient.class);

    @FormUrlEncoded
    @POST(LOGIN)
    Call<MyPOVResponse<User>> login(@Field("mail") String mail, @Field("pwd") String password);

    @FormUrlEncoded
    @POST(REGISTER)
    Call<MyPOVResponse<User>> register(@Field("pseudo") String pseudo, @Field("mail") String mail, @Field("pwd") String password1, @Field("pwd2") String password2);

    @FormUrlEncoded
    @POST(DEL_ACCOUNT)
    Call<MyPOVResponse<String>> delAccount(@Field("mail") String mail, @Field("pwd") String password);

    @FormUrlEncoded
    @POST(UPDATE_PASSWORD)
    Call<MyPOVResponse<String>> updatePassword(@Field("mail") String mail, @Field("pwd") String password, @Field("oldpwd") String oldpwd, @Field("newpwd") String newpwd, @Field("newpwd2") String newpwd2);

    @FormUrlEncoded
    @POST(GET_OBSERVATIONS)
    Call<MyPOVResponse<List<Observation>>> getObservations(@Field("lat") double lat, @Field("lng") double lng, @Field("distance") int distance, @Field("mail") String mail, @Field("pwd") String password);

    @FormUrlEncoded
    @POST(GET_LISTE_OBSERVATIONS)
    Call<MyPOVResponse<List<Observation>>> getListeObservations(@Field("lat") double lat, @Field("lng") double lng, @Field("page") int page, @Field("id_user") int id_user, @Field("tri") String tri, @Field("mail") String mail, @Field("pwd") String password);

    @FormUrlEncoded
    @POST(GET_COMMENTAIRES)
    Call<MyPOVResponse<List<Comment>>> getComments(@Field("id_obs") int id_obs, @Field("mail") String mail, @Field("pwd") String password);

    @FormUrlEncoded
    @POST(ADD_COMMENTAIRE)
    Call<MyPOVResponse<String>> addComment(@Field("id_obs") int id_obs, @Field("texte") String texte, @Field("mail") String mail, @Field("pwd") String password);

    @FormUrlEncoded
    @POST(ADD_OBSERVATION)
    Call<MyPOVResponse<Observation>> addObservation(@Field("nom") String nom, @Field("description") String description, @Field("date") long date, @Field("lat") double lat, @Field("lng") double lng, @Field("mail") String mail, @Field("pwd") String password);

    @Multipart
    @POST(ADD_PHOTO_OBSERVATION)
    Call<MyPOVResponse<String>> addPhotoObservation(@Part("img\"; filename=\"image.jpeg\" ") RequestBody file, @Part("id_obs") int id_obs, @Part("mail") String mail, @Part("pwd") String password);

    @FormUrlEncoded
    @POST(DELETE_OBSERVATION)
    Call<MyPOVResponse<String>> deleteObservation(@Field("id_obs") int id_obs, @Field("mail") String mail, @Field("pwd") String password);

    @FormUrlEncoded
    @POST(GET_NOTE_OBSERVATION)
    Call<MyPOVResponse<List<Note>>> getNoteObservation(@Field("id_obs") int id_obs, @Field("mail") String mail, @Field("pwd") String password);


    @FormUrlEncoded
    @POST(SET_NOTE_OBSERVATION)
    Call<MyPOVResponse<String>> setNoteObservation(@Field("id_obs") int id_obs, @Field("id_user") int id_user, @Field("note") int note, @Field("mail") String mail, @Field("pwd") String password);


    @FormUrlEncoded
    @POST(ADD_NOTE_OBSERVATION)
    Call<MyPOVResponse<String>> addNoteObservation(@Field("id_obs") int id_obs, @Field("id_user") int id_user, @Field("note") int note, @Field("mail") String mail, @Field("pwd") String password);


    @FormUrlEncoded
    @POST(DEL_NOTE_OBSERVATION)
    Call<MyPOVResponse<String>> delNoteObservation(@Field("id_obs") int id_obs, @Field("id_user") int id_user, @Field("mail") String mail, @Field("pwd") String password);

    @FormUrlEncoded
    @POST(SET_OBSERVATION)
    Call<MyPOVResponse<Observation>> setObservation(@Field("id_obs") int id_obs, @Field("lat") double lat, @Field("lng") double lng, @Field("nom") String nom, @Field("description") String description, @Field("date") long date, @Field("mail") String mail, @Field("pwd") String password);

    @FormUrlEncoded
    @POST(DEL_PHOTO_OBSERVATION)
    Call<MyPOVResponse<String>> deletePic(@Field("id_photo") int id_photo, @Field("mail") String mail, @Field("pwd") String password);
}
