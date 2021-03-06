package com.example.camscan.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.camscan.Adapters.Filter_Items_RecyclerAdapter;
import com.example.camscan.Objects.MyDocument;
import com.example.camscan.Objects.MyPicture;
import com.example.camscan.R;
import com.example.camscan.UtilityClass;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

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
    RecyclerView rView;
    Filter_Items_RecyclerAdapter adapter;
    ArrayList<String> names;
    ArrayList<Integer> types;
    ArrayList<Bitmap> tnails;
    ProgressBar pbar;

    SeekBar sBar_bright,sBar_contrast,sBar_exposure;
    TextView propName,propVal;
    LinearLayout resetBtn,adjustView;

    ArrayList<MyPicture> list;
    MyDocument currDoc;


    FloatingActionButton fab;
    boolean isFabOpen=false;

    int br=0;
    int co=0;
    int ex=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);


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

        names=new ArrayList<>();
        types=new ArrayList<>();
        tnails=new ArrayList<>();
        names.add("Original");
        names.add("Luminous");
        names.add("Flat");
        names.add("Grayscale");
        names.add("B/W");
        names.add("invert");
        types.add(1);
        types.add(2);
        types.add(3);
        types.add(4);
        types.add(5);
        types.add(6);

      //  addAllTnails();

        getPicAndDocFromIntent();

        adapter=new Filter_Items_RecyclerAdapter(this,names,types,
                Bitmap.createScaledBitmap(cropped,200,200,true),
                new MyOnclickListener(),tnails);//cropped.copy(cropped.getConfig(),true));
        rView.setHasFixedSize(true);
        rView.setAdapter(adapter);
        rView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false));


        //fab.
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFabOpen){
                    br=co=ex=0;
                    applyBCE(br,co,ex);
                    sBar_exposure.setProgress(0);
                    sBar_contrast.setProgress(0);
                    sBar_bright.setProgress(50);
                    rView.setVisibility(View.GONE);
                    adjustView.setVisibility(View.VISIBLE);
                    isFabOpen=true;
                }else{
                    rView.setVisibility(View.VISIBLE);
                    adjustView.setVisibility(View.GONE);
                    isFabOpen=false;
                }

            }
        });

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

    private void addAllTnails() {
        tnails.add(Bitmap.createScaledBitmap(cropped,200,200,true));

        Filter1 f1=new Filter1(this);
        Bitmap t1=f1.filter(100,Bitmap.createScaledBitmap(cropped,200,200,true));
        tnails.add(t1);
        f1.cleanUp();


        FlatCorrection fc=new FlatCorrection(this);
        t1=fc.flatCorr(Bitmap.createScaledBitmap(cropped,200,200,true));
        tnails.add(t1);
        fc.clear();

        GrayScale gs=new GrayScale();
        t1=gs.toGrayscale(Bitmap.createScaledBitmap(cropped,200,200,true));
        tnails.add(t1);

        BlackAndWhite bnw=new BlackAndWhite(this);
        t1=bnw.getBlackAndWhite(Bitmap.createScaledBitmap(cropped,200,200,true));
        tnails.add(t1);
        bnw.clear();

        Inversion inv=new Inversion(this);
        t1=inv.setInversion(Bitmap.createScaledBitmap(cropped,200,200,true));
        tnails.add(t1);
        inv.clear();
    }


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
        Bitmap bnw=bw.toBnwRender(cropped.copy(cropped.getConfig(),false));
        image.setImageBitmap(bnw);
        selected=bnw;
        bw.clear();

        pbar.setVisibility(View.GONE);
    }

    private void applyContrast(int val){
        System.gc();
        Contrast con=new Contrast(this);
        Bitmap cont=con.setContrast(cropped.copy(cropped.getConfig(),false),val);
        image.setImageBitmap(cont);
        selected=cont;
        con.clear();

    }
    private void applyBrightness(int val){
        System.gc();
        Brightness con=new Brightness(this);
        Bitmap cont=con.setBrightness(cropped.copy(cropped.getConfig(),false),val);
        image.setImageBitmap(cont);
        selected=cont;
        con.clear();
    }
    private void applyGrayScale() {
        System.gc();

        pbar.setVisibility(View.VISIBLE);
        Bitmap gray=new GrayScale().toGrayscale(cropped.copy(cropped.getConfig(),false));
        image.setImageBitmap(gray);
        selected=gray;
        pbar.setVisibility(View.GONE);
    }

    private void applyFlatCorrection() {
        System.gc();
        pbar.setVisibility(View.VISIBLE);
        Thread t=new Thread(new Runnable() {
            @Override
            public void run() {
                FlatCorrection fc=new FlatCorrection(FilterActivity.this);
                Bitmap blur=fc.flatCorr(cropped.copy(cropped.getConfig(),false));
                selected=blur;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        image.setImageBitmap(blur);
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
        Bitmap filtered=f1.filter(exp,cropped.copy(cropped.getConfig(),false));

        image.setImageBitmap(filtered);
        selected=filtered;
        pbar.setVisibility(View.GONE);

        f1.cleanUp();
    }

    private void applyInvert() {
        System.gc();

        pbar.setVisibility(View.VISIBLE);
        Inversion inv=new Inversion(this);
        Bitmap inverted=inv.setInversion(cropped.copy(cropped.getConfig(),false));
        image.setImageBitmap(inverted);
        inv.clear();
        selected=inverted;
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

            return res;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void initializeViews() {
        image=findViewById(R.id.filter_img_view);
        rView=findViewById(R.id.filter_recycler_view);
        pbar=findViewById(R.id.filter_prog_bar);

        sBar_bright=findViewById(R.id.filter_adjust_seek_bright);
        sBar_contrast=findViewById(R.id.filter_adjust_seek_contrast);
        sBar_exposure=findViewById(R.id.filter_adjust_seek_exposure);
        propName=findViewById(R.id.filter_adjust_prop_name);
        propVal=findViewById(R.id.filter_adjust_prop_val);
        resetBtn=findViewById(R.id.filter_adjust_reset_cont);
        adjustView=findViewById(R.id.filter_adjust);
        fab=findViewById(R.id.filter_fab);

    }
    private class MyOnclickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {


            int pos=rView.getChildLayoutPosition(view);
            switch(types.get(pos)){
                case 1:{//original
                    image.setImageBitmap(cropped);
                    selected=cropped;
                    break;
                }
                case 2:{//exposure
                    applyExposure();
                    break;
                }
                case 3:{//flat
                    applyFlatCorrection();

                    break;
                }
                case 4:{
                    //grayscale
                    applyGrayScale();
                    break;
                }
                case 5:{
                    //bnw
                    applyBnW();
                    break;
                }
                case 6:{
                    //invert
                    applyInvert();
                    break;
                }
            }
            adapter.selected=pos;
            adapter.notifyDataSetChanged();
        }
    }


    public void rotate(View view){
        Matrix matrix=new Matrix();
        matrix.preRotate(90);
        selected=Bitmap.createBitmap(selected,0,0,selected.getWidth(),selected.getHeight(),matrix,true);
        cropped=Bitmap.createBitmap(cropped,0,0,cropped.getWidth(),cropped.getHeight(),matrix,true);
        image.setImageBitmap(selected);

    }

    public void onNextButtonClicked(View view){
        BitmapDrawable finalImgDb=(BitmapDrawable)image.getDrawable();
        Bitmap img=finalImgDb.getBitmap();
        String name=list.get(0).getEditedName();
        Uri ediUri=UtilityClass.saveImage(FilterActivity.this,img,name,false);
        list.get(0).setEditedUri(ediUri.toString());

        //convertinto json and send
        String myPicJson=UtilityClass.getStringFromObject(list);
        String myDocJson= UtilityClass.getStringFromObject(currDoc);
        Intent intent=new Intent(FilterActivity.this,InDocRecyclerActivity.class);
        intent.putExtra("MyPicture",myPicJson);
        intent.putExtra("MyDocument",myDocJson);
        intent.putExtra("from","FilterActivity");
        startActivity(intent);
        finish();
    }
}
