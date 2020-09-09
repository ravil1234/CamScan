package com.example.camscan.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.camscan.Objects.NavMenuObject;
import com.example.camscan.R;

import java.util.ArrayList;

public class NavMenuAdapter extends BaseAdapter {

    Context context;
    ArrayList<NavMenuObject> title;

    public NavMenuAdapter(Context context,ArrayList<NavMenuObject> list){
        this.context=context;
        title=list;
    }


    @Override
    public int getCount() {
        return title.size();
    }

    @Override
    public Object getItem(int i) {
        return title.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view==null){
            view= LayoutInflater.from(context).inflate(R.layout.item_nav_menu,viewGroup,false);
        }
        NavMenuObject current=title.get(i);

        ImageView icon=view.findViewById(R.id.item_nav_icon);
       // ImageView expandable=view.findViewById(R.id.item_nav_expand);
        TextView titleText=view.findViewById(R.id.item_nav_title);
        titleText.setText(current.getTitle());
        if(current.getIconId()!=0) {
            icon.setImageDrawable(context.getResources().getDrawable(current.getIconId(), null));
        }


        return view;
    }
}
