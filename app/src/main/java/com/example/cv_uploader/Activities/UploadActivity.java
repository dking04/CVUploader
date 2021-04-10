package com.example.cv_uploader.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cv_uploader.R;
import com.example.cv_uploader.Utils.ProgressDialog;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;

public class UploadActivity extends AppCompatActivity {
    private static final int PICK_PDF_CODE=101;
    private FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference mStorageReference;
    ImageView fileImg;
    TextView fileNameTXT;
    Uri selectedFile;
    Button uploadFileBtn;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Upload CV");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_keyboard_backspace_24);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance("https://cvuploader-4d7ae-default-rtdb.firebaseio.com/");
        databaseReference = firebaseDatabase.getReference("Users");
        mStorageReference = FirebaseStorage.getInstance().getReference();
        user = mAuth.getCurrentUser();

        fileImg = findViewById(R.id.file_img);
        fileNameTXT = findViewById(R.id.file_name);
        Button selectFileBtn = findViewById(R.id.select_file);
        uploadFileBtn = findViewById(R.id.upload_file);
        uploadFileBtn.setEnabled(false);
        selectFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPdf(UploadActivity.this,PICK_PDF_CODE);
            }
        });
        uploadFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadCV();
            }
        });
    }



    public void getPdf(Activity context, int permissionCode) {
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, permissionCode);
            } else {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/pdf");
                startActivityForResult(intent, PICK_PDF_CODE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PICK_PDF_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                  getPdf(UploadActivity.this,PICK_PDF_CODE);
                } else {
                    Toast.makeText(UploadActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PDF_CODE && resultCode == RESULT_OK && null != data) {
            selectedFile = data.getData();
            fileImg.setVisibility(View.VISIBLE);
           File file = new File(selectedFile.getPath());
            String displayName = null;

            if (selectedFile.toString().startsWith("content://")) {
                Cursor cursor = null;
                try {
                    cursor = getContentResolver().query(selectedFile, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                } finally {
                    cursor.close();
                }
            } else if (selectedFile.toString().startsWith("file://")) {
                displayName = file.getName();
            }
            fileNameTXT.setText(""+displayName);
            fileNameTXT.setVisibility(View.VISIBLE);
            uploadFileBtn.setEnabled(true);
        }
    }

    private void uploadCV() {
        if (selectedFile != null) {

            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog = new ProgressDialog(UploadActivity.this);
            progressDialog.show();


            StorageReference ref
                    = mStorageReference.child(System.currentTimeMillis() + ".pdf");

            UploadTask uploadTask = ref.putFile(selectedFile);
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
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String userId = user.getUid();
                        progressDialog.dismiss();
                        HashMap<String, Object> update = new HashMap<>();
                        update.put("cvUrl",downloadUri.toString());
                        update.put("status","1");
                        databaseReference.child(userId).updateChildren(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.i("response","re "+userId);
                                onBackPressed();
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i("response","re "+e.getMessage());

                            }
                        });

                        //sendBroadcast(new Intent("refresh"));
                        Toast.makeText(UploadActivity.this,"File uploaded successfully",Toast.LENGTH_SHORT).show();

                    } else {
                        // Handle failures
                        // ...
                        Toast.makeText(UploadActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return false;
    }
}