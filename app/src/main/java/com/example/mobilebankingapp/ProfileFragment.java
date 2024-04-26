package com.example.mobilebankingapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class ProfileFragment extends Fragment {

    private Button setAlertButton;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

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
}
