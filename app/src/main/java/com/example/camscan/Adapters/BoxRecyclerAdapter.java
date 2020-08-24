package com.example.camscan.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.camscan.MyLayouts.MyBoxLayout;
import com.example.camscan.Objects.MyPicture;
import com.example.camscan.R;
import com.example.camscan.UtilityClass;

import java.util.ArrayList;
import java.util.List;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class BoxRecyclerAdapter extends RecyclerView.Adapter<BoxRecyclerAdapter.MyViewHolder> {

    Context context;
    ArrayList<MyPicture> images;
    ViewPager2 myvp;
    int screenWidth=0;
    int screenHeight=0;
    int dotWidth=0;

    public BoxRecyclerAdapter(Context context,ArrayList<MyPicture> list){
        this.context=context;
        this.images=list;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView imgView;
        MyBoxLayout boxLayout;
        View root;
        public MyViewHolder(View view){
            super(view);
            imgView=view.findViewById(R.id.box_item_img_view);
            boxLayout=view.findViewById(R.id.box_item_bounding_box);
            root=view;
        }
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_box_recycler,parent,false);
        return new MyViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        MyPicture current=images.get(position);
        MyBoxLayout mbl=holder.boxLayout;
        ImageView img=holder.imgView;
        Uri imgUri=Uri.parse(current.getOriginalUri());
        mbl.setMyPicObject(current);
        mbl.setViewPager(myvp);
        //mbl.setDimensions(screenHeight,screenWidth);

//        mbl.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//
//                if(current.getCoordinates().get(3).x==0 && current.getCoordinates().get(3).y==0){
//                    //set the bounding box to default position
//                   // mbl.initViewPos();
//                    Log.e("HERE", "onBindViewHolder: "+"ZERO" );
//                }else{
//                    //set the boucngin box to the defined postion
//                    ArrayList<Point> coos=current.getCoordinates();
//                    Log.e("HERE", "onBindViewHolder: "+"UPDATED"+current.getCoordinates().get(3).x+" "+current.getCoordinates().get(3).y );
//
//                    mbl.updateViewPos(coos);
//                }
//                mbl.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//
//            }
//        });


        if(current.getImg()==null){
            //Bitmap imageBitmap= BitmapFactory.decodeFile(imgUri.getPath());
            Bitmap imageBitmap=UtilityClass.populateImage(context,Uri.parse(current.getOriginalUri()),false,screenWidth,screenHeight);
            //current.setImg(imageBitmap);
            mbl.setBitmap(imageBitmap);
            img.setImageBitmap(imageBitmap);
           // current.setImg(imageBitmap);
                //            if(img.getHeight()!=0){
                //               // Log.e(TAG, "onBindViewHolder: "+imageBitmap.getWidth()+" "+img.getWidth()+" "+imageBitmap.getHeight()+" "+img.getHeight() );
                //                FrameLayout.LayoutParams param2=(FrameLayout.LayoutParams)holder.imgView.getLayoutParams();
                //       //         FrameLayout.LayoutParams params=(FrameLayout.LayoutParams)holder.boxLayout.getLayoutParams();
                //                holder.boxLayout.setLayoutParams(param2);
                //
                //            }
//            Log.e(TAG, "onBindViewHolder: "+imageBitmap.getWidth()+" "+imageBitmap.getHeight()+" "+screenWidth+" "+screenHeight );
//            mbl.updateViewPos(current.getCoordinates());
        }else{
            img.setImageBitmap(current.getImg());
            mbl.setBitmap(current.getImg());

        }

       // Log.e(TAG, "onBindViewHolder: "+screenHeight+" "+screenWidth );
        if(current.getCoordinates().get(3).x==0 && current.getCoordinates().get(3).y==0){
            //set the bounding box to default position
           // mbl.initViewPos();
            Log.e("HERE", "onBindViewHolder: "+"ZERO" );
        }else{
            //set the boucngin box to the defined postion
            ArrayList<Point> coos=current.getCoordinates();
//            Log.e("HERE", "onBindViewHolder: "+"UPDATED"+current.getCoordinates().get(3).x+" "+current.getCoordinates().get(3).y );

            mbl.updateViewPos(coos,dotWidth);
        }

    }
    public void setDot(int d){
        dotWidth=d;
    }
    public void setViewPager(ViewPager2 mvp){
        myvp=mvp;


    }

    public void setDimensions(int w,int h){
        screenWidth=w;
        screenHeight=h;
    }
}
