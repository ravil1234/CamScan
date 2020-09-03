package com.example.camscan.Activities;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.camscan.R;
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homescreen_design);
        getSupportActionBar().hide();
    }
}
