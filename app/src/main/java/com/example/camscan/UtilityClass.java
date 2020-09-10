package com.example.camscan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.example.camscan.Objects.MyDocument;
import com.example.camscan.Objects.MyPicture;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class UtilityClass {

    public static final String PDF_SETTING="CAM_SCAN_PDF_SETTINGS";
    public static final int IMPORT_REQ_CODE=101;
    public static final int RETAKE_REQ_CODE=102;
    public static final String lineSeparator="__";
    public static final String appName="CamScan";

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

    public static Bitmap resizeImage(@NonNull Bitmap img, int w, int h){
        int originalW=img.getWidth();
        int originalHeight=img.getHeight();
        int newH,newW;
        newW=w;
        newH=(newW*originalHeight)/originalW;
        if(newH>h){
            newH=h;
            newW=(newH*originalW)/originalHeight;
        }
        return Bitmap.createScaledBitmap(img,newW,newH,true);

    }

    public static Bitmap populateImage(Context context, Uri imgUri,boolean isThumb,int viewWidth,int viewHeight) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

          BitmapFactory.decodeFile(imgUri.getPath(),options);
        if(isThumb){
            options.inSampleSize = calculateInSampleSize(options,100, 100);

        }else{
            options.inSampleSize = calculateInSampleSize(options,viewWidth, viewHeight);
        }

        options.inJustDecodeBounds = false;
        return  BitmapFactory.decodeFile(imgUri.getPath(),options);

    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
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
        int minWidth=99979939;
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
//            BitmapFactory.Options options=new BitmapFactory.Options();
//            options.inJustDecodeBounds=false;
//            options.inSampleSize=calculateInSampleSize(options,minWidth,heights[i]);
            Bitmap img=BitmapFactory.decodeFile(u.getPath());
            img=Bitmap.createScaledBitmap(img,minWidth,heights[i],true);
            cs.drawBitmap(img,0,currentPos,null);
            currentPos+=heights[i++];
            if(img!=null){
                img.recycle();
                img=null;
            }
        }
        File f;
        File dir;
        if(Build.VERSION.SDK_INT< Build.VERSION_CODES.Q){

            String path= Environment.getExternalStorageDirectory().getPath()+"/CamScan/LongImages/"+name+".jpg";
            f=new File(path);

            dir=new File(Environment.getExternalStorageDirectory().getPath()+"/CamScan/LongImages");
        }else{

            f=new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),"CamScan/LongImages"+name+".jpg");
            dir=new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),"CamScan/LongImages");
        }

        if(!dir.exists()&& !dir.isDirectory()){
            dir.mkdir();
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

    public static void deleteFromStorage(Uri deleteUri){

        File f=new File(deleteUri.getPath());
        if(f.exists()){
            f.delete();
        }
    }

    public static MyDocument getDocFromJson(String docJson){
        try {
            JSONObject obj = new JSONObject(docJson);
            int did = obj.getInt("did");
            String dName = obj.getString("dname");
            long timeCreated = obj.getLong("timeCreated");
            long timeEdited = obj.getLong("timeEdited");
            String fP_URI=null;
            if(obj.has("fp_uri")){
                fP_URI = obj.getString("fp_uri");
            }


            MyDocument mydoc = new MyDocument(dName, timeCreated, timeEdited, fP_URI);
            mydoc.setDid(did);

            return mydoc;
        }catch (JSONException e){
            e.printStackTrace();
            return null;
        }

        }
    public static ArrayList<MyPicture> getListOfPics(String myPicsJson){
        try {
            JSONArray array = new JSONArray(myPicsJson);
            ArrayList<MyPicture> arraylistpicture = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                ArrayList<Point> coordinates = new ArrayList<>();
                coordinates.add(new Point(obj.getInt("x1"), obj.getInt("y1")));
                coordinates.add(new Point(obj.getInt("x2"), obj.getInt("y2")));
                coordinates.add(new Point(obj.getInt("x3"), obj.getInt("y3")));
                coordinates.add(new Point(obj.getInt("x4"), obj.getInt("y4")));
                String editedUri=null;
                if(obj.has("editedUri")){
                    editedUri=obj.getString("editedUri");
                }
                int w=obj.getInt("s_width");
                int h=obj.getInt("s_height");
                MyPicture pic=new MyPicture(obj.getInt("did"), obj.getString("originalUri"), editedUri,
                        obj.getString("editedName"), obj.getInt("position"), coordinates,w,h);
                pic.setPid(obj.getInt("pid"));
                arraylistpicture.add(pic);
            }
            return arraylistpicture;
        }catch (JSONException e){
            e.printStackTrace();
            return null;
        }
    }
    public static MyPicture getPicFromString(String picString){
        JSONObject obj = null;
        try {
            obj = new JSONObject(picString);

            ArrayList<Point> coordinates = new ArrayList<>();
            coordinates.add(new Point(obj.getInt("x1"), obj.getInt("y1")));
            coordinates.add(new Point(obj.getInt("x2"), obj.getInt("y2")));
            coordinates.add(new Point(obj.getInt("x3"), obj.getInt("y3")));
            coordinates.add(new Point(obj.getInt("x4"), obj.getInt("y4")));
            String editedUri=null;
            if(obj.has("editedUri")){
                editedUri=obj.getString("editedUri");
            }
            int w=obj.getInt("s_width");
            int h=obj.getInt("s_height");
            MyPicture pic=new MyPicture(obj.getInt("did"), obj.getString("originalUri"), editedUri,
                    obj.getString("editedName"), obj.getInt("position"), coordinates,w,h);
            pic.setPid(obj.getInt("pid"));
            return pic;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getStringFromObject(Object object){

        return new Gson().toJson(object);
    }




    public static Bitmap getRoundedCroppedBitmap(Bitmap bitmap) {
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

}
