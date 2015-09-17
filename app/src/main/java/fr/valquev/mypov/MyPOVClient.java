package fr.valquev.mypov;

import java.util.List;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by ValQuev on 17/09/15.
 */
public interface MyPOVClient {

    String BASE_URL = "https://mypov.fr/api/";

    MyPOVClient client = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build().create(MyPOVClient.class);

    @FormUrlEncoded
    @POST("test.php")
    Call<MyPOVResponse<List<Observation>>> getObservations();
}
