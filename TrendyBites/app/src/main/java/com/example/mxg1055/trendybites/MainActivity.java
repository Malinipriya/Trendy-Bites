package com.example.mxg1055.trendybites;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Html;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,GoogleApiClient.ConnectionCallbacks {
    private static final String LOG_TAG = "PlacesAPIActivity";
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private GoogleApiClient mGoogleApiClient;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private AutoCompleteTextView txtFoodPlace;
    private TextView txtViewHeader;
    private TextView txtViewName;
    private TextView txtViewAddress;
    private TextView txtViewPlaceId;
    private TextView txtViewPhone;
    private TextView txtViewWeb;
    private PlaceArrayAdapter mPlaceArrayAdapter;
    private LatLngBounds BOUNDS;
    public static final String MyPREFERENCES = "MyPrefs" ;
    private static final  String tag = "MainActivity";
    SharedPreferences sharedPref;
    Set<String> favPlacesSet;
    Map<String, String> favPlacePhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPref = getSharedPreferences(MyPREFERENCES,Context.MODE_PRIVATE);//(this, R.xml.preferences, false);
        List<String> spinnerArray = new ArrayList<String>();

        if (sharedPref.getStringSet("favPlaces",null) != null) {
            favPlacesSet = sharedPref.getStringSet("favPlaces",null);
            spinnerArray.addAll(favPlacesSet);
        }
        else{
            favPlacesSet = new HashSet<String>();
            favPlacePhone = new HashMap<String, String>();
        }

        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        BOUNDS = new LatLngBounds(
                new LatLng(location.getLatitude(), location.getLongitude()), new LatLng(location.getLatitude(), location.getLongitude()));

        mGoogleApiClient = new GoogleApiClient
                .Builder(MainActivity.this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        txtFoodPlace = (AutoCompleteTextView) findViewById(R.id.txtFoodPlace);
        txtFoodPlace.setThreshold(3);
        txtViewHeader = (TextView) findViewById(R.id.header);
        txtViewName = (TextView) findViewById(R.id.name);
        txtViewAddress = (TextView) findViewById(R.id.address);
        txtViewPlaceId = (TextView) findViewById(R.id.placeId);
        txtViewPhone = (TextView) findViewById(R.id.phone);
        txtViewWeb = (TextView) findViewById(R.id.web);
        /** Setting an action listener for the autocomplete search location widget*/
        txtFoodPlace.setOnItemClickListener(mAutocompleteClickListener);
        mPlaceArrayAdapter = new PlaceArrayAdapter(this, android.R.layout.simple_list_item_1,
                BOUNDS, null);
        txtFoodPlace.setAdapter(mPlaceArrayAdapter);

        if(savedInstanceState!=null){
            txtViewName.setText(savedInstanceState.getString("foodName"));
            txtViewPhone.setText(savedInstanceState.getString("foodPhone"));
            txtViewWeb.setText(savedInstanceState.getString("foodWeb"));
        }

        /** Setting an action listener for the Current location button widget*/
        Button btnCurrentLocation = (Button) findViewById(R.id.btnCurrentLocation);
        btnCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this,"inside action listener for button", Toast.LENGTH_SHORT).show();
                if (mGoogleApiClient.isConnected()) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                PERMISSION_REQUEST_CODE);
                    } else {
                        //Toast.makeText(MainActivity.this,"calling place detection api", Toast.LENGTH_SHORT).show();
                        callPlaceDetectionApi();
                    }
                }
            }
        });

        ImageView weather = (ImageView) findViewById(R.id.ic_information);
        weather.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String foodPlacePhone = txtViewPhone.getText().toString();
                if(foodPlacePhone.isEmpty())
                    Toast.makeText(MainActivity.this,"To get extra information, select a food place", Toast.LENGTH_SHORT).show();
                else {
                    Intent informationIntent = new Intent(MainActivity.this, WeatherResults.class);
                    String areaCode = foodPlacePhone.substring(3,6);
                    String phoneNumber = foodPlacePhone.substring(7);
                    foodPlacePhone = "(" + areaCode + ") " + phoneNumber;
                    informationIntent.putExtra("FoodPlacePhone", foodPlacePhone);
                    informationIntent.putExtra("FoodPlaceWeb", txtViewWeb.getText().toString());
                    startActivity(informationIntent);
                }
            }
        });

        ImageView budget = (ImageView) findViewById(R.id.ic_budget);
        budget.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String foodPlacePhone = txtViewPhone.getText().toString();
                if(foodPlacePhone.isEmpty())
                    Toast.makeText(MainActivity.this,"To choose budget preferences, select a food place", Toast.LENGTH_SHORT).show();
                else {
                    Intent budgetQuestionnaireIntent = new Intent(MainActivity.this, BudgetQuestionnaire.class);
                    String areaCode = foodPlacePhone.substring(3,6);
                    String phoneNumber = foodPlacePhone.substring(7);
                    foodPlacePhone = "(" + areaCode + ") " + phoneNumber;
                    budgetQuestionnaireIntent.putExtra("FoodPlacePhone", foodPlacePhone);
                    startActivity(budgetQuestionnaireIntent);
                }
            }
        });
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final PlaceArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i(LOG_TAG, "Selected: " + item.description);
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            Log.i(LOG_TAG, "Fetching details for ID: " + item.placeId);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
    = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e(LOG_TAG, "Place query did not complete. Error: " +
                        places.getStatus().toString());
                return;
            }
            // Selecting the first object buffer.
            final Place place = places.get(0);
            //CharSequence attributions = places.getAttributions();
            txtViewHeader.setText("Selected Place");
            txtViewName.setText(Html.fromHtml(place.getName() + ""));
            txtViewAddress.setText(Html.fromHtml(place.getAddress() + ""));
            txtViewPlaceId.setText(Html.fromHtml(place.getId() + ""));
            txtViewPhone.setText(Html.fromHtml(place.getPhoneNumber() + ""));
            txtViewWeb.setText(Html.fromHtml(place.getWebsiteUri() + ""));
            places.release();
        }
    };

    private void callPlaceDetectionApi() throws SecurityException {
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                double maxLikelihood = 0.0;
                CharSequence maxLikelihoodPlaceName = "", maxLikelihoodPlaceAddress = "", maxLikelihoodPlaceId = "", maxLikelihoodPlacePhone = "", maxLikelihoodPlaceWeb = "";
                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                    if (placeLikelihood.getLikelihood() > maxLikelihood) {
                        maxLikelihood = placeLikelihood.getLikelihood();
                        maxLikelihoodPlaceName = placeLikelihood.getPlace().getName();
                        maxLikelihoodPlaceAddress = placeLikelihood.getPlace().getAddress();
                        maxLikelihoodPlaceId = placeLikelihood.getPlace().getId();
                        maxLikelihoodPlacePhone = placeLikelihood.getPlace().getPhoneNumber();
                        maxLikelihoodPlaceWeb = String.valueOf(placeLikelihood.getPlace().getWebsiteUri());
                    }
                }
                likelyPlaces.release();
                txtViewHeader.setText("Selected Place");
                txtViewName.setText(maxLikelihoodPlaceName + "");
                txtViewAddress.setText(maxLikelihoodPlaceAddress + "");
                txtViewPlaceId.setText(maxLikelihoodPlaceId + "");
                txtViewPhone.setText(maxLikelihoodPlacePhone + "");
                txtViewWeb.setText(maxLikelihoodPlaceWeb + "");
                //Log.d(tag, txtViewAddress.getText().toString());
                Log.i(LOG_TAG, "You are currently in: " + maxLikelihoodPlaceName);
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(LOG_TAG, "Inside onConnected");
        mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);
        Log.i(LOG_TAG, "Google Places API connected.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mPlaceArrayAdapter.setGoogleApiClient(null);
        Log.e(LOG_TAG, "Google Places API connection suspended.");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callPlaceDetectionApi();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        sharedPref = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        MenuItem item = menu.findItem(R.id.action_menu_fav);

        if (sharedPref.getStringSet("favPlaces", null) != null) {

            favPlacesSet = sharedPref.getStringSet("favPlaces", null);
            favPlacePhone = new HashMap<String,String>();
            for( Map.Entry entry : sharedPref.getAll().entrySet() )
                favPlacePhone.put( entry.getKey().toString(), entry.getValue().toString() );

            if ("".equals(txtViewName) || txtViewName == null ||"".equals(txtViewPhone) || txtViewPhone == null) {

            }else{
                SharedPreferences sharedPref1 = PreferenceManager.getDefaultSharedPreferences(this);
                if ("YES".equals(sharedPref1.getString(getString(R.string.pref_fav_restaurant), ""))) {
                    favPlacesSet.add(txtViewName.getText().toString());
                    favPlacePhone.put(txtViewName.getText().toString(), txtViewPhone.getText().toString());
                    editor.putStringSet("favPlaces", favPlacesSet);
                    for (Map.Entry entry : favPlacePhone.entrySet()) {
                        if(!entry.getKey().toString().equals("favPlaces"))
                            editor.putString((String) entry.getKey(), (String) entry.getValue());
                    }
                    editor.commit();
                } else if ("NO".equals(sharedPref1.getString(getString(R.string.pref_fav_restaurant), ""))) {

                } else {
                    //favPlacesSet.add("Favorites");
                }
                SharedPreferences.Editor editor1 =  sharedPref1.edit();
                editor1.clear();
                editor1.commit();
            }
        }
        else{
            favPlacesSet.add("Favorites");
            editor.putStringSet("favPlaces", favPlacesSet);
            editor.commit();
        }

        List<String> spinnerArray = new ArrayList<String>();
        spinnerArray.addAll(favPlacesSet);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, spinnerArray);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selected = parent.getItemAtPosition(pos).toString();

                for (Map.Entry entry : sharedPref.getAll().entrySet()) {
                    /*Log.i(LOG_TAG, "key :" + entry.getKey().toString());
                    Log.i(LOG_TAG, "value :" + selected);*/
                    if (entry.getKey().toString().equals(selected)) {
                        txtViewHeader.setText("Selected Place");
                        txtViewPhone.setText(entry.getValue().toString());
                        txtViewName.setText(entry.getKey().toString());
                        txtViewAddress.setText("");
                        txtViewPlaceId.setText("");
                        txtViewWeb.setText("");
                        break;
                    }
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing, just another required interface callback
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_menu:
                String foodPlacePhone = txtViewPhone.getText().toString();
                if(foodPlacePhone.isEmpty())
                    Toast.makeText(this,
                            "To view the menu, select a food place",
                            Toast.LENGTH_SHORT).show();
                else {
                    Intent foodPlaceIntent = new Intent(MainActivity.this, FoodPlaceMenu.class);
                    String areaCode = foodPlacePhone.substring(3,6);
                    String phoneNumber = foodPlacePhone.substring(7);
                    foodPlacePhone = "(" + areaCode + ") " + phoneNumber;
                    foodPlaceIntent.putExtra("FoodPlacePhone", foodPlacePhone);
                    startActivity(foodPlaceIntent);
                }
                return true;
            case R.id.action_settings:
               startActivity(new Intent(this, Settings.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.invalidateOptionsMenu();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putString("foodName",txtViewName.getText().toString());
        savedInstanceState.putString("foodPhone",txtViewPhone.getText().toString());
        savedInstanceState.putString("foodWeb",txtViewWeb.getText().toString());
        super.onSaveInstanceState(savedInstanceState);
    }
}
