package com.example.mobilebankingapp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.mobilebankingapp.databinding.ActivityProfileEditBinding;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class ProfileEditActivity extends AppCompatActivity {

    private ActivityProfileEditBinding binding;

    private static final String TAG = "PROFILE_EDIT_TAG";

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;
    public SharedPreferences sharedPreferences;

    private Uri imageUri = null;

    public static final String SHARED_PREF_NAME = "MyPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProfileEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);

        // progress Dialog while profile updates
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();

        loadMyInfo();

        //back button on toolbar
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //pick image
        binding.profileImagePickFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imagePickDialog();
            }
        });

        //update button, validate data
        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }

    private String name = "";
    private String phoneNumber = "";
    private void validateData(){
        //input data
        name = binding.nameEt.getText().toString().trim();
        phoneNumber = binding.phoneNumberEt.getText().toString().trim();

        //validate data
        if (imageUri == null){
            //no image to upload to storage, just update db
            updateProfileDb(null);
        } else {
            //image need to upload to storage, first upload image then update db
            uploadProfileImageStorage();
        }
        updateInfo();
    }

    private void uploadProfileImageStorage(){
        Log.d(TAG, "uploadProfileImageStorage: ");
        //show progress
        progressDialog.setMessage("Uploading user profile image...");
        progressDialog.show();

        String userId = getAccountNumberFromSharedPreferences();

        String filePathAndName = "UserImages/"+"profile_"+firebaseAuth.getUid();
        //Storage reference to upload image
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        ref.putFile(imageUri)
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        Log.d(TAG, "onProgress: Progress: "+progress);
                        progressDialog.setMessage("Uploading profile image. Progress: " + (int) progress + "%");
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //Image uploaded successfully, get url of uploaded image
                        Log. d(TAG, "onSuccess: Uploaded");
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();

                        while(!uriTask.isSuccessful());
                        String uploadedImageUrl = uriTask. getResult().toString();
                        if(uriTask.isSuccessful()) {
                            updateProfileDb(uploadedImageUrl);
                        } else {
                            //start main Activity
                            startActivity(new Intent(ProfileEditActivity.this, MainActivity.class));
                            finishAffinity(); // finishes current and all activities from back stock
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Failed to upload image
                        Log. e(TAG,"onFailure: ", e);
                        progressDialog.dismiss();
                        Utils.toast(ProfileEditActivity.this,"Failed: "+e.getMessage());

                        //start main Activity
                        startActivity(new Intent(ProfileEditActivity.this, MainActivity.class));
                        finishAffinity(); // finishes current and all activities from back stock
                    }
                });
    }

    private void updateProfileDb(String imageUrl) {
        //show progress
        progressDialog.setMessage("Updating user info");
        progressDialog.show();
        //setup data in hashmap to update to firebase db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", "" + name);
        if (imageUrl != null) {
            //update profileImageUrl in db only if uploaded image url is not null
            hashMap.put("profileImageUrl", "" + imageUrl);
        }
        hashMap.put("phoneNumber", phoneNumber);

        String userId = getAccountNumberFromSharedPreferences();

        //Reference of current user info in Firebase Realtime Database to get user info
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference ("Users").child(userId);
        reference.child("Profile")
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //Updated successfully
                        Log. d(TAG,"onSuccess: Info updated");
                        progressDialog.dismiss();
                        Utils.toast(ProfileEditActivity.this,"Profile Updated");


                        //start main Activity
                        startActivity(new Intent(ProfileEditActivity.this, MainActivity.class));
                        finishAffinity(); // finishes current and all activities from back stock
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to update
                        Log. e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        Utils. toast(ProfileEditActivity.this,"Failed to update info due to "+e.getMessage());
                    }
                });
    }

    private void loadMyInfo() {
        Log.d(TAG, "LoadMyInfo: ");

        String userId = getAccountNumberFromSharedPreferences();

        //Reference of current user info in Firebase Realtime Database to get user info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference ("Users").child(userId);
        ref.child("Profile")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = "" + snapshot.child("name").getValue();

                        String phoneNumber = "" + snapshot.child("phoneNumber").getValue();
                        String profileImageUrl = "" + snapshot.child("profileImageUrl").getValue();

                        binding.nameEt.setText(name);
                        binding.phoneNumberEt.setText(phoneNumber);

                        if (!isDestroyed()) {
                            try {
                                Glide.with(ProfileEditActivity.this)
                                        .load(profileImageUrl)
                                        .placeholder(R.drawable.ic_person_white)
                                        .into(binding.profileIv);
                            } catch (Exception e) {
                                Log.e(TAG, "onDataChange", e);
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void imagePickDialog() {
        //init popup menu param 1 is context and param 2 is the UI View (profileImagePickFab) to above/below we need to show popup menu
        PopupMenu popupMenu = new PopupMenu(this, binding.profileImagePickFab);
        //add menu items to our popup menu Param#1 is GroupID, Param#2 is ItemID, Param#3 is OrderID, Param#4 is Menu Item Title
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Gallery");

        //show pop up menu
        popupMenu.show();

        //handle popup menu item click
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                //get id of menu item clicked
                int ItemId = menuItem.getItemId();
                if (ItemId == 1) {
                    Log.d(TAG, "onMenuItemClick: Camera Clicked, check if camera permission(s) granted or not");
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        //Device version is Tiramisu or above. We only need camera permission
                        requestCameraPermission.launch(new String[]{Manifest.permission.CAMERA});
                    } else {
                        //Device version below Tiramisu. We need camera and storage permissions
                        requestCameraPermission.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
                    }
                } else if (ItemId == 2) {
                    //Gallery is clicked we need to check if we have permission of Storage before launching Gallery to Pick image
                    Log.d(TAG,"onMenuItemClick: Check if storage permission is granted or not");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES. TIRAMISU) {
                        //Device version is TIRAMISU or above. We don't need Storage permission to lunch Gallery
                        pickImageGallery();
                    } else {
                        //Device version is below TIRAMISU. We need Storage permission to lunch Gallery
                        requestStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                }
                return false;
            }
        });
    }

    private ActivityResultLauncher<String[]> requestCameraPermission = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    Log.d(TAG, "onActivityResult: "+result.toString());

                    //lets check if permission are granted or not
                    boolean areAllGranted = true;
                    for (Boolean isGranted: result.values()) {
                        areAllGranted = areAllGranted && isGranted;
                    }
                    if(areAllGranted) {
                        Log.d(TAG, "onActivityResult: All Granted");
                        pickImageCamera();
                    } else {
                        //permission denied
                        Log.d(TAG, "onActivityResult: All or either one is denied");
                        Utils.toast(ProfileEditActivity.this, "Camera or Storage or both permission denied");
                    }
                }
            }
    );

    private ActivityResultLauncher<String> requestStoragePermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    Log.d(TAG,"onActivityResult: isGranted: "+isGranted);
                    if (isGranted) {
                        //launch gallery to pick image
                        pickImageGallery();
                    } else {
                        //Storage permission denied
                        Utils.toast(ProfileEditActivity.this, "Storage permission denied!");
                    }
                }
            }
    );
    private void pickImageCamera(){
        Log.d(TAG,"pickImageCamera: ");
        //Setup Content values, MediaStore to capture high quality image using compare intent
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "TEMP_TITLE");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "TEMP_DESCRIPTION");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        // Intent to lunch camera
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    //Check if image is captured or not
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        //Image Captured, we have image in imageUri as assigned in pickImageCamera()
                        Log.d(TAG, "onActivityResult: Image Captured: " + imageUri);
                        //set to profileIv
                        try {
                            Glide.with(ProfileEditActivity.this)
                                    .load(imageUri)
                                    .placeholder(R.drawable.ic_person_white)
                                    .into(binding.profileIv);
                        } catch (Exception e) {
                            Log.e(TAG, "onActivityResult: ", e);
                        }
                    } else {
                        //cancelled
                        Utils.toast(ProfileEditActivity.this, "Cancelled");
                    }
                }
            }
    );
    private void pickImageGallery(){
        Log.d(TAG, "pickImageGallery: ");

        //Intent to lunch Image picker e.g. gallery
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        //we only want to pick image
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private void updateInfo() {
        // Save the new account number to SharedPreferences

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", "");
        editor.putString("phone", "");
        editor.apply();
    }
    private String getAccountNumberFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(RegisterActivity.SHARED_PREF_NAME, MODE_PRIVATE);
        return sharedPreferences.getString(RegisterActivity.ACCOUNT_NUMBER_KEY, "");
    }

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    //Check if image is picked or not
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        //get data
                        Intent data = result.getData();
                        //get uri of image picked
                        imageUri = data.getData();
                        Log.d(TAG, "onActivityResult: Image Picked From Gallery: " + imageUri);
                        //set to profileIv
                        try {
                            Glide.with(ProfileEditActivity.this)
                                    .load(imageUri)
                                    .placeholder(R.drawable.ic_person_white)
                                    .into(binding.profileIv);
                        } catch (Exception e) {
                            Log.e(TAG, "onActivityResult: ", e);
                        }
                    } else {
                        //Cancelled
                        Utils.toast(ProfileEditActivity. this, "Cancelled...");
                    }

                }
            }
    );
}