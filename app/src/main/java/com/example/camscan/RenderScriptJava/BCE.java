package com.example.camscan.RenderScriptJava;

import android.content.Context;
import android.graphics.Bitmap;

import android.renderscript.Allocation;
import android.renderscript.RenderScript;

import com.example.camscan.ScriptC_BCE;

public class BCE {

    RenderScript rs;
    public BCE(Context context){
        rs=RenderScript.create(context);
    }

    public Bitmap setBCE(Bitmap image,int b,int c,int e){

        float Cfactor=getFactor(c);
       // Log.e(TAG, "setBCE: "+Cfactor );

        ScriptC_BCE script=new ScriptC_BCE(rs);
        Bitmap result=Bitmap.createBitmap(image);
        Allocation tmpIn=Allocation.createFromBitmap(rs,image);
        Allocation tmpOut=Allocation.createFromBitmap(rs,result);

        script.invoke_setVals(Cfactor,e,b);

        script.forEach_Evaluate(tmpIn,tmpOut);

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
