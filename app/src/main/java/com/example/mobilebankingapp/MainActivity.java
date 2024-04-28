package com.example.mobilebankingapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity {
    private float totExpense = 0.0f;
    private float foodExpense = 0.0f;
    private float rentExpense = 0.0f;
    private float educationExpense = 0.0f;
    private float entertainmentExpense = 0.0f;
    private float transportExpense = 0.0f;
    private float healthExpense = 0.0f;
    private float otherExpense = 0.0f;
    private static final String TAG = "MAIN_ACT_TAG";

    private float totExpenseLimit;
    private float foodExpenseLimit;
    private float rentExpenseLimit;
    private float educationExpenseLimit;
    private float entertainmentExpenseLimit;
    private float transportExpenseLimit;
    private float healthExpenseLimit;
    private float otherExpenseLimit;
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

            loadBudgetData();
            loadCatExpData();



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

    private void checkOverflowInBudget() {
        Log.d(TAG, "checkOverflowInBudget");

        Log.d(TAG, "called edu budgt: " + educationExpenseLimit);
        Log.d(TAG, "called edu expese: " + educationExpense);
        // Compare expenses with limits and trigger notifications
        if (totExpenseLimit > 0.0f && totExpense > totExpenseLimit) {
            makeNotification("Total expense");
        }
        if (foodExpenseLimit > 0.0f && foodExpense > foodExpenseLimit) {
            makeNotification("Food");
        }
        if (rentExpenseLimit > 0.0f && rentExpense > rentExpenseLimit) {
            makeNotification("Rent");
        }
        if (educationExpenseLimit > 0.0f && educationExpense > educationExpenseLimit) {
            Log.d(TAG, "called");
            makeNotification("Education");
        }
        if (entertainmentExpenseLimit > 0.0f && entertainmentExpense > entertainmentExpenseLimit) {
            makeNotification("Entertainment");
        }
        if (healthExpenseLimit > 0.0f && healthExpense > healthExpenseLimit) {
            makeNotification("Health care");
        }
        if (otherExpenseLimit > 0.0f && otherExpense > otherExpenseLimit) {
            makeNotification("Others category");
        }
    }

    private void loadCatExpData() {

        Log.d(TAG, "loadCatExpData");

        // Construct the database reference for the current month/year
        DatabaseReference monthYearRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(getAccountNumberFromSharedPreferences()) // Using the user's account number as the reference
                .child("Transactions")
                .child(formatTimestampToMonthYear(Utils.getTimestamp())) // Month/year node
                .child("OtherData");

        // Fetch data for the specified month/year
        monthYearRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@org.checkerframework.checker.nullness.qual.NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve category totals with default values if the category does not exist
                    Float foodExpenseDB = dataSnapshot.child("FoodCatTotal" + formatTimestampToMonthYear(Utils.getTimestamp())).getValue(Float.class);
                    Float rentExpenseDB = dataSnapshot.child("RentCatTotal" + formatTimestampToMonthYear(Utils.getTimestamp())).getValue(Float.class);
                    Float transportExpenseDB = dataSnapshot.child("TransportationCatTotal" + formatTimestampToMonthYear(Utils.getTimestamp())).getValue(Float.class);
                    Float entertainmentExpenseDB = dataSnapshot.child("EntertainmentCatTotal" + formatTimestampToMonthYear(Utils.getTimestamp())).getValue(Float.class);
                    Float healthcareExpenseDB = dataSnapshot.child("HealthcareCatTotal" + formatTimestampToMonthYear(Utils.getTimestamp())).getValue(Float.class);
                    Float educationExpenseDB = dataSnapshot.child("EducationsCatTotal" + formatTimestampToMonthYear(Utils.getTimestamp())).getValue(Float.class);
                    Float otherExpenseDB = dataSnapshot.child("OthersCatTotal" + formatTimestampToMonthYear(Utils.getTimestamp())).getValue(Float.class);

                    Float totalExpenseDB = dataSnapshot.child("totalExpenseFor" + formatTimestampToMonthYear(Utils.getTimestamp())).getValue(Float.class);

                    // Handle null values by providing default values if necessary
                    foodExpense = (foodExpenseDB != null) ? foodExpenseDB : 0f;
                    rentExpense = (rentExpenseDB != null) ? rentExpenseDB : 0f;
                    transportExpense = (transportExpenseDB != null) ? transportExpenseDB : 0f;
                    entertainmentExpense = (entertainmentExpenseDB != null) ? entertainmentExpenseDB : 0f;
                    healthExpense = (healthcareExpenseDB != null) ? healthcareExpenseDB : 0f;
                    educationExpense = (educationExpenseDB != null) ? educationExpenseDB : 0f;
                    otherExpense = (otherExpenseDB != null) ? otherExpenseDB : 0f;
                    totExpense = (totalExpenseDB != null) ? totalExpenseDB : 0f;

                    Log.d(TAG, "Total Food Expense: " + foodExpense);
                    Log.d(TAG, "Total Rent Expense: " + rentExpense);
                    Log.d(TAG, "Total Transport Expense: " + transportExpense);
                    Log.d(TAG, "Total Entertainment Expense: " + entertainmentExpense);
                    Log.d(TAG, "Total Healthcare Expense: " + healthExpense);
                    Log.d(TAG, "Total Education Expense: " + educationExpense);
                    Log.d(TAG, "Total Other Expense: " + otherExpense);
                    Log.d(TAG, "Total totExpense: " + totExpense);

//
                    checkOverflowInBudget();
//                    // Retrieve total expense and income with default values if not found
//                    Float totalExp = dataSnapshot.child("totalExpenseFor" + formatTimestampToMonthYear(Utils.getTimestamp())).getValue(Float.class);
//                    Float totalInc = dataSnapshot.child("totalIncomeFor" + formatTimestampToMonthYear(Utils.getTimestamp())).getValue(Float.class);

                }
            }

            @Override
            public void onCancelled(@org.checkerframework.checker.nullness.qual.NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read expenses data", databaseError.toException());
                // Handle onCancelled event, such as displaying an error message to the user
                // or retrying the database operation
            }
        });
    }

    private void loadBudgetData() {
        Log.d(TAG, "loadBudgetData" + getAccountNumberFromSharedPreferences());

        // Construct the database reference for the current month/year
        DatabaseReference budgetRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(getAccountNumberFromSharedPreferences()) // Using the user's account number as the reference
                .child("Budgets");

        // Fetch data for the specified month/year
        budgetRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve category totals as strings from Firebase
                    String foodExpenseLimitStr = dataSnapshot.child("Food").getValue(String.class);
                    String rentExpenseLimitStr = dataSnapshot.child("Rent").getValue(String.class);
                    String transportExpenseLimitStr = dataSnapshot.child("Transportation").getValue(String.class);
                    String entertainmentExpenseLimitStr = dataSnapshot.child("Entertainment").getValue(String.class);
                    String healthExpenseLimitStr = dataSnapshot.child("Healthcare").getValue(String.class);
                    String educationExpenseLimitStr = dataSnapshot.child("Educations").getValue(String.class);
                    String otherExpenseLimitStr = dataSnapshot.child("Others").getValue(String.class);
                    String totExpenseLimitStr = dataSnapshot.child("TotalExpenses").getValue(String.class);

                    // Convert string values to float, handling null cases
                    foodExpenseLimit = parseFloatOrDefault(foodExpenseLimitStr, 0f);
                    rentExpenseLimit = parseFloatOrDefault(rentExpenseLimitStr, 0f);
                    transportExpenseLimit = parseFloatOrDefault(transportExpenseLimitStr, 0f);
                    entertainmentExpenseLimit = parseFloatOrDefault(entertainmentExpenseLimitStr, 0f);
                    healthExpenseLimit = parseFloatOrDefault(healthExpenseLimitStr, 0f);
                    educationExpenseLimit = parseFloatOrDefault(educationExpenseLimitStr, 0f);
                    otherExpenseLimit = parseFloatOrDefault(otherExpenseLimitStr, 0f);
                    totExpenseLimit = parseFloatOrDefault(totExpenseLimitStr, 0f);

                    // Log expense limits for debugging
                    Log.d(TAG, "Food Expense Limit: " + foodExpenseLimit);
                    Log.d(TAG, "Rent Expense Limit: " + rentExpenseLimit);
                    Log.d(TAG, "Transport Expense Limit: " + transportExpenseLimit);
                    Log.d(TAG, "Entertainment Expense Limit: " + entertainmentExpenseLimit);
                    Log.d(TAG, "Healthcare Expense Limit: " + healthExpenseLimit);
                    Log.d(TAG, "Education Expense Limit: " + educationExpenseLimit);
                    Log.d(TAG, "Other Expense Limit: " + otherExpenseLimit);
                    Log.d(TAG, "Total Expense Limit: " + totExpenseLimit);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read expenses data", databaseError.toException());
                // Handle onCancelled event, such as displaying an error message to the user
                // or retrying the database operation
            }
        });
    }

    // Helper method to parse float from string or return default value
    private float parseFloatOrDefault(String valueStr, float defaultValue) {
        if (valueStr != null && !valueStr.isEmpty()) {
            try {
                return Float.parseFloat(valueStr);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing float value: " + valueStr, e);
            }
        }
        return defaultValue;
    }

    private String formatTimestampToMonthYear(Long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("MMyy");
        return sdf.format(calendar.getTime());
    }
    // NotificationManager
    private void makeNotification(String category) {
        String channelId = "CHANNEL_ID_NOTIFICATION";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Alert")
                .setContentText("Expense limit exceed")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("You are exceeding your budget for "+ category))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // NotificationManager
        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        // Check if the version is Android Oreo or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Notification Channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            // Register the channel with the system
            notificationManager.createNotificationChannel(channel);
        }

        // Show the notification
        notificationManager.notify(generateNotificationId(), builder.build());
    }
    private int generateNotificationId() {
        // Use current system time as notification ID
        return (int) System.currentTimeMillis();
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

    private String getAccountNumberFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(RegisterActivity.SHARED_PREF_NAME, MODE_PRIVATE);
        return sharedPreferences.getString(RegisterActivity.ACCOUNT_NUMBER_KEY, "");
    }

}