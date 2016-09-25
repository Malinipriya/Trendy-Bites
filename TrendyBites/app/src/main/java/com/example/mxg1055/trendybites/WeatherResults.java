package com.example.mxg1055.trendybites;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;

public class WeatherResults extends AppCompatActivity{

    AsyncHttpRequest asyncHttpRequest = null;
    private String tag="Information Results";
    public static String foodPlacePhone;
    private static String contact;
    private static String name;
    private static String locality;
    private static String country;
    private static String region;
    private static String postalCode;
    private static String address1;
    private static String webAddress;
    private static String paymentMethod;
    private static String attire;
    private static String smoking;
    private static String takeOut;
    private static ArrayList<String> diet=  new ArrayList<String>();
    private static ArrayList<String> meals=  new ArrayList<String>();
    private TextView txtViewName;
    private TextView txtViewAddress;
    private TextView txtViewRegion;
    private TextView txtViewPhone;
    private TextView txtViewCountry;
    private TextView txtViewWeb;
    private TextView txtViewPaymentMethod;
    private TextView txtViewAttire;
    private TextView txtViewSmoking;
    private TextView txtViewTakeOut;
    private TextView txtViewDiet;
    private TextView txtViewMeals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_results);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        txtViewName = (TextView) findViewById(R.id.name);
        txtViewAddress = (TextView) findViewById(R.id.address);
        txtViewRegion = (TextView) findViewById(R.id.region);
        txtViewCountry = (TextView) findViewById(R.id.country);
        txtViewPhone = (TextView) findViewById(R.id.phone);
        txtViewWeb = (TextView) findViewById(R.id.web);
        txtViewPaymentMethod = (TextView) findViewById(R.id.paymentMethod);
        txtViewAttire = (TextView) findViewById(R.id.attire);
        txtViewSmoking = (TextView) findViewById(R.id.smoking);
        txtViewTakeOut = (TextView) findViewById(R.id.takeOut);
        txtViewDiet = (TextView) findViewById(R.id.diet);
        txtViewMeals = (TextView) findViewById(R.id.meals);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        foodPlacePhone = bundle.getString("FoodPlacePhone");
        webAddress = bundle.getString("FoodPlaceWeb");
        Log.d(tag, foodPlacePhone);

        if(foodPlacePhone!=null && !foodPlacePhone.isEmpty()){

            if(asyncHttpRequest != null) {
                asyncHttpRequest.cancel(true);
            }
            String payload;

            payload ="{" +
                    "\"api_key\" : \"d71a988d6afb2a1919b0d7cf00616d7ddaaab4ed\"," +
                    "\"fields\" : [ \"locu_id\",\"name\",\"extended\",\"location\",\"contact\" ]," +
                    "\"venue_queries\" : [ { \"contact\" : { \"phone\" : \"" + foodPlacePhone + "\" } } ]" +
                    "}";

            asyncHttpRequest = new	AsyncHttpRequest("POST", "https://api.locu.com/v2/venue/search", null, payload);
            asyncHttpRequest.execute();
            Log.d(tag, "Request successful");

            Button btnCall = (Button) findViewById(R.id.btnCall);
            btnCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + contact));
                    startActivity(intent);
                }
            });
        }
        else {
            Log.d(tag, "Phone Number not available to query information");
        }
    }

    private void parseJSON(JSONObject jsonObject){
        Log.d(tag, "Inside parseJSON");
        int index=-1;
        paymentMethod="Not Available";
        attire="Not Available";
        smoking="Not Available";
        takeOut="Not Available";
        diet.clear();
        diet.add("Not Available");
        meals.clear();
        meals.add("Not Available");
        if(jsonObject!=null){
            try{
                if(jsonObject.has("venues") && jsonObject.getJSONArray("venues").length()>0){
                    contact = jsonObject.getJSONArray("venues").getJSONObject(0).getJSONObject("contact").getString("phone");
                    name = jsonObject.getJSONArray("venues").getJSONObject(0).getString("name");
                    JSONObject location = jsonObject.getJSONArray("venues").getJSONObject(0).getJSONObject("location");
                    address1 = location.getString("address1");
                    locality = location.getString("locality");
                    region = location.getString("region");
                    postalCode = location.getString("postal_code");
                    country = location.getString("country");

                    for(int i=0;i<jsonObject.getJSONArray("venues").length();i++) {
                        if(jsonObject.getJSONArray("venues").getJSONObject(i).has("extended")){
                            index= i;
                            break;
                        }
                    }
                    if(jsonObject.getJSONArray("venues").getJSONObject(index).has("extended")){
                        JSONObject extended = jsonObject.getJSONArray("venues").getJSONObject(index).getJSONObject("extended");
                        if(extended.has("payment_methods")){
                            JSONObject paymentMethods = extended.getJSONObject("payment_methods");
                            paymentMethod="";
                            if(paymentMethods.getBoolean("visa"))
                                paymentMethod = "Visa";

                            if(paymentMethods.getBoolean("americanexpress"))
                                paymentMethod = paymentMethod + "\nAmerican Express";

                            if(paymentMethods.getBoolean("mastercard"))
                                paymentMethod = paymentMethod + "\nMaster Card";

                            if(paymentMethods.getBoolean("discover"))
                                paymentMethod = paymentMethod + "\nDiscover";
                        }
                        else {
                            paymentMethod = "Not Available";
                        }
                        if(extended.has("attire")) {
                            attire="";
                            attire = extended.getString("attire");
                        }
                        else {
                            attire = "Not Available";
                        }
                        if(extended.has("smoking")) {
                            smoking="";
                            smoking = extended.getString("smoking");
                        }
                        else {
                            smoking = "Not Available";
                        }
                        if(extended.has("takeout")) {
                            takeOut="";
                            if(extended.getBoolean("takeout"))
                                takeOut = "Available";
                            else
                                takeOut = "Not Available";
                        }
                        else {
                            takeOut = "Not Available";
                        }
                        if(extended.has("dietary_restrictions")) {
                            diet.clear();
                            JSONArray dietArray = extended.getJSONArray("dietary_restrictions");
                            for(int j=0;j<dietArray.length();j++){
                                diet.add(dietArray.getString(j));
                            }
                        }
                        else {
                            diet.add("Not Available");
                        }
                        if(extended.has("meals")) {
                            meals.clear();
                            JSONArray mealArray = extended.getJSONArray("meals");
                            for(int j=0;j<mealArray.length();j++){
                                meals.add(mealArray.getString(j));
                            }
                        }
                        else {
                            meals.add("Not Available");
                        }
                    }


                }
            }
            catch (JSONException|NullPointerException e) {
                e.printStackTrace();
            }
        }
        else {
            Log.e("ServiceHandler", "No data received from HTTP Request");
        }
    }

    class AsyncHttpRequest extends AsyncTask<Void,Void,JSONObject> {
        String method;
        String url;
        JSONViaHttp.QueryStringParams queryStringParams;
        String payload;

        public AsyncHttpRequest(String method, String url, JSONViaHttp.QueryStringParams queryStringParams, String payload) {
            this.method = method;
            this.url = url;
            this.queryStringParams = queryStringParams;
            this.payload = payload;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            String jsonResponse =  JSONViaHttp.get(method, url, queryStringParams, payload);
            try {
                JSONObject jsonObject = new JSONObject(jsonResponse);
                parseJSON(jsonObject);
                return jsonObject;
            }
            catch(JSONException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject result){
            super.onPostExecute(result);
            txtViewName.setText(name + ", " + locality);
            txtViewAddress.setText(address1);
            txtViewRegion.setText(region+ " " + postalCode);
            txtViewCountry.setText(country);
            txtViewPhone.setText(contact);
            txtViewWeb.setText(webAddress);
            txtViewPaymentMethod.setText(paymentMethod);
            txtViewAttire.setText(attire);
            txtViewSmoking.setText(smoking);
            txtViewTakeOut.setText(takeOut);
            String temp="";
            for(int j=0;j<diet.size();j++){
                temp= temp + diet.get(j) +"\n";
            }
            txtViewDiet.setText(temp);
            temp="";
            for(int j=0;j<meals.size();j++){
                temp= temp + meals.get(j) +"\n";
            }
            txtViewMeals.setText(temp);
            paymentMethod="Not Available";
            attire="Not Available";
            smoking="Not Available";
            takeOut="Not Available";
            diet.clear();
            diet.add("Not Available");
            meals.clear();
            meals.add("Not Available");
            asyncHttpRequest = null;
        }
    }
}
