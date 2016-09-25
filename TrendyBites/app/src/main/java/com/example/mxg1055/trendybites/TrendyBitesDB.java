package com.example.mxg1055.trendybites;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.SQLException;

/**
 * Created by mxg1055 on 3/27/16.
 */
public class TrendyBitesDB extends SQLiteOpenHelper{

    private static String tag="TrendyBitesDB";
    public static String foodId="_id";
    public static String foodName="name";
    public static String foodSection="section";
    public static String foodPrice="price";
    public static String foodPlacePhone = "phone_number";

    public interface OnDBReadyListener {
        public void onDBReady(SQLiteDatabase theDB);
    }

    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "menu.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE food (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT, " +
                    "section TEXT, " +
                    "price REAL," +
                    "phone_number TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS food";

    private static TrendyBitesDB theDb;
    private Context appContext;

    private TrendyBitesDB(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        appContext = context.getApplicationContext();
    }

    public static synchronized  TrendyBitesDB getInstance(Context context){
        Log.d(tag, "Inside getInstance");
        if(theDb == null){
            theDb = new TrendyBitesDB(context.getApplicationContext());
        }
        return theDb;
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(SQL_CREATE_ENTRIES);
        db.beginTransaction();
        ContentValues values = new ContentValues();
        int i;
        Log.d(tag, "Inside  of database class: " + FoodPlaceMenu.foodNames.length);
        for(i=0; i<FoodPlaceMenu.foodNames.length; i++) {
            values.put(foodName, FoodPlaceMenu.foodNames[i]);
            values.put(foodSection, FoodPlaceMenu.foodSections[i]);
            values.put(foodPrice, FoodPlaceMenu.foodPrices[i]);
            values.put(foodPlacePhone, FoodPlaceMenu.foodPlacePhone);
            Log.d(tag,Float.toString(FoodPlaceMenu.foodPrices[i]));
            db.insert("food", null, values);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
        onUpgrade(db, oldVersion, newVersion);
    }
}
