package com.example.camscan.RenderScriptJava;
import android.content.Context;
import android.graphics.Bitmap;
import androidx.renderscript.Allocation;
import androidx.renderscript.RenderScript;
import com.example.camscan.ScriptC_invert;
public class Inversion {

    RenderScript rs;
    public Inversion(Context context){
        rs=RenderScript.create(context);
    }

    public Bitmap setInversion(Bitmap image){

        ScriptC_invert script=new ScriptC_invert(rs);

        Bitmap result=Bitmap.createBitmap(image);
        Allocation tmpIn=Allocation.createFromBitmap(rs,image);
        Allocation tmpOut=Allocation.createFromBitmap(rs,result);


        script.forEach_invert(tmpIn,tmpOut);

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
