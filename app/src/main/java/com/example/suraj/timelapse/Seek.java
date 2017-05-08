package com.example.suraj.timelapse;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarFinalValueListener;
import com.crystal.crystalrangeseekbar.interfaces.OnSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.crystal.crystalrangeseekbar.widgets.CrystalSeekbar;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.Location;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * Created by Suraj on 05-May-17.
 */
public class Seek extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<String> x = new ArrayList<>();
    private ArrayList<LatLng> xx = new ArrayList<>();
    double Lat;
    double Lng;
    DatabaseReader reader;
    int num = 0;
    int count = 0;

    int least, largest;
    MyAsyncTask m;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        x = getIntent().getStringArrayListExtra("data");


        try {
            reader = new DatabaseReader.Builder(getResources().openRawResource(R.raw.db)).build();
        } catch (IOException e) {
            e.printStackTrace();
        }


        setContentView(R.layout.activity_seek);

        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map1);

        final CrystalRangeSeekbar rangeSeekbar = (CrystalRangeSeekbar) findViewById(R.id.rangeSeekbar1);


        rangeSeekbar.setOnRangeSeekbarFinalValueListener(new OnRangeSeekbarFinalValueListener() {


            @Override
            public void finalValue(Number minValue, Number maxValue) {

                Toast.makeText(Seek.this, "Calculation in progress..", Toast.LENGTH_LONG).show();

                num = minValue.intValue();
                largest = maxValue.intValue() * 216;

                Toast.makeText(Seek.this, "Start Time : " + minValue.intValue() + " " + "End Time : " + maxValue.intValue(), Toast.LENGTH_SHORT).show();

                for (int i = minValue.intValue(); i <= maxValue.intValue(); i++) {

                    MyAsyncTask x = new MyAsyncTask();
                    x.execute();
                    m = x;

                }
            }
        });

        mapFragment.getMapAsync(this);


        //   Toast.makeText(this, "Completed", Toast.LENGTH_SHORT).show();

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(22, 87), 2.0f));
    }

    private void addHeatMap() {

        // mMap.clear();

        List<LatLng> list = xx;

        HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                .data(list)
                .build();

        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));


    }

    void getLocation(String ip) throws IOException, GeoIp2Exception {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        CityResponse response = reader.city(InetAddress.getByName(ip));

        Location location = response.getLocation();

        Lat = location.getLatitude();

        Lng = location.getLongitude();

        LatLng l = new LatLng(Lat, Lng);

        String country = response.getCity().getName();

     //   Log.e("Text", Lat + " " + Lng + " " + country);

        xx.add(l);

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
            addHeatMap();
            xx.clear();
            if (num == largest) {

                return;
            }
        }
    }

    void display() throws IOException, InterruptedException {
        for (int i = num; i <= num + 216; i++) {  // 216 because I'm converting the data to 1 hour.
            final String y = x.get(i);
            count++;
        //    Log.e("Test", count + "");
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        getLocation(y);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (GeoIp2Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();

            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        num += 216;

    }

    @Override
    public void onBackPressed() {
        System.exit(0);
        finish();
        super.onBackPressed();
    }
}
