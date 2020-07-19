package com.example.camscan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class UtilityClass {

    public static final String PDF_SETTING="CAM_SCAN_PDF_SETTINGS";
    public static final int IMPORT_REQ_CODE=101;
    public static Uri saveImage(Context context, Bitmap img, String name, boolean isOriginal){
        File dir;


        File f=null;
        if(name.contains(".jpg")){
            name=name.replace(".jpg","");
        }else {
            name += (int) (System.currentTimeMillis() % 1000000);
        }
        if(Build.VERSION.SDK_INT< Build.VERSION_CODES.Q){
            if(isOriginal){
                String path= Environment.getExternalStorageDirectory().getPath()+"/CamScan/.Original/"+name+".jpg";
                dir=new File(Environment.getExternalStorageDirectory().getPath()+"/CamScan/.Original");
                f=new File(path);
            }else{
                String path= Environment.getExternalStorageDirectory().getPath()+"/CamScan/.Edited/"+name+".jpg";
                dir=new File(Environment.getExternalStorageDirectory().getPath()+"/CamScan/.Edited");
                f=new File(path);
            }

            //          String path2=Environment.getExternalStorageDirectory().getPath()+System.currentTimeMillis()+".jpg";
//            original=new File(path2);
        }else{
            if(isOriginal){
                f=new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),"CamScan/.Original/"+name+".jpg");
                dir=new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),"CamScan/.Original");
            }else{
                f=new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),"CamScan/.Edited/"+name+".jpg");
                dir=new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),"CamScan/.Edited");
            }

        }


        if(!dir.exists() && !dir.isDirectory()){
            dir.mkdirs();
        }

        try {

            FileOutputStream fos=new FileOutputStream(f,false);
            img.compress(Bitmap.CompressFormat.JPEG,100,fos);

            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Uri.fromFile(f);
    }

    public static Bitmap populateImage(Context context, Uri imgUri,boolean isThumb,int viewWidth,int viewHeight) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream is=null;
        try {
            is=context.getContentResolver().openInputStream(imgUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if(is!=null){
            BitmapFactory.decodeStream(is,null,options);
        }
        // Calculate inSampleSize
        if(isThumb){
            options.inSampleSize = calculateInSampleSize(options,100, 100);

        }else{
            options.inSampleSize = calculateInSampleSize(options,viewWidth, viewHeight);
        }

        // Log.e(TAG, "onClick: " + imageView.getWidth() + " " + imageView.getHeight());
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        //Bitmap bmp= BitmapFactory.decodeStream(is,null,options);
        Bitmap bmp=BitmapFactory.decodeFile(imgUri.getPath(),options);
        return bmp;

    }

    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
    public static Uri saveLongImage(Context context, ArrayList<Uri> uris,String name){
        int minWidth=214748364;
        int[] heights=new int[uris.size()];
        int[] widths=new int[uris.size()];
        int totalHeight=0;
        int i=0;
        for(Uri currUri:uris){
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(new File(currUri.getPath()).getAbsolutePath(), options);
            int imageHeight = options.outHeight;
            int imageWidth = options.outWidth;
            if(imageWidth<minWidth){
                minWidth=imageWidth;
            }
            heights[i]=imageHeight;
            widths[i++]=imageWidth;
        }
        for(int x=0;x<uris.size();x++){
            int resizedHeight=getResizedHeight(heights[x],widths[x],minWidth);
            heights[x]=resizedHeight;
            totalHeight+=heights[x];
        }

        Bitmap longImage=Bitmap.createBitmap(minWidth,totalHeight,Bitmap.Config.ARGB_8888);
        Canvas cs=new Canvas(longImage);
        int currentPos=0;
        i=0;
        for(Uri u:uris){
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inJustDecodeBounds=false;
            options.inSampleSize=calculateInSampleSize(options,minWidth,heights[i]);
            Bitmap img=BitmapFactory.decodeFile(u.getPath(),options);
            cs.drawBitmap(img,0,currentPos,null);
            currentPos+=heights[i++];
        }
        File f;
        if(Build.VERSION.SDK_INT< Build.VERSION_CODES.Q){

            String path= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()+"/"+name+".jpg";
            f=new File(path);
        }else{

            f=new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),name+".jpg");
        }

        try{
            FileOutputStream fos=new FileOutputStream(f);
            longImage.compress(Bitmap.CompressFormat.JPEG,100,fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return Uri.fromFile(f);

    }

    private static int getResizedHeight(int height, int width, int minWidth) {

        return (int)(((float)height/(float)width)*minWidth);
    }

    public static void deleteFromStorage(Uri deleteUri,boolean isOriginal){
        //TODO complete it
    }
}
