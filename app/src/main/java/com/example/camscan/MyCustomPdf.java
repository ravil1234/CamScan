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

import com.example.camscan.Objects.PageSize;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

public class MyCustomPdf {

    private String PASSWORD="LOLZ";
    private Boolean isPassSet=false;
    private PageSize PAGE_A5=new PageSize(5.8f,8.3f);       //0
    private PageSize PAGE_A4=new PageSize(8.3f,11.7f);      //1
    private PageSize PAGE_A3=new PageSize(11.7f,16.5f);     //2
    private PageSize PAGE_LEGAL=new PageSize(8.4f,14f);     //3
    private PageSize PAGE_TABLOID=new PageSize(11f,17f);    //4
    private int ORIENTATION=0;          //0->Portrait       1->LANDSCAPE
    private Boolean isMarginAdded=false;
    private int PageSize=1;


    private Context context;
    private ArrayList<Bitmap> images;

    public MyCustomPdf(Context context,ArrayList<Bitmap> images,Boolean isPassSet){
        this.context=context;
        this.images=images;
        this.isPassSet=isPassSet;
        getDefaultSettings();

    }

    private void getDefaultSettings(){
        SharedPreferences pref=context.getSharedPreferences(UtilityClass.PDF_SETTING,Context.MODE_PRIVATE);
        PageSize=pref.getInt("PDF_PAGE_SIZE",1);
        ORIENTATION=pref.getInt("PDF_PAGE_ORIENTATION",0);
        isMarginAdded=pref.getBoolean("PDF_PAGE_MARGIN",false);
        PASSWORD=pref.getString("PDF_PAGE_PASSWORD","admin");
    }
  /*
    public Boolean savePdf(String name){
        PdfDocument doc=new PdfDocument();
        if(isPassSet){

        }
        int h=getHeight();
        int w=getWidth();
        for(int i=0;i<images.size();i++){
            PdfDocument.PageInfo info=new PdfDocument.PageInfo.Builder(w,h,i+1).create();

            PdfDocument.Page page=doc.startPage(info);
            Paint p=new Paint(Color.WHITE);
            retImg r=resize(images.get(i));
            page.getCanvas().drawBitmap(r.img,r.x,r.y,p);

            doc.finishPage(page);
        }

        String path= Environment.getExternalStorageDirectory().getPath()+"/"+name+".pdf";
        File f=new File(path);

        try{
            doc.writeTo(new FileOutputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        doc.close();
        return true;


    }
*/
    public Uri savePdf2(String name){
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
                pdw.setEncryption(PASSWORD.getBytes(),"ADMIN_IS_BACK".getBytes()
                        ,PdfWriter.ALLOW_PRINTING|PdfWriter.ALLOW_COPY|PdfWriter.ALLOW_MODIFY_CONTENTS,
                        PdfWriter.ENCRYPTION_AES_128);
            }
            doc.open();

            for(Bitmap i :images){
               // imgV.setImageBitmap(i);


                ByteArrayOutputStream bos=new ByteArrayOutputStream();
                retImg ret=resize(i);
                ret.img.compress(Bitmap.CompressFormat.PNG,100,bos);
                Image image=Image.getInstance(bos.toByteArray());
                image.setAlignment(Image.ALIGN_CENTER);
                image.setAbsolutePosition(ret.x,ret.y);
                doc.add(image);
                if(ORIENTATION==1){
                    pdw.addPageDictEntry(PdfName.ROTATE, PdfPage.LANDSCAPE);
                }
                if(isMarginAdded){
                    image.setIndentationLeft(16);
                }

                doc.newPage();
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
