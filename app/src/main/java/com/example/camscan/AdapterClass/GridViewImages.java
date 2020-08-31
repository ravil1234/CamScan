package com.example.camscan.AdapterClass;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.camscan.ObjectClass.GridViewImagesList;
import com.example.camscan.R;
import java.util.List;
import androidx.recyclerview.widget.RecyclerView;
public class GridViewImages extends RecyclerView.Adapter<GridViewImages.ServiceViewHolder> {

    private Context mContext;
    private List<GridViewImagesList> gridViewImagesLists;
    View.OnClickListener mClickListener;
    public GridViewImages (Context mContext, List<GridViewImagesList> serviceObjects, View.OnClickListener listener) {
        this.mContext = mContext;
        this.gridViewImagesLists = serviceObjects;
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
         GridViewImagesList gridViewImagesList=gridViewImagesLists.get(position);
         holder.mTitle.setText(gridViewImagesList.getImage_date());
//        Picasso.with(mContext).load(serviceObject.getImage()).placeholder(R.drawable.accept_terms).into(holder.mImage);
//        holder.mTitle.setText(serviceObject.getName());
        holder.itemView.setOnClickListener(mClickListener);
    }
    @Override
    public int getItemCount() {
        return gridViewImagesLists.size();
    }
    public class ServiceViewHolder extends RecyclerView.ViewHolder {

        ImageView mImage;
        TextView mTitle;
        public ServiceViewHolder(View itemView) {
            super(itemView);
            mImage = itemView.findViewById(R.id.pdf_image);
            mTitle = itemView.findViewById(R.id.date);
        }
    }
}

