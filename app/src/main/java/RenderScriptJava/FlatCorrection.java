package RenderScriptJava;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Script;
import androidx.renderscript.ScriptIntrinsicBlur;

import com.example.camscan.ScriptC_FlatCorrection;
import com.example.camscan.ScriptC_contrast;
import com.example.camscan.ScriptC_filter1;

import static androidx.constraintlayout.widget.Constraints.TAG;

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

    //    return correction(image,output2,mean);

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


        ScriptC_filter1 script3=new ScriptC_filter1(rs);

        Bitmap res=Bitmap.createBitmap(image);

        tmpIn=Allocation.createFromBitmap(rs,corrected);
        tmpOut=Allocation.createTyped(rs,tmpIn.getType());

        script3.invoke_setBright(130);
        script3.forEach_exposure(tmpIn,tmpOut);

        tmpOut.copyTo(res);

        tmpIn.destroy();
        tmpOut.destroy();
        script3.destroy();
        return res;





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

}
