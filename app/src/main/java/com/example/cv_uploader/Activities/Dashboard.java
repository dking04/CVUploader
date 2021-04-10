package com.example.cv_uploader.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cv_uploader.Objects.User;
import com.example.cv_uploader.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class Dashboard extends AppCompatActivity {
    ImageView profilePic;
    TextView nameTxt,statusTxt;
    private FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference mStorageReference;
    FloatingActionButton addBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("CV UPLOADER");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance("https://cvuploader-4d7ae-default-rtdb.firebaseio.com/");
        databaseReference = firebaseDatabase.getReference("Users");

        addBtn = findViewById(R.id.add_cv);
        addBtn.setVisibility(View.GONE);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dashboard.this, UploadActivity.class);
                startActivity(intent);
            }
        });

        profilePic = findViewById(R.id.profile_pic);
        nameTxt = findViewById(R.id.name);
        statusTxt = findViewById(R.id.status);
        FirebaseUser user = mAuth.getCurrentUser();

        databaseReference.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user1 = dataSnapshot.getValue(User.class);
                    String firstname = user1.getFirstname();
                    String lastname = user1.getLastname();
                    String cvUrl = user1.getCvUrl();
                    String profilePicUrl = user1.getProfileUrl();
                    String status = user1.getStatus();
                    try{
                        String fullname = lastname.substring(0,1).toUpperCase()+""+lastname.substring(1)+" "+firstname.substring(0,1).toUpperCase()+""+firstname.substring(1);
                        nameTxt.setText(fullname);
                    }catch (StringIndexOutOfBoundsException | NullPointerException e){
                        e.printStackTrace();
                    }
                Picasso.get().load(profilePicUrl).into(profilePic);

                    if(status.equals("0")){
                        statusTxt.setText("Yet to Upload a CV");
                        addBtn.setVisibility(View.VISIBLE);

                    }else if(status.equals("1")){
                        statusTxt.setText("Pending");
                        addBtn.setVisibility(View.GONE);
                    }else if(status.equals("2")){
                        statusTxt.setText("Valid");
                        addBtn.setVisibility(View.GONE);
                    }else if(status.equals("3")){
                        statusTxt.setText("Invalid...Upload another CV");
                        addBtn.setVisibility(View.VISIBLE);
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.on_off_menu,menu);
         return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.power:
              logout();
                return true;
        }

        return false;
    }

    private void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to exit");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                FirebaseAuth.getInstance().signOut();
                SharedPreferences sharedPreferences = getSharedPreferences("settings",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("logged",false);
                editor.apply();
                Intent intent = new Intent(Dashboard.this, Sign_In.class);
                startActivity(intent);
                finish();
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();

    }
}