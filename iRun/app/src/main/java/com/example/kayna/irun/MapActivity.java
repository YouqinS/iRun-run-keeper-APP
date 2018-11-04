package com.example.kayna.irun;

import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<Location> coordinates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        coordinates= (ArrayList<Location>) getIntent().getSerializableExtra("coordinates");

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        System.out.println("Map is ready to show data");
        if (!coordinates.isEmpty()){
        PolylineOptions option = new PolylineOptions();
        for (Location location: coordinates) {
            option.add(new LatLng(location.getLatitude(), location.getLongitude()));
        }
        googleMap.addPolyline(option);
        }


        // Add a marker in Espoo and move the camera
        Location markerLocation = whereToCenterMap(coordinates);

        LatLng markerLatLng = new LatLng(markerLocation.getLatitude(), markerLocation.getLongitude());
        mMap.addMarker(new MarkerOptions().position(markerLatLng).title("Marker at the first coordinate or Espoo"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(markerLatLng));
    }

    @Nullable
    private Location whereToCenterMap(List<Location> coordinates) {
        if(coordinates.isEmpty()){
            Location targetLocation = new Location("");
            targetLocation.setLatitude(60.2139);
            targetLocation.setLongitude(24.8105);
           return targetLocation;
        }
        else {
            return  coordinates.get(0);//get the first coordinate
        }
    }
}
