package fr.valquev.mypov;

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

    String BASE_URL = "https://mypov.fr/api/";
    String LOGIN = "connexion.php";
    String REGISTER = "inscription.php";
    String GET_OBSERVATIONS = "getObservations.php";
    String GET_LISTE_OBSERVATIONS = "getListeObservations.php";
    String GET_COMMENTAIRES = "getCommentaires.php";
    String ADD_COMMENTAIRE = "addCommentaire.php";
    String ADD_OBSERVATION = "addObservation.php";
    String DELETE_OBSERVATION = "delObservation.php";
    String ADD_PHOTO_OBSERVATION = "addPhotoObservation.php";

    MyPOVClient client = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build().create(MyPOVClient.class);

    @FormUrlEncoded
    @POST(LOGIN)
    Call<MyPOVResponse<User>> login(@Field("mail") String mail, @Field("pwd") String password);

    @FormUrlEncoded
    @POST(REGISTER)
    Call<MyPOVResponse<User>> register(@Field("pseudo") String pseudo, @Field("mail") String mail, @Field("pwd") String password1, @Field("pwd2") String password2);

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
    Call<MyPOVResponse<String>> addObservation(@Field("nom") String nom, @Field("description") String description, @Field("date") long date, @Field("lat") double lat, @Field("lng") double lng, @Field("mail") String mail, @Field("pwd") String password);

    @Multipart
    @POST(ADD_PHOTO_OBSERVATION)
    Call<MyPOVResponse<String>> addPhotoObservation(@Part("img\"; filename=\"image.jpeg\" ") RequestBody file, @Part("id_obs") int id_obs, @Part("mail") String mail, @Part("pwd") String password);

    @FormUrlEncoded
    @POST(DELETE_OBSERVATION)
    Call<MyPOVResponse<String>> deleteObservation(@Field("id_obs") int id, @Field("mail") String mail, @Field("pwd") String password);
}
