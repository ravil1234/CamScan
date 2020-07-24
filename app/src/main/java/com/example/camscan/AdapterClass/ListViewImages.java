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
public class ListViewImages extends RecyclerView.Adapter<ListViewHolder> {

    private Context mContext;
    private List<GridViewImagesList> gridViewImagesLists;

    public ListViewImages(Context mContext, List<GridViewImagesList> serviceObjects) {
        this.mContext = mContext;
        this.gridViewImagesLists = serviceObjects;
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view_homescreen, parent, false);
        return new ListViewHolder(mView);
    }
    @Override
    public void onBindViewHolder(final ListViewHolder holder, final int position) {
        GridViewImagesList gridViewImagesList = gridViewImagesLists.get(position);
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
class ListViewHolder extends RecyclerView.ViewHolder {

    ImageView mImage;
    TextView mTitle;

    ListViewHolder(View itemView) {
        super(itemView);
        mImage = itemView.findViewById(R.id.pdf_image);
        mTitle = itemView.findViewById(R.id.date_pdf);
    }
}
