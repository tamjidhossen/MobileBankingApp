package com.example.mobilebankingapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mobilebankingapp.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    private static final String TAG = "LOGIN_TAG";

    private ProgressDialog progressDialog;

    //Firebase Auth for auth related task
    private FirebaseAuth firebaseAuth;
    private SharedPreferences sharedPreferences;
    public static final String SHARED_PREF_NAME = "MyPrefs";
    public static final String ACCOUNT_NUMBER_KEY = "accountNumber";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // get instance of firebase
        firebaseAuth = FirebaseAuth.getInstance();

        //init setup progressbar
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);

        // handle's Don't have an account, Register
        // Tv = TextView
        binding.noAccSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });


        // login button click
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });


        //handle forget password
        binding.forgotPasswordTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showForgotPasswordDialog();
            }
        });
    }

    private String accountNumberFromLogin, email, password;
    private void validateData() {
        // Input data
        accountNumberFromLogin = binding.accNumEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString();

        Log.d(TAG, "validateData: accountNumber: " + accountNumberFromLogin);
        Log.d(TAG, "validateData: email: " + email);
        Log.d(TAG, "validateData: password: " + password);

        // Validate data
        if(accountNumberFromLogin.isEmpty()) {
            binding.accNumEt.setError("Enter Card Number");
            binding.accNumEt.requestFocus();
        } else if(accountNumberFromLogin.length() < 10) {
            binding.accNumEt.setError("Enter Valid Card Number");
            binding.accNumEt.requestFocus();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // Email pattern is invalid, show error
            binding.emailEt.setError("Invalid Email Pattern");
            binding.emailEt.requestFocus();
        } else if (password.isEmpty()) {
            // Password is not entered, show error
            binding.passwordEt.setError("Enter Password");
            binding.passwordEt.requestFocus();
        } else {
            // Check if the user exists in the database
            checkUserExists(accountNumberFromLogin);
        }
    }
    private void checkUserExists(String accountNumberFromLogin) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(accountNumberFromLogin);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User with the provided account number exists
                    String emailFromDatabase = dataSnapshot.child("Profile").child("email").getValue(String.class);
                    if (emailFromDatabase.equals(email)) {
                        // Email associated with the account matches the entered email, proceed with login
                        loginUser();
                    } else {
                        // Email associated with the account does not match the entered email
                        Utils.toast(LoginActivity.this, "Email does not match the provided account number.");
                    }
                } else {
                    // User with the provided account number does not exist
                    Utils.toast(LoginActivity.this, "User does not exist. Please check your credentials.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: ", databaseError.toException());
                Utils.toast(LoginActivity.this, "Failed to check user existence. Please try again.");
            }
        });
    }

    private void loginUser() {
        //show progress
        progressDialog.setTitle("Signing In");
        progressDialog.show();

        //start user login
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //user login success
                        Log.d(TAG, "onSuccess: Logged In");
                        progressDialog.dismiss();

                        //start main Activity
                        updateAccountNumberAndEmailInSharedPreferences(accountNumberFromLogin, email);

                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finishAffinity(); // finishes current and all activities from back stock
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // user login failed
                        Log.e(TAG, "onFailure", e);
                        Utils.toast(LoginActivity.this, "Failed Due to "+e.getMessage());
                        progressDialog.dismiss();
                    }
                });

    }

    private void updateAccountNumberAndEmailInSharedPreferences(String newAccountNumber, String email) {
        // Save the new account number to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ACCOUNT_NUMBER_KEY, newAccountNumber);
        editor.putString("email", email);
        editor.putString("name", "");
        editor.putString("cardValidity", "");
        editor.putString("balance", "");
        editor.apply();
    }

    //send link for forget password
    private void showForgotPasswordDialog() {
        EditText resetEmail = new EditText(this);
        AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(this);
        passwordResetDialog.setTitle("Reset Password?");
        passwordResetDialog.setMessage("Enter Your Email To Receive Reset Link.");
        passwordResetDialog.setView(resetEmail);

        passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String mail = resetEmail.getText().toString().trim();
                sendPasswordResetEmail(mail);
            }
        });

        passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Close the Dialog
            }
        });
        passwordResetDialog.create().show();
    }

    private void sendPasswordResetEmail(String email) {
        progressDialog.setMessage("Sending Reset Link...");
        progressDialog.show();

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void avoid) {
                        progressDialog.dismiss();
                        Utils.toast(LoginActivity.this, "Reset Link Sent To Your Email.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Utils.toast(LoginActivity.this, "Error! Reset Link is Not Sent" + e.getMessage());
                    }
                });
    }

}





















