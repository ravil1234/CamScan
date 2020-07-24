package com.example.camscan.AdapterClass;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.camscan.ObjectClass.GridViewImagesList;
import com.example.camscan.R;
import java.util.List;
import androidx.recyclerview.widget.RecyclerView;
public class GridViewImages extends RecyclerView.Adapter<ServiceViewHolder> {

    private Context mContext;
    private List<GridViewImagesList> gridViewImagesLists;
    public GridViewImages (Context mContext, List<GridViewImagesList> serviceObjects) {
        this.mContext = mContext;
        this.gridViewImagesLists = serviceObjects;
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
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
    }
    @Override
    public int getItemCount() {
        return gridViewImagesLists.size();
    }
}
class ServiceViewHolder extends RecyclerView.ViewHolder {

    ImageView mImage;
    TextView mTitle;
    ServiceViewHolder(View itemView) {
        super(itemView);
        mImage = itemView.findViewById(R.id.pdf_image);
        mTitle = itemView.findViewById(R.id.date);
    }
}
