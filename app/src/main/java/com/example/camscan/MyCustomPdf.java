package com.example.camscan;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.example.camscan.Activities.MyDocumentActivity;
import com.example.camscan.Objects.PageSize;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfFormXObject;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfPage;
import com.itextpdf.text.pdf.PdfPageEvent;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class MyCustomPdf {

    private String PASSWORD="LOLZ";
    private Boolean isPassSet=false;
    private PageSize PAGE_A5=new PageSize(5.8f,8.3f);       //0
    private PageSize PAGE_A4=new PageSize(8.3f,11.7f);      //1
    private PageSize PAGE_A3=new PageSize(11.7f,16.5f);     //2
    private PageSize PAGE_LEGAL=new PageSize(8.4f,14f);     //3
    private PageSize PAGE_TABLOID=new PageSize(11f,17f);    //4
    private int ORIENTATION=0;          //0->Portrait       1->LANDSCAPE
    //private Boolean isMarginAdded=false;
    private Boolean isBorderAdded=false;
    private int PageSize=1;
    private String stampUri;


    private Context context;
    private ArrayList<Bitmap> images;
    private ArrayList<Uri> imgUris;

    public MyCustomPdf(Context context,ArrayList<Uri> images,Boolean isPassSet){
        this.context=context;
        this.imgUris=images;
        this.isPassSet=isPassSet;
        getDefaultSettings();

    }

    private void getDefaultSettings(){
        SharedPreferences pref=context.getSharedPreferences(UtilityClass.PDF_SETTING,Context.MODE_PRIVATE);
        PageSize=pref.getInt("PDF_PAGE_SIZE",1);
        ORIENTATION=pref.getInt("PDF_PAGE_ORIENTATION",0);
        isBorderAdded=pref.getBoolean("PDF_PAGE_BORDER",false);
        PASSWORD=pref.getString("PDF_PAGE_PASSWORD","admin");
        stampUri=pref.getString("PDF_STAMP_URI","android.resource://"+context.getPackageName()+"/"+R.drawable.stamp_default);
    }

    public Uri savePdf2(String name, String pass, MyDocumentActivity.pdfProgress mp){
      File f;
      File dir;
      if(Build.VERSION.SDK_INT< Build.VERSION_CODES.Q) {
          String path = Environment.getExternalStorageDirectory().getPath() + "/CamScan/" + name + ".pdf";
          String path2=Environment.getExternalStorageDirectory().getPath()+"/CamScan";
          dir=new File(path2);
          f = new File(path);
      }else{
          f=new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),"CamScan/"+name+".pdf");
          dir=new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),"CamScan");
      }
      Document doc=new Document(getPageSize());

      if(!dir.exists() && !dir.isDirectory()){
          dir.mkdir();
      }


      //Log.e("SIZE", "savePdf2: "+ com.itextpdf.text.PageSize.A4.getWidth()+" "+ com.itextpdf.text.PageSize.A4.getHeight());
      try{
          PdfWriter pdw=PdfWriter.getInstance(doc,new FileOutputStream(f));
          if(isPassSet){
              pdw.setEncryption(pass.getBytes(),"ADMIN_IS_BACK".getBytes()
                      ,PdfWriter.ALLOW_PRINTING|PdfWriter.ALLOW_COPY|PdfWriter.ALLOW_MODIFY_CONTENTS,
                      PdfWriter.ENCRYPTION_AES_128);
          }

          pdw.setPageEvent(new MyEvent());
          doc.open();
          int a=0;
          int eachPer=100/imgUris.size();
          for(Uri i :imgUris){
              // imgV.setImageBitmap(i);

              Bitmap b=BitmapFactory.decodeFile(i.getPath());
              ByteArrayOutputStream bos=new ByteArrayOutputStream();
              retImg ret=resize(b);
              ret.img.compress(Bitmap.CompressFormat.PNG,100,bos);
              Image image=Image.getInstance(bos.toByteArray());
              image.setAlignment(Image.ALIGN_CENTER);
              image.setAbsolutePosition(ret.x,ret.y);

              if(ORIENTATION==1){
                  pdw.addPageDictEntry(PdfName.ROTATE, PdfPage.LANDSCAPE);
              }
              if(isBorderAdded){
                  image.setBorder(Rectangle.BOX);
                  image.setBorderColor(BaseColor.BLACK);
                  image.setBorderWidth(2f);
              }



              doc.add(image);
              doc.newPage();
              b.recycle();
              b=null;
              a+=eachPer;
              mp.onUpdate(a);
              if(imgUris.indexOf(i)==imgUris.size()-1){
                  mp.onUpdate(100);
              }


          }
          doc.close();

      }
      catch(FileNotFoundException e) {
          e.printStackTrace();
          return null;
      } catch (DocumentException e) {
          e.printStackTrace();
          return null;
      } catch (MalformedURLException e) {
          e.printStackTrace();
          return null;
      } catch (IOException e) {
          e.printStackTrace();
          return null;
      }
      return Uri.fromFile(f);
    }


    private class MyEvent extends PdfPageEventHelper{
        Image stamp;


        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {

            ByteArrayOutputStream bos=new ByteArrayOutputStream();
            try {
                Bitmap st=BitmapFactory.decodeStream(context.getContentResolver().openInputStream(Uri.parse(stampUri)));
                //
                if(st.getWidth()>200 || st.getHeight()>50){
                    st=UtilityClass.resizeImage(st,200,50);
                }
                st.compress(Bitmap.CompressFormat.JPEG,100,bos);
                stamp=Image.getInstance(bos.toByteArray());
                Rectangle rec=getPageSize();
                stamp.setAbsolutePosition(rec.getWidth()-st.getWidth()-50,10);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (BadElementException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            if(stamp!=null) {
                try {
                    writer.getDirectContent().addImage(stamp);
                } catch (DocumentException e) {
                    e.printStackTrace();
                    Log.e("HERE", "onEndPage: "+"HERE2" );
                }
            }
        }
    }
    private com.itextpdf.text.Rectangle getPageSize(){
        switch (PageSize){
            case 0:{
                return com.itextpdf.text.PageSize.A5;

            }
            case 1:{
                return com.itextpdf.text.PageSize.A4;

            }
            case 2:{
                return com.itextpdf.text.PageSize.A3;

            }
            case 3:{
                return com.itextpdf.text.PageSize.LEGAL;

            }
            case 4:{
                return com.itextpdf.text.PageSize.TABLOID;


            }
        }
        return null;
    }
    private int getWidth() {
        switch (PageSize){
            case 0:{
                return getPoint(PAGE_A5.getWidth());

            }
            case 1:{
                return getPoint(PAGE_A4.getWidth());

            }
            case 2:{
                return getPoint(PAGE_A3.getWidth());

            }
            case 3:{
                return getPoint(PAGE_LEGAL.getWidth());

            }
            case 4:{
                return getPoint(PAGE_TABLOID.getWidth());

            }
        }
        return -1;
    }
    private int getHeight() {
        switch (PageSize){
            case 0:{
                return getPoint(PAGE_A5.getHeight());

            }
            case 1:{
                return getPoint(PAGE_A4.getHeight());

            }
            case 2:{
                return getPoint(PAGE_A3.getHeight());

            }
            case 3:{
                return getPoint(PAGE_LEGAL.getHeight());

            }
            case 4:{
                return getPoint(PAGE_TABLOID.getHeight());

            }
        }
        return -1;
    }
    private int getPoint(float inc){
        return (int)(inc*72);
    }
    private retImg resize(Bitmap source){

        int wpx=getWidth();
        int hpx=getHeight();

        int imageWidth=source.getWidth();
        int imageHeight=source.getHeight();

        int x,y;

        int stx,sty;
        x=wpx;
        y=(int)(((float)imageHeight/(float)imageWidth)*wpx);
        stx=0;
        sty=hpx/2-y/2;
        if(y>hpx){
            y=hpx;
            x=(int)(((float)imageWidth/(float)imageHeight)*hpx);
            sty=0;
            stx=wpx/2-x/2;
        }


        Bitmap resized=Bitmap.createScaledBitmap(source,x,y,true);
        return new retImg(resized,stx,sty);


    }

public class retImg{
        public Bitmap img;
        public int x,y;
        public retImg(Bitmap b,int x,int y){
            img=b;
            this.x=x;
            this.y=y;
        }
}
}
