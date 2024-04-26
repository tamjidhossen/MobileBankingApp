package com.example.mobilebankingapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mobilebankingapp.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private static final String TAG = "REGISTER_TAG";
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    // Declare a member variable to hold the account number
    private String accountNumber;

    // Declare SharedPreferences object
    public SharedPreferences sharedPreferences;
    public static final String SHARED_PREF_NAME = "MyPrefs";
    public static final String ACCOUNT_NUMBER_KEY = "accountNumber";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);


        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //handles toolbar back button
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
        binding.haveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        // register button click
        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }


    private String name, validity, email, password, cPassword, accountNumberFromRegisterPage;
    private void validateData() {
        // Input data
        name = binding.accHolderNameEt.getText().toString().trim();
        validity = binding.accValidityEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString();
        cPassword = binding.cPasswordEt.getText().toString();
        accountNumberFromRegisterPage = binding.accNumberEt.getText().toString();


        Log.d(TAG, "validateData: email: " + email);
        Log.d(TAG, "validateData: validity: " + validity);
        Log.d(TAG, "validateData: name: " + name);
        Log.d(TAG, "validateData: password: " + password);
        Log.d(TAG, "validateData: cPassword: " + cPassword);
        Log.d(TAG, "validateData: accountNumberFromRegisterPage: " + accountNumberFromRegisterPage);

        // Validate data
        if(name.isEmpty()) {
            binding.accHolderNameEt.setError("Enter Name");
            binding.accNumberEt.requestFocus();
        } else if(validity.isEmpty()) {
            binding.accValidityEt.setError("Enter Card Validity (mm/yy)");
            binding.accValidityEt.requestFocus();
        } else if(!validity.matches("\\d{2}/\\d{2}")) {
            // Check if the validity format is not "mm/yy"
            binding.accValidityEt.setError("Invalid Card Validity Format (mm/yy)");
            binding.accValidityEt.requestFocus();
        } else if (accountNumberFromRegisterPage.isEmpty()) {
            // Account number is not entered, show error
            binding.accNumberEt.setError("Enter Card Number");
            binding.accNumberEt.requestFocus();
        } else if(accountNumberFromRegisterPage.length() < 10) {
            // Account number is not entered, show error
            binding.accNumberEt.setError("Enter Valid Card Number");
            binding.accNumberEt.requestFocus();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // Email pattern is invalid, show error
            binding.emailEt.setError("Invalid Email Pattern");
            binding.emailEt.requestFocus();
        } else if (password.isEmpty()) {
            // Password is not entered, show error
            binding.passwordEt.setError("Enter Password");
            binding.passwordEt.requestFocus();
        } else if (!password.equals(cPassword)) {
            // Password and confirm password do not match, show error
            binding.cPasswordEt.setError("Password doesn't match");
            binding.cPasswordEt.requestFocus();
        } else {
            // Check if the account number exists in the database
            checkAccountNumberExists(accountNumberFromRegisterPage);
        }
    }


    private void checkAccountNumberExists(String accountNumberFromRegisterPage) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(accountNumberFromRegisterPage);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Account with the same account number already exists
                    Utils.toast(RegisterActivity.this, "An account with this account number already exists.");
                } else {

                    // Account with the provided account number does not exist, proceed with registration
                    registerUser(accountNumberFromRegisterPage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: ", databaseError.toException());
                Utils.toast(RegisterActivity.this, "Failed to check account number existence. Please try again.");
            }
        });
    }



    private void registerUser(String accountNumberFromRegisterPage) {
        progressDialog.setTitle("Creating Account");
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG, "onSuccess: Register Success");

                        saveUserInfoToSharedPreference(accountNumberFromRegisterPage, name, validity, email);

                        updateUserInfo(accountNumberFromRegisterPage);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        Utils.toast(RegisterActivity.this, "Failed due to "+e.getMessage());
                        progressDialog.dismiss();
                    }
                });
    }

    private void saveUserInfoToSharedPreference(String accountNumberFromRegisterPage, String name, String validity, String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ACCOUNT_NUMBER_KEY, accountNumberFromRegisterPage);
        editor.putString("name", name);
        editor.putString("cardValidity", validity);
        editor.putString("email", email);
        editor.putString("balance", "0.0");
        editor.apply();
    }


    private void saveAccountNumber(String accountNumberFromRegisterPage) {
        accountNumber = accountNumberFromRegisterPage;
        // Save account number to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ACCOUNT_NUMBER_KEY, accountNumber);
        editor.apply();
    }


    private void updateUserInfo(String accountNumberFromRegisterPage) {

        String registerUserEmail = firebaseAuth.getCurrentUser().getEmail();
//        String registerUserUid = firebaseAuth.getUid();
        String accFromSharedPref = getAccountNumberFromSharedPreferences();
        Log.d(TAG, "accFromSharedPref: " + accFromSharedPref);


        // initializes db with these fields
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", name);
        hashMap.put("accountNumber", accountNumberFromRegisterPage);
        hashMap.put ("phoneNumber", "");
        hashMap.put("email", registerUserEmail);
//        hashMap.put("uid", registerUserUid);
        hashMap.put("cvc", "");
        hashMap.put("cardValidity", validity);
        hashMap.put("balance", "0.0");


        //Save to firebase DB
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(accountNumberFromRegisterPage).child("Profile")
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // firebase db saved successful
                        Log.d(TAG, "onSuccess: Info Saved...");

                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finishAffinity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // firebase db save failed
                        Log.e(TAG, "onFailure: ", e);

                        Utils.toast(RegisterActivity.this, "Failed to save info due to "+e.getMessage());
                    }
                });
    }


    private String getAccountNumberFromSharedPreferences() {
        // Retrieve account number from SharedPreferences
        return sharedPreferences.getString(ACCOUNT_NUMBER_KEY, "");
    }
}

















