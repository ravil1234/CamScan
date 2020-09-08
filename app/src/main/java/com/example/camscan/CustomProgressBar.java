package com.example.camscan;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.example.camscan.Activities.BoxActivity;

public class CustomProgressBar {

    Context context;
    ImageView back,front;
    Animation down,up;
    boolean isOn=false;
    AlertDialog d;
    public CustomProgressBar(Context context){
        this.context=context;

        AlertDialog.Builder builder=new AlertDialog.Builder(context,R.style.CustomDialog);
        View v= LayoutInflater.from(context).inflate(R.layout.layout_progress_custom_my,null);
        builder.setView(v);

        back=v.findViewById(R.id.custom_pBar_bk);
        front=v.findViewById(R.id.custom_pBar_mover);

        down=AnimationUtils.loadAnimation(context,R.anim.my_doc_anim_slide_down);
        up=AnimationUtils.loadAnimation(context,R.anim.my_doc_anim_slide_up);

        d=builder.create();
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
            dismiss();
            }
        });
        t=new Thread(new Runnable() {
            @Override
            public  void run() {

                while (isOn){
                    new Thread(new Runnable() {
                        @Override
                        public synchronized void run() {
                            Log.e("TAG", "run: Custom Progress bar" );
                            ((BoxActivity)context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    front.startAnimation(down);
                                    front.startAnimation(up);
                                }
                            });

                        }
                    }).start();


                }
            }
        });

    }
    private Thread t;
    public void startAnimation(){
        d.show();
        isOn=true;
        t.start();

    }
    public void dismiss(){
        isOn=false;
        d.dismiss();
        if(t!=null){
            t.interrupt();

        }
    }

}
