package com.example.camscan.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.camscan.Objects.DatabaseObject;
import com.example.camscan.R;

import java.util.ArrayList;

public class InDocActivity extends AppCompatActivity {

    ArrayList<DatabaseObject> images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_doc);

    }
}
