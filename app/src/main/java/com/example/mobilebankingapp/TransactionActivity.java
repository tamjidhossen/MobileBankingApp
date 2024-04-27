package com.example.mobilebankingapp;

import static android.app.PendingIntent.getActivity;
import static com.example.mobilebankingapp.RegisterActivity.ACCOUNT_NUMBER_KEY;
import static com.example.mobilebankingapp.RegisterActivity.SHARED_PREF_NAME;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class TransactionActivity extends AppCompatActivity {

    private ActivityTransactionBinding binding;

    private static final String TAG = "TRANSACTION_ACT_TAG";

    private ProgressDialog progressDialog;

    //Firebase Auth for auth related tasks
    private FirebaseAuth firebaseAuth;

    private String trnAmount = "0.0";
    private String trnName = "";
    private String trnNumber = "";
    private String trnCategory = "";

    public String userName = "", balance = "", validity = "";

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

        loadDataFromSharedPreferences();


        binding.currBalance.setText(balance);
        binding.accNumber.setText(getAccountNumberFromSharedPreferences());


        binding.transferBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

    }



    private void validateData() {
        Log.d(TAG, "validateData: ");

        trnCategory = binding.categoryAct.getText().toString().trim();
        trnName = binding.recipientNameEt.getText().toString().trim();
        trnNumber = binding.recipientAccNumberEt.getText().toString().trim();
        trnAmount = binding.amountEt.getText().toString().trim();



        Log.d(TAG, "Balance loaded from shared pref: ");

        if (trnAmount.isEmpty()) {
            binding.amountEt.setError("Enter Valid Amount");
            binding.amountEt.requestFocus();
        } else if (Double.parseDouble(trnAmount) > Double.parseDouble(balance)) {
            binding.amountEt.setError("Insufficient Fund");
            binding.amountEt.requestFocus();
        } else if (Double.parseDouble(trnAmount) <= 0) {
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
        } else if (trnNumber.length() < 10 || trnNumber.equals(getAccountNumberFromSharedPreferences())) {
            binding.recipientAccNumberEt.setError("Enter Valid Account Number");
            binding.recipientAccNumberEt.requestFocus();
        } else {
            // Check if recipient exists
            checkRecipientExists();
        }
    }


    private void checkRecipientExists() {
        DatabaseReference recipientRef = FirebaseDatabase.getInstance().getReference("Users").child(trnNumber);

        recipientRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Recipient account exists, proceed with transaction
                    executeTransaction();
                } else {
                    // Recipient account does not exist
                    binding.recipientAccNumberEt.setError("Recipient Account Does Not Exist");
                    binding.recipientAccNumberEt.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to check recipient existence", databaseError.toException());
                Utils.toast(TransactionActivity.this, "Failed to check recipient existence. Please try again.");
            }
        });
    }

    private void executeTransaction() {
        Log.d(TAG, "executingTransaction:");


        // Retrieve account number from SharedPreferences
        String userId = getAccountNumberFromSharedPreferences();


        Log.d(TAG, "UserId from shared preference: " + userId);

        // Get current timestamp
        Long timestamp = Utils.getTimestamp();

        // Generate unique transaction ID
        String transactionId = FirebaseDatabase.getInstance().getReference().push().getKey();

        // Format timestamp to month/year
//        String monthYear = formatTimestampToMonthYear(timestamp);
        String monthYear = "0324";

        HashMap<String, Object> hashMapUser = new HashMap<>();
        hashMapUser.put("trnId", transactionId); // Add transaction ID
        hashMapUser.put("trnAmount", trnAmount);
        hashMapUser.put("trnCategory", trnCategory);
        hashMapUser.put("trnNumber", trnNumber);
        hashMapUser.put("trnName", trnName);
        hashMapUser.put("trnTime", timestamp);
        hashMapUser.put("trnState", "send");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users")
                .child(userId)
                .child("Transactions")
                .child(monthYear) //monthYear
                .child("ThisMonthsTransactions")
                .child(transactionId); // Use transaction ID as child node

        ref.setValue(hashMapUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "Transaction onSuccess");


                        //increase this months total expense (Transaction->monthYear->"totalExpensForMonthYear")
                        changeExpense();

                        changeCatExpense();

                        // Show transaction successful message
                        addTransactionDetailsToRecipientsAccount();
                        Utils.toast(TransactionActivity.this, "Transaction Successful");
                        // Finish the activity
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Transaction onFailure: ", e);
                        Utils.toast(TransactionActivity.this, "Transaction Failed");
                    }
                });
    }

    private void changeCatExpense() {
            // Retrieve account number from SharedPreferences
            String userId = getAccountNumberFromSharedPreferences();

            // Get current timestamp
            Long timestamp = Utils.getTimestamp();

            // Format timestamp to month/year
//        String monthYear = formatTimestampToMonthYear(timestamp);
        String monthYear = "0324";


            String categoryCost = trnCategory + "CatTotal" + monthYear;
            // Database reference to the category spending for the current month/year
            DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference("Users")
                    .child(userId)
                    .child("Transactions")
                    .child(monthYear) //monthYear
                    .child("OtherData")
                    .child(categoryCost);

            // Run transaction to update the category spending
            categoryRef.runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                    // Retrieve current category spending or initialize if it doesn't exist
                    Object value = mutableData.getValue();
                    double currentCategoryExpense;

                    if (value instanceof Double) {
                        currentCategoryExpense = (Double) value;
                    } else if (value instanceof String) {
                        try {
                            currentCategoryExpense = Double.parseDouble((String) value);
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "Failed to parse category expense value", e);
                            return Transaction.abort();
                        }
                    } else {
                        currentCategoryExpense = 0.0; // Default value if unexpected type or null
                    }

                    // Increment the category spending by the transaction amount
                    double transactionAmount;
                    try {
                        transactionAmount = Double.parseDouble(trnAmount);
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Failed to parse transaction amount", e);
                        return Transaction.abort(); // Abort the transaction if parsing fails
                    }
                    currentCategoryExpense += transactionAmount;

                    // Set the updated category spending back to the database
                    mutableData.setValue(currentCategoryExpense);

                    // Return the updated data
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot) {
                    if (committed) {
                        Log.d(TAG, "Transaction category expense update successful");
                    } else {
                        Log.d(TAG, "Transaction category expense update failed", databaseError != null ? databaseError.toException() : null);
                    }
                }
            });
        }


        private void changeExpense() {
        Log.d(TAG, "changeExpense........................");

        // Retrieve account number from SharedPreferences
        String userId = getAccountNumberFromSharedPreferences();
        Log.d(TAG, "UserId from shared preference: " + userId);

        // Get current timestamp
        Long timestamp = Utils.getTimestamp();

        // Format timestamp to month/year
//        String monthYear = formatTimestampToMonthYear(timestamp);
            String monthYear = "0324";
        // Database reference to the total expense for the current month/year
        DatabaseReference monthYearRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(userId)
                .child("Transactions")
                .child(monthYear) //monthYear
                .child("OtherData");

        String totalExpenseKey = "totalExpenseFor" + monthYear;
        DatabaseReference totalExpenseRef = monthYearRef.child(totalExpenseKey);

        // Run transaction to update the total expense
        totalExpenseRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                // Retrieve current total expense or initialize if it doesn't exist
                Object value = mutableData.getValue();
                double currentTotalExpense;

                if (value instanceof Double) {
                    currentTotalExpense = (Double) value;
                } else if (value instanceof String) {
                    try {
                        currentTotalExpense = Double.parseDouble((String) value);
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Failed to parse total expense value", e);
                        return Transaction.abort();
                    }
                } else {
                    currentTotalExpense = 0.0; // Default value if unexpected type or null
                }


                // Increment the total expense by the transaction amount
                double transactionAmount;
                try {
                    transactionAmount = Double.parseDouble(trnAmount);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Failed to parse transaction amount", e);
                    return Transaction.abort(); // Abort the transaction if parsing fails
                }
                currentTotalExpense += transactionAmount;

                // Set the updated total expense back to the database
                mutableData.setValue(currentTotalExpense);

                // Return the updated data
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot) {
                if (committed) {
                    Log.d(TAG, "Transaction total expense update successful");
                } else {
                    Log.d(TAG, "Transaction total expense update failed", databaseError != null ? databaseError.toException() : null);
                }
            }
        });


    }



    private void addTransactionDetailsToRecipientsAccount() {
        DatabaseReference recipientRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(trnNumber) // Using the recipient's account number as the reference
                .child("Transactions")
                .child("0324") //monthYear formatTimestampToMonthYear(Utils.getTimestamp())
                .child("ThisMonthsTransactions")
                .child(FirebaseDatabase.getInstance().getReference().push().getKey()); // Unique transaction ID

        HashMap<String, Object> hashMapRecipient = new HashMap<>();
        hashMapRecipient.put("trnAmount", trnAmount);
        hashMapRecipient.put("trnCategory", trnCategory);
        hashMapRecipient.put("trnName", trnName);
        hashMapRecipient.put("trnTime", Utils.getTimestamp());
        hashMapRecipient.put("trnState", "received"); // Indicate that it's a received transaction

        recipientRef.setValue(hashMapRecipient)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "Recipient Transaction Details Updated Successfully");
                        increaseIncome();
                        deductFromUser();
                        addToRecipient();

                        //start main Activity
                        startActivity(new Intent(TransactionActivity.this, MainActivity.class));
                        finishAffinity(); // finishes current and all activities from back stock
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to Update Recipient Transaction Details", e);
                    }
                });
    }
    private void increaseIncome() {

        Log.d(TAG, "increaseIncome........................");

        // Get current timestamp
        Long timestamp = Utils.getTimestamp();

        // Format timestamp to month/year
        String monthYear = formatTimestampToMonthYear(timestamp);

        // Database reference to the total income for the current month/year
        DatabaseReference monthYearRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(trnNumber) // Update the reference to the recipient's account number
                .child("Transactions")
                .child("0324") //monthYear
                .child("OtherData");

        String totalIncomeKey = "totalIncomeFor" + monthYear;
        DatabaseReference totalIncomeRef = monthYearRef.child(totalIncomeKey);

        // Run transaction to update the total income
        totalIncomeRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                // Retrieve current total income or initialize if it doesn't exist
                Object value = mutableData.getValue();
                double currentTotalIncome;

                if (value instanceof Double) {
                    currentTotalIncome = (Double) value;
                } else if (value instanceof String) {
                    try {
                        currentTotalIncome = Double.parseDouble((String) value);
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Failed to parse total income value", e);
                        return Transaction.abort();
                    }
                } else {
                    currentTotalIncome = 0.0; // Default value if unexpected type or null
                }

                // Increment the total income by the transaction amount
                double transactionAmount;
                try {
                    transactionAmount = Double.parseDouble(trnAmount);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Failed to parse transaction amount", e);
                    return Transaction.abort(); // Abort the transaction if parsing fails
                }
                currentTotalIncome += transactionAmount;

                // Set the updated total income back to the database
                mutableData.setValue(currentTotalIncome);

                // Return the updated data
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot) {
                if (committed) {
                    Log.d(TAG, "Transaction total income update successful");
                } else {
                    Log.d(TAG, "Transaction total income update failed", databaseError != null ? databaseError.toException() : null);
                }
            }
        });
    }




    private void deductFromUser() {
        double newBalance = Double.parseDouble(balance) - Double.parseDouble(trnAmount);
        balance = String.valueOf(newBalance);
        saveDataToSharedPreferences(); // Update shared preferences with new balance

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(getAccountNumberFromSharedPreferences()).child("Profile");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    double userBalance = Double.parseDouble(dataSnapshot.child("balance").getValue(String.class));
                    double newUserBalance = userBalance - Double.parseDouble(trnAmount);
                    dataSnapshot.getRef().child("balance").setValue(String.valueOf(newUserBalance));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to deduct amount from user", databaseError.toException());
            }
        });
    }

    private void addToRecipient() {
        DatabaseReference recipientRef = FirebaseDatabase.getInstance().getReference("Users").child(trnNumber).child("Profile");
        recipientRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    double recipientBalance = Double.parseDouble(dataSnapshot.child("balance").getValue(String.class));
                    double newRecipientBalance = recipientBalance + Double.parseDouble(trnAmount);
                    dataSnapshot.getRef().child("balance").setValue(String.valueOf(newRecipientBalance));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to add amount to recipient", databaseError.toException());
            }
        });
    }

    private void saveDataToSharedPreferences() {
        SharedPreferences sharedPreferences = TransactionActivity.this.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("balance", balance);
        editor.apply();
    }


    private void loadDataFromSharedPreferences() {
        SharedPreferences sharedPreferences = TransactionActivity.this.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        userName = sharedPreferences.getString("name", "");
        validity = sharedPreferences.getString("cardValidity", "");
        balance = sharedPreferences.getString("balance", "");

        Log.d(TAG, "user name FromSharedPreferences: " + userName);
        Log.d(TAG, "user balance FromSharedPreferences: " + balance);
        Log.d(TAG, "validity FromSharedPreferences: " + validity);
        // Use the retrieved data as needed
    }

    private String getAccountNumberFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(RegisterActivity.SHARED_PREF_NAME, MODE_PRIVATE);
        return sharedPreferences.getString(RegisterActivity.ACCOUNT_NUMBER_KEY, "");
    }

    private String formatTimestampToMonthYear(Long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("MMyy");
        return sdf.format(calendar.getTime());
    }
}