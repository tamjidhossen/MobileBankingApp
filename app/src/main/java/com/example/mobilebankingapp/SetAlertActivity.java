package com.example.mobilebankingapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mobilebankingapp.databinding.ActivitySetAlertBinding;
import com.example.mobilebankingapp.databinding.ActivityTransactionBinding;
import com.google.firebase.auth.FirebaseAuth;

public class SetAlertActivity extends AppCompatActivity {
    private ActivitySetAlertBinding binding;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //drop down box
        binding = ActivitySetAlertBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Firebase Auth for auth related tasks
        firebaseAuth = FirebaseAuth.getInstance();

        //Setup and set the categories adapter to the Category Input Filed i.e. categoryAct
        ArrayAdapter<String> adapterCategories = new ArrayAdapter<>(this, R.layout.row_category_act, Utils.categories);
        binding.categorySetAlert.setAdapter(adapterCategories);

    }


    private String getAccountNumberFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(RegisterActivity.SHARED_PREF_NAME, MODE_PRIVATE);
        return sharedPreferences.getString(RegisterActivity.ACCOUNT_NUMBER_KEY, "");
    }

}