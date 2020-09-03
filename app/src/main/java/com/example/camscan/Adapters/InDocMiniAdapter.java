package com.example.camscan.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.camscan.Activities.MyDocumentActivity;
import com.example.camscan.Callbacks.ItemMoveCallback;
import com.example.camscan.Objects.MyPicture;
import com.example.camscan.R;

import java.util.ArrayList;
import java.util.Collections;

public class InDocMiniAdapter extends RecyclerView.Adapter<InDocMiniAdapter.MyViewHolder> implements ItemMoveCallback.ItemTouchHelperContract {
    private Context context;
    ArrayList<Bitmap> images;
    ArrayList<MyPicture> pictures;
    View.OnClickListener ocl;
    int selected=0;
    public InDocMiniAdapter(Context context, ArrayList<Bitmap> imgs,ArrayList<MyPicture> pics, View.OnClickListener ocl){
        this.context=context;
        images=imgs;
        this.ocl=ocl;
        pictures=pics;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_in_doc_mini_object,parent,false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.root.setBackground(context.getDrawable(R.color.colorAccent));


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

    @Override
    public void onRowMoved(int fromPosition, int toPosition) {
        if(fromPosition<toPosition){
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(images, i, i + 1);
                Collections.swap(pictures,i,i+1);
            }
        }else{
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(images, i, i - 1);
                Collections.swap(pictures,i,i-1);
            }
        }
            notifyItemMoved(fromPosition,toPosition);
        ((MyDocumentActivity)context).updateListFromMiniAdapter(fromPosition,toPosition);
    }

    @Override
    public void onRowSelected(MyViewHolder myViewHolder) {
        myViewHolder.root.setBackgroundColor(Color.GRAY);
    }

    @Override
    public void onRowClear(MyViewHolder myViewHolder) {
        myViewHolder.root.setBackgroundColor(Color.WHITE);
    }
}
