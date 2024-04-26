package com.example.mobilebankingapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

import android.widget.Button;
import android.widget.TextView;

public class StatisticsFragment extends Fragment {
    private TextView expenseRateTV;
    private TextView incomeRateTV;
    private TextView totalExpenseTV;
    private float totFoodExpense;
    private float totRentExpense;
    private float totTrnasportExpense;
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
    private float totalExpense;
    public StatisticsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_statistics, container, false);

        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

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
        PieChart pieChart = rootView.findViewById(R.id.pieChart);



        // Sample data
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(500f, "Food"));
        entries.add(new PieEntry(700f, "Rent"));
        entries.add(new PieEntry(200f, "Transport"));
        entries.add(new PieEntry(50f, "Entertainment"));
        entries.add(new PieEntry(300f, "Health"));
        entries.add(new PieEntry(600f, "Education"));
        entries.add(new PieEntry(30f, "Others"));

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


        totalExpenseTV.setText(String.format("Total Expense: %.2f", totalExpense));

        return rootView;
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
}