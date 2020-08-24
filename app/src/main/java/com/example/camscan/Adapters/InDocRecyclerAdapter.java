package com.example.camscan.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.camscan.Activities.InDocRecyclerActivity;
import com.example.camscan.Activities.MyDocumentActivity;
import com.example.camscan.Objects.MyPicture;
import com.example.camscan.R;
import com.example.camscan.RenderScriptJava.BlackAndWhite;
import com.example.camscan.RenderScriptJava.Filter1;
import com.example.camscan.RenderScriptJava.FlatCorrection;
import com.example.camscan.RenderScriptJava.GrayScale;
import com.example.camscan.RenderScriptJava.Inversion;
import com.example.camscan.UtilityClass;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class InDocRecyclerAdapter extends RecyclerView.Adapter<InDocRecyclerAdapter.MyViewHolder> {

    Context context;
    ArrayList<MyPicture> images;
    public int viewWidth=0;
    private int viewHeight=0;
    private View.OnClickListener ocl;
    private View.OnLongClickListener olcl;
    public InDocRecyclerAdapter(Context context, ArrayList<MyPicture> objs, View.OnClickListener ocl, View.OnLongClickListener olcl){
        this.context=context;
        images=objs;
        this.ocl=ocl;
        this.olcl=olcl;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView img;
        TextView name,page;
        ProgressBar pbar;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnClickListener(ocl);
            itemView.setOnLongClickListener(olcl);
            img=itemView.findViewById(R.id.in_doc_todo_img);
            name=itemView.findViewById(R.id.in_doc_todo_name);
            pbar=itemView.findViewById(R.id.in_doc_todo_pbar);
          //  page=itemView.findViewById(R.id.in_doc_todo_page);

        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_in_doc_rec,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final MyPicture current=images.get(position);

        holder.name.setText(current.getEditedName());
        Thread t=new Thread(new Runnable() {
            @Override
            public synchronized void run() {
                if(current.getEditedUri()==null){
                    //not yet filtered
                    final Uri uri=Uri.parse(current.getOriginalUri());
                    Bitmap image=UtilityClass.populateImage(context,uri,false,viewWidth,viewHeight);
                    if(image!=null){
                        ((MyDocumentActivity)context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                holder.img.setImageBitmap(image);
                            }
                        });

                    }else{
                        Log.e("THIS", "onBindViewHolder: "+"Cant load Original image" );
                    }
                }else{
                    //already edited

                    final Uri uri=Uri.parse(current.getEditedUri());
                    Bitmap image=UtilityClass.populateImage(context,uri,false,viewWidth,viewHeight);
                    if(image!=null){
                        ((MyDocumentActivity)context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                holder.img.setImageBitmap(image);
                                holder.pbar.setVisibility(View.INVISIBLE);
                            }
                        });
                    }else{
                        Log.e("THIS", "onBindViewHolder: "+"Can't load Edited Image" );
                        ((MyDocumentActivity)context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //show the reset button
                            }
                        });
                    }

                }

            }
        });
        t.start();
//        try {
//            t.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }



    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public void getDimensions(int w,int h){
        viewWidth=w;
        viewHeight=h;
    }


}
