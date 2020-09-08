package com.example.camscan.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import android.text.Layout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.camscan.Adapters.Filter_Items_RecyclerAdapter;
import com.example.camscan.Database.MyDatabase;
import com.example.camscan.Objects.MyDocument;
import com.example.camscan.Objects.MyPicture;
import com.example.camscan.R;
import com.example.camscan.RenderScriptJava.rotator;
import com.example.camscan.UtilityClass;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.example.camscan.RenderScriptJava.BCE;
import com.example.camscan.RenderScriptJava.BlackAndWhite;
import com.example.camscan.RenderScriptJava.Brightness;
import com.example.camscan.RenderScriptJava.Contrast;
import com.example.camscan.RenderScriptJava.Filter1;
import com.example.camscan.RenderScriptJava.FlatCorrection;
import com.example.camscan.RenderScriptJava.GrayScale;
import com.example.camscan.RenderScriptJava.Inversion;

import org.spongycastle.pqc.math.ntru.util.Util;

public class FilterActivity extends AppCompatActivity {

    private static final String TAG = "Filter Activity";
    Bitmap cropped;
    Bitmap selected;
    ImageView image;
 //   RecyclerView rView;
   // Filter_Items_RecyclerAdapter adapter;
//    ArrayList<String> names;
//    ArrayList<Integer> types;
//    ArrayList<Bitmap> tnails;
    ProgressBar pbar;
    Boolean isEffectThreadRunning=false;

    SeekBar sBar_bright,sBar_contrast,sBar_exposure;
    TextView propName,propVal;
    LinearLayout resetBtn,adjustView;

    ArrayList<MyPicture> list;
    MyDocument currDoc;

    //THumbnales for effects
    Bitmap originalTnail,illuminateTnail,flatTnail,grayTnail,bnwTnail,invertTnail;
    //Thumbnales fro effects END

   // FloatingActionButton fab;
    boolean isFabOpen=false;
    boolean isEffectOpen=false;

    LinearLayout filter_effect_frag;
    ImageView eff1,eff2,eff3,eff4,eff5,eff6;

    int br=0;
    int co=0;
    int ex=0;

    BottomNavigationView bnv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        getSupportActionBar().hide();
        initializeViews();


        String path=getIntent().getStringExtra("path");
        String name=getIntent().getStringExtra("name");

        cropped=getImageFromStorage(path,name);

        if(cropped!=null){
            image.setImageBitmap(cropped);
            selected=cropped;
            //default Filter
            applyFlatCorrection();
        }



        getPicAndDocFromIntent();



        //setting seekbars

        sBar_bright.setMax(100);
        sBar_exposure.setMax(100);
        sBar_contrast.setMax(100);

        sBar_contrast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                propName.setText("Contrast :");
                propVal.setText(String.valueOf(i));
                co=i*2;
                applyBCE(br,co,ex);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sBar_exposure.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                propName.setText("Exposure :");
                propVal.setText(String.valueOf(i));
                ex=i;
                applyBCE(br,co,ex);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sBar_bright.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                propName.setText("Brightness :");
                propVal.setText(String.valueOf(i));
                br=(i-50)*2;
                applyBCE(br,co,ex);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                br=co=ex=0;
                sBar_bright.setProgress(50);
                sBar_contrast.setProgress(0);
                sBar_exposure.setProgress(0);
                applyBCE(br,co,ex);
            }
        });


        //bnv.getMenu().setGroupCheckable(0,false,true);
        bnv.getMenu().getItem(0).setCheckable(false);       //BOUND
        bnv.getMenu().getItem(1).setCheckable(false);       //ROTATE
        bnv.getMenu().getItem(2).setCheckable(true);          //EFFECT
        bnv.getMenu().getItem(3).setCheckable(true);       //MODIFY
        bnv.getMenu().getItem(4).setCheckable(false);       //NEXT

        bnv.setOnNavigationItemSelectedListener(new MyNavListener());

        setListenersOnEffects();
    }

    private void setListenersOnEffects() {

        eff1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image.setImageBitmap(cropped);
                selected=cropped;
                bnv.getMenu().getItem(2).setChecked(false);
                closeEffectFrag();
            }
        });
        eff2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                applyExposure();
                bnv.getMenu().getItem(2).setChecked(false);
                closeEffectFrag();
            }
        });
        eff3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                applyFlatCorrection();
                bnv.getMenu().getItem(2).setChecked(false);
                closeEffectFrag();
            }
        });
        eff4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                applyGrayScale();
                bnv.getMenu().getItem(2).setChecked(false);
                closeEffectFrag();
            }
        });
        eff5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                applyBnW();
                bnv.getMenu().getItem(2).setChecked(false);
                closeEffectFrag();
            }
        });
        eff6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                applyInvert();
                bnv.getMenu().getItem(2).setChecked(false);
                closeEffectFrag();
            }
        });

    }

    private void initializeViews() {
        image=findViewById(R.id.filter_img_view);
       // rView=findViewById(R.id.filter_recycler_view);
        pbar=findViewById(R.id.filter_prog_bar);

        sBar_bright=findViewById(R.id.filter_adjust_seek_bright);
        sBar_contrast=findViewById(R.id.filter_adjust_seek_contrast);
        sBar_exposure=findViewById(R.id.filter_adjust_seek_exposure);
        propName=findViewById(R.id.filter_adjust_prop_name);
        propVal=findViewById(R.id.filter_adjust_prop_val);
        resetBtn=findViewById(R.id.filter_adjust_reset_cont);
        adjustView=findViewById(R.id.filter_adjust);
       // fab=findViewById(R.id.filter_fab);

        bnv=findViewById(R.id.filter_navigation);
        filter_effect_frag=findViewById(R.id.filter_effect_fragment);

        eff1=findViewById(R.id.filter_effect_1);
        eff2=findViewById(R.id.filter_effect_2);
        eff3=findViewById(R.id.filter_effect_3);
        eff4=findViewById(R.id.filter_effect_4);
        eff5=findViewById(R.id.filter_effect_5);
        eff6=findViewById(R.id.filter_effect_6);
    }

    private void getPicAndDocFromIntent() {
        String myPics= getIntent().getStringExtra("MyPicture");
        String myDoc=getIntent().getStringExtra("MyDocument");
        ArrayList<MyPicture> pic=UtilityClass.getListOfPics(myPics);
        if(pic!=null){
            list=pic;
        }
        currDoc=UtilityClass.getDocFromJson(myDoc);
    }

//
//    private void addAllTnails() {
//        tnails.add(Bitmap.createScaledBitmap(cropped,200,200,true));
//
//        Filter1 f1=new Filter1(this);
//        Bitmap t1=f1.filter(100,Bitmap.createScaledBitmap(cropped,200,200,true));
//        tnails.add(t1);
//        f1.cleanUp();
//
//
//        FlatCorrection fc=new FlatCorrection(this);
//        t1=fc.flatCorr(Bitmap.createScaledBitmap(cropped,200,200,true));
//        tnails.add(t1);
//        fc.clear();
//
//        GrayScale gs=new GrayScale();
//        t1=gs.toGrayscale(Bitmap.createScaledBitmap(cropped,200,200,true));
//        tnails.add(t1);
//
//        BlackAndWhite bnw=new BlackAndWhite(this);
//        t1=bnw.getBlackAndWhite(Bitmap.createScaledBitmap(cropped,200,200,true));
//        tnails.add(t1);
//        bnw.clear();
//
//        Inversion inv=new Inversion(this);
//        t1=inv.setInversion(Bitmap.createScaledBitmap(cropped,200,200,true));
//        tnails.add(t1);
//        inv.clear();
//    }



    private void applyBCE(int b,int c,int e){
        System.gc();
        pbar.setVisibility(View.VISIBLE);
        BCE bce=new BCE(this);
        Bitmap ex=bce.setBCE(selected.copy(selected.getConfig(),false),b,c,e);
        image.setImageBitmap(ex);
        //selected=bnw;
        bce.clear();

        pbar.setVisibility(View.GONE);
    }

    private void applyBnW() {
        System.gc();
        pbar.setVisibility(View.VISIBLE);
        BlackAndWhite bw=new BlackAndWhite(this);
        selected=bw.toBnwRender(cropped.copy(cropped.getConfig(),false));
        image.setImageBitmap(selected);
        bw.clear();

        pbar.setVisibility(View.GONE);
    }

    private void applyGrayScale() {
        System.gc();

        pbar.setVisibility(View.VISIBLE);
        selected=new GrayScale().toGrayscale(cropped.copy(cropped.getConfig(),false));
        image.setImageBitmap(selected);
        pbar.setVisibility(View.GONE);
    }

    private void applyFlatCorrection() {
        System.gc();
        pbar.setVisibility(View.VISIBLE);
        Thread t=new Thread(new Runnable() {
            @Override
            public void run() {
                FlatCorrection fc=new FlatCorrection(FilterActivity.this);
                selected=fc.flatCorr(cropped.copy(cropped.getConfig(),false));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        image.setImageBitmap(selected);
                        pbar.setVisibility(View.GONE);
                    }
                });
                fc.clear();
            }
        });
        t.start();

    }
    private void applyExposure(){
        applyExposure(100);
    }
    private void applyExposure(int exp) {
        System.gc();
        pbar.setVisibility(View.VISIBLE);
        Filter1 f1=new Filter1(this);
        selected=f1.filter(exp,cropped.copy(cropped.getConfig(),false));
        image.setImageBitmap(selected);
        pbar.setVisibility(View.GONE);
        f1.cleanUp();
    }

    private void applyInvert() {
        System.gc();

        pbar.setVisibility(View.VISIBLE);
        Inversion inv=new Inversion(this);
        selected=inv.setInversion(cropped.copy(cropped.getConfig(),false));
        image.setImageBitmap(selected);
        inv.clear();
        pbar.setVisibility(View.GONE);
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
           // setupThumbnales(res);
            return res;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }





    public void rotate(View view){
        pbar.setVisibility(View.VISIBLE);
        stopInteraction();
        new Thread(new Runnable() {
            @Override
            public void run() {

                rotator r=new rotator(FilterActivity.this);
                selected=r.rotate(selected,true);
                r=new rotator(FilterActivity.this);
                cropped=r.rotate(cropped,true);
                //selected=Bitmap.createBitmap(selected,0,0,selected.getWidth(),selected.getHeight(),matrix,true);
                //cropped=Bitmap.createBitmap(cropped,0,0,cropped.getWidth(),cropped.getHeight(),matrix,true);
                //setupThumbnales(selected);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resumeInteraction();
                        image.setImageBitmap(selected);
                        pbar.setVisibility(View.GONE);
                    }
                });
            }
        }).start();

    }

    public void onNextButtonClicked(View view){

        AlertDialog.Builder builder=new AlertDialog.Builder(FilterActivity.this);
        View v=LayoutInflater.from(this).inflate(R.layout.fragment_progress,null);
        builder.setView(v);

        AlertDialog d=builder.create();
        d.setCancelable(false);
        d.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                BitmapDrawable finalImgDb=(BitmapDrawable)image.getDrawable();
                Bitmap img=finalImgDb.getBitmap();
                String name=list.get(0).getEditedName();
                Uri ediUri=UtilityClass.saveImage(FilterActivity.this,img,name,false);
                list.get(0).setEditedUri(ediUri.toString());
                if(currDoc.getDid()==0){
                    //new doc
                    currDoc.setfP_URI(ediUri.toString());
                }
                //convertinto json and send
                String myPicJson=UtilityClass.getStringFromObject(list);
                String myDocJson= UtilityClass.getStringFromObject(currDoc);
                Intent intent=new Intent(FilterActivity.this,MyDocumentActivity.class);
                intent.putExtra("MyPicture",myPicJson);
                intent.putExtra("MyDocument",myDocJson);
                intent.putExtra("from","FilterActivity");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        d.dismiss();
                        pbar.setVisibility(View.GONE);
                        startActivity(intent);
                        finish();
                    }
                });

            }
        }).start();

    }

    public void stopInteraction(){
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
    public void resumeInteraction(){
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void openEffectFrag(){
        if(!isEffectOpen){
            isEffectOpen=true;

            Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.my_doc_anim_slide_up);

            filter_effect_frag.setVisibility(View.VISIBLE);
            filter_effect_frag.startAnimation(slideUp);

        }
    }
    private void closeEffectFrag(){
        if(isEffectOpen){
            isEffectOpen=false;
            filter_effect_frag.setVisibility(View.GONE);

            Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.my_doc_anim_slide_down);


            filter_effect_frag.startAnimation(slideDown);
            filter_effect_frag.setVisibility(View.GONE);
        }
    }

    private class MyNavListener implements BottomNavigationView.OnNavigationItemSelectedListener{

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if(isEffectOpen && item.getItemId()!=R.id.action_filter_effects){
                closeEffectFrag();
            }
            if(isFabOpen && item.getItemId()!=R.id.action_filter_modification){
                adjustView.setVisibility(View.GONE);
                isFabOpen=false;
            }
            switch (item.getItemId()){
                case R.id.action_filter_bounding_box:{
                    bnv.getMenu().getItem(2).setChecked(false);
                    bnv.getMenu().getItem(3).setChecked(false);
                    //go back to prev activity
                    String myPicJson=UtilityClass.getStringFromObject(list);
                    String myDocJson= UtilityClass.getStringFromObject(currDoc);
                    Intent backIntent=new Intent(FilterActivity.this,BoxActivity.class);
                    backIntent.putExtra("MyPicture",myPicJson);
                    backIntent.putExtra("MyDocument",myDocJson);
                    startActivity(backIntent);
                    finish();
                    break;
                }
                case R.id.action_filter_rotate:{
                    bnv.getMenu().getItem(2).setChecked(false);
                    bnv.getMenu().getItem(3).setChecked(false);
                    rotate(null);
                    break;
                }
                case R.id.action_filter_effects:{
                    if(bnv.getMenu().getItem(2).isChecked()){
                        bnv.getMenu().getItem(2).setChecked(false);
                    }
                    bnv.getMenu().getItem(3).setChecked(false);

                    if(isEffectOpen){
                        closeEffectFrag();
                    }else{
                        openEffectFrag();
                    }

                    break;
                }
                case R.id.action_filter_modification:{
                    if(bnv.getMenu().getItem(3).isChecked()){
                        bnv.getMenu().getItem(3).setChecked(false);
                    }
                    bnv.getMenu().getItem(2).setChecked(false);
                    if(!isFabOpen){
                        br=co=ex=0;
                        applyBCE(br,co,ex);
                        sBar_exposure.setProgress(0);
                        sBar_contrast.setProgress(0);
                        sBar_bright.setProgress(50);
                        //  rView.setVisibility(View.GONE);
                        Animation slideUp = AnimationUtils.loadAnimation(FilterActivity.this, R.anim.my_doc_anim_slide_up);

                        adjustView.setVisibility(View.VISIBLE);
                        adjustView.startAnimation(slideUp);

                        isFabOpen=true;
                    }else{
                        Animation slideUp = AnimationUtils.loadAnimation(FilterActivity.this, R.anim.my_doc_anim_slide_down);

                        adjustView.setVisibility(View.GONE);
                        adjustView.startAnimation(slideUp);

                        isFabOpen=false;
                    }
                    break;
                }
                case R.id.action_filter_next:{
                    bnv.getMenu().getItem(2).setChecked(false);
                    bnv.getMenu().getItem(3).setChecked(false);
                    onNextButtonClicked(null);
                    break;
                }
            }

            return true;
        }
    }


}
