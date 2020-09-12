package com.example.camscan.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.camscan.R;

public class IntroScreenFirstActivity extends AppCompatActivity {

    Button next;
    TextView skip;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_intro_screen_first);
        getSupportActionBar().hide();
        next=findViewById(R.id.next_btn);
        skip=findViewById(R.id.skip);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(IntroScreenFirstActivity.this,IntroScreenSecondActivity.class);
                startActivity(i);
            }
        });
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(IntroScreenFirstActivity.this,HomeScreenActivity.class);
                startActivity(i);
                finish();
            }
        });

    }
}
