package com.example.mxg1055.trendybites;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

public class BudgetQuestionnaire extends AppCompatActivity  {

    private static String foodPlacePhone;
    private static final  String tag = "Budget";
    private static String budgetPreference="";
    Spinner spinner1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(tag, "Inside onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_questionnaire);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        foodPlacePhone = bundle.getString("FoodPlacePhone");
        Log.d(tag, foodPlacePhone);

        spinner1 = (Spinner) findViewById(R.id.foodBudget);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, R.array.foodBudgetArray, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter1);
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View v,
                                       int pos, long id) {
                Log.d(tag, "Inside onItemSelected");
                budgetPreference = spinner1.getSelectedItem().toString();
                Log.d(tag, budgetPreference);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {


            }
        });
        //budgetPreference = spinner1.getSelectedItem().toString();
        //Log.d(tag, budgetPreference);

        Button suggest = (Button) findViewById(R.id.btnSuggest);
        suggest.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent budgetPreferencesIntent = new Intent(BudgetQuestionnaire.this, BudgetPreferences.class);
                budgetPreferencesIntent.putExtra("FoodPlacePhone", foodPlacePhone);
                budgetPreferencesIntent.putExtra("BudgetPreference", budgetPreference);
                startActivity(budgetPreferencesIntent);
            }
        });
    }

    public void onItemSelected(AdapterView<?> parent, View arg1, int pos,long id) {
        budgetPreference = spinner1.getSelectedItem().toString();
        Log.d(tag, budgetPreference);
    }
    @Override
    protected void onResume(){
        Log.d(tag, "Inside onResume");
        super.onResume();
        budgetPreference = spinner1.getSelectedItem().toString();
        Log.d(tag, budgetPreference);

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d(tag, "Inside onSI");
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("budget", budgetPreference);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(tag, "Inside onRI");
        super.onRestoreInstanceState(savedInstanceState);
        budgetPreference = savedInstanceState.getString("budget");
        budgetPreference = spinner1.getSelectedItem().toString();
        Log.d(tag, budgetPreference);

    }
}
