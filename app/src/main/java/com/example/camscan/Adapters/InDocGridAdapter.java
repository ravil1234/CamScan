package com.example.camscan.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.camscan.Objects.DatabaseObject;
import com.example.camscan.R;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

public class InDocGridAdapter extends BaseAdapter {

    Context context;
    ArrayList<DatabaseObject> images;
    private int viewWidth=0;
    private int viewHeight=0;
    public InDocGridAdapter(Context context,ArrayList<DatabaseObject> pts){
        this.context=context;
        this.images=pts;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object getItem(int i) {
        return images.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if(view==null){
            view= LayoutInflater.from(context).inflate(R.layout.in_doc_grid_item,null);
        }

        DatabaseObject current=images.get(i);

        ImageView img=view.findViewById(R.id.in_doc_grid_img);
        TextView name=view.findViewById(R.id.in_doc_grid_name);
        CheckBox cbox=view.findViewById(R.id.in_doc_grid_check);

        Uri imgUri=Uri.parse(current.getEditedUri());
        name.setText(current.getEditedFilename());

        if(viewWidth==0 || viewHeight==0){
            ViewTreeObserver vto=view.getViewTreeObserver();
            View finalView = view;

            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int width=finalView.getWidth();
                    int height=finalView.getHeight();
                    viewHeight=height;
                    viewWidth=width;
                    populateImage(img,imgUri,width,height);

                    finalView.getViewTreeObserver()
                            .removeOnGlobalLayoutListener(this);
                }
            });

        }
        else{
            populateImage(img,imgUri,viewWidth,viewHeight);
        }

        return view;
    }

    private void populateImage(ImageView img, Uri imgUri, int width, int height) {
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

        options.inSampleSize = calculateInSampleSize(options,width, height);
       // Log.e(TAG, "onClick: " + imageView.getWidth() + " " + imageView.getHeight());
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
         Bitmap bmp= BitmapFactory.decodeStream(is,null,options);
        img.setImageBitmap(bmp);
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
}
