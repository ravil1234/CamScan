package com.example.camscan.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    int br=0;
    int co=0;
    int ex=0;

    BottomNavigationView bnv;

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


        bnv.getMenu().setGroupCheckable(0,false,true);
        bnv.setOnNavigationItemSelectedListener(new MyNavListener());
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
            setupThumbnales(res);
            return res;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }


    private void setupThumbnales(Bitmap source) {
        if(!isEffectThreadRunning){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    isEffectThreadRunning=true;
                    originalTnail=Bitmap.createScaledBitmap(source,100,100,true);

                    Filter1 f1=new Filter1(FilterActivity.this);
                    illuminateTnail=f1.filter(100,originalTnail.copy(originalTnail.getConfig(),false));
                    f1.cleanUp();

                    FlatCorrection flat=new FlatCorrection(FilterActivity.this);
                    flatTnail=flat.flatCorr(originalTnail.copy(originalTnail.getConfig(),false));

                    grayTnail=new GrayScale().toGrayscale(originalTnail.copy(originalTnail.getConfig(),false));

                    bnwTnail=new BlackAndWhite(FilterActivity.this).toBnwRender(originalTnail.copy(originalTnail.getConfig(),false));

                    Inversion inv=new Inversion(FilterActivity.this);
                    invertTnail=inv.setInversion(originalTnail.copy(originalTnail.getConfig(),false));
                    inv.clear();


                    originalTnail=getRoundedCroppedBitmap(originalTnail);
                    illuminateTnail=getRoundedCroppedBitmap(illuminateTnail);
                    flatTnail=getRoundedCroppedBitmap(flatTnail);
                    grayTnail=getRoundedCroppedBitmap(grayTnail);
                    bnwTnail=getRoundedCroppedBitmap(bnwTnail);
                    invertTnail=getRoundedCroppedBitmap(invertTnail);
                    isEffectThreadRunning=false;
                }
            }).start();

        }

    }

    private Bitmap getRoundedCroppedBitmap(Bitmap bitmap) {
        int widthLight = bitmap.getWidth();
        int heightLight = bitmap.getHeight();

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);
        Paint paintColor = new Paint();
        paintColor.setFlags(Paint.ANTI_ALIAS_FLAG);

        RectF rectF = new RectF(new Rect(0, 0, widthLight, heightLight));

        canvas.drawRoundRect(rectF, widthLight / 2 ,heightLight / 2,paintColor);

        Paint paintImage = new Paint();
        paintImage.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        canvas.drawBitmap(bitmap, 0, 0, paintImage);

        return output;
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

//        AlertDialog.Builder builder=new AlertDialog.Builder(FilterActivity.this,R.style.CustomDialog);
//        builder.setView(pbar);
        pbar.setVisibility(View.VISIBLE);
//        AlertDialog d=builder.create();
//        d.show();
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
                       // d.dismiss();
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

    private class MyNavListener implements BottomNavigationView.OnNavigationItemSelectedListener{

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()){
                case R.id.action_filter_bounding_box:{
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
                    rotate(null);
                    break;
                }
                case R.id.action_filter_effects:{
                    AlertDialog.Builder builder=new AlertDialog.Builder(FilterActivity.this,R.style.CustomDialog);
                    View view= LayoutInflater.from(FilterActivity.this).inflate(R.layout.fragment_filter_effects,null);
                    builder.setView(view);
                    ImageView ef1,ef2,ef3,ef4,ef5,ef6,icon;
                    ef1=view.findViewById(R.id.fragment_filter_effects_1);
                    ef2=view.findViewById(R.id.fragment_filter_effects_2);
                    ef3=view.findViewById(R.id.fragment_filter_effects_3);
                    ef4=view.findViewById(R.id.fragment_filter_effects_4);
                    ef5=view.findViewById(R.id.fragment_filter_effects_5);
                    ef6=view.findViewById(R.id.fragment_filter_effects_6);
                    icon=view.findViewById(R.id.fragment_filter_effects_icon);


                    if(originalTnail==null){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Bitmap original=Bitmap.createScaledBitmap(cropped,100,100,true);

                                Filter1 f1=new Filter1(FilterActivity.this);
                                Bitmap filtered=f1.filter(100,original.copy(original.getConfig(),false));
                                f1.cleanUp();

                                FlatCorrection flat=new FlatCorrection(FilterActivity.this);
                                Bitmap flatBit=flat.flatCorr(original.copy(original.getConfig(),false));

                                Bitmap gray=new GrayScale().toGrayscale(original.copy(original.getConfig(),false));

                                Bitmap bnw=new BlackAndWhite(FilterActivity.this).toBnwRender(original.copy(original.getConfig(),false));

                                Inversion inv=new Inversion(FilterActivity.this);
                                Bitmap inverted=inv.setInversion(original.copy(original.getConfig(),false));
                                inv.clear();

                                originalTnail=getRoundedCroppedBitmap(original);
                                illuminateTnail=getRoundedCroppedBitmap(filtered);
                                flatTnail =getRoundedCroppedBitmap(flatBit);
                                grayTnail =getRoundedCroppedBitmap(gray);
                                bnwTnail =getRoundedCroppedBitmap(bnw);
                                invertTnail =getRoundedCroppedBitmap(inverted);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ef1.setImageBitmap(originalTnail);
                                        ef2.setImageBitmap(illuminateTnail);
                                        ef3.setImageBitmap(flatTnail);
                                        ef4.setImageBitmap(grayTnail);
                                        ef5.setImageBitmap(bnwTnail);
                                        ef6.setImageBitmap(invertTnail);
                                    }
                                });

                            }
                        }).start();
                    }else{

                        ef1.setImageBitmap(originalTnail);
                        ef2.setImageBitmap(illuminateTnail);
                        ef3.setImageBitmap(flatTnail);
                        ef4.setImageBitmap(grayTnail);
                        ef5.setImageBitmap(bnwTnail);
                        ef6.setImageBitmap(invertTnail);

                    }
                    AlertDialog d=builder.create();

                    icon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //close this activity

                            d.dismiss();
                        }
                    });

                    ef1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            image.setImageBitmap(cropped);
                            selected=cropped;
                            d.dismiss();
                        }
                    });
                    ef2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            applyExposure();
                            d.dismiss();
                        }
                    });
                    ef3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            applyFlatCorrection();
                            d.dismiss();
                        }
                    });
                    ef4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            applyGrayScale();
                            d.dismiss();
                        }
                    });
                    ef5.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            applyBnW();
                            d.dismiss();
                        }
                    });
                    ef6.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            applyInvert();
                            d.dismiss();
                        }
                    });


                    d.show();

                    break;
                }
                case R.id.action_filter_modification:{
                    if(!isFabOpen){
                        br=co=ex=0;
                        applyBCE(br,co,ex);
                        sBar_exposure.setProgress(0);
                        sBar_contrast.setProgress(0);
                        sBar_bright.setProgress(50);
                        //  rView.setVisibility(View.GONE);
                        adjustView.setVisibility(View.VISIBLE);
                        isFabOpen=true;
                    }else{
                        // rView.setVisibility(View.VISIBLE);
                        adjustView.setVisibility(View.GONE);
                        isFabOpen=false;
                    }
                    break;
                }
                case R.id.action_filter_next:{
                    onNextButtonClicked(null);
                    break;
                }
            }

            return true;
        }
    }


}
