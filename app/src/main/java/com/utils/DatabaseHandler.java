package com.utils;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import org.json.JSONObject;

import static com.tam.winati.ksa.BaseActivity.appLog;

public class DatabaseHandler extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION   = 1;
    public static final String DATABASE_NAME   = "db-app-data";
    public static final String TABLE_CART      = "cart";

    public static final String KEY_ID          = "id";
    public static final String KEY_STOREID     = "storeid";
    public static final String KEY_STORENAME   = "storename";
    public static final String KEY_LOCID       = "locid";
    public static final String KEY_LOCNAME     = "locname";
    public static final String KEY_DELIVERY    = "delivery";
    public static final String KEY_DATA        = "data";
    SQLiteDatabase db;

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        db       = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CART + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_STOREID + " TEXT,"
                + KEY_STORENAME + " TEXT,"
                + KEY_LOCID + " TEXT,"
                + KEY_LOCNAME + " TEXT,"
                + KEY_DELIVERY + " DATETIME,"
                + KEY_DATA + " TEXT"+")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        onCreate(db);
    }


    public void reCreate()
    {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        onCreate(db);
    }

    public void addToCart(JSONObject jobj,String lang) {

        try
        {
            appLog("addtoCart :: "+jobj+" -- "+lang);
            SQLiteDatabase db       = this.getWritableDatabase();
            ContentValues values    = new ContentValues();
            values.put(KEY_STOREID, jobj.getString("storeid"));
            values.put(KEY_STORENAME, jobj.getString("store"+lang));
            values.put(KEY_LOCID, jobj.getString("locid"));
            values.put(KEY_LOCNAME, jobj.getString("loc"+lang));
            values.put(KEY_DELIVERY, jobj.getString("delivery"));
            values.put(KEY_DATA, jobj.toString());
            db.insert(TABLE_CART, null, values);

        }catch (Exception e){
            e.printStackTrace();
        }

    }


    public Cursor getData() {
        String selectQuery          = "SELECT  * FROM " + TABLE_CART;
        SQLiteDatabase db           = this.getWritableDatabase();
        Cursor cursor               = db.rawQuery(selectQuery, null);
        int count                   = cursor.getCount();
        return cursor;
    }


    public Cursor getStores() {
        String selectQuery          = "SELECT  " + KEY_STOREID + " FROM " + TABLE_CART + " GROUP BY "+KEY_STOREID+" ORDER BY  "+KEY_STORENAME+" ASC";
        SQLiteDatabase db           = this.getWritableDatabase();
        Cursor cursor               = db.rawQuery(selectQuery, null);
        return cursor;
    }

    public Cursor getLocations(String storeid) {
        String selectQuery          = "SELECT  * FROM " + TABLE_CART +" WHERE   "+KEY_STOREID+" = '"+storeid+"' GROUP BY "+KEY_LOCID;
        SQLiteDatabase db           = this.getWritableDatabase();
        Cursor cursor               = db.rawQuery(selectQuery, null);
        return cursor;
    }

    public Cursor getFullCart(String storeid,String locid) {
        String selectQuery          = "SELECT  * FROM " + TABLE_CART + " WHERE   "+KEY_STOREID+" = '"+storeid+"' AND "+KEY_LOCID+" = '"+locid+"'";
        SQLiteDatabase db           = this.getWritableDatabase();
        Cursor cursor               = db.rawQuery(selectQuery, null);
        return cursor;
    }

    public void closeConnection()
    {
        SQLiteDatabase db           = this.getWritableDatabase();
        db.close();
    }

}