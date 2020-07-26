package com.example.camscan.MyLayouts;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.camscan.R;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class MyBoxLine extends View {

    Context context;
    int viewWidth;
    int viewHeight;

    int x1,x2,y1,y2;
    public MyBoxLine(Context context) {
        super(context);
        this.context=context;
        init(null);


    }

    public MyBoxLine(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context=context;
        init(attrs);
    }

    public MyBoxLine(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context=context;
        init(attrs);
    }

    public MyBoxLine(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context=context;
        init(attrs);
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld){
        super.onSizeChanged(xNew, yNew, xOld, yOld);

        viewWidth = xNew;
        viewHeight = yNew;
    }


    public void init(@Nullable AttributeSet set){
       // icon=BitmapFactory.decodeResource(context.getResources(),R.drawable.dot_draggable);
        TypedArray a=context.getTheme().obtainStyledAttributes(
                set,
                R.styleable.MyBoxLine,
                0,0
        );
        try{
            x1=a.getInteger(R.styleable.MyBoxLine_setX1,0);
            x2=a.getInteger(R.styleable.MyBoxLine_setX2,0);
            y1=a.getInteger(R.styleable.MyBoxLine_setY1,0);
            y2=a.getInteger(R.styleable.MyBoxLine_setY2,0);

        }finally {
            a.recycle();
        }

    }

    public void setX1(int x1){
        this.x1=x1;
        invalidate();
    }
    public void setX2(int x2){
        this.x2=x2;
        invalidate();
    }
    public void setY1(int y1){
        this.y1=y1;
        invalidate();
    }
    public void setY2(int y2){
        this.y2=y2;
        invalidate();
    }
    @Override
    protected void onDraw(Canvas canvas) {

    Paint  p=new Paint();
    p.setColor(Color.GREEN);
    p.setStyle(Paint.Style.STROKE);
    p.setStrokeWidth(4);

    canvas.drawLine(x1,y1,x2,y2,p);


    }
}
