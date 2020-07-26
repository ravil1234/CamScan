package com.example.camscan.Activities;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.example.camscan.R;
public class SplashScreenActivity extends Activity
{
    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spalsh_screen);
        handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run()
            {
                 Intent i=new Intent(SplashScreenActivity.this,CameraXActivity.class);
                 startActivity(i);
                 finish();
            }
        },2000);
    }
}
