package com.example.sophie.bankomap;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
        logodict.put("Other", R.drawable.logo_bank_austria); // TODO

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
                                                data.getString(6));
            Toast.makeText(this, data.getString(1), Toast.LENGTH_SHORT).show();
            listData.add(lvd);
        }
        MyCustomListAdapter customAdapter = new MyCustomListAdapter(this, R.layout.custom_list, listData);
        mlistView.setAdapter(customAdapter);
    }

    class ListViewData{
        String session, fee, open;
        long date;
        int id;
        double lat, lon;
        String bank;

        public ListViewData(int id, String session, long date, double lat, double lon, String bank, String fee, String open){
            this.id = id;
            this.session = session;
            this.date = date;
            this.lat = lat;
            this.lon = lon;
            this.bank = bank;
            this.fee = fee;
            this.open = open;
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
}

