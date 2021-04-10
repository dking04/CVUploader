package com.example.cv_uploader.Utils;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;


import com.example.cv_uploader.R;

import androidx.annotation.NonNull;

public class ProgressDialog extends Dialog {
    private Activity activity;
    public ProgressDialog(@NonNull Activity context) {
        super(context);
        this.activity =context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog);
    }
}
