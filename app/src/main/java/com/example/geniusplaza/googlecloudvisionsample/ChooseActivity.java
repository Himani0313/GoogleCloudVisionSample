package com.example.geniusplaza.googlecloudvisionsample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class ChooseActivity extends AppCompatActivity {

    public static String optSelected;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);

    }
    public void objectButtonClicked (View v){
        Intent i = new Intent(this, MainActivity.class);
        optSelected = "o";
        startActivity(i);
    }
    public void textButtonClicked (View v){
        Intent i = new Intent(this, MainActivity.class);
        optSelected = "t";
        startActivity(i);
    }
    public void landmarkButtonClicked (View v){
        Intent i = new Intent(this, MainActivity.class);
        optSelected = "l";
        startActivity(i);
    }
}
