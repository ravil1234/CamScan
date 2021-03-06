package com.example.camscan.RenderScriptJava;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import com.example.camscan.ScriptC_BlackNdWhite;
public class BlackAndWhite {

    RenderScript rs;
    public BlackAndWhite(Context context){
        rs=RenderScript.create(context);
    }

    public Bitmap getBlackAndWhite(Bitmap image){

        Bitmap res=image.copy(Bitmap.Config.ARGB_8888,true);
        int width=image.getWidth();
        int height=image.getHeight();

        for(int i=0;i<height;i++){
            for(int j=0;j<width;j++){
                int color=image.getPixel(j,i);
                int r= Color.red(color);
                int g= Color.green(color);
                int b= Color.blue(color);

                float[] hsv=new float[3];
                Color.RGBToHSV(r,g,b,hsv);

                hsv[0]=0;
                hsv[1]=0;

                if(hsv[2]>0.4){
                    hsv[2]=1.f;
                }else{
                    hsv[2]=0;
                }
                int newColor=Color.HSVToColor(hsv);
                res.setPixel(j,i,newColor);

            }
        }
        return res;
    }


    public Bitmap toBnW(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpBnW = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpBnW);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);

        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpBnW;
    }

    public Bitmap toBnwRender(Bitmap image){
        Bitmap output=Bitmap.createBitmap(image);
        ScriptC_BlackNdWhite script=new ScriptC_BlackNdWhite(rs);

        Allocation tmpIn=Allocation.createFromBitmap(rs,image);
        Allocation tmpOut=Allocation.createFromBitmap(rs,output);

        script.invoke_setThresh(0.4f);
        script.forEach_applyBWFilter(tmpIn,tmpOut);
        tmpOut.copyTo(output);

        script.destroy();
        tmpIn.destroy();
        tmpOut.destroy();
        return output;
    }
    public void clear(){
        rs.destroy();
    }
}
