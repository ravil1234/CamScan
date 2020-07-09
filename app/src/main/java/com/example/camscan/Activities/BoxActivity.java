package com.example.camscan.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.example.camscan.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class BoxActivity extends AppCompatActivity {

    ImageView imageView;
    FloatingActionButton nextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box);

        initializeViews();
        getSupportActionBar().hide();



    }

    private void initializeViews() {
        imageView=findViewById(R.id.box_img_view);
        nextBtn=findViewById(R.id.box_next_btn);
    }
}
