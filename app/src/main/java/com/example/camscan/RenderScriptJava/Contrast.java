package com.example.camscan.RenderScriptJava;

import android.content.Context;
import android.graphics.Bitmap;

import android.renderscript.Allocation;
import android.renderscript.RenderScript;

import com.example.camscan.ScriptC_contrast;

public class Contrast {
    RenderScript rs;
    public Contrast(Context context){
        rs=RenderScript.create(context);
    }

    public Bitmap setContrast(Bitmap image,int C){
        float factor=getFactor(C);
        ScriptC_contrast script=new ScriptC_contrast(rs);

        Bitmap result=Bitmap.createBitmap(image);
        Allocation tmpIn=Allocation.createFromBitmap(rs,image);
        Allocation tmpOut=Allocation.createFromBitmap(rs,result);

        script.invoke_setFactor(factor);
        script.forEach_setContrast(tmpIn,tmpOut);

        tmpOut.copyTo(result);

        tmpIn.destroy();
        tmpOut.destroy();
        script.destroy();
        return result;

    }
    public void clear(){
        rs.destroy();
    }

    private float getFactor(int c) {

        return (259*((float)c+255))/(255*(259-(float)c));
    }
}
