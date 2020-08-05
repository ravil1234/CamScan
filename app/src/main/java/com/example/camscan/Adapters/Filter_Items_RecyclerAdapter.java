package com.example.camscan.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.camscan.Activities.FilterActivity;
import com.example.camscan.R;

import java.util.ArrayList;

import com.example.camscan.RenderScriptJava.BlackAndWhite;
import com.example.camscan.RenderScriptJava.Filter1;
import com.example.camscan.RenderScriptJava.FlatCorrection;
import com.example.camscan.RenderScriptJava.GrayScale;
import com.example.camscan.RenderScriptJava.Inversion;

public class Filter_Items_RecyclerAdapter extends RecyclerView.Adapter<Filter_Items_RecyclerAdapter.MyViewHolder> {


    Context context;
    ArrayList<String> names;
    ArrayList<Integer> types;
    Bitmap thumbnail;
    View.OnClickListener listener;
    ArrayList<Bitmap> imgs;
    public int selected=2;
    public Filter_Items_RecyclerAdapter(Context context, ArrayList names, ArrayList types,
                                        Bitmap thumbnail, View.OnClickListener myOnClickListener,ArrayList<Bitmap> imgs){
        this.context=context;
        this.names=names;
        this.thumbnail=thumbnail;
        this.types=types;
        this.imgs=imgs;
        listener=myOnClickListener;
    }
    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        ImageView img;
        View root;
        public MyViewHolder(View view){
            super(view);
            root=view;
            name=view.findViewById(R.id.filter_item_name);
            img=view.findViewById(R.id.filter_item_image);
            view.setOnClickListener(listener);
        }
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filter_rview,parent,false);

        return new MyViewHolder(view);

        }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if(selected==position){
            holder.root.setBackground(context.getDrawable(R.color.colorAccent));
        }else{
            holder.root.setBackground(context.getDrawable(android.R.color.transparent));
        }
        holder.name.setText(names.get(position));
        switch(types.get(position)){
            case 1:{//original
                holder.img.setImageBitmap(thumbnail);
                break;
            }
            case 2:{//exposure

                    Filter1 f1=new Filter1(context);
                    Bitmap filtered=f1.filter(100,thumbnail);
                    holder.img.setImageBitmap(filtered);
                    f1.cleanUp();

                break;

            }
            case 3:{//flat

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        FlatCorrection fc=new FlatCorrection(context);
                        Bitmap result=fc.flatCorr(thumbnail);
                        ((FilterActivity)context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                holder.img.setImageBitmap(result);
                            }
                        });
                        fc.clear();
                    }
                }).start();
                break;
            }
            case 4:{//GrayScale
                Bitmap gray=new GrayScale().toGrayscale(thumbnail);
                holder.img.setImageBitmap(gray);
                break;
            }
            case 5:{//BndW

                Bitmap bnw=new BlackAndWhite(context).toBnwRender(thumbnail);
                holder.img.setImageBitmap(bnw);
                break;
            }
            case 6:{
                //invert
                Inversion inv=new Inversion(context);
                Bitmap inverted=inv.setInversion(thumbnail.copy(thumbnail.getConfig(),false));
                holder.img.setImageBitmap(inverted);
                inv.clear();
                break;
            }
        }


    }


    @Override
    public int getItemCount() {
        return names.size();
    }
}
