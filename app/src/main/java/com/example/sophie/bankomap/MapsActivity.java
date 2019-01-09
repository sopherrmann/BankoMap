package com.example.sophie.bankomap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.MaskFilter;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.example.sophie.bankomap.R.layout.atm_layout;

//import static com.example.sophie.bankomap.R.array.numbers;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    Button takeImage;
    private static final int CAM_REQUEST = 1313;
    ImageView imageView;
    Bitmap bitmap;
    byte[] image;

    private GoogleMap mMap;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private Boolean mLocationPermissionGranted = false;
    LatLng mDefaultLocation = new LatLng(48.2087623, 16.3725753);
    DatabaseHelper mDatabaseHelper;
    String str_sessionName;
    //Attributes of an ATM
    Integer str_nbAtm;
    String str_bank;
    String str_fee;
    String str_ophours;
    Editable str_notes;
    Location curr_location;
    private FusedLocationProviderClient mFusedLocationClient;

    Button btn_start;
    Button btn_load;
    Button btn_atmmap;
    Button btn_end;
    Button btn_del;
    Button btn_bg;
    Button btn_adj;
    TextView disp_sesname;

    double curr_lat;
    double curr_lon;

    Map<String, Integer> logodict;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mDatabaseHelper = new DatabaseHelper(this);

        btn_start = findViewById(R.id.btn_start);
        btn_load = findViewById(R.id.btn_load);
        btn_atmmap = findViewById(R.id.btn_atmmap);
        btn_end = findViewById(R.id.btn_end);
        btn_del = findViewById(R.id.btn_del);
        disp_sesname = findViewById(R.id.disp_sesname);
        btn_bg = (Button) findViewById(R.id.btn_map_bg);

        // Logos für die Banken
        logodict = new HashMap<String, Integer>();
        logodict.put("Bank Austria", R.drawable.logo_bank_austria);
        logodict.put("BAWAG", R.drawable.logo_bawag);
        logodict.put("Deniz Bank AG", R.drawable.logo_denizbank);
        logodict.put("Erste Group Bank AG", R.drawable.logo_erste);
        logodict.put("Euronet", R.drawable.logo_euronet);
        logodict.put("Raiffeisen", R.drawable.logo_raiffeisen);
        logodict.put("Volksbank", R.drawable.logo_volksbank);
        logodict.put("Other", R.drawable.logo_other);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
        btn_bg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btn_bg.getText().toString().equals("TERRAIN")){
                    mMap.setMapType(mMap.MAP_TYPE_TERRAIN);
                    btn_bg.setText("SATELLITE");
                }else{
                    mMap.setMapType(mMap.MAP_TYPE_SATELLITE);
                    btn_bg.setText("TERRAIN");
                }
            }
        });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        deleteMarkers();
        showMarkers(str_sessionName);
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
        mMap.setMapType(mMap.MAP_TYPE_SATELLITE);
        enableMyLocationIfPermitted();
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            curr_location = (Location) task.getResult();
                            if (curr_location != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(curr_location.getLatitude(),
                                                curr_location.getLongitude()), 14));
                            } else {

                                Log.d("t", "Current location is null. Using defaults.");
                                Log.e("t", "Exception: %s", task.getException());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, 14));
                            }
                        } else {
                            Log.d("t", "Current location is null. Using defaults.");
                            Log.e("t", "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, 14));
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CAM_REQUEST){
            bitmap = (Bitmap) data.getExtras().get("data");
            Toast.makeText(getApplicationContext(),"Image added, to change image, click again on 'Take Picture'",Toast.LENGTH_LONG).show();
        }
    }

    public void showMarkers(final String session){
        // Clears the previously touched position
        mMap.clear();
        final Cursor cursor = mDatabaseHelper.getData(session);
        while(cursor.moveToNext()){
            LatLng latLng = new LatLng(cursor.getDouble(2), cursor.getDouble(3));
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            // Animating to the touched position
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            Marker mMarker = mMap.addMarker(markerOptions);
            mMarker.setTag(cursor.getInt(0));

            //Pop-up for each marker
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    LayoutInflater marker_infoInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View marker_infoView = marker_infoInflater.inflate(R.layout.marker_info_layout, null);
                    final int curr_ID = (int) marker.getTag();
                    Cursor data_row = mDatabaseHelper.getRow(curr_ID);
                    data_row.moveToNext();
                    curr_lat = data_row.getDouble(2);
                    curr_lon = data_row.getDouble(3);
                    final AlertDialog info_builder = new AlertDialog.Builder(MapsActivity.this)
                            .setTitle(data_row.getString(7))
                            .setView(marker_infoView).create();

                    info_builder.show();

                    TextView tv_fee = marker_infoView.findViewById(R.id.viewFee);
                    TextView tv_ophours = marker_infoView.findViewById(R.id.viewOpen);
                    TextView tv_nbatms = marker_infoView.findViewById(R.id.viewNbatm);
                    TextView tv_comments = marker_infoView.findViewById(R.id.viewComments);
                    TextView tv_lat = marker_infoView.findViewById(R.id.viewLat);
                    TextView tv_lon = marker_infoView.findViewById(R.id.viewLon);
                    TextView tv_time = marker_infoView.findViewById(R.id.viewTime);
                    ImageView logoView = marker_infoView.findViewById(R.id.viewLogo);
                    ImageView photoView = marker_infoView.findViewById(R.id.viewPhoto);
                    Button btn_deleteMarker = marker_infoView.findViewById(R.id.buttonDelete);
                    btn_deleteMarker.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            removeItem(curr_ID);
                            info_builder.dismiss();
                        }
                    });

                    /*Button btn_adjustMarker = marker_infoView.findViewById(R.id.buttonAdjust);
                    btn_adjustMarker.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            deleteMarkers();
                            mMap.setOnMapLongClickListener(new OnMapLongClickListener() {
                                @Override
                                public void onMapLongClick(LatLng latLng) {
                                    // Creating a marker
                                    MarkerOptions markerOptions = new MarkerOptions();
                                    markerOptions.position(latLng);
                                    // This will be displayed on taping the marker
                                    markerOptions.title(String.format("lat: %.2f\nlon: %.2f", latLng.latitude, latLng.longitude));
                                    // Clears the previously touched position
                                    mMap.clear();
                                    // Animating to the touched position
                                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                                    mMap.addMarker(markerOptions);

                                    Date currentTime = Calendar.getInstance().getTime();
                                    //Toast.makeText(getApplicationContext(), currentTime.toString(), Toast.LENGTH_SHORT).show();
                                    MyLocation myLocation = new MyLocation(str_sessionName, latLng.latitude, latLng.longitude, currentTime.toString());
                                    mDatabaseHelper = new DatabaseHelper(getApplicationContext());
                                    mDatabaseHelper.addData(myLocation);

                                    Intent intent = new Intent(getApplicationContext(), ListDataActivity.class);
                                    intent.putExtra("session_name", str_sessionName);
                                    startActivity(intent);
                                }
                            });
                        }
                    });*/

                    tv_fee.setText(data_row.getString(9));
                    tv_ophours.setText(data_row.getString(6));
                    tv_nbatms.setText(data_row.getString(8));
                    tv_comments.setText(data_row.getString(10));
                    View.OnTouchListener listener = new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            boolean isLarger;

                            isLarger = ((TextView) v).getLineCount()
                                    * ((TextView) v).getLineHeight() > v.getHeight();
                            if (event.getAction() == MotionEvent.ACTION_MOVE
                                    && isLarger) {
                                v.getParent().requestDisallowInterceptTouchEvent(true);

                            } else {
                                v.getParent().requestDisallowInterceptTouchEvent(false);

                            }
                            return false;
                        }
                    };
                    tv_comments.setOnTouchListener(listener);


                    tv_lat.setText(String.format("%.2f", data_row.getDouble(2)));
                    tv_lon.setText(String.format("%.2f", data_row.getDouble(3)));
                    Date currentDate = new Date(data_row.getLong(5));
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(currentDate);
                    SimpleDateFormat sdf = new SimpleDateFormat("E yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                    String datetext = sdf.format(currentDate);
                    tv_time.setText(datetext);
                    logoView.setImageResource(logodict.get(data_row.getString(7)));

                    if (data_row.getBlob(11).length == 0){
                        photoView.setVisibility(View.GONE);
                    }
                    else {
                        photoView.setImageBitmap(BitmapFactory.decodeByteArray(data_row.getBlob(11), 0, data_row.getBlob(11).length));
                    }
                    return false;
                }
            });
        }
    }

    public void deleteMarkers(){
        mMap.clear();
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
            mLocationPermissionGranted = true;
            mMap.setMyLocationEnabled(true);
            getDeviceLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    mMap.setMyLocationEnabled(true);
                    getDeviceLocation();
                } else {
                    Toast.makeText(MapsActivity.this, "App cannot be used without allowing to access your Location",Toast.LENGTH_LONG)
                            .show();
                    enableMyLocationIfPermitted();
                }
        }
    }

    // open dialog to enter a sessionname
    private void open_startdialog(){
        LayoutInflater dialogInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View startView = dialogInflater.inflate(R.layout.dialog_layout, null);

        final EditText input = (EditText) startView.findViewById(R.id.session_name);

        AlertDialog sessionDialog = new Builder(this)
                .setView(startView)
                .setTitle("Start a Session")
                .setMessage("Please enter the name of your session:")
                .setPositiveButton("Continue",
                        new Dialog.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                            @Override
                            public void onClick(DialogInterface sessionDialog, int which)
                            {
                                //get the entered name
                                str_sessionName = input.getText().toString();
                                if(str_sessionName.compareTo("")==0){
                                    Toast.makeText(getApplicationContext(),"Please enter a name!", Toast.LENGTH_SHORT).show();
                                    open_startdialog(); // otherwise Dialog closes, not the best solution

                                } else if(str_sessionName.length()>16){
                                    Toast.makeText(getApplicationContext(),"Your entered name is too long, please enter a short name!", Toast.LENGTH_SHORT).show();
                                    open_startdialog();
                                }else {
                                    //Start the Session and save sessionname
                                    sessionDialog.cancel();
                                    start_session(str_sessionName);

                                    Toast.makeText(getApplicationContext(), "Click on the Sessionname to see and export all registered ATMs", Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        sessionDialog.show();
    }

    private void open_loaddialog(){

        Builder builderSingle = new Builder(this);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice);
        Cursor cursor = mDatabaseHelper.getSessions();

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
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                str_sessionName = arrayAdapter.getItem(which);
                Toast.makeText(getApplicationContext(), "Click on the Sessionname to see and export all registered ATMs", Toast.LENGTH_LONG).show();
                start_session(str_sessionName);
                showMarkers(str_sessionName);
            }});
        builderSingle.show();
    }

    // Now you can add ATMs
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void start_session(final String name){
        btn_start.setVisibility(View.INVISIBLE);
        btn_load.setVisibility(View.INVISIBLE);
        btn_atmmap.setVisibility(View.VISIBLE);
        btn_end.setVisibility(View.VISIBLE);
        btn_del.setVisibility(View.VISIBLE);
        disp_sesname.setText(name);

        findViewById(R.id.header).setVisibility(View.VISIBLE);

        disp_sesname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ListDataActivity.class);
                intent.putExtra("session_name", str_sessionName);
                startActivity(intent);
            }
        });

        btn_atmmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                set_curr_location();  // Set curr_location
                // TODO handle null location
                if (curr_location == null){
                    Toast.makeText(getApplicationContext(),"No Location available, check if GPS is enabled",Toast.LENGTH_SHORT).show();
                } else {
                    map_atm();
                }
            }
        });
        btn_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //end Session
                open_enddialog(name);
            }
        });

        btn_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //end Session
                open_deldialog(name);
            }
        });
    }

    // opens dialog to end a session
    private void open_enddialog(String name){
        AlertDialog endDialog = new Builder(this)
                .setTitle("End the Session: "+ name)
                .setMessage("Do you really want to end the session?")
                .setPositiveButton("Yes",

                        new Dialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface endDialog, int which) {
                                btn_start.setVisibility(View.VISIBLE);
                                btn_load.setVisibility(View.VISIBLE);
                                btn_atmmap.setVisibility(View.INVISIBLE);
                                btn_end.setVisibility(View.INVISIBLE);
                                btn_del.setVisibility(View.INVISIBLE);
                                findViewById(R.id.header).setVisibility(View.GONE);
                                str_sessionName = "unnamed";
                                deleteMarkers();
                            }
                        }).setNegativeButton("No", new Dialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface endDialog, int which) {
                            }}).create();
        endDialog.show();
    }

    // opens dialog to end a session
    private void open_deldialog(String name){
        AlertDialog endDialog = new Builder(this)
                .setTitle("Delete the Session: "+ name)
                .setMessage("Do you really want to delete the session?")
                .setPositiveButton("Yes",
                        new Dialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface endDialog, int which) {
                                btn_start.setVisibility(View.VISIBLE);
                                btn_load.setVisibility(View.VISIBLE);
                                btn_atmmap.setVisibility(View.INVISIBLE);
                                btn_end.setVisibility(View.INVISIBLE);
                                btn_del.setVisibility(View.INVISIBLE);
                                findViewById(R.id.header).setVisibility(View.GONE);

                                deleteMarkers();
                                mDatabaseHelper.deleteSession(str_sessionName);
                                str_sessionName = "unnamed";
                            }
                        }).setNegativeButton("No", new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface endDialog, int which) {
                    }}).create();
        endDialog.show();
    }

    int count = 0;
    private void set_curr_location(){
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        curr_location =  location;
                        //Toast.makeText(getApplicationContext(),"Location object" + location.toString(), Toast.LENGTH_SHORT).show();
                        if (location == null && count<3) {
                            set_curr_location();
                            count++;
                            // Logic to handle location object
                        }
                    }
                });
    }

    // to enter the information about the ATM
    private void map_atm(){
        LayoutInflater dialogInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View atmView = dialogInflater.inflate(R.layout.atm_layout, null);

        str_nbAtm = 0;
        str_bank = "Other";
        str_fee = "Unknown";
        str_ophours = "Unknown";
        //info Boxes
        atmView.findViewById(R.id.btn_info_bank).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                open_info(v.getResources().getString(R.string.info_bank));
            }
        });

        atmView.findViewById(R.id.btn_info_ophours).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                open_info(v.getResources().getString(R.string.info_ophours));
            }
        });

        atmView.findViewById(R.id.btn_info_nbatm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                open_info(v.getResources().getString(R.string.info_nbatm));
            }
        });

        atmView.findViewById(R.id.btn_info_fee).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                open_info(v.getResources().getString(R.string.info_fee));
            }
        });

        //to the Atm belonging Bank
        final Button btn_bank  = atmView.findViewById(R.id.btn_bank);
        btn_bank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] banks = v.getResources().getStringArray(R.array.banks);
                ArrayAdapter<String> adapter_bank = new ArrayAdapter<>(MapsActivity.this,android.R.layout.simple_spinner_dropdown_item, banks);
                new Builder(MapsActivity.this).setTitle("Bank").setAdapter(adapter_bank, new DialogInterface.OnClickListener() {
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
                new Builder(MapsActivity.this).setTitle("Are there opening hours?").setAdapter(adapter_oh, new DialogInterface.OnClickListener() {
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
                new Builder(MapsActivity.this).setTitle("Number of ATMs").setAdapter(adapter_nb, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        btn_nbatm.setText("#ATM: " + numbers[which]);
                        str_nbAtm = numbers[which];
                        dialog.dismiss();
                    }
                }).create().show();
            }
        });

        //Is there a fee
        final Button btn_fee  = atmView.findViewById(R.id.btn_fee);
        btn_fee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] fee = v.getResources().getStringArray(R.array.fee);
                ArrayAdapter<String> adapter_fee = new ArrayAdapter<String>(MapsActivity.this,android.R.layout.simple_spinner_dropdown_item, fee);
                new Builder(MapsActivity.this).setTitle("Is there a fee for a withdrawal?").setAdapter(adapter_fee, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        btn_fee.setText("Fee?: " + fee[which]);
                        str_fee = (String) fee[which];
                        dialog.dismiss();
                    }
                }).create().show();
            }
        });

        final Button btn_image = atmView.findViewById(R.id.btn_image);
        btn_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAM_REQUEST);
                // TODO: show that image has been saved
            }
        });

        //any notes to the atm
        final EditText input_notes = atmView.findViewById(R.id.input_notes);

        // Dialog in which the information about the ATM can be entered
        final AlertDialog atmDialog = new Builder(this)
                .setTitle("Add an ATM")
                .setView(atmView)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        str_notes = input_notes.getText();
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();

                        if(bitmap != null){
                            bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
                        }
                        image = stream.toByteArray();

                        MyLocation myLocation = new MyLocation(curr_location, str_sessionName,
                                str_ophours, str_bank, str_nbAtm, str_fee, str_notes.toString(), image);
                        mDatabaseHelper.addData(myLocation);
                        showMarkers(str_sessionName);
                        bitmap = null;
                    }
                }).setNegativeButton(android.R.string.cancel,null)
                .create();

        atmDialog.show();
    }

    private void open_info(String text){
        new Builder(this).setTitle("Info").setMessage(text).create().show();
    }

    //
    public void removeItem(final int pos){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Are you sure you want to delete?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i){
                //DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
                mDatabaseHelper.deleteRow(pos);
                onRestart();
                dialogInterface.dismiss();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i){
            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}