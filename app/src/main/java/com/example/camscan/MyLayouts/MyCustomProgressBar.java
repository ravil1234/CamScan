package com.example.camscan.MyLayouts;

import android.app.AlertDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;

import com.example.camscan.R;

public class MyCustomProgressBar extends ProgressBar {

    //private static volatile MyCustomProgressBar INSTANCE;
    AlertDialog d;

    private Context context;
    public MyCustomProgressBar(Context context) {
        super(context);
        this.context=context;
    }

    public MyCustomProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        //here
        this.context=context;

    }

    public MyCustomProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context=context;
    }

    public MyCustomProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context=context;
    }

    public void start(){

        AlertDialog.Builder builder=new AlertDialog.Builder(context);

        View view= LayoutInflater.from(context).inflate(R.layout.layout_progress_custom_my,null);
        builder.setView(view);
        ImageView mover=view.findViewById(R.id.custom_pBar_mover);
        ImageView bk=view.findViewById(R.id.custom_pBar_bk);
        float originalY=mover.getY();
        Animation anim=new TranslateAnimation(mover.getX(),mover.getX(),mover.getY(),mover.getY()+1000);
        anim.setDuration(1500);
        anim.setInterpolator(new OvershootInterpolator());
        anim.setFillAfter(true);
        mover.startAnimation(anim);
//
//
//        Animation anim2=new TranslateAnimation(mover.getX(),mover.getX(),mover.getY(),originalY);
//        anim2.setDuration(1500);
//        anim2.setInterpolator(new OvershootInterpolator());
//        anim2.setFillAfter(true);
//        mover.startAnimation(anim2);

        builder.setPositiveButton("CLOSE",null);
        d=builder.create();
        d.show();
    }

    public void stop(){
        if(d!=null){
            d.dismiss();
        }
    }

}
