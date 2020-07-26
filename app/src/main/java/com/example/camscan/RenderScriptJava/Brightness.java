package com.example.camscan.RenderScriptJava;

import android.content.Context;
import android.graphics.Bitmap;

import android.renderscript.Allocation;
import android.renderscript.RenderScript;

import com.example.camscan.ScriptC_brightness;

public class Brightness {
    RenderScript rs;
    public Brightness(Context context){
        rs=RenderScript.create(context);
    }

    public Bitmap setBrightness(Bitmap image,int C){


        ScriptC_brightness script=new ScriptC_brightness(rs);
        Bitmap result=Bitmap.createBitmap(image);
        Allocation tmpIn=Allocation.createFromBitmap(rs,image);
        Allocation tmpOut=Allocation.createFromBitmap(rs,result);

        script.invoke_setValue(C);
        script.forEach_setBrightness(tmpIn,tmpOut);

        tmpOut.copyTo(result);

        script.destroy();
        tmpIn.destroy();
        tmpOut.destroy();
        return result;

    }
    public void clear(){
        rs.destroy();
    }

}
