package com.example.mobilebankingapp;

import android.content.Intent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.mobilebankingapp.databinding.FragmentProfileBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;


public class ProfileFragment extends Fragment {

    private Button setAlertButton;
    private FragmentProfileBinding binding;
    private FirebaseAuth firebaseAuth;
    private Context mContext;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutPopup();
            }
        });
    }

    private void logoutPopup() {
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(mContext);
        materialAlertDialogBuilder.setTitle("Log Out")
                .setMessage("Are you sure you want to Log Out?")
                .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        firebaseAuth.signOut();
                        startLoginOptions();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Cancel Clicked, dismiss dialog
                        dialog.dismiss();
                    }
                })
                .show();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        setAlertButton = rootView.findViewById(R.id.alertSetBtn);
        setAlertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the activity you want to open
                openSetAlertActivity();
            }
        });

        return rootView;
    }

    private void openSetAlertActivity() {
        Intent intent = new Intent(getActivity(), SetAlertActivity.class);
        startActivity(intent);

        // Show a toast message indicating that the intent is passed
        Toast.makeText(getContext(), "Intent is passed", Toast.LENGTH_SHORT).show();
    }

    private void startLoginOptions() {
        startActivity(new Intent(mContext, LoginActivity.class));
        getActivity().finish(); // Finish the current activity after starting the LoginActivity
    }
}