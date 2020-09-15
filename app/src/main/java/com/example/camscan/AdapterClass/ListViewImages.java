package com.example.camscan.AdapterClass;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.camscan.ObjectClass.GridViewImagesList;
import com.example.camscan.R;
import com.squareup.picasso.Picasso;

import java.util.List;
import androidx.recyclerview.widget.RecyclerView;
public class ListViewImages extends RecyclerView.Adapter<ListViewHolder> {

    private Context mContext;
    private List<GridViewImagesList> gridViewImagesLists;
    View.OnClickListener mListener;

    public ListViewImages(Context mContext, List<GridViewImagesList> serviceObjects, View.OnClickListener listener) {
        this.mContext = mContext;
        this.gridViewImagesLists = serviceObjects;
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
        holder.mTitle.setText(list.getDid()+" ");

      //  Picasso.with(mContext).load(list.getImage_url()).into(holder.mImage);
        holder.itemView.setOnClickListener(mListener);
//        holder.checkBox.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view)
//            {
//                if(holder.checkBox.isChecked())
//                    list.setIschecked(false);
//                else
//                    list.setIschecked(true);
//            }
//        });
    }
    @Override
    public int getItemCount() {
        return gridViewImagesLists.size();
    }
}
class ListViewHolder extends RecyclerView.ViewHolder {

    ImageView mImage;
    TextView mTitle;
    CheckBox checkBox;

    ListViewHolder(View itemView) {
        super(itemView);
        mImage = itemView.findViewById(R.id.pdf_image);
        mTitle = itemView.findViewById(R.id.date_pdf);
        checkBox=itemView.findViewById(R.id.checkbox);
        checkBox.setClickable(false);
    }
}
