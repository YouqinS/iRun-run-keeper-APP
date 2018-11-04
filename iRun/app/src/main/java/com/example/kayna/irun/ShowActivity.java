package com.example.kayna.irun;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ShowActivity extends AppCompatActivity {

    Button buttonNumbers, buttonMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        final List<Location> coordinates = (List<Location>) getIntent().getSerializableExtra("coordinates");
        System.out.println("ShowActivity" + coordinates);


        buttonNumbers = findViewById(R.id.buttonNumbers);
        buttonNumbers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double distanceInMeters = calculateDistance(coordinates);
                System.out.println("Calculated Distance" + distanceInMeters);

                double durationInMinutes = calculateDuration(coordinates);
                System.out.println("Calculated Duration" + durationInMinutes);

                double averageSpeed = calculateSpeed(distanceInMeters, durationInMinutes);
                System.out.println("Calculated Speed" + averageSpeed);

                Intent intentNumbers = new Intent(ShowActivity.this, NumbersActivity.class);
                intentNumbers.putExtra("distance", distanceInMeters);
                intentNumbers.putExtra("duration", durationInMinutes);
                intentNumbers.putExtra( "speed", averageSpeed);
                setResult(Activity.RESULT_OK, intentNumbers);
                startActivity(intentNumbers);
                finish();


            }
        });

        buttonMap = findViewById(R.id.buttonMap);
        buttonMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mapIntent = new Intent(ShowActivity.this, MapActivity.class);
                mapIntent.putExtra("coordinates", new ArrayList(coordinates) );
                setResult(Activity.RESULT_OK, mapIntent);
                startActivity(mapIntent);
                finish();

            }
        });

    }

    private double calculateDistance(List<Location> locations){
        if (locations.isEmpty() || locations.size() == 1) {
            return 0.0;
        }
        double distanceInMeters = 0.0;
        for (Iterator<Location> iterator = locations.iterator(); iterator.hasNext(); ) {
            Location location = iterator.next();
            if (iterator.hasNext()) {
                Location nextLocation = iterator.next();
                //https://developer.android.com/reference/android/location/Location.html#distanceBetween%28double,%20double,%20double,%20double,%20float%5b%5d%29
                distanceInMeters = distanceInMeters + location.distanceTo(nextLocation);
            }
        }
        return Math.round(distanceInMeters);

    }

    private double calculateDuration(List<Location> locations){
        if(locations.isEmpty() || locations.size()==1){
            return 0;
        }
        long duration = locations.get(locations.size()-1).getTime() - locations.get(0).getTime();
        double durationInMinutes = Math.round(duration * 0.000016);
        return Math.abs(durationInMinutes);
    }

    private double calculateSpeed(double distance, double durationInMinutes){
        double averageSpeed = distance / durationInMinutes;
        return Math.round(averageSpeed);
    }


}
