package com.example.camscan.Activities;

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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.camscan.Adapters.BoxRecyclerAdapter;
import com.example.camscan.MyLayouts.MyBoxLayout;
import com.example.camscan.Objects.MyDocument;
import com.example.camscan.Objects.MyPicture;
import com.example.camscan.R;
import com.example.camscan.RenderScriptJava.FlatCorrection;
import com.example.camscan.UtilityClass;
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
    FloatingActionButton nextBtn;

    ViewPager2 vp2;

    ArrayList<MyPicture> list;
    BoxRecyclerAdapter adapter;

    MyDocument currDoc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box);

        initializeViews();
        getSupportActionBar().hide();

        list=new ArrayList<>();
        adapter=new BoxRecyclerAdapter(this,list);
        adapter.setViewPager(vp2);
        vp2.setAdapter(adapter);
        populateList();


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
        adapter.notifyDataSetChanged();

//        addDummyData();
    }

    private void addDummyData() {
        currDoc=new MyDocument("MyDoc",21648612l,1354516l,1,null);
        list.add(new MyPicture(0,"file:///storage/emulated/0/CamScan/.original/159514406622666226.jpg",null,"01",1,null));
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

    private void initializeViews() {
        vp2=findViewById(R.id.box_viewPager);
        nextBtn=findViewById(R.id.box_next_btn);
    }

    public Bitmap cornerPin(Bitmap B,ArrayList<Point> dis){
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

        Bitmap resized=Bitmap.createBitmap(result,minLeft,minTop,wid,hei);
//        Log.e(TAG, "cornerPin: "+ result.getHeight()+" "+result.getWidth());
     //   Log.e(TAG, "cornerPin: "+resized.getHeight()+" "+resized.getWidth() );
        return resized;

    }

    private Bitmap applyFlatCorrection(Bitmap cropped) {
        System.gc();

        try {
            return new MyAsync().execute(cropped).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;

    }

    public void onNextPressed(View view){
        String[] save=null;
        for(MyPicture p:list){
            Bitmap image=p.getImg();
            Bitmap transformed=cornerPin(image,p.getCoordinates());
            Bitmap emptyBitmap = Bitmap.createBitmap(transformed.getWidth(), transformed.getHeight(),
                    transformed.getConfig());
            if(transformed.sameAs(emptyBitmap)){
                Toast.makeText(this, "Can't Crop", Toast.LENGTH_SHORT).show();
                vp2.setCurrentItem(list.indexOf(p));
                return;
            }
            if(list.size()==1){
                //send direct to filter activity
                save=saveBitmap(transformed);
            }else{
                //transforming each image into flatCorrection
                Bitmap filtered=applyFlatCorrection(transformed);
                String[] names=p.getEditedName().split(".jpg");
                Uri savedEdited= UtilityClass.saveImage(BoxActivity.this,filtered,names[0],false);
                p.setEditedUri(savedEdited.toString());
            }
        }

        if(list.size()!=1 && currDoc.getDid()!=0){
            currDoc.setfP_URI(list.get(0).getEditedUri());
        }
        for(MyPicture p:list){
            p.setImg(null);
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
//            //  Log.e(TAG, "onClick: "+sa[1] );
            startActivity(intent);
            finish();
        }else{
            //goto indoc activity

            Intent intent=new Intent(BoxActivity.this,InDocRecyclerActivity.class);
            intent.putExtra("MyPicture",myPics);
            intent.putExtra("MyDocument",myDoc);
         //   Log.e(TAG, "onNextPressed: "+myPics );
            intent.putExtra("from","BoxActivity");
            startActivity(intent);
            finish();
        }

    }
    public void rotateBitmapClockWise(View view){

        int currIndex=vp2.getCurrentItem();
        MyPicture currPic=list.get(currIndex);
        Bitmap image=currPic.getImg();
        if(image!=null){
            Matrix matrix=new Matrix();
            matrix.preRotate(90);
            image=Bitmap.createBitmap(image,0,0,image.getWidth(),image.getHeight(),matrix,true);
            currPic.setImg(image);
            adapter.notifyDataSetChanged();

        }
    }
    public void rotateBitmapAntiClockWise(View view){
        int currIndex=vp2.getCurrentItem();
        MyPicture currPic=list.get(currIndex);
        Bitmap image=currPic.getImg();
        if(image!=null){
            Matrix matrix=new Matrix();
            matrix.preRotate(-90);
            image=Bitmap.createBitmap(image,0,0,image.getWidth(),image.getHeight(),matrix,true);
            currPic.setImg(image);
            adapter.notifyDataSetChanged();
        }
    }

    public void resetCurrPoint(View view){
        int currInd=vp2.getCurrentItem();
        MyPicture pic=list.get(currInd);
        int height=vp2.getHeight();
        int width=vp2.getWidth();
        ArrayList<Point> pts=new ArrayList<>();
        pts.add(new Point(0,0));
        pts.add(new Point(width,0));
        pts.add(new Point(width,height));
        pts.add(new Point(0,height));

        pic.setCoordinates(pts);
        adapter.notifyDataSetChanged();
    }

    public  class MyAsync extends AsyncTask<Bitmap,Void,Bitmap>{

        @Override
        protected Bitmap doInBackground(Bitmap... bitmaps) {
            FlatCorrection fc=new FlatCorrection(BoxActivity.this);
            Bitmap blur=fc.flatCorr(bitmaps[0].copy(bitmaps[0].getConfig(),false));
            return blur;
        }
    }
}
