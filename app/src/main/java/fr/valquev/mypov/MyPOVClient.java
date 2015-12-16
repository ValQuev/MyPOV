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

    MyPOVClient client = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build().create(MyPOVClient.class);

    @FormUrlEncoded
    @POST("connexion.php")
    Call<MyPOVResponse<User>> login(@Field("mail") String mail, @Field("pwd") String password);

    @FormUrlEncoded
    @POST("getObservations.php")
    Call<MyPOVResponse<List<Observation>>> getObservations(@Field("lat") double lat, @Field("lng") double lng, @Field("distance") int distance, @Field("mail") String mail, @Field("pwd") String password);

    @FormUrlEncoded
    @POST("getListeObservations.php")
    Call<MyPOVResponse<List<Observation>>> getListeObservations(@Field("lat") double lat, @Field("lng") double lng, @Field("page") int page, @Field("tri") String tri, @Field("mail") String mail, @Field("pwd") String password);

    @FormUrlEncoded
    @POST("getCommentaires.php")
    Call<MyPOVResponse<List<Comment>>> getComments(@Field("id_obs") int id_obs, @Field("mail") String mail, @Field("pwd") String password);

    @FormUrlEncoded
    @POST("addCommentaire.php")
    Call<MyPOVResponse<String>> addComment(@Field("id_obs") int id_obs, @Field("texte") String texte, @Field("mail") String mail, @Field("pwd") String password);

    @FormUrlEncoded
    @POST("addObservation.php")
    Call<MyPOVResponse<String>> addObservation(@Field("nom") String nom, @Field("description") String description, @Field("lat") double lat, @Field("lng") double lng, @Field("mail") String mail, @Field("pwd") String password);

    @Multipart
    @POST("addPhotoObservation.php")
    Call<MyPOVResponse<String>> addPhotoObservation(@Part("img\"; filename=\"image.jpeg\" ") RequestBody file, @Part("id_obs") int id_obs, @Part("mail") String mail, @Part("pwd") String password);
}
