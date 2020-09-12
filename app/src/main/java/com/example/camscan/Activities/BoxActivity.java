package com.example.camscan.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.camscan.Adapters.BoxRecyclerAdapter;
import com.example.camscan.CustomProgressBar;
import com.example.camscan.MyLayouts.MyBoxLayout;
import com.example.camscan.Objects.MyDocument;
import com.example.camscan.Objects.MyPicture;
import com.example.camscan.R;
import com.example.camscan.RenderScriptJava.BlackAndWhite;
import com.example.camscan.RenderScriptJava.Brightness;
import com.example.camscan.RenderScriptJava.Contrast;
import com.example.camscan.RenderScriptJava.Filter1;
import com.example.camscan.RenderScriptJava.FlatCorrection;
import com.example.camscan.RenderScriptJava.GrayScale;
import com.example.camscan.RenderScriptJava.Inversion;
import com.example.camscan.RenderScriptJava.rotator;
import com.example.camscan.UtilityClass;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.itextpdf.text.pdf.draw.LineSeparator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.pqc.math.ntru.util.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class BoxActivity extends AppCompatActivity {

    private static final String TAG = "BOXACTIVITY";
   // FloatingActionButton nextBtn;

    ViewPager2 vp2;

    ArrayList<MyPicture> list;
    BoxRecyclerAdapter adapter;

    MyDocument currDoc;

    BottomNavigationView bnv;

    CustomProgressBar pBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box);
        pBar=new CustomProgressBar(this);
        initializeViews();
        getSupportActionBar().hide();

        list=new ArrayList<>();
        adapter=new BoxRecyclerAdapter(this,list);
        adapter.setViewPager(vp2);
        vp2.setAdapter(adapter);


        vp2.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                vp2.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width=vp2.getWidth();
                int height=vp2.getHeight();
                adapter.setDimensions(width,height);
                populateList();
            }
        });
//        populateList();

        bnv.getMenu().setGroupCheckable(0,false,true);
        bnv.setOnNavigationItemSelectedListener(new MyNavListener());

        if(autoCropSelected()){
            onNextPressed(null);
        }
    }

    private boolean autoCropSelected() {
        //TODO check it in the settings
        return false;
    }

    private void initializeViews() {
        vp2=findViewById(R.id.box_viewPager);
        bnv=findViewById(R.id.box_navigation);
    }
    private void populateList() {
        //fetch from Json
        String myPicString=getIntent().getStringExtra("MyPicture");
        String myDocString=getIntent().getStringExtra("MyDocument");


        ArrayList<MyPicture> listTmp=UtilityClass.getListOfPics(myPicString);
        if(listTmp!=null){
            list.addAll(listTmp);
        }
        currDoc= UtilityClass.getDocFromJson(myDocString);
       // Log.e(TAG, "populateList: "+"currDoc"+currDoc.getDid() );
       // adapter.notifyDataSetChanged();

        vp2.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                initializeCoordinates();
                vp2.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                adapter.notifyDataSetChanged();
            }
        });
//        initializeCoordinates();
//        addDummyData();
    }

    private void initializeCoordinates() {
        ImageView tmp=findViewById(R.id.box_temp_btn);
        int w=tmp.getWidth();
        adapter.setDot(w);
        for(MyPicture p:list){
            if(p.getCoordinates().get(3).x==0 &&p.getCoordinates().get(3).y==0){
                ArrayList<Point> pts=new ArrayList<>();
                pts.add(new Point(w/2,w/2));
                pts.add(new Point(vp2.getWidth()-w/2,w/2));
                pts.add(new Point(vp2.getWidth()-w/2,vp2.getHeight()-w/2));
                pts.add(new Point(w/2,vp2.getHeight()-w/2));
                p.setCoordinates(pts);
            //    UtilityClass.displayPoints(p.getCoordinates());
              //  Log.e(TAG, "initializeCoordinates: "+"THIS" );
            }
        }

    }

    private void addDummyData() {
        currDoc=new MyDocument("MyDoc",21648612l,1354516l,null);
        list.add(new MyPicture(0,"file:///storage/emulated/0/CamScan/.original/159514406622666226.jpg",null,"01",1,null,0,0));
      //  list.add(new MyPicture(0,"file:///storage/emulated/0/CamScan/.original/159514406650066500.jpg",null,"02",2,null));
       // list.add(new MyPicture(0,"file:///storage/emulated/0/CamScan/.original/1595087242244242244.jpg",null,"03",3,null));
       // list.add(new MyPicture(0,"file:///storage/emulated/0/CamScan/.original/1595133729167729167.jpg",null,"04",4,null));
        adapter.notifyDataSetChanged();

    }

    private String[] saveBitmap(Bitmap trns) {

        ContextWrapper cw=new ContextWrapper(BoxActivity.this.getApplicationContext());

        File dir=cw.getDir(".temp", Context.MODE_PRIVATE);
        String name=System.currentTimeMillis()+".jpg";
        File path=new File(dir,name);

        FileOutputStream fos=null;
        try{
            fos=new FileOutputStream(path);
            trns.compress(Bitmap.CompressFormat.JPEG,100,fos);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            try{
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new String[]{name,dir.getAbsolutePath()};
      //  return dir.getAbsolutePath();

    }



    public Bitmap cornerPin(Bitmap B,ArrayList<Point> dis){
      //  UtilityClass.displayPoints(dis);
        int w=B.getWidth();
        int h=B.getHeight();

        int ImgWidth=vp2.getWidth();
        int imgHeight=vp2.getHeight();

        float widthRatio=(float)w/(float)ImgWidth;
        float heightRatio=(float)h/(float) imgHeight;

        int x1=(int)(dis.get(0).x*widthRatio);
        int y1=(int)(dis.get(0).y*heightRatio);

        int x2=(int)(dis.get(1).x*widthRatio);
        int y2=(int)(dis.get(1).y*heightRatio);

        int x3=(int)(dis.get(2).x*widthRatio);
        int y3=(int)(dis.get(2).y*heightRatio);

        int x4=(int)(dis.get(3).x*widthRatio);
        int y4=(int)(dis.get(3).y*heightRatio);

        int minLeft=Math.min(Math.min(x1,x2),Math.min(x3,x4));
        int maxRight=Math.max(Math.max(x1,x2),Math.max(x3,x4));
        int minTop=Math.min(Math.min(y1,y2),Math.min(y3,y4));
        int maxBottom=Math.max(Math.max(y1,y2),Math.max(y3,y4));

        int wid=maxRight-minLeft;
        int hei=maxBottom-minTop;

        float[] src = {
                minLeft, minTop, // Coordinate of top left point
                minLeft, maxBottom, // Coordinate of bottom left point
                maxRight, maxBottom, // Coordinate of bottom right point
                maxRight,minTop  // Coordinate of top right point
        };


        float[] dst = {
                x1,y1,//dis.get(0).x, dis.get(0).y,        // Desired coordinate of top left point
                x4,y4,//dis.get(3).x, dis.get(3).y,        // Desired coordinate of bottom left point
                x3,y3,//dis.get(2).x, dis.get(2).y, // Desired coordinate of bottom right point
                x2,y2//dis.get(1).x, dis.get(1).y  // Desired coordinate of top right point
        };


//        Log.e(TAG, "cornerPin: "+dis );
        Bitmap result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        Canvas c = new Canvas(result);
        Matrix m = new Matrix();
        m.setPolyToPoly(dst, 0, src, 0, 4);
        c.setMatrix(m);
        c.drawBitmap(B, 0,0, p);

        result=Bitmap.createBitmap(result,minLeft,minTop,wid,hei);
        if(B!=null){
            B.recycle();
            B=null;
        }
//        Log.e(TAG, "cornerPin: "+ result.getHeight()+" "+result.getWidth());
     //   Log.e(TAG, "cornerPin: "+resized.getHeight()+" "+resized.getWidth() );
        return result;

    }


    public void onNextPressed(View view){
        AlertDialog.Builder b=new AlertDialog.Builder(this);
        View v=LayoutInflater.from(this).inflate(R.layout.fragment_progress,null);
        b.setView(v);
        AlertDialog d=b.create();
        d.setCancelable(false);
        d.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String[] save=null;
                int screenWidth=vp2.getWidth();
                int screenHeight=vp2.getHeight();
                Bitmap transformed=null;
                if(list.size()==1){
                    MyPicture p =list.get(0);
                    p.setS_width(screenWidth);
                    p.setS_height(screenHeight);
                    transformed= BitmapFactory.decodeFile(Uri.parse(p.getOriginalUri()).getPath());
                    transformed=cornerPin(transformed,p.getCoordinates());
                    Bitmap emptyBitmap = Bitmap.createBitmap(transformed.getWidth(), transformed.getHeight(),
                            transformed.getConfig());

                    if(transformed.sameAs(emptyBitmap)){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                d.dismiss();
                                Toast.makeText(BoxActivity.this, "Can't Crop", Toast.LENGTH_SHORT).show();
                            }
                        });
                        vp2.setCurrentItem(0);
                        transformed.recycle();
                        return;
                    }
                    save=saveBitmap(transformed);
                }

                for(MyPicture p :list){
                    p.setS_width(screenWidth);
                    p.setS_height(screenHeight);
                }
                String myPics=new Gson().toJson(list);
                String myDoc=new Gson().toJson(currDoc);

                if(list.size()==1){
                    //single image
                    Intent intent=new Intent(BoxActivity.this,FilterActivity.class);
                    intent.putExtra("path",save[1]);
                    intent.putExtra("name",save[0]);
                    intent.putExtra("MyPicture",myPics);
                    intent.putExtra("MyDocument",myDoc);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            d.dismiss();
                            startActivity(intent);
                            finish();
                        }
                    });

                }else{
                    //goto indoc activity

                    Intent intent=new Intent(BoxActivity.this,MyDocumentActivity.class);
                    intent.putExtra("MyPicture",myPics);
                    intent.putExtra("MyDocument",myDoc);
                    intent.putExtra("from","BoxActivity");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            d.dismiss();
                            startActivity(intent);
                            finish();
                        }
                    });

                }


            }
        }).start();



    }

    public void rotateBitmapClockWise(View view){

        AlertDialog.Builder builder=new AlertDialog.Builder(BoxActivity.this);
        View view2=LayoutInflater.from(this).inflate(R.layout.fragment_progress,null);
        builder.setView(view2);
        AlertDialog d=builder.create();
        d.setCancelable(false);
        d.show();


        new Thread(new Runnable() {
            @Override
            public void run() {
                int currIndex=vp2.getCurrentItem();
                MyPicture currPic=list.get(currIndex);
                Bitmap image=BitmapFactory.decodeFile(Uri.parse(currPic.getOriginalUri()).getPath());
                if(image!=null){
//                    Matrix matrix=new Matrix();
//                    matrix.preRotate(90);
//                    image=Bitmap.createBitmap(image,0,0,image.getWidth(),image.getHeight(),matrix,true);
                    //currPic.setImg(image);
                    rotator r=new rotator(BoxActivity.this);
                    image=r.rotate(image,true);
                    currPic.setCoordinates(null);
                    String name=Uri.parse(currPic.getOriginalUri()).getLastPathSegment();
                    Uri savedUri=UtilityClass.saveImage(BoxActivity.this,image,name,true);
                    currPic.setOriginalUri(savedUri.toString());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //resumeInteraction();
                            d.dismiss();
                            adapter.notifyDataSetChanged();
                        }
                    });

                    image.recycle();

                }
            }
        }).start();

    }
    public void rotateBitmapAntiClockWise(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(BoxActivity.this);
        View view2 = LayoutInflater.from(this).inflate(R.layout.fragment_progress, null);
        builder.setView(view2);
        final AlertDialog d = builder.create();
        d.show();
        stopInteraction();

        new Thread(new Runnable() {
            @Override
            public void run() {
                int currIndex = vp2.getCurrentItem();
                MyPicture currPic = list.get(currIndex);
                Bitmap image=BitmapFactory.decodeFile(Uri.parse(currPic.getOriginalUri()).getPath());// = currPic.getImg();
                if (image != null) {
//                    Matrix matrix = new Matrix();
//                    matrix.preRotate(-90);
//                    image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
//                    currPic.setImg(image);
                    rotator r=new rotator(BoxActivity.this);
                    image=r.rotate(image,false);
                    currPic.setCoordinates(null);
                    String name=Uri.parse(currPic.getOriginalUri()).getLastPathSegment();
                    Uri savedUri=UtilityClass.saveImage(BoxActivity.this,image,name,true);
                    currPic.setOriginalUri(savedUri.toString());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            resumeInteraction();
                            d.dismiss();
                            adapter.notifyDataSetChanged();
                        }
                    });
                }

            }
        }).start();
    }

    public void resetCurrPoint(View view){
        int currInd=vp2.getCurrentItem();
        MyPicture pic=list.get(currInd);
        ImageView tmp=findViewById(R.id.box_temp_btn);
        int w=tmp.getWidth();
        ArrayList<Point> pts=new ArrayList<>();
        pts.add(new Point(w/2,w/2));
        pts.add(new Point(vp2.getWidth()-w/2,w/2));
        pts.add(new Point(vp2.getWidth()-w/2,vp2.getHeight()-w/2));
        pts.add(new Point(w/2,vp2.getHeight()-w/2));

        pic.setCoordinates(pts);
        adapter.notifyDataSetChanged();
    }

    public void retakePic()
    {
        int index=vp2.getCurrentItem();
        MyPicture p=list.get(index);
        //String picString=UtilityClass.getStringFromObject(p);
        Intent intent=new Intent(BoxActivity.this,SingleCamActivity.class);
        intent.putExtra("PICTURE_URI",p.getOriginalUri());
        startActivityForResult(intent,UtilityClass.RETAKE_REQ_CODE);
    }

    public void stopInteraction(){
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
    public void resumeInteraction(){
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==UtilityClass.RETAKE_REQ_CODE){
            if(resultCode==RESULT_OK){

//                int index=vp2.getCurrentItem();
//                MyPicture p=list.get(index);
                adapter.notifyDataSetChanged();
            }else if(resultCode==RESULT_CANCELED){
                //cancelled!! dont do anything
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
            }
        }

    }
//
//    public  class MyAsync extends AsyncTask<Bitmap,Void,Bitmap>{
//
//        @Override
//        protected Bitmap doInBackground(Bitmap... bitmaps) {
//            FlatCorrection fc=new FlatCorrection(BoxActivity.this);
//            Bitmap blur=fc.flatCorr(bitmaps[0].copy(bitmaps[0].getConfig(),false));
//            return blur;
//        }
//    }


    private class MyNavListener implements BottomNavigationView.OnNavigationItemSelectedListener{

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()){
                case R.id.action_box_retake:{
                    //call for retake in new activity with return intent
                    retakePic();
                    break;
                }
                case R.id.action_box_clockwise:{
                    rotateBitmapClockWise(null);
                    break;
                }
                case R.id.action_box_Full_Screen:{
                    resetCurrPoint(null);
                    break;
                }
                case R.id.action_box_anti_clockwise:{
                    rotateBitmapAntiClockWise(null);
                    break;
                }
                case R.id.action_box_next:{
                    onNextPressed(null);
                    break;
                }
            }

            return true;
        }
    }
}
