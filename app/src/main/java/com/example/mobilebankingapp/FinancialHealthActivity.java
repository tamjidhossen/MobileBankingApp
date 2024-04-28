package com.example.mobilebankingapp;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebankingapp.R;
import com.github.mikephil.charting.charts.BarChart;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FinancialHealthActivity extends AppCompatActivity {

    private BarChart barChart;
    private List<MonthData> monthDataList;

    private TextView incomelastThreeMonth;
    private TextView expenselastThreeMonth;
    private TextView briefText;
    private TextView financialStatus;

    private float incomelastMon_1=10000;
    private float incomelastMon_2=12000;
    private float incomelastMon_3=13000;
    private float incomelastMon_4=10000;

    private float expenselastMon_1=5000;
    private float expenselastMon_2=7000;
    private float expenselastMon_3=8000;
    private float expenselastMon_4=7000;
    private float rate;
    private float totalLastThreeExpense;
    private float totalLastThreeIncome;
    private static final String TAG = "FINANCIAL_ACT_TAG";


    public static class MonthData {
        String month;
        float income;
        float expense;

        public MonthData(String month, float income, float expense) {
            this.month = month;
            this.income = income;
            this.expense = expense;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_financial_health);


        // Initialize monthDataList
        monthDataList = new ArrayList<>();


        loadDataFromDB();

        totalLastThreeExpense=expenselastMon_1+expenselastMon_2+expenselastMon_3;
        totalLastThreeIncome=incomelastMon_1+incomelastMon_2+incomelastMon_3;

        //TextView
//        incomelastThreeMonth = findViewById(R.id.totLastThreeMonthIncomeId);
//        expenselastThreeMonth = findViewById(R.id.totLastThreeMonthExpenseId);
        briefText = findViewById(R.id.BriefText);
        financialStatus = findViewById(R.id.financialStatusId);

        // Assuming totalLastThreeExpense and totalLastThreeIncome are Float variables representing expenses and income

        // Convert Float values to strings with two decimal points
        String formattedExpense = String.format("%.2f", totalLastThreeExpense);
        String formattedIncome = String.format("%.2f", totalLastThreeIncome);

        // Set the formatted values in TextViews
//        expenselastThreeMonth.setText(formattedExpense);
//        incomelastThreeMonth.setText(formattedIncome);


        if(totalLastThreeExpense<totalLastThreeIncome){

            rate = (totalLastThreeExpense/totalLastThreeIncome)*100;
            String formattedRate = String.format("%.2f%%", rate);
            briefText.setText("You spend "+formattedRate+" of total income.");
            if(rate*totalLastThreeIncome < 1000){
                financialStatus.setText("average");
            }
            else if(rate*totalLastThreeIncome < 3000){
                financialStatus.setText("good");
            }
            else if(rate*totalLastThreeIncome < 4000){
                financialStatus.setText("very good");
            }
            else if(rate*totalLastThreeIncome < 15000){
                financialStatus.setText("excellent");
            }else{
                financialStatus.setText("outstanding");
            }
        }
        else{
            if(rate*totalLastThreeIncome < 1000){
                financialStatus.setText("not good");
            }
            else if(rate*totalLastThreeIncome < 5000){
                financialStatus.setText("bad");
            }
            else if(rate*totalLastThreeIncome < 10000){
                financialStatus.setText("very bad");
            }
            else{
                financialStatus.setText("devastating");
            }
            rate=((totalLastThreeExpense-totalLastThreeIncome)/totalLastThreeIncome)*100;

            String formattedRate = String.format("%.2f%%", rate);
            briefText.setText("You spend " + formattedRate + " more than total income.");

        }

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

    private void setData(List<MonthData> monthDataList) {
        ArrayList<BarEntry> incomeEntries = new ArrayList<>();
        ArrayList<BarEntry> expenseEntries = new ArrayList<>();
        String[] labels = new String[monthDataList.size()]; // Month names for X-axis

        for (int i = 0; i < monthDataList.size(); i++) {
            MonthData data = monthDataList.get(i);
            incomeEntries.add(new BarEntry(i, data.income));
            expenseEntries.add(new BarEntry(i, data.expense));
            labels[i] = data.month;
        }

        BarDataSet incomeDataSet = new BarDataSet(incomeEntries, "Income");
        incomeDataSet.setColor(Color.argb(150, 0, 10, 255)); // Set dark blue for income bars

        BarDataSet expenseDataSet = new BarDataSet(expenseEntries, "Expense");
        expenseDataSet.setColor(Color.argb(150, 0, 100, 255)); // Set lighter blue for expense bars

        expenseDataSet.setStackLabels(new String[]{"Expense"}); // Stack expense on top of income

        BarData barData = new BarData(incomeDataSet, expenseDataSet);
        barData.setValueTextSize(16f); // Set value text size

        barChart.setScaleEnabled(false);// remove all zooming
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setData(barData);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels)); // Set month labels
        barChart.getAxisLeft().setAxisMinimum(0f); // Set minimum value for Y-axis
        barChart.getLegend().setEnabled(true); // Enable chart legend
        barChart.getDescription().setEnabled(false); // Hide chart description
        barChart.setPinchZoom(false); // Disable pinch zoom
        barChart.getXAxis().setDrawGridLines(false); // Remove vertical grid lines
        barChart.getAxisLeft().setDrawGridLines(false); // Remove horizontal grid lines
        barChart.getXAxis().setDrawLabels(true); // Remove labels from the X-axis
        barChart.getAxisLeft().setDrawLabels(false); // Remove labels from the Y-axis
        barChart.getDescription().setEnabled(false); // Hide chart description
        // Set padding for the chart view (left, top, right, bottom)
        barChart.setPadding(barChart.getPaddingLeft(), barChart.getPaddingTop(), barChart.getPaddingRight(), 50); // Adjust bottom padding

        barChart.setExtraBottomOffset(20f); // Add extra bottom offset (in pixels)
        barChart.setPinchZoom(false); // Disable pinch zoom
        barChart.animateY(500); // Add animation
        barChart.setMinimumHeight((int) 100f);// (optional)
        barChart.setDrawValueAboveBar(false);
        barChart.invalidate(); // Refresh chart
    }

    private void loadDataFromDB() {
        // Load data for the current month
        loadMonthData(0); // 0 represents the current month

        // Load data for the previous 4 months
        for (int i = 1; i <= 3; i++) {
            loadMonthData(-i); // Load data for the i-th previous month (e.g., -1 for last month, -2 for two months ago, etc.)
        }
    }

    private void loadMonthData(final int monthOffset) {
        String monthYear = formatTimestampToMonthYear(Utils.getTimestamp(), monthOffset); // Calculate month/year based on offset

        // Construct the database reference for the specified month/year
        DatabaseReference monthRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(getAccountNumberFromSharedPreferences())
                .child("Transactions")
                .child(monthYear)
                .child("OtherData");

        // Fetch data for the specified month/year
        monthRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve total expense and income for the specified month/year
                    Float totalExpense = dataSnapshot.child("totalExpenseFor" + monthYear).getValue(Float.class);
                    Float totalIncome = dataSnapshot.child("totalIncomeFor" + monthYear).getValue(Float.class);

                    // Handle null values by providing default values if necessary
                    float expenseValue = (totalExpense != null) ? totalExpense : 0f;
                    float incomeValue = (totalIncome != null) ? totalIncome : 0f;

                    // Log the retrieved values for debugging
                    Log.d(TAG, "Month: " + monthYear + ", Total Expense: " + expenseValue + ", Total Income: " + incomeValue);

                    // Update UI or process the retrieved data as needed
                    // Example: Store the data in a list or update a chart with the fetched values
                    processMonthData(monthYear, expenseValue, incomeValue);
                } else {
                    Log.d(TAG, "No data found for month: " + monthYear);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read data for month: " + monthYear, databaseError.toException());
                // Handle onCancelled event, such as displaying an error message to the user
                // or retrying the database operation
            }
        });
    }

    private void processMonthData(String monthYear, float expense, float income) {

        // Find the BarChart view
        barChart = findViewById(R.id.barChart);
        // Process the retrieved data (e.g., store in a list, update a chart)
        // Example: Store the data in a list of MonthData objects

        String month = changeToMonth(monthYear);

        Log.d(TAG, "month -> " + month);

        MonthData monthData = new MonthData(month, income, expense);
        monthDataList.add(monthData);

        // If all necessary data has been loaded (e.g., for the last month in the loop), update the UI
        if (monthDataList.size() == 3) { // 5 months including the current month
            // Update the UI with the loaded data (e.g., display in a chart)
//            updateUI();
            setData(monthDataList);
        }

    }

    private String changeToMonth(String monthYear) {
        try {
            // Parse the input month-year string (e.g., "MMyy")
            SimpleDateFormat inputFormat = new SimpleDateFormat("MMyy", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM", Locale.getDefault());

            // Parse the input string to a Date object
            Date date = inputFormat.parse(monthYear);

            // Format the Date object to a month abbreviation string (e.g., "MMM")
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return ""; // Return empty string or handle the error accordingly
        }
    }


    private String formatTimestampToMonthYear(long timestamp, int monthOffset) {
        // Calculate the month and year based on the provided offset (0 for current month, -1 for last month, -2 for two months ago, etc.)
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.add(Calendar.MONTH, monthOffset); // Adjust month by the specified offset
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMyy", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }


    private String getAccountNumberFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(RegisterActivity.SHARED_PREF_NAME, MODE_PRIVATE);
        return sharedPreferences.getString(RegisterActivity.ACCOUNT_NUMBER_KEY, "");
    }

}
