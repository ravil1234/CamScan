package com.example.camscan.Activities;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.camscan.R;
public class IntroScreenSecondActivity extends AppCompatActivity {

    Button next;
    TextView skip;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_screen_second);
        getSupportActionBar().hide();
        next=findViewById(R.id.next_btn);
        skip=findViewById(R.id.skip);
        SharedPreferences preferences=getSharedPreferences("SharedPreference",MODE_PRIVATE);
        next.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                preferences.edit().putString("myintro","true").apply();
                Intent i=new Intent(IntroScreenSecondActivity.this,HomeScreenActivity.class);
                startActivity(i);
                finish();
            }
        });
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                preferences.edit().putString("myintro","true").apply();
                Intent i=new Intent(IntroScreenSecondActivity.this,HomeScreenActivity.class);
                startActivity(i);
                finish();
            }
        });
    }
}
