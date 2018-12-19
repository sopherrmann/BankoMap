package com.example.sophie.bankomap;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ListDataActivity extends AppCompatActivity {

    private static final String TAG = "ListDataActivity";
    DatabaseHelper mDatabaseHelper;
    private ListView mlistView;
    ArrayList<ListViewData> listData;
    Map<String, Integer> logodict;
    // for the export
    private static final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 1;
    private boolean permission_granted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_data);

        logodict = new HashMap<String, Integer>();
        logodict.put("Bank Austria", R.drawable.logo_bank_austria);
        logodict.put("BAWAG", R.drawable.logo_bawag);
        logodict.put("Deniz Bank AG", R.drawable.logo_denizbank);
        logodict.put("Erste Group Bank AG", R.drawable.logo_erste);
        logodict.put("Euronet", R.drawable.logo_euronet);
        logodict.put("Raiffeisen", R.drawable.logo_raiffeisen);
        logodict.put("Volksbank", R.drawable.logo_volksbank);
        logodict.put("Other", R.drawable.logo_other);

        Intent intent = getIntent();
        String session_name = intent.getStringExtra("session_name");

        mDatabaseHelper = new DatabaseHelper(this);
        mlistView = findViewById(R.id.listView);
        populateListView(session_name);
    }

    private void populateListView(String session){
        Log.d(TAG, "populateListView: Displaying Data in the ListView");

        Cursor data = mDatabaseHelper.getData(session);
        listData = new ArrayList<>();
        while(data.moveToNext()){
            ListViewData lvd = new ListViewData(data.getInt(0),
                                                data.getString(1),
                                                data.getLong(5),
                                                data.getDouble(2),
                                                data.getDouble(3),
                                                data.getString(7),
                                                data.getString(9),
                                                data.getString(6),
                                                data.getBlob(11),
                                                data.getString(10));
            listData.add(lvd);
        }
        MyCustomListAdapter customAdapter = new MyCustomListAdapter(this, R.layout.custom_list, listData);
        mlistView.setAdapter(customAdapter);
    }

    class ListViewData{
        String session, fee, open, info;
        long date;
        int id;
        double lat, lon;
        String bank;
        byte[] image;

        public ListViewData(int id, String session, long date, double lat, double lon, String bank, String fee, String open, byte[] image, String info){
            this.id = id;
            this.session = session;
            this.date = date;
            this.lat = lat;
            this.lon = lon;
            this.bank = bank;
            this.fee = fee;
            this.open = open;
            this.image = image;
            this.info = info;
        }

        public int getId() {
            return id;
        }
        public String getSession() {
            return session;
        }
        public long getDate() {
            return date;
        }
        public Double getLat() {
            return lat;
        }
        public Double getLon() {
            return lon;
        }
        public String getBank() {
            return bank;
        }
        public String getFee() {
            return fee;
        }
        public String getOpen() {
            return open;
        }
        public byte[] getImage() {
            return image;
        }
        public String getInfo() {
            return info;
        }
    }

    class MyCustomListAdapter extends ArrayAdapter<ListViewData>{
        Context cont;
        int resource;
        List<ListViewData> lv;

        public MyCustomListAdapter(Context c, int r, List<ListViewData> l){
            super(c, r, l);
            this.cont = c;
            this.resource = r;
            this.lv = l;
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(cont);
            View view = inflater.inflate(resource, null);

            TextView bankView = (TextView) view.findViewById(R.id.viewBank);
            TextView timeView = (TextView) view.findViewById(R.id.viewTime);
            TextView latView = (TextView) view.findViewById(R.id.viewLat);
            TextView lonView = (TextView) view.findViewById(R.id.viewLon);
            TextView openView = (TextView) view.findViewById(R.id.viewOpen);
            TextView feeView = (TextView) view.findViewById(R.id.viewFee);
            ImageView logoView = (ImageView) view.findViewById(R.id.viewLogo);
            Button deleteButton = (Button) view.findViewById(R.id.buttonDelete);

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeItem(position);
                }
            });

            ListViewData l = listData.get(position);
            bankView.setText(l.getBank());
            Date currentDate = new Date(l.getDate());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);
            SimpleDateFormat sdf = new SimpleDateFormat("E yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            String datetext = sdf.format(currentDate);
            timeView.setText(datetext);
            latView.setText(String.format("%.2f", l.getLat()));
            lonView.setText(String.format("%.2f", l.getLon()));
            logoView.setImageResource(logodict.get(l.getBank()));
            openView.setText(l.getOpen());
            feeView.setText(l.getFee());

            ImageView photoView = (ImageView) view.findViewById(R.id.viewPhoto);
            TextView infoView = (TextView) view.findViewById(R.id.viewInfo);
            infoView.setText(l.getInfo());
            infoView.setMaxLines(100);
            infoView.setVerticalScrollBarEnabled(true);
            infoView.setMovementMethod(new ScrollingMovementMethod());
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
            infoView.setOnTouchListener(listener);

            if(l.getImage().length == 0){
                photoView.setVisibility(View.GONE);
            } else {
                photoView.setImageBitmap(BitmapFactory.decodeByteArray(l.getImage(), 0, l.getImage().length));
            }

            // Set Title for Action Bar
            setTitle(l.getSession());
            return view;
        }

        public void removeItem(final int pos){
            AlertDialog.Builder builder = new AlertDialog.Builder(cont);
            builder.setTitle("Are you sure you want to delete?");

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i){
                    //DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
                    mDatabaseHelper.deleteRow(listData.get(pos).getId());
                    listData.remove(pos);
                    notifyDataSetChanged();
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i){
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    //export the data
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.export_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_export:
                final AlertDialog.Builder export_Builder = new AlertDialog.Builder(this)
                        .setTitle("What do you want to export?");

                export_Builder.setItems(new CharSequence[]{"Export all Sessions", "Export this Session","Cancel"}, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        enableMyStorageIfPermitted();
                                        if (permission_granted) {
                                            switch (which) {
                                                case 0:
                                                    mDatabaseHelper.exportDB(ListDataActivity.this, 1, "allSessions");
                                                    break;
                                                case 1:
                                                    mDatabaseHelper.exportDB(ListDataActivity.this, 0, (String) getTitle());
                                                    break;
                                                case 2:
                                                    AlertDialog alert = export_Builder.create();
                                                    alert.cancel();
                                            }
                                        }
                                    }
                                });
                export_Builder.create().show();
                break;
            default:
                break;
        }
        return true;
    }

    private void enableMyStorageIfPermitted() {
        // If permission is not yet granted
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
        } else {
            permission_granted = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permission_granted = true;
                } else
                    Toast.makeText(this, "Cannot export data, because permission for the storage was not granted", Toast.LENGTH_SHORT)
                    .show();
        }
    }

}