package com.example.mobilebankingapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mobilebankingapp.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.text.DecimalFormat;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private DatabaseReference userReference;
    private FirebaseAuth firebaseAuth;
    //TAG to show logs in logcat
    private static final String TAG = "HOME_TAG";

    //Context for this fragment class
    private Context mContext;

    private ArrayList<ModelTransaction> transactionArrayList;

    private AdapterTransactionForHome adapterTransactionForHome;

    public String userName = "", balance = "", validity = "";

    @Override
    public void onAttach(@NonNull Context context) {//get and init the context for this fragment class
        mContext = context;
        super.onAttach(context);
    }
    public HomeFragment(){
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(LayoutInflater.from(mContext), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadBalance();


        loadLatestTransactions();



        String userId = getAccountNumberFromSharedPreferences();
        binding.accountNumber.setText(userId);

        Log.d(TAG, "userId FromSharedPreferences: " + userId);

        loadDataFromSharedPreferences();

        if(Objects.equals(userName, "") || Objects.equals(balance, "") || Objects.equals(validity, "")) {
            // Get card data from Firebase
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Profile");

            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Retrieve data from the database
                        String name = dataSnapshot.child("name").getValue(String.class);
//                    String cardNumber = dataSnapshot.child("accountNumber").getValue(String.class);
                        String balance = dataSnapshot.child("balance").getValue(String.class);
                        String validity = dataSnapshot.child("cardValidity").getValue(String.class);

                        double balanceValue = Double.parseDouble(balance);

                        // Create a DecimalFormat object to format the number to two decimal places
                        DecimalFormat decimalFormat = new DecimalFormat("0.00");
                        String formattedBalance = decimalFormat.format(balanceValue);

                        // Set the retrieved data to the UI elements
                        binding.name.setText(name);
                        binding.availBalance.setText(formattedBalance);
                        binding.validThruTv.setText(validity);

                        saveDataToSharedPreferences(name, validity, formattedBalance, 1);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle any errors

                }
            });
        } else {
            Log.d(TAG, "taking values from shared preference");

            binding.name.setText(userName);
            binding.availBalance.setText(balance);
            binding.validThruTv.setText(validity);


        }

    }

    private void loadBalance() {
        String userId = getAccountNumberFromSharedPreferences();

        Log.d(TAG, "userId FromSharedPreferences: " + userId);

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Profile");

            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Retrieve data from the database

                        String balance = dataSnapshot.child("balance").getValue(String.class);

                        double balanceValue = Double.parseDouble(balance);

                        // Create a DecimalFormat object to format the number to two decimal places
                        DecimalFormat decimalFormat = new DecimalFormat("0.00");
                        String formattedBalance = decimalFormat.format(balanceValue);

                        // Set the retrieved data to the UI elements
                        binding.availBalance.setText(formattedBalance);

                        saveDataToSharedPreferences("", "", formattedBalance, 2);



                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle any errors

                }
            });
    }

    private void loadLatestTransactions() {
        Log.d(TAG, "loadTransactions: ");

        transactionArrayList = new ArrayList<>();

        String accountId = getAccountNumberFromSharedPreferences();

        Log.d(TAG, "account ID: " + accountId);


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(accountId).child("Transactions");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                transactionArrayList.clear(); // Clear the list before adding new data

                Log.d(TAG, "transactionArrayList cleared: ");

                for (DataSnapshot monthYearSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot transactionSnapshot : monthYearSnapshot.child("ThisMonthsTransactions").getChildren()) {
                        // Parse each transaction data and add it to the ArrayList
                        ModelTransaction transaction = transactionSnapshot.getValue(ModelTransaction.class);
                        transactionArrayList.add(transaction);
                    }
                }

                Log.d(TAG, "data added to transactionArrayList " + transactionArrayList.size());

                adapterTransactionForHome = new AdapterTransactionForHome(mContext, transactionArrayList, true);
                binding.adsRvHome.setAdapter(adapterTransactionForHome);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
                Log.e(TAG, "Failed to load transactions: " + databaseError.getMessage());
            }
        });
    }

    private void saveDataToSharedPreferences(String name, String cardValidity, String balance, int x) {
        if (getActivity() == null) {
            return; // Fragment not attached to activity, avoid accessing SharedPreferences
        }

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (x == 1) {
            editor.putString("name", name);
            editor.putString("cardValidity", cardValidity);
            editor.putString("balance", balance);
        } else if (x == 2) {
            editor.putString("balance", balance);
        }
        editor.apply();
    }

    private void loadDataFromSharedPreferences() {
        if (getActivity() == null) {
            return; // Fragment not attached to activity, avoid accessing SharedPreferences
        }

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        userName = sharedPreferences.getString("name", "");
        validity = sharedPreferences.getString("cardValidity", "");
        balance = sharedPreferences.getString("balance", "");

        Log.d(TAG, "user name FromSharedPreferences: " + userName);
        Log.d(TAG, "user balance FromSharedPreferences: " + balance);
        Log.d(TAG, "validity FromSharedPreferences: " + validity);
        // Use the retrieved data as needed
    }

    private String getAccountNumberFromSharedPreferences() {
        // Use getActivity() to get the activity associated with the fragment
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(RegisterActivity.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(RegisterActivity.ACCOUNT_NUMBER_KEY, "");
    }


}