package com.example.mobilebankingapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mobilebankingapp.databinding.ActivityTransactionBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class TransactionActivity extends AppCompatActivity {

    private ActivityTransactionBinding binding;

    private static final String TAG = "Transaction_Activity_Tag";

    private ProgressDialog progressDialog;

    //Firebase Auth for auth related tasks
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Firebase Auth for auth related tasks
        firebaseAuth = FirebaseAuth.getInstance();

        //Setup and set the categories adapter to the Category Input Filed i.e. categoryAct
        ArrayAdapter<String> adapterCategories = new ArrayAdapter<>(this, R.layout.row_category_act, Utils.categories);
        binding.categoryAct.setAdapter(adapterCategories);

        binding.transferBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

    }

    private String trnAmount = "";
    private String trnName = "";
    private String trnNumber = "";
    private String trnCategory = "";

    private void validateData() {
        Log.d(TAG, "validateData: ");

        trnCategory = binding.categoryAct.getText().toString().trim();
        trnName = binding.recipientNameEt.getText().toString().trim();
        trnNumber = binding.recipientAccNumberEt.getText().toString().trim();
        trnAmount = binding.amountEt.getText().toString().trim();

        if (trnAmount.isEmpty() || Double.parseDouble(trnAmount) <= 0) {
            binding.amountEt.setError("Enter Valid Amount");
            binding.amountEt.requestFocus();
        } else if (trnCategory.isEmpty()) {
            binding.categoryAct.setError("Enter Category");
            binding.categoryAct.requestFocus();
        } else if (trnName.isEmpty()) {
            binding.recipientNameEt.setError("Enter Recipient Name");
            binding.recipientNameEt.requestFocus();
        } else if (trnNumber.isEmpty()) {
            binding.recipientAccNumberEt.setError("Enter Recipient Account Number");
            binding.recipientAccNumberEt.requestFocus();
        } else if(trnNumber.length() < 10) {
            binding.recipientAccNumberEt.setError("Enter Valid Account Number");
            binding.recipientAccNumberEt.requestFocus();
        } else {
            executeTransaction();
        }
    }

    private void executeTransaction() {
        Log.d(TAG, "executingTransaction: ");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("trnAmount", trnAmount);
        hashMap.put ("trnCategory", trnCategory);
        hashMap.put ("trnNumber", trnNumber);
        hashMap.put ("trnName", trnName);

    }
}