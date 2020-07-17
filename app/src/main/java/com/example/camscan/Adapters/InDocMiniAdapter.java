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

import com.example.camscan.R;

import java.util.ArrayList;

public class InDocMiniAdapter extends RecyclerView.Adapter<InDocMiniAdapter.MyViewHolder> {
    private Context context;
    ArrayList<Bitmap> images;
    View.OnClickListener ocl;
    int selected=0;
    public InDocMiniAdapter(Context context, ArrayList<Bitmap> imgs, View.OnClickListener ocl){
        this.context=context;
        images=imgs;
        this.ocl=ocl;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_filter_rview,parent,false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if(selected==position){
            holder.root.setBackground(context.getDrawable(R.color.colorAccent));
        }else{
            holder.root.setBackground(context.getDrawable(android.R.color.transparent));
        }

        holder.img.setImageBitmap(images.get(position));
        holder.name.setText(String.valueOf(position+1));
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView img;
        TextView name;
        View root;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(ocl);
            img=itemView.findViewById(R.id.filter_item_image);
            name=itemView.findViewById(R.id.filter_item_name);
            root=itemView;
        }
    }

    public void setSelected(int pos){
        selected=pos;
    }

}
