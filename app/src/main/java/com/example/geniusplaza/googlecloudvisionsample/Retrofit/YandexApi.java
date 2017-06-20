package com.example.geniusplaza.googlecloudvisionsample.Retrofit;

/**
 * Created by geniusplaza on 6/20/17.
 */

import com.example.geniusplaza.googlecloudvisionsample.POJO.TranslatePOJO;
import com.memetix.mst.translate.Translate;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import static com.google.api.client.http.HttpMethods.GET;

public interface YandexApi{

@GET("api/translate")
Call<TranslatePOJO> getSpanishTranslation(@Query("text") String text, @Query("from") String from, @Query("to") String to);
}