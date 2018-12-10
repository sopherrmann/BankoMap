package com.example.sophie.bankomap;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;


import java.nio.channels.FileChannel;

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
    private static final String OPEN = "open";
    private static final String BANK = "bank";
    private static final String NUMBER = "number";
    private static final String FEE = "fee";
    private static final String INFO = "info";
    private static final String IMAGE = "image";



    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SESSION + " TEXT, " +
                LAT + " REAL, " +
                LON + " REAL, " +
                ALT + " REAL, " +
                TIME + " INTEGER, " +
                OPEN + " TEXT, " +
                BANK + " TEXT, " +
                NUMBER + " INTEGER, " +
                FEE + " TEXT, " +
                INFO + " TEXT, " +
                IMAGE + " BLOB)";
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
        contentValues.put(LAT, myLocation.getLocation().getLatitude());
        contentValues.put(LON, myLocation.getLocation().getLongitude());
        contentValues.put(ALT, myLocation.getLocation().getAltitude()); // TODO hier wird trotzdem nur latitude angezeigt
        contentValues.put(TIME, myLocation.getLocation().getTime());
        contentValues.put(OPEN, myLocation.getOpen());
        contentValues.put(BANK, myLocation.getBank());
        contentValues.put(NUMBER, myLocation.getNum());
        contentValues.put(FEE, myLocation.getFee());
        contentValues.put(INFO, myLocation.getInfo());
        contentValues.put(IMAGE, myLocation.getImage());

        //Log.d(TAG, "addData: Adding " + item + " to " + TABLE_NAME);
        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1;
    }

    public Cursor getData(String session){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + SESSION + "='" + session + "'";
        Cursor data = db.rawQuery(query, null);
        return data;
    }
    public Cursor getRow(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + ID + "=" + id;
        Cursor data = db.rawQuery(query,null);
        return data;
    }
    public boolean deleteRow(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, ID + "=" + id, null) > 0;
    }

    public boolean deleteSession(String session){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME,  "session=?", new String[]{session}) > 0;
    }

    public Cursor getSessions(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT distinct session FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    public void exportDB(Activity act, int all, String session){
        // all = 0 exports only one Session
        // all = 1 exports all session
        try {
            SQLiteDatabase sqldb = this.getReadableDatabase();
            Cursor c = null;
            if (all == 0) {
                c = sqldb.rawQuery("select * from " + TABLE_NAME + " WHERE " + SESSION + " = '" + session + "'", null);
            } else {
                c = sqldb.rawQuery("select * from " + TABLE_NAME, null);
            }
            int rowcount = 0;
            int colcount = 0;
            File sdCardDir = Environment.getExternalStorageDirectory();
            session = session.replaceAll("\\s","");
            String filename = session + ".csv";
            // the name of the file to export with or the String allSessions if you export all
            File saveFile = new File(sdCardDir, filename);
            FileWriter fw = new FileWriter(saveFile);
            BufferedWriter bw = new BufferedWriter(fw); //BufferedWriter
            rowcount = c.getCount();
            colcount = c.getColumnCount();

            if (rowcount > 0) {
                c.moveToFirst();
                for (int i = 0; i < colcount; i++) {
                    if (i != colcount - 1) {
                        if(c.getColumnName(i) == "ID"){
                            // sonst gibt es Probleme beim Öffnen der Datei
                            // erstes Element darf nicht gleich ID sein
                            bw.write("'" + c.getColumnName(i) + ",");
                        } else {
                            bw.write(c.getColumnName(i) + ",");
                        }
                    } else {
                        bw.write(c.getColumnName(i));
                    }
                }
                bw.newLine();
                for (int i = 0; i < rowcount; i++) {
                    c.moveToPosition(i);
                    bw.write(c.getInt(0) + "," + c.getString(1) + "," + c.getDouble(2) + ",");
                    bw.write(c.getDouble(3) + ","+ c.getDouble(4) + "," + c.getLong(5) + ",");
                    bw.write(c.getString(6) + "," + c.getString(7) + "," + c.getInt(8) + ",");
                    bw.write(c.getString(9) + "," + c.getInt(10) + "," + c.getBlob(11));
                    bw.newLine();

                }
                bw.flush();
                bw.close();
                Toast.makeText(act, "Exported Successfully.", Toast.LENGTH_SHORT).show();
                // TODO enable Permission to store something
                // TODO data is not saved as csv but as sylk!, add dependency for csvWriter
            }
        } catch (Exception e) {
            // TODO
            Toast.makeText(act, e.toString(), Toast.LENGTH_LONG)
                    .show();
        }
    }
}


