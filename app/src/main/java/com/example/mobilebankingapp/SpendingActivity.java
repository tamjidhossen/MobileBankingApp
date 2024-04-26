package com.example.mobilebankingapp;

import android.graphics.Color;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_spending);

        barChart = (HorizontalBarChart)findViewById(R.id.barchartCompareTwoExpense); // Getting Id of the bar chart

        foodCategoryValueTV = findViewById(R.id.foodsCategoryValueTV);
        transportationCategoryValueTV = findViewById(R.id.transportationCategoryValueTV);
        rentCategoryValueTV = findViewById(R.id.rentCategoryValueTV);
        entertainmentCategoryValueTV = findViewById(R.id.entertainmentCategoryValueTV);
        healthcareCategoryValueTV = findViewById(R.id.healthcareCategoryValueTV);
        educationCategoryValueTV = findViewById(R.id.educationCategoryValueTV);
        otherCategoryValueTV = findViewById(R.id.otherCategoryValueTV);

        monthDataList = new ArrayList<>();
        monthDataList.add(new MonthData("Previous Month", 800f));
        monthDataList.add(new MonthData("Current Month", 900f));

        // Setting data into chart
        setData(monthDataList);
    }






    private void setData(List<MonthData> monthDataList) {
        ArrayList<BarEntry> expenseEntries = new ArrayList<>();
        String[] labels = new String[monthDataList.size()]; // Month names for X-axis

        for (int i = 0; i < monthDataList.size(); i++) {
            MonthData data = monthDataList.get(i);
            expenseEntries.add(new BarEntry(i, data.expense));
            labels[i] = ""; // Empty string to avoid labels on X-axis
        }

        // Create separate datasets for Last Month and Current Month with different colors
        BarDataSet currentMonthDataSet = new BarDataSet(expenseEntries.subList(1, 2), "Current Month");
        currentMonthDataSet.setColor(Color.parseColor("#9ADDFF")); // Light blue for current month

        BarDataSet lastMonthDataSet = new BarDataSet(expenseEntries.subList(0, 1), "Previous Month");
        lastMonthDataSet.setColor(Color.parseColor("#3F51B5")); // Dark blue for last month

        // Combine datasets into BarData
        BarData barData = new BarData(lastMonthDataSet, currentMonthDataSet);
        barData.setValueTextSize(16f); // Set value text size

        barChart.setData(barData);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels)); // Empty labels on X-axis
        barChart.getAxisLeft().setAxisMinimum(0f); // Set minimum value for Y-axis
        barChart.getLegend().setEnabled(true);
        //barData.setValueTextSize(16f); // Set value text size
        barChart.setData(barData);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels)); // Set month labels
        barChart.getAxisLeft().setAxisMinimum(0f); // Set minimum value for Y-axis
        barChart.getLegend().setEnabled(true); // Enable chart legend (optional)
        barChart.getXAxis().setDrawGridLines(false); // Remove vertical grid lines
        barChart.getAxisLeft().setDrawGridLines(false); // Remove horizontal grid lines
        barChart.getXAxis().setDrawLabels(false); // Remove labels from the X-axis
        barChart.getAxisLeft().setDrawLabels(false); // Remove labels from the Y-axis
        barChart.getDescription().setEnabled(false); // Hide chart description
        barChart.setPinchZoom(false); // Disable pinch zoom
        barChart.animateY(500); // Add animation
        barChart.setMinimumHeight((int) 100f);// (optional)
        barChart.setDrawValueAboveBar(false);
        barChart.invalidate(); // Refresh chart
    }

    private void updateCategoryValue(TextView textView, String value) {
        textView.setText(value);
    }
}
