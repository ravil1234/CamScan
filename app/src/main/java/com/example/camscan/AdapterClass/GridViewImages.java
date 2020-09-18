package com.example.camscan.AdapterClass;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.camscan.ObjectClass.GridViewImagesList;
import com.example.camscan.ObjectClass.RoundedCornersTransformation;
import com.example.camscan.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.List;
import androidx.recyclerview.widget.RecyclerView;
public class GridViewImages extends RecyclerView.Adapter<GridViewImages.ServiceViewHolder> {

    private Context mContext;
    private List<GridViewImagesList> gridViewImagesLists;

    View.OnClickListener mClickListener;
    Transformation transformation;
    public GridViewImages (Context mContext, List<GridViewImagesList> serviceObjects, View.OnClickListener listener) {
        this.mContext = mContext;
        this.gridViewImagesLists = serviceObjects;
        transformation=new RoundedCornersTransformation(12,0);
        mClickListener=listener;
    }

    @Override
    public ServiceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_layout_homescreen, parent, false);

        return new ServiceViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(final ServiceViewHolder holder, final int position)
    {
        GridViewImagesList list = gridViewImagesLists.get(position);
        if(list.checkbox_visibility())
            holder.checkBox.setVisibility(View.VISIBLE);
        else
            holder.checkBox.setVisibility(View.GONE);
        if(list.Ischecked())
            holder.checkBox.setChecked(true);
        else
            holder.checkBox.setChecked(false);
        holder.file_name.setText(list.getName());
        holder.doc_date_created.setText(list.getImage_date());
        if(list.getPcount()<9)
            holder.no_pages.setText("0"+list.getPcount());
        else
            holder.no_pages.setText(list.getPcount()+"");
       Picasso.with(mContext).load(list.getImage_url()).into(holder.mImage);
        holder.itemView.setOnClickListener(mClickListener);
    }
    @Override
    public int getItemCount() {
        return gridViewImagesLists.size();
    }
    public class ServiceViewHolder extends RecyclerView.ViewHolder {

        ImageView mImage;
        TextView doc_date_created,file_name,no_pages;
        CheckBox checkBox;
        public ServiceViewHolder(View itemView) {
            super(itemView);
            mImage = itemView.findViewById(R.id.pdf_image);
            doc_date_created = itemView.findViewById(R.id.date);
            file_name = itemView.findViewById(R.id.pdf_name);
            checkBox=itemView.findViewById(R.id.checkbox);
            no_pages=itemView.findViewById(R.id.no_pages);
            checkBox.setClickable(false);
        }
    }
}

