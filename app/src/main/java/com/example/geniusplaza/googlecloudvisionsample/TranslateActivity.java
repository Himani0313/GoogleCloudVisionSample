package com.example.geniusplaza.googlecloudvisionsample;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.geniusplaza.googlecloudvisionsample.Retrofit.YandexApi;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.example.geniusplaza.googlecloudvisionsample.POJO.TranslatePOJO;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

/**
 * Created by geniusplaza on 6/19/17.
 */

public class TranslateActivity extends AppCompatActivity {

    String englishWord;
    TextView textViewSpanish, textViewEnglish;
    public String spanishTranslation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate);
        englishWord = MainActivity.imageArray.get(0);


        textViewSpanish = (TextView) findViewById(R.id.textViewSpanish);
        textViewEnglish = (TextView) findViewById(R.id.textViewEnglish);
        textViewEnglish.setText(englishWord);

        if (englishWord != null) {

            Toast.makeText(this, "Getting Translations",
                    Toast.LENGTH_LONG).show();

            new SaveTheFeed().execute();

        } else {

            Toast.makeText(this, "Enter Words to Translate",
                    Toast.LENGTH_SHORT).show();

        }
    }

    class SaveTheFeed extends AsyncTask<Void, Void, Void> {

        String jsonString = "";
        String result = "";

        @Override
        protected Void doInBackground(Void... voids) {

            //EditText translateEditText = (EditText) findViewById(R.id.editText);

            // Get the text from EditText
            String wordsToTranslate = englishWord;

            wordsToTranslate = wordsToTranslate.replace(" ", "+");

            DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());

            HttpPost httpPost = new HttpPost("http://newjustin.com/translateit.php?action=translations&english_words=" + wordsToTranslate);
            Log.d("URL: ", httpPost.toString());

            httpPost.setHeader("Content-type", "application/json");

            InputStream inputStream = null;

            try {

                HttpResponse response = httpClient.execute(httpPost);

                HttpEntity entity = response.getEntity();

                inputStream = entity.getContent();

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);

                StringBuilder sb = new StringBuilder();

                String line = null;

                while ((line = reader.readLine()) != null) {

                    sb.append(line + "\n");

                }

                jsonString = sb.toString();

                JSONObject jObject = new JSONObject(jsonString);

                JSONArray jArray = jObject.getJSONArray("translations");
                JSONObject translationObject = jArray.getJSONObject(9);
                Log.d("holllla",translationObject.getString("spanish"));
                Log.d("jArray value: ", jArray.toString());
                Log.d("trryyyyyyyyyy", jArray.get(9).toString());
                spanishTranslation = translationObject.getString("spanish");
                //outputTranslations(jArray.get(9).toString());

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            textViewSpanish = (TextView) findViewById(R.id.textViewSpanish);
            textViewSpanish.setText(spanishTranslation);
            // textViewSpanish = (TextView) findViewById(R.id.textViewSpanish);
//            Log.d("Result value: ", result);
//            textViewSpanish.setText(result);

        }
    }
}

//        protected void outputTranslations(String temp) {
//
////                String[] languages = {"spanish"};
////
////                try {
////
////                    for (int i = 0; i < jsonArray.length(); i++) {
////
////                        JSONObject translationObject =
////                                jsonArray.getJSONObject(i);
////                        Log.d("Translation Object: ", translationObject.toString());
////
////                        result = result + languages[0] + " : " +
////                                translationObject.getString(languages[i]) + "\n";
////
////                    }
////
////                } catch (JSONException e) {
////                    e.printStackTrace();
////                }
////
////            }
//            textViewSpanish.setText(temp);
//        }
//    }



