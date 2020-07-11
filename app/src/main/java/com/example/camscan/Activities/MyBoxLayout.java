package com.example.camscan.Activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.camscan.R;

import java.util.ArrayList;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class MyBoxLayout extends RelativeLayout {

    ImageView dot1,dot2,dot3,dot4,dot12,dot23,dot34,dot14;
    MyBoxLine line12,line23,line34,line14;
    Bitmap image;
    ImageView magnifier;
    FrameLayout imageCont;
    int x_delta;
    int y_delta;
    int screen_width;
    int screen_height;
    //private Rect rect;
    //View rootView;
    int dotWidth;
    int dotHeight;

    public MyBoxLayout(Context context) {
        super(context);
    }

    public MyBoxLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater= LayoutInflater.from(context);
        View view=inflater.inflate(R.layout.layout_box_my,this);
        //rootView=view;


        dot1=view.findViewById(R.id.my_box_dot1);
        dot2=view.findViewById(R.id.my_box_dot2);
        dot3=view.findViewById(R.id.my_box_dot3);
        dot4=view.findViewById(R.id.my_box_dot4);
        dot12=view.findViewById(R.id.my_box_dot12);
        dot23=view.findViewById(R.id.my_box_dot23);
        dot34=view.findViewById(R.id.my_box_dot34);
        dot14=view.findViewById(R.id.my_box_dot14);
        line12=view.findViewById(R.id.my_box_line12);
        line23=view.findViewById(R.id.my_box_line23);
        line34=view.findViewById(R.id.my_box_line34);
        line14=view.findViewById(R.id.my_box_line14);
        magnifier=view.findViewById(R.id.my_box_magnifier);
        imageCont=view.findViewById(R.id.my_box_img_frame);


        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                initViewPos(view.getHeight(),view.getWidth());
                screen_height=view.getHeight();
                screen_width=view.getWidth();
                dotWidth= dot1.getWidth();
                dotHeight=dot1.getHeight();


            }
        });



        dot1.setOnTouchListener(new MyListener());
        dot2.setOnTouchListener(new MyListener());
        dot3.setOnTouchListener(new MyListener());
        dot4.setOnTouchListener(new MyListener());

        dot12.setOnTouchListener(new MyCenterListener());
        dot23.setOnTouchListener(new MyCenterListener());
        dot34.setOnTouchListener(new MyCenterListener());
        dot14.setOnTouchListener(new MyCenterListener());


    }


    public MyBoxLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyBoxLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    class MyListener implements OnTouchListener{

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            final int X=(int)motionEvent.getRawX();
            final int Y=(int)motionEvent.getRawY();

            switch (motionEvent.getAction() & motionEvent.ACTION_MASK) {

                case MotionEvent.ACTION_DOWN:{
                    RelativeLayout.LayoutParams params=(RelativeLayout.LayoutParams)view.getLayoutParams();
                    x_delta=X-params.leftMargin;
                    y_delta=Y-params.topMargin;
                    updateViews(view);

                    imageCont.setVisibility(VISIBLE);

                    if(view.getId()==R.id.my_box_dot1){
                        RelativeLayout.LayoutParams par=(RelativeLayout.LayoutParams)imageCont.getLayoutParams();
                        par.addRule(RelativeLayout.ALIGN_PARENT_END);
                        par.removeRule(RelativeLayout.ALIGN_PARENT_START);
                        imageCont.setLayoutParams(par);
                    }else if(view.getId()==R.id.my_box_dot2){
                        RelativeLayout.LayoutParams par=(RelativeLayout.LayoutParams)imageCont.getLayoutParams();
                        par.removeRule(RelativeLayout.ALIGN_PARENT_END);
                        par.addRule(RelativeLayout.ALIGN_PARENT_START);
                        imageCont.setLayoutParams(par);
                    }
                    else{
                        RelativeLayout.LayoutParams par=(RelativeLayout.LayoutParams)imageCont.getLayoutParams();
                        par.removeRule(RelativeLayout.ALIGN_PARENT_END);
                        //par.addRule(RelativeLayout.ALIGN_PARENT_START);
                        imageCont.setLayoutParams(par);
                    }
                    updateMagnifier((int)view.getLeft()+view.getWidth()/2,(int)view.getTop()+view.getHeight()/2);
                    break;
                }
                case MotionEvent.ACTION_UP:{
                    imageCont.setVisibility(GONE);
                    break;

                }
                case MotionEvent.ACTION_POINTER_DOWN:{
                    break;
                }
                case MotionEvent.ACTION_POINTER_UP:{
                    break;
                }
                case MotionEvent.ACTION_OUTSIDE:{
                    RelativeLayout.LayoutParams params=(RelativeLayout.LayoutParams)view.getLayoutParams();
                    params.leftMargin=0;
                    params.topMargin=0;
                    view.setLayoutParams(params);
                    updateViews(view);
                    break;
                }
                case MotionEvent.ACTION_MOVE:{

                    RelativeLayout.LayoutParams params=(RelativeLayout.LayoutParams)view.getLayoutParams();
                    params.leftMargin=(X-x_delta);
                    params.topMargin=(Y-y_delta);
                    params.rightMargin=-250;
                    params.bottomMargin=-250;
                    //75 300 1005 1548

                    if(params.leftMargin>screen_width-dotWidth/2){
                        params.leftMargin=screen_width-dotWidth/2;
                    }
                    if(params.leftMargin<-dotWidth/2){
                        params.leftMargin=-dotWidth/2;
                    }
                    if(params.topMargin>screen_height-dotHeight/2){
                        params.topMargin=screen_height-dotHeight/2;
                    }
                    if(params.topMargin<-dotHeight/2){
                        params.topMargin=-dotHeight/2;
                    }


                    //Log.e(TAG, "onTouch: "+params.leftMargin+" "+params.topMargin+" "+xTmp+" "+yTmp );
                    view.setLayoutParams(params);
                    view.invalidate();
                    updateViews(view);

                    int x = params.leftMargin + (view.getWidth() / 2);
                    int y = params.topMargin + (view.getHeight() / 2);

                    updateMagnifier(x, y);

                    break;
                }


            }


            return true;
        }
    }

    private void updateMagnifier(int x, int y) {

        int imgWidth=image.getWidth();
        int imgHeight=image.getHeight();

        float ratioX=(float)imgWidth/(float)screen_width;
        float ratioY=(float)imgHeight/(float)screen_height;

        x=(int)(x*ratioX);
        y=(int)(y*ratioY);
//        int upperX=(int)((x-24)*ratioX);
 //       int upperY=(int)((y-24)*ratioY);

        int upperX=x-24;
        int upperY=y-24;
        int width=49;
        int height=49;

        if(upperX<0){
            width+=upperX;
            upperX=0;
        }
        if(upperY<0){
            height+=upperY;
            upperY=0;
        }
        if(upperX+49>imgWidth){
            width=imgWidth-upperX;
        }
        if(upperY+49>imgHeight){
            height=imgHeight-upperY;

        }

        if(width>0 && height>0) {
            Bitmap cropped = Bitmap.createBitmap(image, upperX, upperY, width, height);
            Bitmap Resized = Bitmap.createScaledBitmap(cropped, 98, 98, true);
            magnifier.setImageBitmap(Resized);

        }

    }

    class MyCenterListener implements OnTouchListener{

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            final int X=(int)motionEvent.getRawX();
            final int Y=(int)motionEvent.getRawY();

            switch (motionEvent.getAction() & motionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:{
                    RelativeLayout.LayoutParams params=(RelativeLayout.LayoutParams)view.getLayoutParams();
                    x_delta=X-params.leftMargin;
                    y_delta=Y-params.topMargin;

                    break;
                }
                case MotionEvent.ACTION_UP:{
                    break;

                }
                case MotionEvent.ACTION_POINTER_DOWN:{
                    break;
                }
                case MotionEvent.ACTION_POINTER_UP:{
                    break;
                }
                case MotionEvent.ACTION_MOVE:{

                    RelativeLayout.LayoutParams params=(RelativeLayout.LayoutParams)view.getLayoutParams();
                    RelativeLayout.LayoutParams param1=(RelativeLayout.LayoutParams)dot1.getLayoutParams();
                    RelativeLayout.LayoutParams param2=(RelativeLayout.LayoutParams)dot2.getLayoutParams();
                    RelativeLayout.LayoutParams param3=(RelativeLayout.LayoutParams)dot3.getLayoutParams();
                    RelativeLayout.LayoutParams param4=(RelativeLayout.LayoutParams)dot4.getLayoutParams();
                    switch(view.getId()){
                        case R.id.my_box_dot12:{

                            int diff=params.topMargin;

                            params.topMargin=Y-y_delta;
                            diff-=params.topMargin;

                           // params.bottomMargin=-250;

                            param1.topMargin-=diff;


                            if(param1.topMargin>screen_height-dotHeight/2){
                                param1.topMargin=screen_height-dotHeight/2;
                            }
                            if(param1.topMargin<-dotHeight/2){
                                param1.topMargin=-dotHeight/2;
                            }


                            dot1.setLayoutParams(param1);
                            param2.topMargin-=diff;

                            if(param2.topMargin>screen_height-dotHeight/2){
                                param2.topMargin=screen_height-dotHeight/2;
                            }
                            if(param2.topMargin<-dotHeight/2){
                                param2.topMargin=-dotHeight/2;
                            }

                            dot2.setLayoutParams(param2);

                            view.setLayoutParams(params);

                            updateViews(dot1);
                            updateViews(dot2);
                            break;
                        }
                        case R.id.my_box_dot23:{
                            int diff=params.leftMargin;

                            params.leftMargin=X-x_delta;
                            //params.rightMargin=-250;
                            diff-=params.leftMargin;


                            param2.leftMargin-=diff;

                            if(param2.leftMargin>screen_width-dotWidth/2){
                                param2.leftMargin=screen_width-dotWidth/2;
                            }
                            if(param2.leftMargin<-dotWidth/2){
                                param2.leftMargin=-dotWidth/2;
                            }

                            dot2.setLayoutParams(param2);

                            param3.leftMargin-=diff;

                            if(param3.leftMargin>screen_width-dotWidth/2){
                                param3.leftMargin=screen_width-dotWidth/2;
                            }
                            if(param3.leftMargin<-dotWidth/2){
                                param3.leftMargin=-dotWidth/2;
                            }


                            dot3.setLayoutParams(param3);
                            view.setLayoutParams(params);

                            updateViews(dot2);
                            updateViews(dot3);

                            break;
                        }
                        case R.id.my_box_dot34:{
                            int diff=params.topMargin;

                            params.topMargin=Y-y_delta;
                            diff-=params.topMargin;

                            //params.bottomMargin=-250;

                            param3.topMargin-=diff;

                            if(param3.topMargin>screen_height-dotHeight/2){
                                param3.topMargin=screen_height-dotHeight/2;
                            }
                            if(param3.topMargin<-dotHeight/2){
                                param3.topMargin=-dotHeight/2;
                            }


                            dot3.setLayoutParams(param3);

                            param4.topMargin-=diff;

                            if(param4.topMargin>screen_height-dotHeight/2){
                                param4.topMargin=screen_height-dotHeight/2;
                            }
                            if(param4.topMargin<-dotHeight/2){
                                param4.topMargin=-dotHeight/2;
                            }


                            dot4.setLayoutParams(param4);
                            view.setLayoutParams(params);

                            updateViews(dot3);
                            updateViews(dot4);
                            break;
                        }
                        case R.id.my_box_dot14:{
                            int diff=params.leftMargin;

                            params.leftMargin=X-x_delta;

                            //params.rightMargin=-250;

                            diff-=params.leftMargin;

                            param1.leftMargin-=diff;
                            if(param1.leftMargin>screen_width-dotWidth/2){
                                param1.leftMargin=screen_width-dotWidth/2;
                            }
                            if(param1.leftMargin<-dotWidth/2){
                                param1.leftMargin=-dotWidth/2;
                            }

                            dot1.setLayoutParams(param1);

                            param4.leftMargin-=diff;
                            if(param4.leftMargin>screen_width-dotWidth/2){
                                param4.leftMargin=screen_width-dotWidth/2;
                            }
                            if(param4.leftMargin<-dotWidth/2){
                                param4.leftMargin=-dotWidth/2;
                            }
                            dot4.setLayoutParams(param4);
                            view.setLayoutParams(params);

                            updateViews(dot1);
                            updateViews(dot4);
                            break;
                        }
                    }

                    break;
                }


            }


            return true;
        }
    }

    private void initViewPos(int height, int width) {
        Log.e(TAG, "initViewPos: "+height+" "+width );
        RelativeLayout.LayoutParams param1=(RelativeLayout.LayoutParams)dot1.getLayoutParams();
        RelativeLayout.LayoutParams param2=(RelativeLayout.LayoutParams)dot2.getLayoutParams();
        RelativeLayout.LayoutParams param3=(RelativeLayout.LayoutParams)dot3.getLayoutParams();
        RelativeLayout.LayoutParams param4=(RelativeLayout.LayoutParams)dot4.getLayoutParams();

        param1.leftMargin=50;
        param1.topMargin=50;

        param2.leftMargin=width-dot2.getWidth()-50;
        //param2.bottomMargin=height-dot2.getHeight()-50;
        param2.topMargin=50;

        param3.leftMargin=width-dot3.getWidth()-50;
        param3.topMargin=height-dot3.getHeight()-50;

        param4.topMargin=height-dot4.getHeight()-50;
        //param4.rightMargin=width-dot4.getWidth()-50;
        param4.leftMargin=50;

        dot2.setLayoutParams(param2);
        dot3.setLayoutParams(param3);
        dot4.setLayoutParams(param4);

        updateViews(dot1);
        updateViews(dot2);
        updateViews(dot3);
        updateViews(dot4);

    }


    public void updateViews(View view){

        if(view.getId()==R.id.my_box_dot1) {

            RelativeLayout.LayoutParams paramsMain = (RelativeLayout.LayoutParams) view.getLayoutParams();
            RelativeLayout.LayoutParams paramsRight = (RelativeLayout.LayoutParams) dot12.getLayoutParams();
            RelativeLayout.LayoutParams paramsLeft = (RelativeLayout.LayoutParams) dot14.getLayoutParams();
            RelativeLayout.LayoutParams paramsRightMost = (RelativeLayout.LayoutParams) dot2.getLayoutParams();
            RelativeLayout.LayoutParams paramsLefttMost = (RelativeLayout.LayoutParams) dot4.getLayoutParams();

            paramsRight.leftMargin = (paramsMain.leftMargin + paramsRightMost.leftMargin) / 2;
            paramsRight.rightMargin = (paramsMain.rightMargin + paramsRightMost.rightMargin) / 2;
            paramsRight.topMargin = (paramsMain.topMargin + paramsRightMost.topMargin) / 2;
            paramsRight.bottomMargin = (paramsMain.bottomMargin + paramsRightMost.bottomMargin) / 2;

            paramsLeft.leftMargin = (paramsMain.leftMargin + paramsLefttMost.leftMargin) / 2;
            paramsLeft.rightMargin = (paramsMain.rightMargin + paramsLefttMost.rightMargin) / 2;
            paramsLeft.topMargin = (paramsMain.topMargin + paramsLefttMost.topMargin) / 2;
            paramsLeft.bottomMargin = (paramsMain.bottomMargin + paramsLefttMost.bottomMargin) / 2;

            dot12.setLayoutParams(paramsRight);
            dot14.setLayoutParams(paramsLeft);

            //RelativeLayout.LayoutParams paramLine12=(RelativeLayout.LayoutParams)line12.getLayoutParams();

            line12.setX1(paramsMain.leftMargin+(dot1.getWidth()/2));
            line12.setY1(paramsMain.topMargin+(dot1.getHeight()/2));

            line14.setX2(paramsMain.leftMargin+(dot1.getWidth()/2));
            line14.setY2(paramsMain.topMargin+(dot1.getHeight()/2));


        }
        else if(view.getId()==R.id.my_box_dot2){

            RelativeLayout.LayoutParams paramsMain = (RelativeLayout.LayoutParams) view.getLayoutParams();
            RelativeLayout.LayoutParams paramsRight = (RelativeLayout.LayoutParams) dot23.getLayoutParams();
            RelativeLayout.LayoutParams paramsLeft = (RelativeLayout.LayoutParams) dot12.getLayoutParams();
            RelativeLayout.LayoutParams paramsRightMost = (RelativeLayout.LayoutParams) dot3.getLayoutParams();
            RelativeLayout.LayoutParams paramsLefttMost = (RelativeLayout.LayoutParams) dot1.getLayoutParams();

            paramsRight.leftMargin = (paramsMain.leftMargin + paramsRightMost.leftMargin) / 2;
            paramsRight.rightMargin = (paramsMain.rightMargin + paramsRightMost.rightMargin) / 2;
            paramsRight.topMargin = (paramsMain.topMargin + paramsRightMost.topMargin) / 2;
            paramsRight.bottomMargin = (paramsMain.bottomMargin + paramsRightMost.bottomMargin) / 2;

            paramsLeft.leftMargin = (paramsMain.leftMargin + paramsLefttMost.leftMargin) / 2;
            paramsLeft.rightMargin = (paramsMain.rightMargin + paramsLefttMost.rightMargin) / 2;
            paramsLeft.topMargin = (paramsMain.topMargin + paramsLefttMost.topMargin) / 2;
            paramsLeft.bottomMargin = (paramsMain.bottomMargin + paramsLefttMost.bottomMargin) / 2;

            dot23.setLayoutParams(paramsRight);
            dot12.setLayoutParams(paramsLeft);

            line12.setX2(paramsMain.leftMargin+(dot2.getWidth()/2));
            line12.setY2(paramsMain.topMargin+(dot2.getHeight()/2));

            line23.setX1(paramsMain.leftMargin+(dot2.getWidth()/2));
            line23.setY1(paramsMain.topMargin+(dot2.getHeight()/2));


        }
        else if(view.getId()==R.id.my_box_dot3){

            RelativeLayout.LayoutParams paramsMain = (RelativeLayout.LayoutParams) view.getLayoutParams();
            RelativeLayout.LayoutParams paramsRight = (RelativeLayout.LayoutParams) dot23.getLayoutParams();
            RelativeLayout.LayoutParams paramsLeft = (RelativeLayout.LayoutParams) dot34.getLayoutParams();
            RelativeLayout.LayoutParams paramsRightMost = (RelativeLayout.LayoutParams) dot2.getLayoutParams();
            RelativeLayout.LayoutParams paramsLefttMost = (RelativeLayout.LayoutParams) dot4.getLayoutParams();

            paramsRight.leftMargin = (paramsMain.leftMargin + paramsRightMost.leftMargin) / 2;
            paramsRight.rightMargin = (paramsMain.rightMargin + paramsRightMost.rightMargin) / 2;
            paramsRight.topMargin = (paramsMain.topMargin + paramsRightMost.topMargin) / 2;
            paramsRight.bottomMargin = (paramsMain.bottomMargin + paramsRightMost.bottomMargin) / 2;

            paramsLeft.leftMargin = (paramsMain.leftMargin + paramsLefttMost.leftMargin) / 2;
            paramsLeft.rightMargin = (paramsMain.rightMargin + paramsLefttMost.rightMargin) / 2;
            paramsLeft.topMargin = (paramsMain.topMargin + paramsLefttMost.topMargin) / 2;
            paramsLeft.bottomMargin = (paramsMain.bottomMargin + paramsLefttMost.bottomMargin) / 2;

            dot23.setLayoutParams(paramsRight);
            dot34.setLayoutParams(paramsLeft);

            line34.setX1(paramsMain.leftMargin+(dot3.getWidth()/2));
            line34.setY1(paramsMain.topMargin+(dot3.getHeight()/2));

            line23.setX2(paramsMain.leftMargin+(dot3.getWidth()/2));
            line23.setY2(paramsMain.topMargin+(dot3.getHeight()/2));
        }else if(view.getId()==R.id.my_box_dot4){
            RelativeLayout.LayoutParams paramsMain = (RelativeLayout.LayoutParams) view.getLayoutParams();
            RelativeLayout.LayoutParams paramsRight = (RelativeLayout.LayoutParams) dot14.getLayoutParams();
            RelativeLayout.LayoutParams paramsLeft = (RelativeLayout.LayoutParams) dot34.getLayoutParams();
            RelativeLayout.LayoutParams paramsRightMost = (RelativeLayout.LayoutParams) dot1.getLayoutParams();
            RelativeLayout.LayoutParams paramsLefttMost = (RelativeLayout.LayoutParams) dot3.getLayoutParams();

            paramsRight.leftMargin = (paramsMain.leftMargin + paramsRightMost.leftMargin) / 2;
            paramsRight.rightMargin = (paramsMain.rightMargin + paramsRightMost.rightMargin) / 2;
            paramsRight.topMargin = (paramsMain.topMargin + paramsRightMost.topMargin) / 2;
            paramsRight.bottomMargin = (paramsMain.bottomMargin + paramsRightMost.bottomMargin) / 2;

            paramsLeft.leftMargin = (paramsMain.leftMargin + paramsLefttMost.leftMargin) / 2;
            paramsLeft.rightMargin = (paramsMain.rightMargin + paramsLefttMost.rightMargin) / 2;
            paramsLeft.topMargin = (paramsMain.topMargin + paramsLefttMost.topMargin) / 2;
            paramsLeft.bottomMargin = (paramsMain.bottomMargin + paramsLefttMost.bottomMargin) / 2;

            dot14.setLayoutParams(paramsRight);
            dot34.setLayoutParams(paramsLeft);

            line34.setX2(paramsMain.leftMargin+(dot4.getWidth()/2));
            line34.setY2(paramsMain.topMargin+(dot4.getHeight()/2));

            line14.setX1(paramsMain.leftMargin+(dot4.getWidth()/2));
            line14.setY1(paramsMain.topMargin+(dot4.getHeight()/2));
        }
//        invalidate();


    }



    public void setBitmap(Bitmap copy){
        image=copy;
    }
    public ArrayList<Point> getCorners(){

        RelativeLayout.LayoutParams param1=(RelativeLayout.LayoutParams)dot1.getLayoutParams();
        RelativeLayout.LayoutParams param2=(RelativeLayout.LayoutParams)dot2.getLayoutParams();
        RelativeLayout.LayoutParams param3=(RelativeLayout.LayoutParams)dot3.getLayoutParams();
        RelativeLayout.LayoutParams param4=(RelativeLayout.LayoutParams)dot4.getLayoutParams();

        int x1=param1.leftMargin+(int)(dot1.getWidth()/2);
        int y1=param1.topMargin+(int)(dot1.getHeight()/2);

        int x2=param2.leftMargin+(int)(dot2.getWidth()/2);
        int y2=param2.topMargin+(int)(dot2.getHeight()/2);

        int x3=param3.leftMargin+(int)(dot3.getWidth()/2);
        int y3=param3.topMargin+(int)(dot3.getHeight()/2);

        int x4=param4.leftMargin+(int)(dot4.getWidth()/2);
        int y4=param4.topMargin+(int)(dot4.getHeight()/2);


        ArrayList<Point> points=new ArrayList<>();
        points.add(new Point(x1,y1));
        points.add(new Point(x2,y2));
        points.add(new Point(x3,y3));
        points.add(new Point(x4,y4));

        return points;

    }

    public void resetPoints(){
        RelativeLayout.LayoutParams param1=(RelativeLayout.LayoutParams)dot1.getLayoutParams();
        RelativeLayout.LayoutParams param2=(RelativeLayout.LayoutParams)dot2.getLayoutParams();
        RelativeLayout.LayoutParams param3=(RelativeLayout.LayoutParams)dot3.getLayoutParams();
        RelativeLayout.LayoutParams param4=(RelativeLayout.LayoutParams)dot4.getLayoutParams();

        param1.leftMargin=-dotWidth/2;
        param1.topMargin=-dotHeight/2;

        param2.leftMargin=screen_width-dotWidth/2;
        param2.topMargin=-dotHeight/2;

        param3.leftMargin=screen_width-dotWidth/2;
        param3.topMargin=screen_height-dotHeight/2;

        param4.leftMargin=-dotWidth/2;
        param4.topMargin=screen_height-dotHeight/2;


        dot1.setLayoutParams(param1);
        dot2.setLayoutParams(param2);
        dot3.setLayoutParams(param3);
        dot4.setLayoutParams(param4);

        updateViews(dot1);
        updateViews(dot2);
        updateViews(dot3);
        updateViews(dot4);

    }
}
