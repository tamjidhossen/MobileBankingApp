package com.example.mobilebankingapp;

import static java.lang.Math.abs;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SpendingActivity extends AppCompatActivity {

    private TextView foodCategoryValueTV;
    private TextView transportationCategoryValueTV;
    private TextView rentCategoryValueTV;
    private TextView entertainmentCategoryValueTV;
    private TextView healthcareCategoryValueTV;
    private TextView educationCategoryValueTV;
    private TextView otherCategoryValueTV;
    private TextView currentMonthExpenseTV;
    private TextView lastMonthExpenseTV;
    private TextView thisMonthNameTv;
    private TextView trendRsltTv;

    private float totFoodExpense;
    private float totRentExpense;
    private float totTransportExpense;
    private float totEntertainmentExpense;
    private float totHealthExpense;
    private float totEducationExpense;
    private float totOtherExpense;
    private float currentMonthExpense;
    private float previousMontExpense;
    private float currentMonthIncome;
    private float previousMontIncome;


    private static final String TAG = "SPENDING_ACT_TAG";

    private BarChart barChart;
    private List<MonthData> monthDataList;

    public static class MonthData {
        String month;
        float expense;

        public MonthData(String month, float expense) {
            this.month = month;
            this.expense = expense;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_spending);

        loadDataFromDB();

        barChart = (HorizontalBarChart)findViewById(R.id.barchartCompareTwoExpense); // Getting Id of the bar chart

        foodCategoryValueTV = findViewById(R.id.foodsCategoryValueTV);
        transportationCategoryValueTV = findViewById(R.id.transportationCategoryValueTV);
        rentCategoryValueTV = findViewById(R.id.rentCategoryValueTV);
        entertainmentCategoryValueTV = findViewById(R.id.entertainmentCategoryValueTV);
        healthcareCategoryValueTV = findViewById(R.id.healthcareCategoryValueTV);
        educationCategoryValueTV = findViewById(R.id.educationCategoryValueTV);
        otherCategoryValueTV = findViewById(R.id.otherCategoryValueTV);
        thisMonthNameTv = findViewById(R.id.thisMonthLabelTv);
        trendRsltTv = findViewById(R.id.trendResult);

        // Initialize the toolbar back button
        ImageButton toolbarBackBtn = findViewById(R.id.toolbarBackBtn);

        // Set click listener for the toolbar back button
        toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed(); // Handle back navigation
            }
        });



    }

    private void loadDataFromDB() {

        loadPrevMonthData();

        // Construct the database reference for the current month/year
        DatabaseReference monthYearRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(getAccountNumberFromSharedPreferences()) // Using the user's account number as the reference
                .child("Transactions")
                .child(formatTimestampToMonthYear(Utils.getTimestamp())) // Month/year node
                .child("OtherData");

        // Fetch data for the specified month/year
        monthYearRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve category totals with default values if the category does not exist
                    Float foodExpense = dataSnapshot.child("FoodCatTotal" + formatTimestampToMonthYear(Utils.getTimestamp())).getValue(Float.class);
                    Float rentExpense = dataSnapshot.child("RentCatTotal" + formatTimestampToMonthYear(Utils.getTimestamp())).getValue(Float.class);
                    Float transportExpense = dataSnapshot.child("TransportationCatTotal" + formatTimestampToMonthYear(Utils.getTimestamp())).getValue(Float.class);
                    Float entertainmentExpense = dataSnapshot.child("EntertainmentCatTotal" + formatTimestampToMonthYear(Utils.getTimestamp())).getValue(Float.class);
                    Float healthcareExpense = dataSnapshot.child("HealthcareCatTotal" + formatTimestampToMonthYear(Utils.getTimestamp())).getValue(Float.class);
                    Float educationExpense = dataSnapshot.child("EducationsCatTotal" + formatTimestampToMonthYear(Utils.getTimestamp())).getValue(Float.class);
                    Float otherExpense = dataSnapshot.child("OthersCatTotal" + formatTimestampToMonthYear(Utils.getTimestamp())).getValue(Float.class);
//                    if (foodExpense != null) {
//                        totFoodExpense = foodExpense;
//                    } else {
//                        totFoodExpense = 0f; // Provide a default value or handle the null case appropriately
//                    }
                    // Handle null values by providing default values if necessary
//                    totFoodExpense = (foodExpense != null) ? foodExpense : 0f;
                    totRentExpense = (rentExpense != null) ? rentExpense : 0f;
                    totTransportExpense = (transportExpense != null) ? transportExpense : 0f;
                    totEntertainmentExpense = (entertainmentExpense != null) ? entertainmentExpense : 0f;
                    totHealthExpense = (healthcareExpense != null) ? healthcareExpense : 0f;
                    totEducationExpense = (educationExpense != null) ? educationExpense : 0f;
                    totOtherExpense = (otherExpense != null) ? otherExpense : 0f;

                    Log.d(TAG, "Total Food Expense: " + totFoodExpense);
                    Log.d(TAG, "Total Rent Expense: " + totRentExpense);
                    Log.d(TAG, "Total Transport Expense: " + totTransportExpense);
                    Log.d(TAG, "Total Entertainment Expense: " + totEntertainmentExpense);
                    Log.d(TAG, "Total Healthcare Expense: " + totHealthExpense);
                    Log.d(TAG, "Total Education Expense: " + totEducationExpense);
                    Log.d(TAG, "Total Other Expense: " + totOtherExpense);


                    // Retrieve total expense and income with default values if not found
                    Float totalExp = dataSnapshot.child("totalExpenseFor" + formatTimestampToMonthYear(Utils.getTimestamp())).getValue(Float.class);
                    Float totalInc = dataSnapshot.child("totalIncomeFor" + formatTimestampToMonthYear(Utils.getTimestamp())).getValue(Float.class);

                    currentMonthExpense = (totalExp != null) ? totalExp : 0f;
                    currentMonthIncome = (totalInc != null) ? totalInc : 0f;
                    Log.d(TAG, "currentMonthExpense: " + currentMonthExpense);
                    Log.d(TAG, "currentMonthIncome: " + currentMonthIncome);

                    updateUI();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("TransactionActivity", "Failed to read expenses data", databaseError.toException());
                // Handle onCancelled event, such as displaying an error message to the user
                // or retrying the database operation
            }
        });
    }

    private void loadPrevMonthData() {
        String previousMonthYear = getPreviousMonthYear();

        Log.d(TAG, "previousMonthYear: " + previousMonthYear);

        // Construct the database reference for the previous month/year
        DatabaseReference previousMonthRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(getAccountNumberFromSharedPreferences())
                .child("Transactions")
                .child(previousMonthYear) // Previous month/year node
                .child("OtherData");

        // Fetch data for the previous month/year
        previousMonthRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve previous month's expense and income
                    Float previousExpense = dataSnapshot.child("totalExpenseFor" + previousMonthYear).getValue(Float.class);
                    Float previousIncome = dataSnapshot.child("totalIncomeFor" + previousMonthYear).getValue(Float.class);



                    Log.d(TAG, "previousExpense: " + previousExpense);
                    Log.d(TAG, "previousIncome: " + previousIncome);
                    // Handle null values
                    previousMontExpense = (previousExpense != null) ? previousExpense : 0f;
                    previousMontIncome = (previousIncome != null) ? previousIncome : 0f;

                    // Log the retrieved values for debugging
                    Log.d(TAG, "Previous Month Expense: " + previousMontExpense);
                    Log.d(TAG, "Previous Month Income: " + previousMontIncome);
                } else {
                    Log.d(TAG, "No data found for previous month: " + previousMonthYear);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read previous month's data", databaseError.toException());
                // Handle onCancelled event, such as displaying an error message to the user
                // or retrying the database operation
            }
        });
    }

    public static String getPreviousMonthYear() {
        // Get the current date and time
        Calendar calendar = Calendar.getInstance();

        // Subtract one month from the current date
        calendar.add(Calendar.MONTH, -1);

        // Format the calendar's date to "MMyy" (e.g., "0324" for March 2024)
        SimpleDateFormat sdf = new SimpleDateFormat("MMyy");
        return sdf.format(calendar.getTime());
    }

    private String getAccountNumberFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(RegisterActivity.SHARED_PREF_NAME, MODE_PRIVATE);
        return sharedPreferences.getString(RegisterActivity.ACCOUNT_NUMBER_KEY, "");
    }


    private String formatTimestampToMonthYear(Long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("MMyy");
        return sdf.format(calendar.getTime());
    }

    private void updateUI() {
        // Update TextViews with the retrieved expense values, adding a dollar sign before each value
        foodCategoryValueTV.setText(String.format("$%.2f", totFoodExpense));
        rentCategoryValueTV.setText(String.format("$%.2f", totRentExpense));
        transportationCategoryValueTV.setText(String.format("$%.2f", totTransportExpense));
        entertainmentCategoryValueTV.setText(String.format("$%.2f", totEntertainmentExpense));
        healthcareCategoryValueTV.setText(String.format("$%.2f", totHealthExpense));
        educationCategoryValueTV.setText(String.format("$%.2f", totEducationExpense));
        otherCategoryValueTV.setText(String.format("$%.2f", totOtherExpense));


        String thisMonthName = getThisMonthName();
        String prevMonthName = getPrevMonthName();

        // Calculate percentage change
        double expenseChange = currentMonthExpense - previousMontExpense;
        double percentageChange;

        if(expenseChange > 0) percentageChange = (expenseChange / previousMontExpense) * 100.0;
        else percentageChange = (expenseChange / currentMonthExpense) * 100.0;

        // Determine the color and sign for the UI
        String trendText;
        int trendColor;

        if (expenseChange > 0) {
            trendText = String.format("+%.2f%%", Math.abs(percentageChange));
            trendColor = Color.parseColor("#800020"); // Red color
        } else if (expenseChange < 0) {
            trendText = String.format("-%.2f%%", Math.abs(percentageChange));
            trendColor = Color.parseColor("#0d5e59"); //Green
        } else {
            trendText = "0.00%";
            trendColor = Color.parseColor("#000000"); // Black color (default)
        }

        // Display the trend result in the TextView
        trendRsltTv.setText(trendText);
        trendRsltTv.setTextColor(trendColor);

        thisMonthNameTv.setText(thisMonthName);

        monthDataList = new ArrayList<>();
        monthDataList.add(new MonthData(thisMonthName, currentMonthExpense));
        monthDataList.add(new MonthData(prevMonthName, previousMontExpense));

        // Setting data into chart
        setData(monthDataList);
    }


    private void setData(List<MonthData> monthDataList) {

        String thisMonthName = getThisMonthName();
        String prevMonthName = getPrevMonthName();

        ArrayList<BarEntry> expenseEntries = new ArrayList<>();
        String[] labels = new String[monthDataList.size()]; // Month names for X-axis

        for (int i = 0; i < monthDataList.size(); i++) {
            MonthData data = monthDataList.get(i);
            expenseEntries.add(new BarEntry(i, data.expense));
            labels[i] = ""; // Empty string to avoid labels on X-axis
        }

        // Create separate datasets for Last Month and Current Month with different colors
        BarDataSet currentMonthDataSet = new BarDataSet(expenseEntries.subList(1, 2), prevMonthName);
        currentMonthDataSet.setColor(Color.parseColor("#7CB9E8")); // Light blue for current month
        currentMonthDataSet.setValueTextColor(Color.WHITE); // Set value text color (e.g., white)

        BarDataSet lastMonthDataSet = new BarDataSet(expenseEntries.subList(0, 1), thisMonthName);
        lastMonthDataSet.setColor(Color.parseColor("#005FAF")); // Dark blue for last month
        lastMonthDataSet.setValueTextColor(Color.WHITE); // Set value text color (e.g., black)
        // Combine datasets into BarData
        BarData barData = new BarData(lastMonthDataSet, currentMonthDataSet);
        barData.setValueTextSize(16f); // Set value text size

        barChart.setScaleEnabled(false);// remove all zooming
        barChart.setData(barData);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels)); // Empty labels on X-axis
        barChart.getAxisLeft().setAxisMinimum(0f); // Set minimum value for Y-axis
        barChart.getLegend().setEnabled(true);
        barData.setValueTextSize(16f); // Set value text size
        barChart.setData(barData);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels)); // Set month labels
        barChart.getAxisLeft().setAxisMinimum(0f); // Set minimum value for Y-axis
        barChart.getLegend().setEnabled(true); // Enable chart legend (optional)
        barChart.getXAxis().setDrawGridLines(false); // Remove vertical grid lines
        barChart.getAxisLeft().setDrawGridLines(false); // Remove horizontal grid lines
        barChart.getXAxis().setDrawLabels(false); // Remove labels from the X-axis
        barChart.getAxisLeft().setDrawLabels(false); // Remove labels from the Y-axis
        barChart.getDescription().setEnabled(false); // Hide chart description
        barChart.setExtraBottomOffset(20f); // Add extra bottom offset (in pixels)
        // Set padding for the chart view (left, top, right, bottom)
        barChart.setPadding(barChart.getPaddingLeft(), barChart.getPaddingTop(), barChart.getPaddingRight(), 50); // Adjust bottom padding

        barChart.setPinchZoom(false); // Disable pinch zoom
        barChart.animateY(500); // Add animation
        barChart.setMinimumHeight((int) 100f);// (optional)
        barChart.setDrawValueAboveBar(false);
        barChart.invalidate(); // Refresh chart
    }


    // Function to get the name of the current month
    private String getThisMonthName() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

    // Function to get the name of the previous month
    private String getPrevMonthName() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }
}
