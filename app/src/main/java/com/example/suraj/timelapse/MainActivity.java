package com.example.suraj.timelapse;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.maxmind.geoip2.exception.GeoIp2Exception;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;


public class MainActivity extends AppCompatActivity {

ArrayList<String> x = new ArrayList();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toast.makeText(this,"Please wait while setting up the database!",Toast.LENGTH_LONG).show();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    getText();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (GeoIp2Exception e) {
                    e.printStackTrace();
                }

            }
        });

        t.start();



        Toast.makeText(this,"Database Fetched Successfully.",Toast.LENGTH_SHORT).show();


    }


    public void autoPlay(View view) {
        Intent intent = new Intent(this, AutoPlay.class);
        intent.putStringArrayListExtra("data",x);
        startActivity(intent);
        Toast.makeText(this,"Downloading Data",Toast.LENGTH_SHORT);

    }

    public void seek(View view) {
        Intent intent = new Intent(this, Seek.class);
        intent.putStringArrayListExtra("data",x);
        startActivity(intent);
        Toast.makeText(this,"Seek mode",Toast.LENGTH_SHORT).show();

    }

    void getText() throws IOException, GeoIp2Exception {

        String word = "";
        Scanner s = new Scanner(getResources().openRawResource(R.raw.input));

        while (s.hasNextLine()) {
            word = s.nextLine();
            StringTokenizer st = new StringTokenizer(word, ",");
            word = st.nextToken();
        //    Log.e("Text", word);
            x.add(word);
        }
    }


    }
