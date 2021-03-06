package com.example.geniusplaza.googlecloudvisionsample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.geniusplaza.googlecloudvisionsample.POJO.Result;
import com.example.geniusplaza.googlecloudvisionsample.POJO.WordContent;
import com.example.geniusplaza.googlecloudvisionsample.Retrofit.RestClient;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DictionaryActivity extends AppCompatActivity {

    TextView meaning, sentence;
    List<Result> definitionArray, exampleArray;
    List<String> definition, example;
    int counter =0;
    DatabaseHandler databaseHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);
        meaning = (TextView)findViewById(R.id.textViewDefinition);
        sentence = (TextView)findViewById(R.id.textViewExample);
        definition = new ArrayList<String>();
        example = new ArrayList<String>();
        exampleArray= new ArrayList<Result>();
        definitionArray = new ArrayList<Result>();
        databaseHandler = new DatabaseHandler(this);
        Log.d("Word looking up", MainActivity.imageArray.get(0));

        RestClient.getExampleApi().wordLookup(MainActivity.imageArray.get(0)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new io.reactivex.Observer<WordContent>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d("inside thw call", " success");
            }

            @Override
            public void onNext(WordContent value) {
                Log.d("checking the api", value.toString());
                definitionArray = value.getResults();
                counter = 0;
                for(Result result: definitionArray){
                    if(result.getExamples() != null){
                        definition.add(result.getDefinition());
                        definition.add("\n");
                        counter = 1;
                    }
                    if (counter == 0){
                        definition.add(result.getDefinition());
                        definition.add("\n");
                    }
                }
                for(Result result: definitionArray){
                    if(result.getExamples() != null){
                        example.add(result.getExamples().toString());
                        example.add("\n");
                    }
                }
                meaning.setText(definition.toString());
                sentence.setText(example.toString());

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {

            }
        });
    }
    public void addToDatabaseButtonClicked(View v){
        byte[] holderString = DBBitmapUtility.getBytes(MainActivity.bitmap);
        databaseHandler.addWordInfo(MainActivity.imageArray.get(0), meaning.getText().toString(), sentence.getText().toString(),holderString);
        Toast.makeText(getApplicationContext(),"Add to database Successful",Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, WordsDatabase.class);
        startActivity(i);

    }
}
