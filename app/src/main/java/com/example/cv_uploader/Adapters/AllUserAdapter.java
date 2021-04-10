package com.example.cv_uploader.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.cv_uploader.Objects.User;
import com.example.cv_uploader.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

public class AllUserAdapter extends RecyclerView.Adapter<AllUserAdapter.AllUserVH>{
    private Context context;
    private List<User> usersList;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    public AllUserAdapter(Context context, List<User> usersList) {
        this.context = context;
        this.usersList = usersList;
        firebaseDatabase = FirebaseDatabase.getInstance("https://cvuploader-4d7ae-default-rtdb.firebaseio.com/");
        databaseReference = firebaseDatabase.getReference("Users");
    }

    @NonNull
    @Override
    public AllUserVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.user_item_layout,parent,false);
        return new AllUserVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AllUserVH holder, int position) {
        User user = usersList.get(position);
        String firstname = user.getFirstname();
        try {
            firstname = firstname.substring(0,1).toUpperCase()+""+firstname.substring(1);
        }catch (StringIndexOutOfBoundsException e){
            e.printStackTrace();
        }
        String lastname = user.getLastname();
        try {
            lastname = lastname.substring(0,1).toUpperCase()+""+lastname.substring(1);
        }catch (StringIndexOutOfBoundsException | NullPointerException e){
            e.printStackTrace();
        }
        String cvUrl = user.getCvUrl();
        String profilePicUrl = user.getProfileUrl();
        String status = user.getStatus();
        String userId = user.getUserId();
        holder.nameTXT.setText(firstname+" "+lastname);
        Picasso.get().load(profilePicUrl).into(holder.profilePic);
        String status1 = setStatusTxt(status);
        holder.statusTXT.setText(status1);
        if(status.equals("0")){
            holder.invalidateBtn.setVisibility(View.GONE);
            holder.validateBtn.setVisibility(View.GONE);
            holder.downloadBtn.setVisibility(View.GONE);

        }else {
            holder.invalidateBtn.setVisibility(View.VISIBLE);
            holder.validateBtn.setVisibility(View.VISIBLE);
            holder.downloadBtn.setVisibility(View.VISIBLE);
        }
        holder.downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(cvUrl),"application/pdf");
                context.startActivity(Intent.createChooser(intent,"Open CV"));
            }
        });

        holder.validateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Do you want to Validate this user's CV?");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(!user.getStatus().equals("2")){
                            HashMap<String, Object> update = new HashMap<>();
                            update.put("status","2");
                            databaseReference.child(userId).updateChildren(update);
                        }
                    }
                });
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.show();

            }
        });

        holder.invalidateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Do you want to invalidate this user's CV?");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(!user.getStatus().equals("3")){
                            HashMap<String, Object> update = new HashMap<>();
                            update.put("status","3");
                            databaseReference.child(userId).updateChildren(update);
                        }
                    }
                });
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.show();

            }
        });

    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class AllUserVH extends RecyclerView.ViewHolder{
        ImageView profilePic;
        TextView nameTXT,statusTXT;
        LinearLayout downloadBtn,validateBtn,invalidateBtn;
        public AllUserVH(@NonNull View itemView) {
            super(itemView);
            profilePic = itemView.findViewById(R.id.profile_pic);
            nameTXT = itemView.findViewById(R.id.name);
            statusTXT = itemView.findViewById(R.id.status);
            downloadBtn = itemView.findViewById(R.id.download);
            validateBtn = itemView.findViewById(R.id.validate);
            invalidateBtn = itemView.findViewById(R.id.invalidate);
        }
    }

    private String setStatusTxt(String code){
        String status ="";
        switch (code){
            case "0":
                status="CV not available";
                break;
            case "1":
                status="Pending";
                break;
            case "2":
                status="Valid";
                break;
            case "3":
                status="Invalid";
                break;
        }
        return status;
    }
}
