package com.example.camscan.AdapterClass;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.camscan.ObjectClass.FilterObject;
import com.example.camscan.R;
import java.util.List;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FilterGridAdapter extends RecyclerView.Adapter<ServiceViewHolder> {

    private Context mContext;
    private List<FilterObject> filterObjects;
    private int selected_position;
    private ServiceViewHolder prev_holder;
    SharedPreferences preferences;
    public FilterGridAdapter(Context mContext, List<FilterObject> serviceObjects,int selected_position)
    {
        this.mContext = mContext;
        this.filterObjects = serviceObjects;
        this.selected_position=selected_position;
        preferences=mContext.getSharedPreferences("SharedPreference",Context.MODE_PRIVATE);
    }
    @Override
    public ServiceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_grid_layout, parent,
                false);
        return new ServiceViewHolder(mView);
    }
    @Override
    public void onBindViewHolder(final ServiceViewHolder holder, final int position)
    {
        FilterObject serviceObject=filterObjects.get(position);
        holder.mTitle.setText(serviceObject.getFilter_name());
        holder.mImage.setImageResource(serviceObject.getImage_id());
        if(selected_position==position)
        {
            prev_holder=holder;
            holder.mcardView.setCardBackgroundColor(Color.parseColor("#273696FF"));
        }
        else
            holder.mcardView.setCardBackgroundColor(Color.parseColor("#ffffff"));
        holder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                prev_holder.mcardView.setCardBackgroundColor(Color.parseColor("#ffffff"));
                holder.mcardView.setCardBackgroundColor(Color.parseColor("#273696FF"));
                preferences.edit().putInt("myfilter",position).apply();
                selected_position=position;
                prev_holder=holder;
            }
        });
    }
    @Override
    public int getItemCount() {
        return filterObjects.size();
    }
}
class ServiceViewHolder extends RecyclerView.ViewHolder {

    ImageView mImage;
    TextView mTitle;
    CardView mcardView;
    ServiceViewHolder(View itemView) {
        super(itemView);
        mImage = itemView.findViewById(R.id.filter_image);
        mTitle = itemView.findViewById(R.id.filter_name);
        mcardView=itemView.findViewById(R.id.cardview_filter);
    }
}