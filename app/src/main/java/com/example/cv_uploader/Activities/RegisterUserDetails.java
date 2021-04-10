package com.example.cv_uploader.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.cv_uploader.Objects.User;
import com.example.cv_uploader.R;
import com.example.cv_uploader.Utils.ProgressDialog;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class RegisterUserDetails extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference mStorageReference;
    private static final int PICK_FROM_GALLERY = 100;
    ImageView profileImageview;
    Uri selectedImage;
    EditText firstNameEdt;
    EditText lastNameEdt;
    String firstname;
    String lastname;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user_details);
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance("https://cvuploader-4d7ae-default-rtdb.firebaseio.com/");
        databaseReference = firebaseDatabase.getReference("Users");
        mStorageReference = FirebaseStorage.getInstance().getReference();

        Intent i = getIntent();
        final String phoneNumber = i.getStringExtra("phone");
        final Button signUpBtn = findViewById(R.id.sign_up);
        firstNameEdt = findViewById(R.id.first_name);
        lastNameEdt = findViewById(R.id.last_name);
        profileImageview = findViewById(R.id.profile_pic);
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateUserInput()){
                    uploadImage();
                }
                /*FirebaseUser user = mAuth.getCurrentUser();
                String userId = user.getUid();
                User obj = new User();
                obj.setEmail("dikekingsleyoneh@yahoo.com");
                obj.setFirstname("kingsley");
                obj.setLastname("dike");
                obj.setUserId(userId);
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        databaseReference.child(userId).setValue(obj);
                        Toast.makeText(RegisterUserDetails.this, "data added", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(RegisterUserDetails.this, "failed", Toast.LENGTH_SHORT).show();

                    }
                });*/

            }
        });

        ImageView addPic = findViewById(R.id.add_pic);
        addPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage(RegisterUserDetails.this,PICK_FROM_GALLERY);
            }
        });
    }

    private boolean validateUserInput(){
        firstname = firstNameEdt.getText().toString();
        lastname = lastNameEdt.getText().toString();
        boolean valid = true;

        CardView cardView = findViewById(R.id.rootview);
        if(firstname.isEmpty() || lastname.isEmpty() ){
            valid=false;
            Snackbar.make(cardView,"All fields are required", Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            }).show();
        }else if (selectedImage==null) {
            valid = false;
            Snackbar.make(cardView,"Select a Profile picture", Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            }).show();
        }

        return valid;
    }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            switch (requestCode) {
                case PICK_FROM_GALLERY:
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(galleryIntent, PICK_FROM_GALLERY);
                    } else {
                        //do something like displaying a message that he didn`t allow the app to access gallery and you wont be able to let him select from gallery
                        Toast.makeText(RegisterUserDetails.this, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == PICK_FROM_GALLERY && resultCode == RESULT_OK && null != data) {
                selectedImage = data.getData();
                profileImageview.setImageURI(selectedImage);
            }
        }

    public void chooseImage(Activity context, int permissionCode) {
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, permissionCode);
            } else {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                context.startActivityForResult(galleryIntent, permissionCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadImage()
    {
        if (selectedImage != null) {

            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog = new ProgressDialog(RegisterUserDetails.this);
            progressDialog.show();

            // Defining the child of storageReference
            FirebaseUser user = mAuth.getCurrentUser();

            StorageReference ref
                    = mStorageReference.child( user.getUid() + ".jpg");

            UploadTask uploadTask=ref.putFile(selectedImage);
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        FirebaseUser user = mAuth.getCurrentUser();
                        String userId = user.getUid();
                        User obj = new User();
                        obj.setEmail("");
                        obj.setFirstname(firstname);
                        obj.setLastname(lastname);
                        obj.setUserId(userId);
                        obj.setStatus("0");
                        obj.setCvUrl("");
                        obj.setProfileUrl(downloadUri.toString());
                        databaseReference.child(userId).setValue(obj);
                        SharedPreferences sharedPreferences = getSharedPreferences("settings",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("logged",true);
                        editor.apply();
                        Intent intent = new Intent(RegisterUserDetails.this, Dashboard.class);
                        startActivity(intent);
                        finish();

                    } else {
                        // Handle failures
                        // ...
                        Toast.makeText(RegisterUserDetails.this,"Something went wrong",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
