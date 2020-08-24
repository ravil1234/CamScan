package com.example.camscan.RenderScriptJava;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;

import com.example.camscan.ScriptC_BCE;
import com.example.camscan.ScriptC_FlatCorrection;

import static androidx.camera.core.CameraXThreads.TAG;

public class FlatCorrection {

    RenderScript rs;
    private static final float BLUR_RADIUS=25f;
    //private static final float BITMAP_SCALE=0.4f;
    public FlatCorrection(Context context){
        rs=RenderScript.create(context);

    }

    public static float[] getAverageColorRGB(Bitmap bitmap) {
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();
        int size = width * height;
        int pixelColor;
        float r, g, b;
        r = g = b = 0f;
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                pixelColor = bitmap.getPixel(x, y);
                if (pixelColor == 0) {
                    size--;
                    continue;
                }
                r += Color.red(pixelColor);
                g += Color.green(pixelColor);
                b += Color.blue(pixelColor);
            }
        }
        r /= size;
        g /= size;
        b /= size;
        return new float[] {
                r, g, b
        };
    }

    public Bitmap flatCorr(Bitmap image){
        //Blurring
  //      int width=image.getWidth();
//        int height=image.getHeight();

        Bitmap output=Bitmap.createScaledBitmap(image,(int)(image.getWidth()*0.3),(int)(image.getHeight()*0.3),false);
        Bitmap input=Bitmap.createBitmap(output);
        ScriptIntrinsicBlur script=ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

        Allocation tmpIn=Allocation.createFromBitmap(rs,input);
        Allocation tmpOut=Allocation.createFromBitmap(rs,output);

        script.setRadius(BLUR_RADIUS);
        script.setInput(tmpIn);
        script.forEach(tmpOut);

        tmpOut.copyTo(output);

        Bitmap output2=Bitmap.createScaledBitmap(output,image.getWidth(),image.getHeight(),false);


        float[] mean=getAverageColorRGB(output);

      //  Log.e(TAG, "flatCorr: "+mean[0]+" "+mean[1]+" "+mean[2] );
        //output is blurred mean is calculated


        ScriptC_FlatCorrection script2=new ScriptC_FlatCorrection(rs);
        Bitmap corrected=Bitmap.createBitmap(image);

        tmpIn=Allocation.createFromBitmap(rs,output2);
    //        Allocation tmp2In=Allocation.createFromBitmap(rs,output);
        tmpOut=Allocation.createFromBitmap(rs,image);

        script2.invoke_setMean(mean[0],mean[1],mean[2]);
        script2.forEach_applyFilter(tmpIn,tmpOut);


        tmpOut.copyTo(corrected);
        script.destroy();
        script2.destroy();


        ScriptC_BCE script3=new ScriptC_BCE(rs);

       // Bitmap res=Bitmap.createBitmap(image);

        tmpIn=Allocation.createFromBitmap(rs,corrected);
        tmpOut=Allocation.createTyped(rs,tmpIn.getType());

//        int AvgMean=(int)((mean[0]+mean[1]+mean[2])/3);
//        if(AvgMean<160){
//            script3.invoke_setVals(getFactor(80),70,60);
//        }else{
            script3.invoke_setVals(getFactor(70),60,-15);
        //}

        script3.forEach_Evaluate(tmpIn,tmpOut);

        tmpOut.copyTo(image);

        tmpIn.destroy();
        tmpOut.destroy();
        script3.destroy();
        return image;





        //return corrected;

        /*
        ScriptC_contrast script3=new ScriptC_contrast(rs);
        int width=image.getWidth();
        int height=image.getHeight();

        script3.set_size(width*height);
        script3.forEach_root(tmpIn,tmpOut);
        script3.invoke_createRemapArray();
        script3.forEach_remaptoRGB(tmpOut,tmpIn);

        tmpIn.copyTo(res);
        tmpIn.destroy();
        tmpOut.destroy();
        script3.destroy();

        return res;
       // return output2;
    */

    }

    public void clear(){
        rs.destroy();


    }

    private float getFactor(int c) {

        return (259*((float)c+255))/(255*(259-(float)c));
    }

}
