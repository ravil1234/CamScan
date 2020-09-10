package com.example.camscan.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.camscan.Activities.PdfSettingsActivity;
import com.example.camscan.Callbacks.ItemMoveCallback;
import com.example.camscan.Objects.MyPicture;
import com.example.camscan.R;
import com.example.camscan.UtilityClass;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

public class PdfPreviewAdapter extends RecyclerView.Adapter<PdfPreviewAdapter.MyViewHolder> implements ItemMoveCallback.ItemTouchHelperContract{

    Context context;
    ArrayList<Uri> imgUris;
    Bitmap stamp=null;
    boolean isBorder=false;
    boolean orientation=true;       //PORTRAIT
    float ratio=0;
    int pageSize=-1;
 //   ArrayList<MyPicture> pics;

    public PdfPreviewAdapter(Context context,ArrayList<Uri> uris){
        this.context=context;
        imgUris=uris;
        getDefaultSettings();
   //     pics=picList;
        //options share save pdf_settings

    }

    public void getDefaultSettings(){
        SharedPreferences pref=context.getSharedPreferences(UtilityClass.PDF_SETTING, Context.MODE_PRIVATE);
        String uriString=pref.getString("PDF_STAMP_URI","android.resource://"+context.getPackageName()+"/drawable/"+R.drawable.stamp);
        pageSize =pref.getInt("PDF_PAGE_SIZE",1);
       // Log.e("Page", "getDefaultSettings: "+pageSize );
        switch (pageSize){
            case 0:{
                ratio=8.3f/5.8f;
                break;
            }
            case 1:{
                ratio=11.7f/8.3f;
                break;
            }
            case 2:{
                ratio=16.5f/11.7f;
                break;
            }
            case 3:{
                ratio=14f/8.4f;
                break;
            }
            case 4:{
                ratio=17f/11f;
                break;
            }

        }
        int o=pref.getInt("PDF_PAGE_ORIENTATION",0);
        if(o!=0){
            orientation=false;
        }
        isBorder=pref.getBoolean("PDF_PAGE_BORDER",false);


        try {
            // Log.e("THIS SETTINGS", "onStampClicked: "+uriString );
            Uri uri=Uri.parse(uriString);
            InputStream is= context.getContentResolver().openInputStream(uri);
            stamp = BitmapFactory.decodeStream(is);
            stamp=UtilityClass.resizeImage(stamp,400,100);



        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onRowMoved(int fromPosition, int toPosition) {
        if(fromPosition<toPosition){
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(imgUris, i, i + 1);

            }
        }else{
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(imgUris, i, i - 1);

            }
        }
        notifyItemMoved(fromPosition,toPosition);
    }

    @Override
    public void onRowSelected(InDocMiniAdapter.MyViewHolder myViewHolder) {
        myViewHolder.root.setBackgroundColor(Color.GRAY);
    }

    @Override
    public void onRowClear(InDocMiniAdapter.MyViewHolder myViewHolder) {
        myViewHolder.root.setBackgroundColor(Color.WHITE);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView img,stampImg;
        TextView count;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            img=itemView.findViewById(R.id.pdf_preview_img);
            stampImg=itemView.findViewById(R.id.pdf_preview_stamp);
            count=itemView.findViewById(R.id.pdf_preview_count);
            if(isBorder){
                float scale = context.getResources().getDisplayMetrics().density;
                int dpAsPixels = (int) (1*scale + 0.5f);
                img.setPadding(dpAsPixels,dpAsPixels,dpAsPixels,dpAsPixels);
            }
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(context).inflate(R.layout.item_pdf_preview,parent,false);
        v.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                v.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int w=v.getWidth();
                int h=(int)(ratio*w);
                RecyclerView.LayoutParams params=(RecyclerView.LayoutParams)v.getLayoutParams();
                params.height=h;

               // Log.e("Params", "onGlobalLayout: "+h+" "+ratio+" "+w );
                v.setLayoutParams(params);
            }
        });
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Uri currUri=imgUris.get(position);
        Bitmap img=BitmapFactory.decodeFile(currUri.getPath());
        if(img!=null){
            holder.img.setImageBitmap(img);
        }
        if(stamp!=null){
            holder.stampImg.setImageBitmap(stamp);
        }else{
            Log.e("ADPTER", "onBindViewHolder: "+"Stamp NULL" );
        }
        holder.count.setText(position+1+"/"+imgUris.size());
    }

    @Override
    public int getItemCount() {
        return imgUris.size();
    }


}
