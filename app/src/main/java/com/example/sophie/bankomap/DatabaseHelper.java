package com.example.sophie.bankomap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "locations.db";
    private static final String TAG = "DatabaseHelper";
    private static final String TABLE_NAME = "table_locations";
    private static final String ID = "ID";
    private static final String SESSION = "session";
    private static final String LAT = "latitude";
    private static final String LON = "longitude";
    private static final String ALT = "altitude";
    private static final String TIME = "time";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SESSION + " TEXT, " +
                LAT + " REAL, " +
                LON + " REAL, " +
                ALT + " REAL, " +
                TIME + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(sql);
        onCreate(db);
    }

    public boolean addData(MyLocation myLocation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SESSION, myLocation.getSession());
        contentValues.put(LAT, myLocation.getLat());
        contentValues.put(LON, myLocation.getLon());
        contentValues.put(ALT, myLocation.getAlt());
        contentValues.put(TIME, myLocation.getTime());

        //Log.d(TAG, "addData: Adding " + item + " to " + TABLE_NAME);
        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1;
    }

    public Cursor getData(String session){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + TIME + "=" + "'heute'";
        Cursor data = db.rawQuery(query, null);
        return data;
    }
}


