package com.example.cv_uploader.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cv_uploader.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sign_In extends AppCompatActivity {
    private Button login;
    private Button register,fb;
    private TextInputLayout emailWrapper,passwordWrapper;
    private String email,password;
    private ProgressDialog mProgressDialog;
    private Toolbar toolbar;
    private TextView forgotPswd;
    private String token;
    FirebaseAuth mAuth;
    private static final String ADMIN_EMAIL="dikekingsleyoneh@yahoo.com";
    private static final String ADMIN_PASSWORD = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign__in);
        mAuth = FirebaseAuth.getInstance();
        emailWrapper = findViewById(R.id.emailWrapper_login);
        passwordWrapper=findViewById(R.id.passwordWrapper_login);

        Button loginBtn = findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if( validateUserInput(view)){
                    if(email.equalsIgnoreCase(ADMIN_EMAIL) && password.equalsIgnoreCase(ADMIN_PASSWORD)){
                        Intent intent = new Intent(Sign_In.this, Admin.class);
                        startActivity(intent);
                        finish();
                    }else {
                        FirebaseUser user = mAuth.getCurrentUser();
                        com.example.cv_uploader.Utils.ProgressDialog progressDialog = new com.example.cv_uploader.Utils.ProgressDialog(Sign_In.this);
                        progressDialog.show();
                        loginUserAccount(progressDialog);
                    }
                }
            }
        });

        Button registerBtn = findViewById(R.id.registerBtn);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( validateUserInput(view)){

                        FirebaseUser user = mAuth.getCurrentUser();
                        com.example.cv_uploader.Utils.ProgressDialog progressDialog = new com.example.cv_uploader.Utils.ProgressDialog(Sign_In.this);
                        if(user==null) {
                            progressDialog.show();
                            createNewUser(progressDialog);
                        }

                }
            }
        });


    }

    private void createNewUser(com.example.cv_uploader.Utils.ProgressDialog progressDialog){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(Sign_In.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();

                            DatabaseReference rootRef = FirebaseDatabase.getInstance("https://cvuploader-4d7ae-default-rtdb.firebaseio.com/").getReference("Users");
                            DatabaseReference userNameRef = rootRef.child(user.getUid());
                            ValueEventListener eventListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    progressDialog.dismiss();

                                    if (!dataSnapshot.exists()) {
                                        //create new user
                                        Intent intent = new Intent(Sign_In.this, RegisterUserDetails.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putBoolean("logged", true);
                                        editor.apply();
                                        Intent intent = new Intent(Sign_In.this, Dashboard.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    progressDialog.dismiss();
                                }
                            };
                            userNameRef.addListenerForSingleValueEvent(eventListener);

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(Sign_In.this, "Error: "+task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();

                        }
                    }
                });
    }


    private boolean validateUserInput(View v) {
        boolean valid =true;
        email = emailWrapper.getEditText().getText().toString();
        password = passwordWrapper.getEditText().getText().toString();

        if ( TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Snackbar snackbar = Snackbar.make(v, "All fields are required", Snackbar.LENGTH_LONG);
            snackbar.show();
            valid=false;
        } else if (!isValid(email)) {
            Snackbar snackbar = Snackbar.make(v, "Email is not valid", Snackbar.LENGTH_LONG);
            snackbar.show();
            valid=false;
        }
        return valid;
    }


    private boolean isValid(String email) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
    private void loginUserAccount(com.example.cv_uploader.Utils.ProgressDialog progressDialog) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();

                            DatabaseReference rootRef = FirebaseDatabase.getInstance("https://cvuploader-4d7ae-default-rtdb.firebaseio.com/").getReference("Users");
                            DatabaseReference userNameRef = rootRef.child(user.getUid());
                            ValueEventListener eventListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    progressDialog.dismiss();

                                    if (!dataSnapshot.exists()) {
                                        //create new user
                                        Intent intent = new Intent(Sign_In.this, RegisterUserDetails.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putBoolean("logged", true);
                                        editor.apply();
                                        Intent intent = new Intent(Sign_In.this, Dashboard.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    progressDialog.dismiss();
                                }
                            };
                            userNameRef.addListenerForSingleValueEvent(eventListener);

                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Login failed! Please try again later", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

}