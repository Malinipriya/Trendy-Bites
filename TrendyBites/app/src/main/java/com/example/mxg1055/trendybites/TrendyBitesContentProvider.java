package com.example.mxg1055.trendybites;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

public class TrendyBitesContentProvider extends ContentProvider {

    private TrendyBitesDB theDB;

    private static final String AUTHORITY = "com.example.mxg1055.trendybites";
    private static final  String BASE_PATH = "food";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int FOOD = 1;
    private static final int FOOD_ID = 2;
    static{
        uriMatcher.addURI(AUTHORITY, BASE_PATH, FOOD);
        uriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", FOOD_ID);
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        long id;
        SQLiteDatabase db = theDB.getWritableDatabase();
        //Log.d("ContentProvider", "Inside insert function of Content Provider");
        switch(uriMatcher.match(uri)) {
            case FOOD:
                id = db.insert("food", null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH + "/" +id);
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        Log.d("ContentProvider", "Inside onCreate function of Content Provider");
        theDB = TrendyBitesDB.getInstance(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO: Implement this to handle query requests from clients.
        SQLiteDatabase db = theDB.getReadableDatabase();
        Cursor cursor;

        Log.d("ContentProvider", "Inside query function of content provider");
        switch(uriMatcher.match(uri)) {
            case FOOD:
                cursor = db.query("food", projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case FOOD_ID:
                cursor = db.query("food", projection, appendToSelection(selection, uri.getLastPathSegment()), selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        if (!(cursor.moveToFirst()) || cursor.getCount() ==0){
            //cursor is empty
            Log.d("ContentProvider", "Cursor is empty");
            return null;
        }
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        int count;
        SQLiteDatabase db = theDB.getWritableDatabase();

        Log.d("ContentProvider", "Inside update function of Content Provider");
        switch (uriMatcher.match(uri)){
            case FOOD:
                count = db.update("food", values, selection, selectionArgs);
                break;
            case FOOD_ID:
                count = db.update("food", values, appendToSelection(selection, uri.getLastPathSegment()), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        int count;
        SQLiteDatabase db = theDB.getWritableDatabase();

        Log.d("ContentProvider", "Inside delete function of Content Provider");
        switch (uriMatcher.match(uri)){
            case FOOD:
                count = db.delete("food", selection, selectionArgs);
                break;
            case FOOD_ID:
                count = db.delete("food", appendToSelection(selection, uri.getLastPathSegment()), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    private String appendToSelection(String selection, String sId){
        int id = Integer.valueOf(sId);
        if(selection == null || selection.trim().equals(""))
            return "_ID = " + id;
        else
            return selection + " AND _ID = " + id;
    }
}
