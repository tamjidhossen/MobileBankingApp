package com.example.mobilebankingapp;

import android.content.Intent;
import android.content.Intent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mobilebankingapp.databinding.FragmentProfileBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;


public class ProfileFragment extends Fragment {

    private Button setAlertButton;
    private FragmentProfileBinding binding;
    private FirebaseAuth firebaseAuth;
    private Context mContext;
    private static final String TAG = "PROFILE_TAG";
    private ProgressDialog progressDialog;


    public String userName = "", email = "", phone = "";

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

        loadProfileInfo();

        firebaseAuth = FirebaseAuth.getInstance();
        binding.editProfileCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(mContext, ProfileEditActivity.class));
            }
        });

        binding.alertSetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public int hashCode() {
                return super.hashCode();
            }

            @Override
            public void onClick(View v) {
                openSetAlertActivity();
            }
        });

        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutPopup();
            }
        });
    }

    private void loadProfileInfo() {

        String userId = getAccountNumberFromSharedPreferences();

        Log.d(TAG, "userId FromSharedPreferences: " + userId);

        loadDataFromSharedPreferences();

        loadImage();

        if(Objects.equals(userName, "") || Objects.equals(email, "") || Objects.equals(phone, "")) {
            // Get card data from Firebase
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Profile");

            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Retrieve data from the database
                        userName = dataSnapshot.child("name").getValue(String.class);
                        email = dataSnapshot.child("email").getValue(String.class);
                        phone = dataSnapshot.child("phoneNumber").getValue(String.class);
                        String profileImageUrl = ""+ dataSnapshot.child("profileImageUrl").getValue();

                        // Set the retrieved data to the UI elements
                        binding.nameTv.setText(userName);
                        binding.phoneTv.setText(phone);
                        binding.emailTv.setText(email);

                        saveDataToSharedPreferences(userName, phone);

                        try {
                            // set profile image to profileIV
                            Glide.with(mContext)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.ic_person_white)
                                    .into(binding.profileIv);
                        } catch (Exception e) {
                            Log.e(TAG, "onDataChange", e);
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle any errors

                }
            });
        } else {
            Log.d(TAG, "taking values from shared preference");
            // Set the retrieved data to the UI elements
            binding.nameTv.setText(userName);
            binding.phoneTv.setText(phone);
            binding.emailTv.setText(email);

        }
    }

    private void loadImage() {
        String userId = getAccountNumberFromSharedPreferences();

        Log.d(TAG, "userId FromSharedPreferences: " + userId);


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Profile");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    String profileImageUrl = ""+ dataSnapshot.child("profileImageUrl").getValue();

                    try {
                        // set profile image to profileIV
                        Glide.with(mContext)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.ic_person_white)
                                .into(binding.profileIv);
                    } catch (Exception e) {
                        Log.e(TAG, "onDataChange", e);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors

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
    }

    private void startLoginOptions() {
        startActivity(new Intent(mContext, LoginActivity.class));
        getActivity().finish(); // Finish the current activity after starting the LoginActivity
    }


    private void saveDataToSharedPreferences(String name, String phone) {

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("phone", phone);
        editor.putString("name", name);
        editor.apply();
    }

    private void loadDataFromSharedPreferences() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        userName = sharedPreferences.getString("name", "");
        phone = sharedPreferences.getString("phone", "");
        email = sharedPreferences.getString("email", "");
//        imageUrl = sharedPreferences.getString("imageUrl", "");

        Log.d(TAG, "user name FromSharedPreferences: " + userName);
        Log.d(TAG, "user balance FromSharedPreferences: " + phone);
        Log.d(TAG, "validity FromSharedPreferences: " + email);
//        Log.d(TAG, "url FromSharedPreferences: " + imageUrl);
        // Use the retrieved data as needed
    }

    private String getAccountNumberFromSharedPreferences() {
        // Use getActivity() to get the activity associated with the fragment
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(RegisterActivity.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(RegisterActivity.ACCOUNT_NUMBER_KEY, "");
    }


    private void openSetAlertActivity() {
        Intent intent = new Intent(getActivity(), SetAlertActivity.class);
        startActivity(intent);

        // Show a toast message indicating that the intent is passed
//        Toast.makeText(getContext(), "Intent is passed", Toast.LENGTH_SHORT).show();
    }

    private void startLoginOptions() {
        startActivity(new Intent(mContext, LoginActivity.class));
        getActivity().finish(); // Finish the current activity after starting the LoginActivity
    }
}