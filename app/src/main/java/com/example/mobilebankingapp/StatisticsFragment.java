package com.example.mobilebankingapp;

import android.content.Intent;
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

import java.util.ArrayList;import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class StatisticsFragment extends Fragment {

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

        // Set OnClickListener on the TextView
        buttonHealth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the activity you want to open
                openFinancialHealthActivity();
            }
        });

        Button buttonBehaviour = rootView.findViewById(R.id.btnBehaviour);

        // Set OnClickListener on the TextView
        buttonBehaviour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the activity you want to open
                openFinancialSpeendingActivity();
            }
        });
        PieChart pieChart = rootView.findViewById(R.id.pieChart);


        // Sample data
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(18.5f, "Slice 1"));
        entries.add(new PieEntry(26.7f, "Slice 2"));
        entries.add(new PieEntry(24.0f, "Slice 3"));

        PieDataSet dataSet = new PieDataSet(entries, "Pie Chart Title");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new PercentFormatter(pieChart));

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.animateY(1000);
        pieChart.invalidate();

        return rootView;
    }
    private void openFinancialHealthActivity() {
        Intent intent = new Intent(getActivity(), FinancialHealthActivity.class);
        startActivity(intent);
    }
    private void openFinancialSpeendingActivity() {
        Intent intent = new Intent(getActivity(), SpeendingActivity.class);
        startActivity(intent);
    }
}