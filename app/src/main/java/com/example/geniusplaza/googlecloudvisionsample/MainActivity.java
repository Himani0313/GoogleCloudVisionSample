package com.example.geniusplaza.googlecloudvisionsample;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String CLOUD_VISION_API_KEY = "AIzaSyDGE-zwAF7TkOhCwIRCXWd8TrR0fRsbnko";
    public static final String FILE_NAME = "temp.jpg";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    Button translate, dictionaryButton;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;
    public static List<String> spanishString;
    public static List<String> imageArray;
    private TextView mImageDetails;
    private ImageView mMainImage;
    ProgressBar mProgressBar;
    public static Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar_image_search);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder
                        .setMessage(R.string.dialog_select_prompt)
                        .setPositiveButton(R.string.dialog_select_gallery, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startGalleryChooser();

                            }
                        })
                        .setNegativeButton(R.string.dialog_select_camera, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startCamera();
                            }
                        });
                builder.create().show();
            }
        });

        mImageDetails = (TextView) findViewById(R.id.image_details);
        mMainImage = (ImageView) findViewById(R.id.main_image);
        translate = (Button) findViewById(R.id.buttonTranslate);
        dictionaryButton = (Button)findViewById(R.id.buttonDictionary);
    }

    public void startGalleryChooser() {
        //rxPermissions = new RxPermissions(this);

        if (PermissionUtils.requestPermission(this, GALLERY_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent intent = new Intent();
            intent.setType("image*//*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select a photo"),
                    GALLERY_IMAGE_REQUEST);
        }
    }

    public void startCamera() {
        if (PermissionUtils.requestPermission(
                this,
                CAMERA_PERMISSIONS_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }
    }

    public File getCameraFile() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            uploadImage(data.getData());
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            uploadImage(photoUri);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, CAMERA_PERMISSIONS_REQUEST, grantResults)) {
                    startCamera();
                }
                break;
            case GALLERY_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, GALLERY_PERMISSIONS_REQUEST, grantResults)) {
                    startGalleryChooser();
                }
                break;
        }
    }

    public void uploadImage(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                                1200);

                callCloudVision(bitmap);
                mMainImage.setImageBitmap(bitmap);

            } catch (IOException e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(TAG, "Image picker gave us a null image.");
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }

    private void callCloudVision(final Bitmap bitmap) throws IOException {
        // Switch text to loading
        mImageDetails.setText(R.string.loading_message);
        mProgressBar.setVisibility(View.VISIBLE);

        // Do the real work in an async task, because we need to use the network anyway
        new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object... params) {
                try {
                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    VisionRequestInitializer requestInitializer =
                            new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                                /**
                                 * We override this so we can inject important identifying fields into the HTTP
                                 * headers. This enables use of a restricted cloud platform API key.
                                 */
                                @Override
                                protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                                        throws IOException {
                                    super.initializeVisionRequest(visionRequest);

                                    String packageName = getPackageName();
                                    visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);
                                    String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);
                                    visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                                }
                            };

                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(requestInitializer);

                    Vision vision = builder.build();

                    BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                            new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
                        AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                        // Add the image
                        Image base64EncodedImage = new Image();
                        // Convert the bitmap to a JPEG
                        // Just in case it's a format that Android understands but Cloud Vision
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                        byte[] imageBytes = byteArrayOutputStream.toByteArray();

                        // Base64 encode the JPEG
                        base64EncodedImage.encodeContent(imageBytes);
                        annotateImageRequest.setImage(base64EncodedImage);

                        if (ChooseActivity.optSelected.equals("o")){
                            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                                Feature labelDetection = new Feature();
                                labelDetection.setType("LABEL_DETECTION");
                                labelDetection.setMaxResults(10);
                                add(labelDetection);
                            }});
                        }
                        else if (ChooseActivity.optSelected.equals("t")){
                            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                                Feature textDetection = new Feature();
                                textDetection.setType("TEXT_DETECTION");
                                textDetection.setMaxResults(10);
                                add(textDetection);
                            }});
                        }
                        else if (ChooseActivity.optSelected.equals("l")){
                            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                                Feature landmarkDetection = new Feature();
                                landmarkDetection.setType("LANDMARK_DETECTION");
                                landmarkDetection.setMaxResults(10);
                                add(landmarkDetection);
                            }});
                        }
                        // add the features we want
                        /*annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                            Feature labelDetection = new Feature();
                            labelDetection.setType("LABEL_DETECTION");
                            labelDetection.setMaxResults(10);
                            add(labelDetection);

                            Feature textDetection = new Feature();
                            textDetection.setType("TEXT_DETECTION");
                            textDetection.setMaxResults(10);
                            add(textDetection);

                            Feature landmarkDetection = new Feature();
                            landmarkDetection.setType("LANDMARK_DETECTION");
                            landmarkDetection.setMaxResults(10);
                            add(landmarkDetection);
                        }});*/

                        // Add the list of one thing to the request
                        add(annotateImageRequest);
                    }});

                    Vision.Images.Annotate annotateRequest =
                            vision.images().annotate(batchAnnotateImagesRequest);
                    // Due to a bug: requests to Vision API containing large images fail when GZipped.
                    annotateRequest.setDisableGZipContent(true);
                    Log.d(TAG, "created Cloud Vision request object, sending request");

                    BatchAnnotateImagesResponse response = annotateRequest.execute();
                    return convertResponseToString(response);

                } catch (GoogleJsonResponseException e) {
                    Log.d(TAG, "failed to make API request because " + e.getContent());
                } catch (IOException e) {
                    Log.d(TAG, "failed to make API request because of other IOException " +
                            e.getMessage());
                }
                return "Cloud Vision API request failed. Check logs for details.";
            }

            protected void onPostExecute(String result) {
                mProgressBar.setVisibility(View.GONE);
                mImageDetails.setText(result);

                translate.setVisibility(View.VISIBLE);
                dictionaryButton.setVisibility(View.VISIBLE);
            }
        }.execute();
    }

    public Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        imageArray = new ArrayList<String>();
        spanishString = new ArrayList<String>();
        StringBuilder message = new StringBuilder("Results:\n\n");
        message.append("Object:\n");

        if (ChooseActivity.optSelected.equals("o")){
            List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
            if (labels != null) {
                //spanishString.add(response.getResponses().get(0).getLabelAnnotations().get(0).setLocale("es").getDescription());
                imageArray.add(response.getResponses().get(0).getLabelAnnotations().get(0).getDescription());
                Log.d("trrrryyy",imageArray.toString());
                for (EntityAnnotation label : labels) {
                    message.append(String.format(Locale.getDefault(), "%.3f: %s",
                            label.getScore(), label.getDescription()));

                    message.append("\n");
                }
            } else {
                message.append("nothing\n");
            }
        }
        else if(ChooseActivity.optSelected.equals("t")){
            message.append("Texts:\n");
            List<EntityAnnotation> texts = response.getResponses().get(0)
                    .getTextAnnotations();
            if (texts != null) {
                Log.d("in text",response.getResponses().toString());
                imageArray.add(response.getResponses().get(0).getTextAnnotations().get(0).getDescription().replace("\n", " "));
                Log.d("trial", imageArray.toString());
                for (EntityAnnotation text : texts) {
                    message.append(String.format(Locale.getDefault(), "%s: %s",
                            text.getLocale(), text.getDescription()));
                    message.append("\n");
                }
            } else {
                message.append("nothing\n");
            }
        }
        else if (ChooseActivity.optSelected.equals("l")){
            message.append("Landmarks:\n");
            List<EntityAnnotation> landmarks = response.getResponses().get(0)
                    .getLandmarkAnnotations();
            if (landmarks != null) {
                imageArray.add(response.getResponses().get(0).getLandmarkAnnotations().get(0).getDescription());
                for (EntityAnnotation landmark : landmarks) {
                    message.append(String.format(Locale.getDefault(), "%.3f: %s",
                            landmark.getScore(), landmark.getDescription()));
                    message.append("\n");
                }
            } else {
                message.append("nothing\n");
            }
        }

        Log.d("Arrrayyyy",imageArray.toString());

        return message.toString();
    }
    public void translateButtonClicked(View view){
        if(imageArray == null){
            Toast.makeText(getApplicationContext(),"TAKE ANOTHER PICTURE",Toast.LENGTH_SHORT).show();
        }
        else{
            Intent i = new Intent(this, TranslateActivity.class);
            startActivity(i);
        }

    }
    public void dictionaryButtonClicked(View view){
        if(imageArray == null){
            Toast.makeText(getApplicationContext(),"TAKE ANOTHER PICTURE",Toast.LENGTH_SHORT).show();
        }
        else{
            Intent i = new Intent(this, DictionaryActivity.class);
            startActivity(i);
        }

    }

}
