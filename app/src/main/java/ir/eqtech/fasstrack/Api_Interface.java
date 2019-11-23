package ir.eqtech.fasstrack;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface Api_Interface {

        @POST("/timetable/123")
        Call<ReceivingData> getStringScalar(@Body SendingData body);

   /* String URL_BASE = "http://34.254.196.82:5000/";

    @Headers("Content-Type: application/json")
    @POST("login")
    Call<User> getUser(@Body String body);*/

}
