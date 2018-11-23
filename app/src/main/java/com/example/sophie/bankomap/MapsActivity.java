package com.example.sophie.bankomap;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.MaskFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import static com.example.sophie.bankomap.R.array.numbers;

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
        final Button btn_load = findViewById(R.id.btn_load);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View start_session) {
                open_startdialog();
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

    String str_sessionName = "";
    // open dialog to enter a sessionname
    private void open_startdialog(){
        LayoutInflater dialogInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View vDialog = dialogInflater.inflate(R.layout.dialog_layout, null);

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
                                str_sessionName = input.getText().toString();
                                if(str_sessionName.compareTo("")==0){
                                    Toast.makeText(getApplicationContext(),"Bitte Namen eintragen!", Toast.LENGTH_SHORT).show();
                                    open_startdialog(); // otherwise Dialog closes, not the best solution

                                } else {
                                    //Start the Session and save sessionname
                                    sessionDialog.cancel();
                                    start_session(str_sessionName);
                                }
                            }
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        sessionDialog.show();
    }

    // Now you can add ATMs
    private void start_session(final String name){
        final Button btn_start = findViewById(R.id.btn_start);
        final Button btn_load = findViewById(R.id.btn_load);
        btn_start.setVisibility(View.INVISIBLE);
        btn_load.setVisibility(View.INVISIBLE);

        final Button btn_atmmap = findViewById(R.id.btn_atmmap);
        final Button btn_end = findViewById(R.id.btn_end);
        final TextView disp_sesname = findViewById(R.id.disp_sesname);
        disp_sesname.setText(name);
        btn_atmmap.setVisibility(View.VISIBLE);
        btn_end.setVisibility(View.VISIBLE);
        disp_sesname.setVisibility(View.VISIBLE);

        btn_atmmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map_atm();
            }
        });
        btn_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //end Session
                open_enddialog(name);
            }
        });
    }

    // opens dialog to end a session
    private void open_enddialog(String name){
        AlertDialog endDialog = new AlertDialog.Builder(this)
                .setTitle("Beenden der Session:"+ name)
                .setMessage("Wollen Sie diese Session wirklich beenden?")
                .setPositiveButton("Beenden",
                        new Dialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface endDialog, int which) {
                                final Button btn_start = findViewById(R.id.btn_start);
                                final Button btn_load = findViewById(R.id.btn_load);
                                btn_start.setVisibility(View.VISIBLE);
                                btn_load.setVisibility(View.VISIBLE);

                                final Button btn_atmmap = findViewById(R.id.btn_atmmap);
                                final Button btn_end = findViewById(R.id.btn_end);
                                final TextView disp_sesname = findViewById(R.id.disp_sesname);
                                btn_atmmap.setVisibility(View.INVISIBLE);
                                btn_end.setVisibility(View.INVISIBLE);
                                disp_sesname.setVisibility(View.INVISIBLE);
                            }
                }).setNegativeButton(android.R.string.cancel, null)
                .create();
        endDialog.show();
    }


    String str_nbAtm = new String("");
    String str_bank = str_nbAtm;
    String str_charge = str_bank;
    // to enter the information about the ATM
    private void map_atm(){
        LayoutInflater dialogInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View vDialog = dialogInflater.inflate(R.layout.atm_layout, null);

        // Anzahl der Bankomaten
        final Button btn_nbatm = vDialog.findViewById(R.id.btn_nbatm);
        btn_nbatm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View vDialog) {
                final String[] numbers = vDialog.getResources().getStringArray(R.array.numbers);
                final ArrayAdapter<String> adapter_nb = new ArrayAdapter<String>(MapsActivity.this, android.R.layout.simple_spinner_dropdown_item, numbers);
                new AlertDialog.Builder(MapsActivity.this).setTitle("Anzahl der Bankomaten").setAdapter(adapter_nb, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        btn_nbatm.setText(numbers[which]);
                        str_nbAtm = (String) btn_nbatm.getText();
                        dialog.dismiss();
                    }
                }).create().show();
            }
        });

        //to the Atm belonging Bank
        final Button btn_bank  = vDialog.findViewById(R.id.btn_bank);
        btn_bank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View vDialog) {
                final String[] banks = vDialog.getResources().getStringArray(R.array.banks);
                final ArrayAdapter<String> adapter_bank = new ArrayAdapter<String>(MapsActivity.this,android.R.layout.simple_spinner_dropdown_item, banks);
                new AlertDialog.Builder(MapsActivity.this).setTitle("Bank").setAdapter(adapter_bank, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        btn_bank.setText(banks[which]);
                        str_bank = (String) btn_bank.getText();

                        dialog.dismiss();
                    }
                }).create().show();

            }
        });

        //Are there any charges
        final Button btn_charge  = vDialog.findViewById(R.id.btn_charge);
        btn_charge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View vDialog) {
                final String[] charge = vDialog.getResources().getStringArray(R.array.charge);
                final ArrayAdapter<String> adapter_charge = new ArrayAdapter<String>(MapsActivity.this,android.R.layout.simple_spinner_dropdown_item, charge);
                new AlertDialog.Builder(MapsActivity.this).setTitle("Gibt es eine Gebühr?").setAdapter(adapter_charge, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        btn_charge.setText(charge[which]);
                        str_charge = (String) btn_charge.getText();
                        dialog.dismiss();
                    }
                }).create().show();

            }
        });
        // Dialog in which the information about the ATM can be entered
        final AlertDialog atmDialog = new AlertDialog.Builder(this)
                .setTitle("BankoMap")
                .setView(vDialog)
                .setPositiveButton("Speichern", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO in Datenbank einspeichern
                    }
                }).setNegativeButton(android.R.string.cancel,null)
                .create();

        atmDialog.show();
    }

}
