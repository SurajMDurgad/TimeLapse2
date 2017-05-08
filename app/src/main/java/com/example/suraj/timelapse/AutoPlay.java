package com.example.suraj.timelapse;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.Location;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;


public class AutoPlay extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<LatLng> x = new ArrayList<>();
    double Lat;
    double Lng;
    DatabaseReader reader;
    private ArrayList<String> IPs = new ArrayList<>();
    String data = "";
    int num = 0;
    MyAsyncTask m;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_auto_play);

        IPs = getIntent().getStringArrayListExtra("data");

    //    Toast.makeText(this, " " + IPs.size(), Toast.LENGTH_SHORT).show();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);


        try {
            reader = new DatabaseReader.Builder(getResources().openRawResource(R.raw.db)).withCache(new CHMCache()).build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mapFragment.getMapAsync(this);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(22, 87), 3.0f));
    }

    private void addHeatMap() throws IOException, InterruptedException {

        List<LatLng> list = x;

        HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                .data(list)
                .build();

        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));


    }



    void getLocation(String ip) throws IOException {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        CityResponse response = null;
        try {
            response = reader.city(InetAddress.getByName(ip));
        } catch (GeoIp2Exception e) {
            return;
        }

        Location location = response.getLocation();

        Lat = location.getLatitude();

        Lng = location.getLongitude();

        LatLng l = new LatLng(Lat, Lng);

        String country = response.getCity().getName();

    //    Log.e("Text", Lat + " " + Lng + " " + country);

        x.add(l);
    }

    int count = 0;

    public void Play(View view) throws IOException, GeoIp2Exception, InterruptedException, ExecutionException {
        for (int i = 0; i < 60; i++) {

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    MyAsyncTask xy = new MyAsyncTask();
                    try {
                        xy.execute().get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();

        }
    }

    void display() throws IOException, InterruptedException {
        for (int i = num; i <= num + 216; i++) { // Converting 22 mins data to 1 hour. 
            final String y = IPs.get(i);
            count++;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        getLocation(y);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
            t.join();
            //     Log.e("Count", "" + count);
        }


        num += 216;

    }

    private class MyAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                display();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                addHeatMap();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x.clear();
        }

    }

    @Override
    public void onBackPressed() {
        System.exit(0);
        super.onBackPressed();
    }
}
