package com.example.camscan.Activities;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.example.camscan.Adapters.InDocRecyclerAdapter;
import com.example.camscan.R;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

      //  Bitmap image= BitmapFactory.decodeResource(getResources(),R.drawable.test_img);
       // ByteArrayOutputStream bstream=new ByteArrayOutputStream();
       // image.compress(Bitmap.CompressFormat.PNG,100,bstream);
        //byte[] byteArray=bstream.toByteArray();
        Intent intent=new Intent(MainActivity.this, InDocRecyclerActivity.class);
        //intent.putExtra("image",byteArray);
        startActivity(intent);

    }
}
