package com.example.geniusplaza.googlecloudvisionsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

/**
 * Created by geniusplaza on 6/19/17.
 */

public class TranslateActivity extends AppCompatActivity {

    String englishWord, spanishTranslation;
    TextView textViewSpanish, textViewEnglish;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate);

        textViewEnglish = (TextView) findViewById(R.id.textViewEnglish);
        textViewSpanish = (TextView) findViewById(R.id.textViewSpanish);

        englishWord = MainActivity.imageArray.get(0);

        Translate.setClientId("2732d054-ecee-4d8a-9fff-86b694791093");
        Translate.setClientSecret("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9");
        //Translate.setKey("e2b92ce7627942f2aadb88eceaa1ee83");
        // Translate an English string to Hindi
        try {
            spanishTranslation = Translate.execute(englishWord, Language.SPANISH);
        } catch (Exception e) {
            e.printStackTrace();
        }
        textViewSpanish.setText(spanishTranslation);
        textViewEnglish.setText(englishWord);
        //System.out.println("Original English : " + englishString);
        //System.out.println("Translated Hindi : " + spanishTranslation);
    }
}
