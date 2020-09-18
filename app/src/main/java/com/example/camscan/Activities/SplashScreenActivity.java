package com.example.camscan.Activities;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import com.example.camscan.R;
import com.example.camscan.UtilityClass;

public class SplashScreenActivity extends Activity
{
    Handler handler;
    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spalsh_screen);
        preferences=getSharedPreferences(UtilityClass.APP_SETTINGS_PREF,MODE_PRIVATE);
        if(!preferences.contains("mytheme"))
        {
            preferences.edit().putInt("mytheme",1).putInt("myactivity",1).putInt("myview",1).
                    putInt("myfilter",0).apply();
            preferences.edit().putString("mydocname","NewDocument").apply();
        }
        handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run()
            {
                if(!preferences.contains("myintro"))
                {
                    Intent i = new Intent(SplashScreenActivity.this, IntroScreenFirstActivity.class);
                    startActivity(i);
                    finish();
                }
                else
                {
                    Intent i = new Intent(SplashScreenActivity.this, HomeScreenActivity.class);
                    startActivity(i);
                }
            }
        },1500);
    }
}
