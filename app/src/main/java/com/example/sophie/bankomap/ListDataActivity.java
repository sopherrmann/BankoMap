package com.example.sophie.bankomap;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;

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
        logodict.put("andere", R.drawable.logo_bank_austria); // TODO

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
            ListViewData lvd = new ListViewData(data.getString(1),
                                                data.getInt(5),
                                                data.getDouble(2),
                                                data.getDouble(3),
                                                data.getString(7));
            Toast.makeText(this, data.getString(1), Toast.LENGTH_SHORT).show();
            listData.add(lvd);
        }
        MyCustomListAdapter customAdapter = new MyCustomListAdapter(this, R.layout.custom_list, listData);
        mlistView.setAdapter(customAdapter);
    }

    class ListViewData{
        String session;
        int date;
        double lat, lon;
        String bank;

        public ListViewData(String session, int date, double lat, double lon, String bank){
            this.session = session;
            this.date = date;
            this.lat = lat;
            this.lon = lon;
            this.bank = bank;
        }
        public String getSession() {
            return session;
        }
        public int getDate() {
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
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(cont);
            View view = inflater.inflate(resource, null);

            TextView sessionView = (TextView) view.findViewById(R.id.viewSession);
            TextView timeView = (TextView) view.findViewById(R.id.viewTime);
            TextView latView = (TextView) view.findViewById(R.id.viewLat);
            TextView lonView = (TextView) view.findViewById(R.id.viewLon);
            ImageView logoView = (ImageView) view.findViewById(R.id.viewLogo);

            ListViewData l = listData.get(position);

            String x = l.getSession();

            sessionView.setText(l.getSession());
            Date currentDate = new Date(l.getDate());
            timeView.setText(currentDate.toString());
            latView.setText(String.format("%.2f", l.getLat()));
            lonView.setText(String.format("%.2f", l.getLon()));
            logoView.setImageResource(logodict.get(l.getBank()));

            return view;
        }
    }
}

