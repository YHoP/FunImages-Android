package com.epicodus.funimagesapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.epicodus.funimagesapp.models.PhotoUrl;
import com.koushikdutta.ion.Ion;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    @Bind(R.id.testing) ImageView mTest;
    @Bind(R.id.getImage) Button mGetImage;
    @Bind(R.id.imageTag) EditText mImageTag;
    Boolean isVisiable = true;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 1000;
    public ArrayList<PhotoUrl> mPhotoUrls;
    int mIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensor, mSensorManager.SENSOR_DELAY_NORMAL);

        final Runnable getPhotoArrayList = new Runnable() {
            @Override
            public void run() {
                setImageFromURL();
            }
        };
        mGetImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mTag = mImageTag.getText().toString();
                getFlickrPhoto(getPhotoArrayList, mTag);

                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        });

    }

    private void setImageFromURL() {

        if (mIndex == mPhotoUrls.size()){
            mIndex = 0;
        }

        String flickrUrl = "http://farm" + mPhotoUrls.get(mIndex).getFarm() +".staticflickr.com/"
                + mPhotoUrls.get(mIndex).getServer() + "/" + mPhotoUrls.get(mIndex).getPhotoId()
                + "_" + mPhotoUrls.get(mIndex).getSecret() + "_m.jpg";

        Picasso.with(this).load(flickrUrl).into(mTest);

//        Ion.with(mTest).load(flickrUrl);

        mIndex++;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;

        if(mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            long curTime = System.currentTimeMillis();

            if((curTime - lastUpdate) > 100 ) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float speed = Math.abs(x + y + z - last_x - last_y - last_z)/diffTime * 10000;

                if(speed > SHAKE_THRESHOLD) {
                    setImageFromURL();


//                    if (isVisiable){
//                        setImageFromURL();
//                        mTest.setVisibility(View.INVISIBLE);
//                        isVisiable = false;
//                    } else {
//                        mTest.setVisibility(View.VISIBLE);
//                        isVisiable = true;
//                    }
                }

                last_x = x;
                last_y = y;
                last_z = z;

//                Toast.makeText(this, "Last_x: " + last_x + "Last_y: " + last_y + "Last_z: " + last_z, Toast.LENGTH_LONG).show();
//                Log.i(" last x , y, z", last_x + ", " + last_y + ", " + last_z);
            }
        }

    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void getFlickrPhoto(final Runnable runnable, String mTag){

        String apiKey = "d5efdb80291dbd978c44ca25672aa5aa";
//        String flickrURL = "https://api.flickr.com/services/rest/?&method=flickr.photos.getRecent&api_key=" + apiKey + "&extras=tags%3Ddogs&format=json&per_page=50";

        String flickrURL = "https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=" + apiKey +
                "&tags=" + mTag + "&format=json";

        if (isNetworkAvailable()){
            OkHttpClient client = new OkHttpClient();
            final Request request = new Request.Builder().url(flickrURL).build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
//                    Toast.makeText(getApplicationContext(), "Error, plase try again", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    try{

                        String jsonData = response.body().string();
                        jsonData = jsonData.replace("jsonFlickrApi(", "");
                        jsonData = jsonData.substring(0, jsonData.length()-1);

                        Log.v("JsonData: ", jsonData);

                        if(response.isSuccessful()){
                            mPhotoUrls = getFlickrPhotoUrl(jsonData);

                            runOnUiThread(runnable);
                        }

                    } catch (IOException e){
                        Log.e("Flickr", "Exception Caught", e);
                    }catch (JSONException e){
                        Log.e("Flickr", "Exception Caught", e);
                    }

                }
            });

        }

    }

    private ArrayList<PhotoUrl> getFlickrPhotoUrl(String jsonData) throws JSONException{


        ArrayList<PhotoUrl> photoURLArrayList = new ArrayList<>();
        JSONObject photosData = new JSONObject(jsonData);
        String photosInfo = photosData.getString("photos");
        Log.v("Photos Data: ", photosInfo);

        JSONObject photoData = new JSONObject(photosInfo);
        String photoInfo = photoData.getString("photo");
        Log.v("Photo Data: ", photoInfo);

        JSONArray jsonArray = new JSONArray(photoInfo);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonPart = jsonArray.getJSONObject(i);
            String photoId = jsonPart.getString("id");
            String photoSecret = jsonPart.getString("secret");
            String photoServer = jsonPart.getString("server");
            String photoFarm = jsonPart.getString("farm");

            Log.v("Photo ID: ", photoId);

            PhotoUrl thisPhoto = new PhotoUrl(photoId, photoSecret, photoServer, photoFarm);
            photoURLArrayList.add(thisPhoto);

        }

        return photoURLArrayList;
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }


}
