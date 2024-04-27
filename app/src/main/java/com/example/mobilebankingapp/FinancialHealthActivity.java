package com.example.mobilebankingapp;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebankingapp.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

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
    private float totalLastThreeExpense=expenselastMon_1+expenselastMon_2+expenselastMon_3;
    private float totalLastThreeIncome=incomelastMon_1+incomelastMon_2+incomelastMon_3;


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
        //TextView
        incomelastThreeMonth = findViewById(R.id.totLastThreeMonthIncomeId);
        expenselastThreeMonth = findViewById(R.id.totLastThreeMonthExpenseId);
        briefText = findViewById(R.id.BriefText);
        financialStatus = findViewById(R.id.financialStatusId);

        // Assuming totalLastThreeExpense and totalLastThreeIncome are Float variables representing expenses and income

        // Convert Float values to strings with two decimal points
        String formattedExpense = String.format("%.2f", totalLastThreeExpense);
        String formattedIncome = String.format("%.2f", totalLastThreeIncome);

        // Set the formatted values in TextViews
        expenselastThreeMonth.setText(formattedExpense);
        incomelastThreeMonth.setText(formattedIncome);


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

        // Find the BarChart view
        barChart = findViewById(R.id.barChart);

        // Sample data (replace with your actual data)
        monthDataList = new ArrayList<>();
        monthDataList.add(new MonthData("Jan", incomelastMon_1, expenselastMon_1));
        monthDataList.add(new MonthData("Feb", incomelastMon_2, expenselastMon_2));
        monthDataList.add(new MonthData("Mar", incomelastMon_3, expenselastMon_3));
        monthDataList.add(new MonthData("Jan", incomelastMon_4, expenselastMon_4));

        // ... add data for other months

        // Set data to the chart
        setData(monthDataList);
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
        barChart.setPinchZoom(false); // Disable pinch zoom
        barChart.animateY(500); // Add animation
        barChart.setMinimumHeight((int) 100f);// (optional)
        barChart.setDrawValueAboveBar(false);
        barChart.invalidate(); // Refresh chart
    }
}
