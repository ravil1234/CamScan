package com.example.camscan.Activities;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.camscan.R;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class MyBoxLayout extends RelativeLayout {

    ImageView dot1,dot2,dot3,dot4,dot12,dot23,dot34,dot14;
    View rect1;
    int x_delta;
    int y_delta;

    public MyBoxLayout(Context context) {
        super(context);
    }

    public MyBoxLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater= LayoutInflater.from(context);
        View view=inflater.inflate(R.layout.layout_box_my,this);

        dot1=view.findViewById(R.id.my_box_dot1);
        dot2=view.findViewById(R.id.my_box_dot2);
        dot3=view.findViewById(R.id.my_box_dot3);
        dot4=view.findViewById(R.id.my_box_dot4);
        dot12=view.findViewById(R.id.my_box_dot12);
        dot23=view.findViewById(R.id.my_box_dot23);
        dot34=view.findViewById(R.id.my_box_dot34);
        dot14=view.findViewById(R.id.my_box_dot14);
        rect1=view.findViewById(R.id.my_box_rect12);

//        RelativeLayout.LayoutParams params=(RelativeLayout.LayoutParams)view.getLayoutParams();
      //  Log.e("THIS", "MyBoxLayout: "+params.height );
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                view.getHeight(); //height is ready
                initViewPos(view.getHeight(),view.getWidth());

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

    private void initViewPos(int height, int width) {
        Log.e(TAG, "initViewPos: "+height+" "+width );
        RelativeLayout.LayoutParams param1=(RelativeLayout.LayoutParams)dot1.getLayoutParams();
        RelativeLayout.LayoutParams param2=(RelativeLayout.LayoutParams)dot2.getLayoutParams();
        RelativeLayout.LayoutParams param3=(RelativeLayout.LayoutParams)dot3.getLayoutParams();
        RelativeLayout.LayoutParams param4=(RelativeLayout.LayoutParams)dot4.getLayoutParams();

        param1.leftMargin=50;
        param1.topMargin=50;

        param2.leftMargin=width-dot2.getWidth()-50;
        param2.bottomMargin=height-dot2.getHeight()-50;

        param3.leftMargin=width-dot3.getWidth()-50;
        param3.topMargin=height-dot3.getHeight()-50;

        param4.topMargin=height-dot4.getHeight()-50;
        param4.rightMargin=width-dot4.getWidth()-50;

        dot2.setLayoutParams(param2);
        dot3.setLayoutParams(param3);
        dot4.setLayoutParams(param4);

        updateViews(dot1);
        updateViews(dot2);
        updateViews(dot3);
        updateViews(dot4);

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
                    params.leftMargin=X-x_delta;
                    params.topMargin=Y-y_delta;
                    params.rightMargin=-250;
                    params.bottomMargin=-250;
                    view.setLayoutParams(params);
                    updateViews(view);
                    break;
                }


            }


            return true;
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

                            params.bottomMargin=-250;

                            param1.topMargin-=diff;
                            dot1.setLayoutParams(param1);

                            param2.topMargin-=diff;
                            dot2.setLayoutParams(param2);
                            view.setLayoutParams(params);

                            updateViews(dot1);
                            updateViews(dot2);
                            break;
                        }
                        case R.id.my_box_dot23:{
                            int diff=params.leftMargin;

                            params.leftMargin=X-x_delta;
                            params.rightMargin=-250;
                            diff-=params.leftMargin;

                            param2.leftMargin-=diff;
                            dot2.setLayoutParams(param2);

                            param3.leftMargin-=diff;
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

                            params.bottomMargin=-250;

                            param3.topMargin-=diff;
                            dot3.setLayoutParams(param3);

                            param4.topMargin-=diff;
                            dot4.setLayoutParams(param4);
                            view.setLayoutParams(params);

                            updateViews(dot3);
                            updateViews(dot4);
                            break;
                        }
                        case R.id.my_box_dot14:{
                            int diff=params.leftMargin;

                            params.leftMargin=X-x_delta;

                            params.rightMargin=-250;

                            diff-=params.leftMargin;

                            param1.leftMargin-=diff;
                            dot1.setLayoutParams(param1);

                            param4.leftMargin-=diff;
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
        }
    }


}
