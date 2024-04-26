package com.example.mobilebankingapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.mobilebankingapp.databinding.ActivityLoginBinding;
import com.example.mobilebankingapp.databinding.ActivityMainBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.example.mobilebankingapp.StatisticsFragment;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    //Firebase Auth for auth related tasks
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super. onCreate(savedInstanceState);

        //activity_main.xml = ActivityMainBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //get instance of firebase auth for Auth related tasks
        firebaseAuth = FirebaseAuth.getInstance();
        //check if user is logged in or not
        if (firebaseAuth.getCurrentUser() == null){
            //user is not logged in, move to LoginActivity
            startLoginOptions();
            finish();
        } else {

            //not working.......
            FloatingActionButton transactionBtn = findViewById(R.id.transactionBtn);
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_transfer);
            if (drawable != null) {
                drawable = DrawableCompat.wrap(drawable);
                DrawableCompat.setTint(drawable, ContextCompat.getColor(this, R.color.white));
                transactionBtn.setImageDrawable(drawable);
            }
            //...................

            showHomeFragment();

            //handle bottomNv item clicks to navigate between fragments
            binding.bottomNv.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    //get id of the menu item clicked
                    int itemId = item.getItemId();
                    if(itemId == R.id.menuHome) {
                        //home item clicked, show fragment
                        showHomeFragment();
                        return true;

                    } else if(itemId == R.id.menuHistory) {
                        showHistoryFragment();
                        return true;

                    } else if(itemId == R.id.menuStatistics) {
                        showStatisticsFragment();
                        return true;

                    } else if(itemId == R.id.menuProfile) {
                        showProfileFragment();
                        return true;

                    } else {
                        return false;
                    }
                }
            });

            //handle transactionBtn click, start TransactionActivity
            binding.transactionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(MainActivity.this, TransactionActivity.class));
                }
            });
        }

    }

    private void showHomeFragment() {
        //change toolbar textView text/title to Home
//        binding. toolbarTitleTv.setText ("Home");

        //Show HomeFragment
        HomeFragment fragment = new HomeFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment, "HomeFragment");
        fragmentTransaction.commit();

        // Set status bar color
//        setStatusBarColor(R.color.white, R.color.colorGray03);

    }
    private void showHistoryFragment(){
        //change toolbar textView text/title to My Ads
//        binding.toolbarTitleTv.setText("History");

        //Show MyAdsFragment
        HistoryFragment fragment = new HistoryFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment, "HistoryFragment");
        fragmentTransaction.commit();

        // Set status bar color
//        setStatusBarColor(R.color.DarkGreen, R.color.DarkGreen);
    }
    private void showStatisticsFragment(){
        //Show AccountFragment
        StatisticsFragment fragment = new StatisticsFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment, "StatisticsFragment");
        fragmentTransaction.commit();

        // Set status bar color
//        setStatusBarColor(R.color.white, R.color.colorGray03);
    }
    private void showProfileFragment(){
        //Show AccountFragment
        ProfileFragment fragment = new ProfileFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment, "ProfileFragment");
        fragmentTransaction.commit();

        // Set status bar color
//        setStatusBarColor(R.color.white, R.color.colorGray03);
    }
    private void startLoginOptions() {
        startActivity(new Intent(this, LoginActivity.class));
    }

}