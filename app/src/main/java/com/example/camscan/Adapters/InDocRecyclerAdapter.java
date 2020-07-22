package com.example.camscan.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.camscan.Objects.MyPicture;
import com.example.camscan.R;
import com.example.camscan.UtilityClass;

import java.util.ArrayList;

public class InDocRecyclerAdapter extends RecyclerView.Adapter<InDocRecyclerAdapter.MyViewHolder> {

    Context context;
    ArrayList<MyPicture> images;
    private int viewWidth=0;
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
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnClickListener(ocl);
            itemView.setOnLongClickListener(olcl);
            img=itemView.findViewById(R.id.in_doc_todo_img);
            name=itemView.findViewById(R.id.in_doc_todo_name);
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
        MyPicture current=images.get(position);

        holder.name.setText(current.getEditedName());
//        holder.page.setVisibility(View.VISIBLE);
//        holder.page.setText(String.format("%d/%d", position + 1, images.size()));

        final Uri uri=Uri.parse(current.getEditedUri());

        if(current.getImg()==null) {
            if (viewHeight == 0 || viewWidth == 0) {
                ViewTreeObserver vto = holder.img.getViewTreeObserver();

                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        viewHeight = holder.img.getHeight();
                        viewWidth = holder.img.getWidth();

                        Bitmap img = UtilityClass.populateImage(context, uri, false, viewWidth, viewHeight);
                        holder.img.setImageBitmap(img);
                        holder.img.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        current.setImg(img);

                    }
                });
            } else {
                Bitmap img = UtilityClass.populateImage(context, uri, false, viewWidth, viewHeight);
                holder.img.setImageBitmap(img);
                current.setImg(img);
            }
        }else{
            holder.img.setImageBitmap(current.getImg());
        }

    }

    @Override
    public int getItemCount() {
        return images.size();
    }


}
