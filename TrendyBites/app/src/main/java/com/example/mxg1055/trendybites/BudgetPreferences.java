package com.example.mxg1055.trendybites;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class BudgetPreferences extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private SimpleCursorAdapter mAdapter;
    public static String foodPlacePhone;
    ArrayList<String> tempFoodNames = new ArrayList<String>();
    ArrayList<String> tempFoodSections = new ArrayList<String>();
    ArrayList<Float> tempFoodPrices = new ArrayList<Float>();
    public static String[] foodNames;
    public static String[] foodSections;
    public static Float[] foodPrices;
    AsyncHttpRequest asyncHttpRequest = null;
    private String tag="Budget Preferences";
    private String budgetPreference;
    private static int cursorCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_preferences);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        foodPlacePhone = bundle.getString("FoodPlacePhone");
        budgetPreference = bundle.getString("BudgetPreference");
        Log.d(tag, foodPlacePhone);
        Log.d(tag, budgetPreference);

        if(foodPlacePhone!=null && !foodPlacePhone.isEmpty()){

            if(asyncHttpRequest != null) {
                asyncHttpRequest.cancel(true);
            }
            String payload;
            payload ="{" +
                    "\"api_key\" : \"d71a988d6afb2a1919b0d7cf00616d7ddaaab4ed\"," +
                    "\"fields\" : [ \"locu_id\",\"name\",\"menus\",\"location\",\"contact\" ]," +
                    "\"venue_queries\" : [ { \"contact\" : { \"phone\" : \"" + foodPlacePhone + "\" } } ]" +
                    "}";

            asyncHttpRequest = new	AsyncHttpRequest("POST", "https://api.locu.com/v2/venue/search", null, payload);
            asyncHttpRequest.execute();
            Log.d(tag, "Request successful");

            mAdapter = new SimpleCursorAdapter(this, R.layout.list_item, null, new String[]{TrendyBitesDB.foodName}, new int[] {R.id.txtName}, 0);
            ListView listView = (ListView) findViewById(R.id.lstFood);
            listView.setAdapter(mAdapter);
        }
        else {
            Log.d(tag, "Phone Number not available to query menu");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args){
        Log.d(tag, "Inside onCreateLoader");
        String[] projection = new String[]{TrendyBitesDB.foodId, TrendyBitesDB.foodName};
        String where = null, price;
        try{
            Log.d(tag, "Inside try");
            if(foodPlacePhone!=null && !foodPlacePhone.equals("") && budgetPreference.equals("0$ - 5$")){
                //Log.d(tag, "Inside 0$ - 10$");
                where = TrendyBitesDB.foodPlacePhone + " = '" + foodPlacePhone + "' and " + TrendyBitesDB.foodPrice + " <= 5.00";
            }
            else if(foodPlacePhone!=null && !foodPlacePhone.equals("") && budgetPreference.equals("5$ - 10$")){
                //Log.d(tag, "Inside 10$ - 20$");
                where = TrendyBitesDB.foodPlacePhone + " = '" + foodPlacePhone + "' and " + TrendyBitesDB.foodPrice + " > 5.00 and " + TrendyBitesDB.foodPrice + " <= 10.00";
            }
            else if(foodPlacePhone!=null && !foodPlacePhone.equals("") && budgetPreference.equals("10$ - 15$")){
                //Log.d(tag, "Inside 20$ - 30$");
                where = TrendyBitesDB.foodPlacePhone + " = '" + foodPlacePhone + "' and " + TrendyBitesDB.foodPrice + " > 10.00 and " + TrendyBitesDB.foodPrice + " <= 15.00";
            }
            else if(foodPlacePhone!=null && !foodPlacePhone.equals("") && budgetPreference.equals("15$ - 20$")){
                //Log.d(tag, "Inside 30$ - 40$");
                where = TrendyBitesDB.foodPlacePhone + " = '" + foodPlacePhone + "' and " + TrendyBitesDB.foodPrice + " > 15.00 and " + TrendyBitesDB.foodPrice + " <= 20.00";
            }
        }
        catch(NumberFormatException ex) {
            Log.d(tag, "Inside catch");
            if(foodPlacePhone!=null && !foodPlacePhone.equals("")){
                where = TrendyBitesDB.foodPlacePhone + " = '" + foodPlacePhone + "'";
            }
        }
        return new CursorLoader(this, TrendyBitesContentProvider.CONTENT_URI, projection, where, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor){
        Log.d(tag, "Inside onLoadFinished");
        Log.d(tag, "cursorCount: "+cursorCount);
        if (cursor == null && cursorCount<2) {
            //cursor is empty. So populate the database with restaurant menu.
            ContentValues values = new ContentValues();
            int i;
            Log.d(tag, "Inserting new menu in the database");
            for(i=0; i<foodNames.length; i++) {
                //System.out.println(foodNames[i]+" "+foodSections[i]+" ")
                values.put(TrendyBitesDB.foodName, foodNames[i]);
                values.put(TrendyBitesDB.foodSection, foodSections[i]);
                values.put(TrendyBitesDB.foodPrice, foodPrices[i]);
                values.put(TrendyBitesDB.foodPlacePhone, foodPlacePhone);
                getContentResolver().insert(TrendyBitesContentProvider.CONTENT_URI, values);
            }
            getLoaderManager().initLoader(1, null, BudgetPreferences.this);
        }
        else if(cursor==null && cursorCount>=2)
            Toast.makeText(BudgetPreferences.this,"Sorry!! No food items within the selected range", Toast.LENGTH_SHORT).show();
        else
            mAdapter.swapCursor(cursor);
        cursorCount++;
        //cursor.close();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    private void parseJSON(JSONObject jsonObject){
        Log.d(tag, "Inside parseJSON");
        if(jsonObject!=null){
            try{
                if(jsonObject.has("venues") && jsonObject.getJSONArray("venues").length()>0){
                    JSONArray menusArray=null;

                    if(jsonObject.getJSONArray("venues").getJSONObject(0).has("menus"))
                        menusArray = jsonObject.getJSONArray("venues").getJSONObject(0).getJSONArray("menus");
                    if(jsonObject.getJSONArray("venues").getJSONObject(0).has("menu_items"))
                        menusArray = jsonObject.getJSONArray("venues").getJSONObject(0).getJSONArray("menu_items");

                    int i,j,k,m,arrayIndex=0;

                    if(menusArray!=null){
                        for(k=0;k<menusArray.length();k++){
                            JSONObject menu = menusArray.getJSONObject(k);

                            JSONArray sectionsArray = menu.getJSONArray("sections");
                            String sectionName="";

                            for(i=0;i<sectionsArray.length();i++){
                                JSONObject sectionObject = sectionsArray.getJSONObject(i);
                                sectionName = sectionObject.getString("section_name");
                                //Log.d("FoodPlaceMenu", "Section Name: " + sectionName);
                                JSONArray subsectionsArray = sectionObject.getJSONArray("subsections");
                                String subsectionName="";
                                //.d("FoodPlaceMenu", "SubSection Name: " + subsectionName);

                                for(j=0;j<subsectionsArray.length();j++){
                                    JSONObject subsectionObject = subsectionsArray.getJSONObject(j);
                                    //subsectionName = subsectionObject.getString("subsection_name");
                                    //Log.d("FoodPlaceMenu", "SubSection Name: " + subsectionName);
                                    if(subsectionObject.has("contents")){
                                        JSONArray contentsArray = subsectionObject.getJSONArray("contents");
                                        String foodName="",foodPrice="";

                                        for(m=0;m<contentsArray.length();m++){
                                            JSONObject contentObject = contentsArray.getJSONObject(m);

                                            if(contentObject.has("name")){
                                                foodName = contentObject.getString("name");
                                                //Log.d("FoodPlaceMenu", "Food Name: " + foodName);
                                                if(contentObject.has("price")){
                                                    foodPrice = contentObject.getString("price");
                                                    //Log.d("FoodPlaceMenu", "Food Price: " + foodPrice);
                                                }
                                                if(foodName!=null && !foodName.isEmpty()){
                                                    tempFoodNames.add(foodName);
                                                    if(sectionName!=null && !sectionName.isEmpty())
                                                        tempFoodSections.add(sectionName);
                                                    else
                                                        tempFoodSections.add("");

                                                   /* if(foodPrice!=null && !foodPrice.isEmpty())
                                                        tempFoodPrices.add(foodPrice);
                                                    else
                                                        tempFoodPrices.add("");*/
                                                    float tempFoodPrice = extractNumber(foodPrice);
                                                    tempFoodPrices.add(tempFoodPrice);
                                                    /*Log.d(tag, "Food Name: " + foodNames[arrayIndex]);
                                                    Log.d(tag, "Food Section: " + foodSections[arrayIndex]);
                                                    Log.d(tag, "Food Price: " + foodPrices[arrayIndex]);*/
                                                    arrayIndex++;
                                                }
                                            }

                                        }
                                    }
                                }
                            }
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

    public static float extractNumber(final String str) {

        if(str == null || str.isEmpty())
            return 0;

        StringBuilder sb = new StringBuilder();

        for(char c : str.toCharArray()){
            if(Character.isDigit(c) || c=='.'){
                sb.append(c);
            }
        }
        if(sb==null || sb.toString().equals(""))
            return 0;
        return Float.parseFloat(sb.toString());
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
            Log.d(tag + ": AsyncTask:", "onPostExecute called with result " + result);
            if(tempFoodNames.size()>0 && tempFoodSections.size()>0 && tempFoodPrices.size()>0) {
                Log.d(tag, "Found menu for the given restaurant");
                foodNames = new String[tempFoodNames.size()];
                foodNames = tempFoodNames.toArray(foodNames);
                foodSections = new String[tempFoodSections.size()];
                foodSections = tempFoodSections.toArray(foodSections);
                foodPrices = new Float[tempFoodPrices.size()];
                foodPrices = tempFoodPrices.toArray(foodPrices);
                tempFoodNames.clear();
                tempFoodPrices.clear();
                tempFoodSections.clear();
                getLoaderManager().initLoader(0, null, BudgetPreferences.this);
            }
            else{
                Toast.makeText(BudgetPreferences.this, "Sorry. There's no menu available online.", Toast.LENGTH_LONG).show();
                startActivity(new Intent(BudgetPreferences.this, MainActivity.class));
            }
            asyncHttpRequest = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_food_place_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_settings:
                startActivity(new Intent(this, Settings.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
