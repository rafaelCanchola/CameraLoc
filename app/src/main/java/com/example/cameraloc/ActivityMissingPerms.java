package com.example.cameraloc;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class ActivityMissingPerms extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_missing_perms);
    }
}