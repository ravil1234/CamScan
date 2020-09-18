package com.example.camscan.AdapterClass;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class ListViewImages extends RecyclerView.Adapter<ListViewHolder> {

    private Context mContext;
    private List<GridViewImagesList> gridViewImagesLists;
    private View.OnClickListener mListener;
    private Transformation transformation;
    public ListViewImages(Context mContext, List<GridViewImagesList> serviceObjects, View.OnClickListener listener) {
        this.mContext = mContext;
        this.gridViewImagesLists = serviceObjects;
        transformation=new RoundedCornersTransformation(10,0);
        mListener=listener;
    }
    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view_homescreen, parent, false);

        return new ListViewHolder(mView);
    }
    @Override
    public void onBindViewHolder(final ListViewHolder holder, final int position)
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
//        holder.mTitle.setText(list.getImage_date());
        holder.file_name.setText(list.getName());
        holder.doc_date_created.setText(list.getImage_date());
        if(list.getPcount()<9)
        holder.no_pages.setText("0"+list.getPcount());
        else
            holder.no_pages.setText(list.getPcount()+"");
        Picasso.with(mContext).load(list.getImage_url()).transform(transformation).fit().centerCrop().into(holder.mImage);
        holder.itemView.setOnClickListener(mListener);
    }
    @Override
    public int getItemCount() {
        return gridViewImagesLists.size();
    }
}
class ListViewHolder extends RecyclerView.ViewHolder {

    ImageView mImage;
    TextView file_name,doc_date_created;
    CheckBox checkBox;
    TextView no_pages;
    ListViewHolder(View itemView) {
        super(itemView);
        mImage = itemView.findViewById(R.id.image_pdf);
        file_name = itemView.findViewById(R.id.name_pdf);
        checkBox=itemView.findViewById(R.id.checkbox);
        no_pages=itemView.findViewById(R.id.no_pages);
        doc_date_created=itemView.findViewById(R.id.date_pdf);
        checkBox.setClickable(false);
    }
}
