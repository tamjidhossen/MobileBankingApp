package com.example.mobilebankingapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.widget.Button;
import android.widget.TextView;

import org.checkerframework.checker.nullness.qual.NonNull;

public class StatisticsFragment extends Fragment implements OnChartValueSelectedListener {
    private TextView expenseRateTV;
    private TextView incomeRateTV;
    private TextView totalExpenseTV;
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

    private float incomeRate;
    private float expenseRate;
    private float totalExpenseVal;

    private static final String TAG = "STAT_TAG";

    private PieChart pieChart;

    private DatabaseReference expensesRef; // Reference to expenses data in Firebase
    public StatisticsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_statistics, container, false);

        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        pieChart = rootView.findViewById(R.id.pieChart);

        // Set the listener for the PieChart
        pieChart.setOnChartValueSelectedListener(this);

        loadDataFromDB();

        // Find the TextView
        Button buttonHealth = rootView.findViewById(R.id.btnHealth);

        //Texview
        expenseRateTV = rootView.findViewById(R.id.expenseRateId);
        incomeRateTV = rootView.findViewById(R.id.incomeRateId);
        totalExpenseTV = rootView.findViewById(R.id.TotalExpenseId);


        // Set OnClickListener on the Button
        buttonHealth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the activity you want to open
                openFinancialHealthActivity();
            }
        });

        //calling the rate calculating function
        calculateRates();

        Button buttonBehaviour = rootView.findViewById(R.id.btnBehaviour);

        // Set OnClickListener on the TextView
        buttonBehaviour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the activity you want to open
                openFinancialSpendingActivity();
            }
        });


        return rootView;
    }



    @Override
    public void onValueSelected(Entry e, Highlight h) {
        // Display the name of the selected slice
        if (e instanceof PieEntry) {
            PieEntry pieEntry = (PieEntry) e;
            showSliceName(pieEntry.getLabel());
        }
    }

    @Override
    public void onNothingSelected() {
        // Hide the slice name when nothing is selected
        hideSliceName();
    }

    private void showSliceName(String name) {
        // Display the slice name using a TextView or any other UI element
        // For example:
        TextView sliceNameTextView = getView().findViewById(R.id.sliceNameTextView);
        sliceNameTextView.setText(name);
        sliceNameTextView.setVisibility(View.VISIBLE);
    }

    private void hideSliceName() {
        // Hide the slice name TextView
        TextView sliceNameTextView = getView().findViewById(R.id.sliceNameTextView);
        sliceNameTextView.setVisibility(View.GONE);
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
                    Float educationExpense = dataSnapshot.child("EducationCatTotal" + formatTimestampToMonthYear(Utils.getTimestamp())).getValue(Float.class);
                    Float otherExpense = dataSnapshot.child("OtherCatTotal" + formatTimestampToMonthYear(Utils.getTimestamp())).getValue(Float.class);
                    if (foodExpense != null) {
                        totFoodExpense = foodExpense;
                    } else {
                        totFoodExpense = 0f; // Provide a default value or handle the null case appropriately
                    }
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
                    // Update UI with loaded data
                    totalExpenseVal = currentMonthExpense;

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


    private void updateUI() {
        // Update PieChart with dynamic data
        PieChart pieChart = getView().findViewById(R.id.pieChart);
        setupPieChart(pieChart);

        // Update total expense TextView
        totalExpenseTV.setText(String.format("Total Expense: %.2f", totalExpenseVal));

        // Calculate and display rates
        calculateRates();
    }

    private void setupPieChart(PieChart pieChart) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(totFoodExpense, "Food"));
        entries.add(new PieEntry(totRentExpense, "Rent"));
        entries.add(new PieEntry(totTransportExpense, "Transport"));
        entries.add(new PieEntry(totEntertainmentExpense, "Entertainment"));
        entries.add(new PieEntry(totHealthExpense, "Health"));
        entries.add(new PieEntry(totEducationExpense, "Education"));
        entries.add(new PieEntry(totOtherExpense, "Others"));


        // Individual colors for each entry
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.argb(255, 0, 100, 255)); // Dark blue
        colors.add(Color.argb(200, 30, 100, 255)); // Slightly lighter
        colors.add(Color.argb(170, 30, 50, 255)); // Even lighter
        colors.add(Color.argb(140, 30, 30, 255)); // Lighter
        colors.add(Color.argb(120, 30, 40, 255));  // Very light
        colors.add(Color.argb(100, 0, 50, 255));  // Very very light
        colors.add(Color.argb(80, 10, 60, 255));  // Almost white;

        PieDataSet dataSet = new PieDataSet(entries, "Expending Analysis");
        dataSet.setValueTextSize(12f);
        dataSet.setColors(colors); // Set individual colors for each entry
        dataSet.setDrawValues(false); // Disable drawing values on the slices


        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false); // Hide description
        pieData.setDrawValues(false);
        dataSet.setDrawValues(false);
        pieChart.setDrawSliceText(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.animateY(500);
        pieChart.invalidate();
    }
    private void openFinancialHealthActivity() {
        Intent intent = new Intent(getActivity(), FinancialHealthActivity.class);
        startActivity(intent);
    }
    private void openFinancialSpendingActivity() {
        Intent intent = new Intent(getActivity(), SpendingActivity.class);
        startActivity(intent);
    }
    private void calculateRates() {
        // Assuming currentMonthExpense, previousMontExpense, currentMonthIncome, and previousMontIncome
        // are already initialized with appropriate values.

        // Calculate expense rate change
        if (previousMontExpense != 0) {
            expenseRate = ((currentMonthExpense - previousMontExpense) / previousMontExpense) * 100;
        } else {
            // Handle division by zero scenario
            expenseRate = 0;
        }

        // Calculate income rate change
        if (previousMontIncome != 0) {
            incomeRate = ((currentMonthIncome - previousMontIncome) / previousMontIncome) * 100;
        } else {
            // Handle division by zero scenario
            incomeRate = 0;
        }

        // Display the calculated rates in TextViews
        expenseRateTV.setText(String.format("%.2f%%", expenseRate));
        incomeRateTV.setText(String.format("%.2f%%", incomeRate));
    }


    private String getAccountNumberFromSharedPreferences() {
        // Use getActivity() to get the activity associated with the fragment
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(RegisterActivity.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(RegisterActivity.ACCOUNT_NUMBER_KEY, "");
    }


    private String formatTimestampToMonthYear(Long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("MMyy");
        return sdf.format(calendar.getTime());
    }
}