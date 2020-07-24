package com.example.camscan.RenderScriptJava;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;

import com.example.camscan.ScriptC_filter1;

public class Filter1 {
    private Context context;
    RenderScript rs;
    ScriptC_filter1 script;
    public Filter1(Context context){
        this.context=context;

        rs=RenderScript.create(context);
        script=new ScriptC_filter1(rs);
    }


    public Bitmap filter(float exp, Bitmap image){
        Bitmap output=Bitmap.createBitmap(image);

        Allocation tmpIn=    Allocation.createFromBitmap(rs,image);
        Allocation tmpOut= Allocation.createFromBitmap(rs,output);

        script.invoke_setBright(exp);
        script.forEach_exposure(tmpIn,tmpOut);


        tmpOut.copyTo(output);
        tmpIn.destroy();
        tmpOut.destroy();
        //script.destroy();
        return output;
    }

    public void cleanUp(){
        rs.destroy();
        script.destroy();
    }


}
