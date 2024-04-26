package com.example.mobilebankingapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mobilebankingapp.databinding.FragmentHistoryBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class HistoryFragment extends Fragment {

    private FragmentHistoryBinding binding;

    private static final String TAG = "HISTORY_TAG";

    private Context mContext;

    private ArrayList<ModelTransaction> transactionArrayList;

    private AdapterTransaction adapterTransaction;

    public void onAttach(@NonNull Context context) {//get and init the context for this fragment class
        mContext = context;
        super.onAttach(context);
    }

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHistoryBinding.inflate(LayoutInflater.from(mContext), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadTransactions();
    }

    private void loadTransactions() {
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

                Log.d(TAG, "data added to transactionArrayList ");

                adapterTransaction = new AdapterTransaction(mContext, transactionArrayList, false);
                binding.adsRv.setAdapter(adapterTransaction);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
                Log.e(TAG, "Failed to load transactions: " + databaseError.getMessage());
            }
        });
    }


    private String getAccountNumberFromSharedPreferences() {
        // Use getActivity() to get the activity associated with the fragment
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(RegisterActivity.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(RegisterActivity.ACCOUNT_NUMBER_KEY, "");
    }
}