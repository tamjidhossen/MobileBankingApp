package com.example.mobilebankingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebankingapp.databinding.ActivitySetAlertBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SetAlertActivity extends AppCompatActivity {
    private ActivitySetAlertBinding binding;
    private static final String TAG = "SET_ALERT_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetAlertBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ArrayAdapter<String> adapterCategories = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, Utils.categories);
        binding.categorySetAlert.setAdapter(adapterCategories);

        // Listen for category selection
        binding.categorySetAlert.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCategory = (String) parent.getItemAtPosition(position);
            loadCategoryData(selectedCategory);
        });

        binding.saveAlertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndSaveData();
            }
        });

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOnBackPressedDispatcher().onBackPressed(); // Handle back navigation
            }
        });
    }

    private void loadCategoryData(String selectedCategory) {
        String accountNumber = getAccountNumberFromSharedPreferences();

        if (accountNumber.isEmpty()) {
//            Toast.makeText(this, "Account Number not found", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(accountNumber).child("Budgets").child(selectedCategory);

        reference.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String categoryAmount = snapshot.getValue(String.class);
                binding.monthyCategoryExpensesLimitET.setText(categoryAmount);
//                Toast.makeText(SetAlertActivity.this, "Category Data Loaded", Toast.LENGTH_SHORT).show();
            } else {
                binding.monthyCategoryExpensesLimitET.setText("");
//                Toast.makeText(SetAlertActivity.this, "No Data found for this Category", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to retrieve category data", e);
//            Toast.makeText(SetAlertActivity.this, "Failed to retrieve category data", Toast.LENGTH_SHORT).show();
        });
    }

    private void validateAndSaveData() {
        String totalExpenseBudget = binding.monthyExpensesLimitET.getText().toString().trim();
        String selectedCategory = binding.categorySetAlert.getText().toString().trim();
        String selectedCategoryAmount = binding.monthyCategoryExpensesLimitET.getText().toString().trim();

        // Check if either totalExpenseBudget or selectedCategoryAmount is empty
        if (totalExpenseBudget.isEmpty() && selectedCategoryAmount.isEmpty()) {
            Toast.makeText(this, "Please enter Total Expenses or Category Expense Limit", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if selectedCategory is empty when category amount is provided
        if (!selectedCategoryAmount.isEmpty() && selectedCategory.isEmpty()) {
            Toast.makeText(this, "Please select a Category", Toast.LENGTH_SHORT).show();
            return;
        }

        // All data is valid, proceed to update budget limits
        updateBudgetLimits(totalExpenseBudget, selectedCategory, selectedCategoryAmount);
    }
    private void updateBudgetLimits(String totalExpenseBudget, String selectedCategory, String selectedCategoryAmount) {
        String accountNumber = getAccountNumberFromSharedPreferences();

        if (accountNumber.isEmpty()) {
            Toast.makeText(this, "Account Number not found", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(accountNumber).child("Budgets");

        // Retrieve current budget data from the database
        reference.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                HashMap<String, Object> currentBudgetData = (HashMap<String, Object>) snapshot.getValue();

                // Initialize or update the budgetLimits HashMap
                HashMap<String, Object> budgetLimits = new HashMap<>(currentBudgetData);

                if (!totalExpenseBudget.isEmpty()) {
                    budgetLimits.put("TotalExpenses", totalExpenseBudget);
                }

                if (!selectedCategory.isEmpty() && !selectedCategoryAmount.isEmpty()) {
                    budgetLimits.put(selectedCategory, selectedCategoryAmount);
                }

                // Set the updated budgetLimits HashMap to the database
                reference.setValue(budgetLimits)
                        .addOnSuccessListener(unused -> {
                            Log.d(TAG, "Budget Updated Successfully");
                            Toast.makeText(SetAlertActivity.this, "Budget Updated", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to update budget limits", e);
                            Toast.makeText(SetAlertActivity.this, "Failed to update budget limits", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Log.e(TAG, "No budget data found for the user");
                Toast.makeText(SetAlertActivity.this, "No budget data found for the user", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to retrieve budget data", e);
            Toast.makeText(SetAlertActivity.this, "Failed to retrieve budget data", Toast.LENGTH_SHORT).show();
        });
    }

    private String getAccountNumberFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(RegisterActivity.SHARED_PREF_NAME, MODE_PRIVATE);
        return sharedPreferences.getString(RegisterActivity.ACCOUNT_NUMBER_KEY, "");
    }
}
