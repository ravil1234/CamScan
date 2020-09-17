package com.example.camscan.RenderScriptJava;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;



import android.util.Log;

import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Script;
import androidx.renderscript.ScriptIntrinsicBlur;

import com.example.camscan.ScriptC_BCE;
import com.example.camscan.ScriptC_FlatCorrection;
import com.example.camscan.ScriptC_mean;


public class FlatCorrection {

    RenderScript rs;
    private static final float BLUR_RADIUS=25f;
    //private static final float BITMAP_SCALE=0.4f;
    public FlatCorrection(Context context){
        rs=RenderScript.create(context);

    }

    public Bitmap flatCorr(Bitmap image){
        Log.e("FLAT", "flatCorr: in heere" );
        Bitmap input=Bitmap.createBitmap(image);
        Bitmap scaled=Bitmap.createScaledBitmap(image,(int)(image.getWidth()*0.3),(int)(image.getHeight()*0.3),false);
        Bitmap output=Bitmap.createBitmap(scaled);
        ScriptIntrinsicBlur scriptIntrinsicBlur=ScriptIntrinsicBlur.create(rs,Element.U8_4(rs));
        Allocation tmpIn=Allocation.createFromBitmap(rs,scaled);
        Allocation tmpOut=Allocation.createFromBitmap(rs,output);

        scriptIntrinsicBlur.setRadius(BLUR_RADIUS);
        scriptIntrinsicBlur.setInput(tmpIn);
        scriptIntrinsicBlur.forEach(tmpOut);
        tmpOut.copyTo(output);
        if(scaled!=null){
            scaled.recycle();
            scaled=null;
        }
        output=Bitmap.createScaledBitmap(output,image.getWidth(),image.getHeight(),true);
        scriptIntrinsicBlur.destroy();
        tmpIn=Allocation.createFromBitmap(rs,output);

        Allocation sumAllocation = Allocation.createSized(rs, Element.F32(rs), 1);
        Allocation sumAllocation2 = Allocation.createSized(rs, Element.F32(rs), 1);
        Allocation sumAllocation3 = Allocation.createSized(rs, Element.F32(rs), 1);


        ScriptC_mean meanS=new ScriptC_mean(rs);

        meanS.forEach_addChannel(tmpIn);
        meanS.forEach_getTotalSum(sumAllocation);
        float sumArray[] = new float[1];
        sumAllocation.copyTo(sumArray);

        meanS.forEach_getTotalSum2(sumAllocation2);
        float sumArray2[] = new float[1];
        sumAllocation2.copyTo(sumArray2);

        meanS.forEach_getTotalSum3(sumAllocation3);
        float sumArray3[] = new float[1];
        sumAllocation3.copyTo(sumArray3);

        meanS.destroy();
        float [] mean={sumArray[0],sumArray2[0],sumArray3[0]};
        float avg=(sumArray[0]+sumArray2[0]+sumArray3[0])/3;
        if(Math.abs(sumArray[0]-sumArray2[0])>5) {
            sumArray[0]=sumArray2[0]-5;
        }
        if(Math.abs(sumArray2[0]-sumArray3[0])>5){
            sumArray3[0]=sumArray2[0]+5;
        }

            String m="";
        boolean isBlack=true;
        int c=0;
        for (int i=0;i<3;i++){
            m+=mean[i]+" ";
            if(mean[i]>127){
                c++;
            }else{
                int dif=(int)(150-mean[i]);
                mean[i]+=dif;
            }
        }
        if(c>=2){
            isBlack=false;
        }
        Log.e("THIS", "flatCorr: "+m+" "+c );


        ScriptC_FlatCorrection script2=new ScriptC_FlatCorrection(rs);
       // Bitmap corrected=Bitmap.createBitmap(image);

        tmpIn=Allocation.createFromBitmap(rs,output);
        //        Allocation tmp2In=Allocation.createFromBitmap(rs,output);
        tmpOut= Allocation.createFromBitmap(rs,input);

        script2.invoke_setMean(mean[0],mean[1],mean[2]);
        script2.forEach_applyFilter(tmpIn,tmpOut);


        tmpOut.copyTo(input);
        script2.destroy();
        if(output!=null){
            output.recycle();
            output=null;
        }

        //flat corrected now post processing
        ScriptC_BCE script3=new ScriptC_BCE(rs);

        Bitmap res=Bitmap.createBitmap(image);

        tmpIn=Allocation.createFromBitmap(rs,input);
        tmpOut=Allocation.createTyped(rs,tmpIn.getType());
        script3.invoke_setVals(getFactor(150),185,-30);
//        if(isBlack){
//            script3.invoke_setVals(getFactor(120),100,50);
//        }else{
//
//            script3.invoke_setVals(getFactor(100),50,20);
//        }

        script3.forEach_Evaluate(tmpIn,tmpOut);

        tmpOut.copyTo(res);

        tmpIn.destroy();
        tmpOut.destroy();
        script3.destroy();

        return res;

    }




    public void clear(){
        rs.destroy();
    }

    private float getFactor(int c) {
        return (259*((float)c+255))/(255*(259-(float)c));
    }

}
