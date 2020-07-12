package com.example.camscan.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.renderscript.RenderScript;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import android.renderscript.ScriptC;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.camscan.R;
import com.example.camscan.ScriptC_filter1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import RenderScriptJava.Filter1;
import RenderScriptJava.FlatCorrection;

public class FilterActivity extends AppCompatActivity {

    private static final String TAG = "Filter Activity";
    Bitmap cropped;
    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        String path=getIntent().getStringExtra("path");
        String name=getIntent().getStringExtra("name");
        cropped=getImageFromStorage(path,name);
        initializeViews();

        if(cropped!=null){
            image.setImageBitmap(cropped);


            //using renderscript
        //    applyRenderScript();
            applyBlur();

        }





    }

    private void applyBlur() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                FlatCorrection fc=new FlatCorrection(FilterActivity.this);
                Bitmap blur=fc.flatCorr(cropped);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        image.setImageBitmap(blur);
                    }
                });
                fc.clear();
            }
        }).start();

    }

    private void applyRenderScript() {
        Filter1 f1=new Filter1(this);
        Bitmap filtered=f1.filter(100,cropped);

        image.setImageBitmap(filtered);

        f1.cleanUp();
    }

    private Bitmap getImageFromStorage(String path,String name) {

        try{
            File f=new File(path,name);
            Bitmap res=BitmapFactory.decodeStream(new FileInputStream(f));
            //now delete file
            if(f.exists()){
                if(f.delete()){
           //         Toast.makeText(this,"Deleted",Toast.LENGTH_SHORT).show();
                }
            }

            return res;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void initializeViews() {
        image=findViewById(R.id.filter_img_view);
    }
}
