package com.example.sophie.bankomap;

import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class ListDataActivity extends AppCompatActivity {

    private static final String TAG = "ListDataActivity";
    DatabaseHelper mDatabaseHelper;
    private ListView mlistView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_data);

        mDatabaseHelper = new DatabaseHelper(this);
        mlistView = findViewById(R.id.listView);
        populateListView();
    }

    private void populateListView(){
        Log.d(TAG, "populateListView: Displaying Data in the ListView");

        Cursor data = mDatabaseHelper.getData("heute");
        ArrayList<String> listData = new ArrayList<>();
        while(data.moveToNext()){
            Toast.makeText(this, data.getString(1), Toast.LENGTH_SHORT).show();
            listData.add(data.getString(1));
        }
        ListAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listData);
        mlistView.setAdapter(adapter);
    }
}

