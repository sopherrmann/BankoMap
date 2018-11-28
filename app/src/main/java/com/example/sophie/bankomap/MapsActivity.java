package com.example.sophie.bankomap;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.MaskFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.Calendar;
import java.util.Date;

//import static com.example.sophie.bankomap.R.array.numbers;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    DatabaseHelper mDatabaseHelper;
    String str_sessionName = "unnamed";
    //Attributes of an ATM
    Integer str_nbAtm;
    String str_bank;
    String str_charge;
    String str_ophours;
    Editable str_notes;
    Location curr_location;
    private FusedLocationProviderClient mFusedLocationClient;

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

        btn_load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View start_session) {
                open_loaddialog();
            }
        });

        /*
        mDatabaseHelper = new DatabaseHelper(this);
        MyLocation test = new MyLocation("session1", 48.0, 16.0, 180.0, "heute");
        mDatabaseHelper.addData(test);
        Intent intent = new Intent(this, ListDataActivity.class);
        startActivity(intent);
        */
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

        enableMyLocationIfPermitted();
        // Show Zoom and location button
        // mMap.getUiSettings().setZoomControlsEnabled(true);

        // Setting a click event handler for the map
        mMap.setOnMyLocationClickListener(new GoogleMap.OnMyLocationClickListener() {
            @Override
            public void onMyLocationClick(@NonNull Location location) {
                Log.d("LocClick", "My location is " + location);
                curr_location = location;

                map_atm();
                /*
                MyLocation myLocation = new MyLocation(location, str_sessionName, true, "Austria", 4, false, "info");
                mDatabaseHelper = new DatabaseHelper(getApplicationContext());
                mDatabaseHelper.addData(myLocation);

                Intent intent = new Intent(getApplicationContext(), ListDataActivity.class);
                intent.putExtra("session_name", str_sessionName);
                startActivity(intent);
                */
            }
        });
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

    //String str_sessionName = "";
    // open dialog to enter a sessionname
    private void open_startdialog(){
        LayoutInflater dialogInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View startView = dialogInflater.inflate(R.layout.dialog_layout, null);

        final EditText input = (EditText) startView.findViewById(R.id.session_name);

        AlertDialog sessionDialog = new AlertDialog.Builder(this)
                .setView(startView)
                .setTitle("Start a Session")
                .setMessage("Please enter the name of your session:")
                .setPositiveButton("Continue",
                        new Dialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface sessionDialog, int which)
                            {
                                //get the entered name
                                str_sessionName = input.getText().toString();
                                if(str_sessionName.compareTo("")==0){

                                    Toast.makeText(getApplicationContext(),"Please enter a name!", Toast.LENGTH_SHORT).show();
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

    private void open_loaddialog(){

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice);
        DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
        Cursor cursor = databaseHelper.getSessions();

        while(cursor.moveToNext()){
            arrayAdapter.add(cursor.getString(0));
        }

        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                str_sessionName = arrayAdapter.getItem(which);
                start_session(str_sessionName);
            }});
        builderSingle.show();
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
        disp_sesname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ListDataActivity.class);
                intent.putExtra("session_name", str_sessionName);
                startActivity(intent);
            }
        });

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
                .setTitle("End the Session: "+ name)
                .setMessage("Do you want to save the session?")
                .setPositiveButton("Yes",
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
                                str_sessionName = "unnamed";
                            }
                        }).setNegativeButton("No", new Dialog.OnClickListener() {
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
                                DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
                                databaseHelper.deleteSession(str_sessionName);
                                str_sessionName = "unnamed";
                            }}).setNeutralButton("Cancel", null)
                .create();
        endDialog.show();
    }


    // to enter the information about the ATM
    private void map_atm(){
        LayoutInflater dialogInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View atmView = dialogInflater.inflate(R.layout.atm_layout, null);

        str_nbAtm = 0;
        str_bank = "Other";
        str_charge = "Unknown";
        str_ophours = "Unknown";

        //to the Atm belonging Bank
        final Button btn_bank  = atmView.findViewById(R.id.btn_bank);
        btn_bank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] banks = v.getResources().getStringArray(R.array.banks);
                ArrayAdapter<String> adapter_bank = new ArrayAdapter<>(MapsActivity.this,android.R.layout.simple_spinner_dropdown_item, banks);
                new AlertDialog.Builder(MapsActivity.this).setTitle("Bank").setAdapter(adapter_bank, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        btn_bank.setText("Bank: " + banks[which]);
                        str_bank = (String) banks[which];

                        dialog.dismiss();
                    }
                }).create().show();

            }
        });

        // Inside/Outside
        final Button btn_ophours = atmView.findViewById(R.id.btn_ophours);
        btn_ophours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] ophours = v.getResources().getStringArray(R.array.ophours);
                ArrayAdapter<String> adapter_oh = new ArrayAdapter<>(MapsActivity.this, android.R.layout.simple_spinner_dropdown_item, ophours);
                new AlertDialog.Builder(MapsActivity.this).setTitle("Are there opening hours?").setAdapter(adapter_oh, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        btn_ophours.setText("Opening Hours: " + ophours[which]);
                        str_ophours = ophours[which];

                        dialog.dismiss();
                    }
                }).create().show();
            }
        });

        // Number of ATMs
        final Button btn_nbatm = atmView.findViewById(R.id.btn_nbatm);
        btn_nbatm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Integer[] numbers = new Integer[]{1,2,3,4,5,6,7,8,9,10};
                ArrayAdapter<Integer> adapter_nb = new ArrayAdapter<Integer>(MapsActivity.this, android.R.layout.simple_spinner_dropdown_item, numbers);
                new AlertDialog.Builder(MapsActivity.this).setTitle("Number of ATMs").setAdapter(adapter_nb, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        btn_nbatm.setText("#ATM: " + numbers[which]);
                        str_nbAtm = numbers[which];
                        dialog.dismiss();
                    }
                }).create().show();
            }
        });

        //Are there any charges
        final Button btn_charge  = atmView.findViewById(R.id.btn_charge);
        btn_charge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] charge = v.getResources().getStringArray(R.array.charge);
                ArrayAdapter<String> adapter_charge = new ArrayAdapter<String>(MapsActivity.this,android.R.layout.simple_spinner_dropdown_item, charge);
                new AlertDialog.Builder(MapsActivity.this).setTitle("Is there a charge for a withdrawal?").setAdapter(adapter_charge, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        btn_charge.setText("Charge?: " + charge[which]);
                        str_charge = (String) charge[which];
                        dialog.dismiss();
                    }
                }).create().show();
            }
        });

        //any notes to the atm
        final EditText input_notes = atmView.findViewById(R.id.input_notes);

        // Dialog in which the information about the ATM can be entered
        final AlertDialog atmDialog = new AlertDialog.Builder(this)
                .setTitle("Add an ATM")
                .setView(atmView)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        str_notes = input_notes.getText();

                        MyLocation myLocation = new MyLocation(curr_location, str_sessionName,
                                str_ophours, str_bank, str_nbAtm, str_charge, str_notes.toString());
                        mDatabaseHelper = new DatabaseHelper(getApplicationContext());
                        mDatabaseHelper.addData(myLocation);
                    }
                }).setNegativeButton(android.R.string.cancel,null)
                .create();

        atmDialog.show();
    }
}