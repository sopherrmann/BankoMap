package com.example.sophie.bankomap;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final Button btn_start = findViewById(R.id.btn_start);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View start_session) {
                open_dialog();
            }
        });
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

        mMap.setOnMyLocationClickListener(new GoogleMap.OnMyLocationClickListener() {
            @Override
            public void onMyLocationClick(@NonNull Location location) {
                Log.d("LocClick", "My location is " + location);
            }
        });

        enableMyLocationIfPermitted();
        // Show Zoom and location button
        // mMap.getUiSettings().setZoomControlsEnabled(true);
    }

    private void enableMyLocationIfPermitted() {
        // If permission is not yet granted
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            // show location button
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    mMap.setMyLocationEnabled(true);
                else
                    finish();
        }
    }

    String sessionName = "";
    // open dialog to enter a sessionname
    private void open_dialog(){
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View vDialog = inflater.inflate(R.layout.dialog_layout, null);

        final EditText input = (EditText) vDialog.findViewById(R.id.session_name);

        final AlertDialog sessionDialog = new AlertDialog.Builder(this)
                .setView(vDialog)
                .setTitle("Starte eine Session")
                .setMessage("Bitte geben Sie einen Namen für Ihre Session an:")
                .setPositiveButton("Weiter",
                        new Dialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface sessionDialog, int which)
                            {
                                //get the entered name
                                sessionName = input.getText().toString();
                                Log.d("sessionName",sessionName);
                                //check if there is an entered name
                                if(sessionName.compareTo("")==0){
                                    Toast.makeText(getApplicationContext(),"Bitte Namen eintragen!", Toast.LENGTH_SHORT).show();
                                    open_dialog(); // otherwise Dialog closes, not the best solution

                                } else {
                                    //Start the Session and save sessionname
                                    sessionDialog.cancel();
                                    start_session(sessionName);
                                }
                            }
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        sessionDialog.show();
    }

    // Now you can add ATMs
    private void start_session(String name){
        // Start and Load Button --> Invisible
        final Button btn_start = findViewById(R.id.btn_start);
        final Button btn_load = findViewById(R.id.btn_load);
        btn_start.setVisibility(View.GONE);
        btn_load.setVisibility(View.GONE);

        // ATM-Map und end Button visible
        final Button btn_atmmap = findViewById(R.id.btn_atmmap);
        final Button btn_end = findViewById(R.id.btn_end);
        btn_atmmap.setVisibility(View.VISIBLE);
        btn_end.setVisibility(View.VISIBLE);

        // to end Session
        btn_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View start_session) {
                //übergangslösung wird noch geändert!!
                btn_load.setVisibility(View.VISIBLE);
                btn_start.setVisibility(View.VISIBLE);
                btn_atmmap.setVisibility(View.GONE);
                btn_end.setVisibility(View.GONE);
            }
        });
    }

}
