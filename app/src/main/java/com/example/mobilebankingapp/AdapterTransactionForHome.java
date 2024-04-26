package com.example.mobilebankingapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilebankingapp.databinding.DummyTransactionsBinding;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Collections;

public class AdapterTransactionForHome extends RecyclerView.Adapter<AdapterTransactionForHome.HolderTransaction> {

    private DummyTransactionsBinding binding;
    private FirebaseAuth firebaseAuth;
    private static final String TAG = "ADAPTER_TRANSACTIONS_FOR_HOME_TAG";

    private Context context;
    public ArrayList<ModelTransaction> transactionsArrayList;

    private DatabaseReference databaseReference;

    public boolean isHome = false;
    public AdapterTransactionForHome(Context context, ArrayList<ModelTransaction> transactionsArrayList, boolean isHomeFragment) {
        this.context = context;
        this.isHome = isHomeFragment;
        this.transactionsArrayList = reverseAndLimitList(transactionsArrayList);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    // Helper method to reverse the list, so the latest transaction stays on top
    // Helper method to reverse the list and limit to 5 transactions
    private ArrayList<ModelTransaction> reverseAndLimitList(ArrayList<ModelTransaction> list) {
        ArrayList<ModelTransaction> reversedList = new ArrayList<>(list);
        Collections.reverse(reversedList);

        // Limit the list to 5 transactions
        if (reversedList.size() > 5) {
            reversedList.subList(5, reversedList.size()).clear();
        }

        return reversedList;
    }

    @NonNull
    @Override
    public HolderTransaction onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        binding = DummyTransactionsBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderTransaction(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderTransaction holder, int position) {
        ModelTransaction modelTransaction = transactionsArrayList.get(position);

        // Extracting transaction details
        String trnName = modelTransaction.getTrnName();
        String trnCategory = modelTransaction.getTrnCategory();
        Long trnTime = modelTransaction.getTrnTime();
        String trnAmount = modelTransaction.getTrnAmount();
        String trnState = modelTransaction.getTrnState();
        String formattedDate = Utils.formatTimestampDate(trnTime);

        // Bind data to the views using the ViewHolder
        holder.trnAccName.setText(trnName);
        holder.amount.setText(trnAmount);
        holder.dateTv.setText(formattedDate);
        holder.category.setText(trnCategory);

        // Set text color based on transaction state
        if ("send".equalsIgnoreCase(trnState)) {
            holder.statusSymbol.setTextColor(ContextCompat.getColor(context, R.color.Red));
            holder.statusSymbol.setText("-$");
            holder.amount.setTextColor(ContextCompat.getColor(context, R.color.Red));
            holder.imageIv.setImageResource(R.drawable.ic_spent);
        } else {
            holder.statusSymbol.setTextColor(ContextCompat.getColor(context, R.color.DarkGreen));
            holder.statusSymbol.setText("+$");
            holder.amount.setTextColor(ContextCompat.getColor(context, R.color.DarkGreen));
            holder.imageIv.setImageResource(R.drawable.ic_add_money);
        }
    }

    @Override
    public int getItemCount() {
        return transactionsArrayList.size();
    }


    class HolderTransaction extends RecyclerView.ViewHolder{

        TextView trnAccName, category, amount, dateTv, statusSymbol;
        ImageView imageIv;
        MaterialCardView fullTransactionCard;
        public HolderTransaction(@NonNull View itemView) {
            super(itemView);
            trnAccName = binding.trnAccName;
            amount = binding.amount;
            category = binding.category;
            statusSymbol = binding.statusSymbol;
            dateTv = binding.dateTv;
            imageIv = binding.imageIv;
            fullTransactionCard = binding.fullTransactionCard;
        }
    }
}
