package com.example.mobilebankingapp;

import android.app.ProgressDialog;
import android.content.Intent;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private static final String TAG = "REGISTER_TAG";
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

    private String email, password, cPassword, accountNumber;
    private void validateData() {
        //input data
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString();
        cPassword = binding.cPasswordEt.getText().toString();
        accountNumber = binding.accNumberEt.getText().toString();

        Log.d(TAG, "validateData: email: " + email);
        Log.d(TAG, "validateData: password: " + password);
        Log.d(TAG, "validateData: cPassword: " + cPassword);
        Log.d(TAG, "validateData: accountNumber: " + accountNumber);

        //validate data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            //email pattern is invalid, show error
            binding.emailEt.setError("Invalid Email Pattern");
            binding.emailEt.requestFocus();
        } else if (password.isEmpty()) {
            //password is not entered, show error
            binding.passwordEt.setError("Enter Password");
            binding.passwordEt.requestFocus();
        } else if (!password.equals(cPassword)) {
            //password and confirm password is not same, show error
            binding.cPasswordEt.setError("Password doesn't match");
            binding.cPasswordEt.requestFocus();
        } else if (accountNumber.isEmpty()){
            binding.accNumberEt.setError("Enter Account Number");
            binding.passwordEt.requestFocus();
        } else{
            //all data is valid, start sign-up
            registerUser(accountNumber);
        }
    }

    private void registerUser(String accountNumber) {
        progressDialog.setTitle("Creating Account");
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG, "onSuccess: Register Success");
                        updateUserInfo(accountNumber);
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

    private void updateUserInfo(String accountNumber) {

        String registerUserEmail = firebaseAuth.getCurrentUser().getEmail();
        String registerUserUid = firebaseAuth.getUid();

        // initializes db with these fields
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", "");
        hashMap.put("accountNumber", accountNumber);
        hashMap.put ("phoneNumber", "");
        hashMap.put("email", registerUserEmail);
        hashMap.put("uid", registerUserUid);

        //Save to firebase DB
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(registerUserUid)
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

}

















