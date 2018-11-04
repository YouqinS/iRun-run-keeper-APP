package com.example.kayna.irun;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

public class NumbersActivity extends AppCompatActivity {
    EditText editTextDistance, editTextDuration, editTextSpeed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_numbers);

        editTextDistance = findViewById(R.id.editTextDistance);
        editTextDuration = findViewById(R.id.editTextDuration);
        editTextSpeed = findViewById(R.id.editTextSpeed);

        //show calculated distance, time and average speed

        double distance = getIntent().getDoubleExtra("distance", 0);
        String distanceInMeters = String.valueOf(distance);
        editTextDistance.setText(distanceInMeters + " Meters");

        double duration = getIntent().getDoubleExtra("duration", 0);
        String durationInMinutes = String.valueOf(duration);
        editTextDuration.setText(durationInMinutes + " Minutes");

        double speed = getIntent().getDoubleExtra("speed", 0);
        String averageSpeed = String.valueOf(speed);
        editTextSpeed.setText(averageSpeed + " Meters per Minute");


    }
}
