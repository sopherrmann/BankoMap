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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

public class ListDataActivity extends AppCompatActivity {

    private static final String TAG = "ListDataActivity";
    DatabaseHelper mDatabaseHelper;
    private ListView mlistView;
    ArrayList<ListViewData> listData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_data);

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
                                                data.getString(5),
                                                data.getDouble(2),
                                                data.getDouble(3));
            Toast.makeText(this, data.getString(1), Toast.LENGTH_SHORT).show();
            listData.add(lvd);
        }
        MyCustomListAdapter customAdapter = new MyCustomListAdapter(this, R.layout.custom_list, listData);
        mlistView.setAdapter(customAdapter);
    }

    class ListViewData{
        String session, date;
        Double lat, lon;

        public ListViewData(String session, String date, Double lat, Double lon){
            this.session = session;
            this.date = date;
            this.lat = lat;
            this.lon = lon;
        }
        public String getSession() {
            return session;
        }
        public String getDate() {
            return date;
        }
        public Double getLat() {
            return lat;
        }
        public Double getLon() {
            return lon;
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

            ListViewData l = listData.get(position);

            String x = l.getSession();

            sessionView.setText(l.getSession());
            timeView.setText(l.getDate());
            latView.setText(String.format("%.2f", l.getLat()));
            lonView.setText(String.format("%.2f", l.getLon()));

            return view;
        }
    }
}

