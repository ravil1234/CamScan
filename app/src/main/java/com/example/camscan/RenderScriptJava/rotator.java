package com.example.camscan.RenderScriptJava;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;

import ua.kulku.rs.ScriptC_rotator;

public class rotator {

    Context context;
    RenderScript rs=null;
    public rotator(Context c){
        context=c;
        rs=RenderScript.create(c);
    }
    public Bitmap rotate(Bitmap bitmap,boolean isClockwise) {
        ScriptC_rotator script = new ScriptC_rotator(rs);
        script.set_inWidth(bitmap.getWidth());
        script.set_inHeight(bitmap.getHeight());
        Allocation sourceAllocation = Allocation.createFromBitmap(rs, bitmap,
                Allocation.MipmapControl.MIPMAP_NONE,
                Allocation.USAGE_SCRIPT);
        bitmap.recycle();
        script.set_inImage(sourceAllocation);

        int targetHeight = bitmap.getWidth();
        int targetWidth = bitmap.getHeight();
        Bitmap.Config config = bitmap.getConfig();
        Bitmap target = Bitmap.createBitmap(targetWidth, targetHeight, config);
        final Allocation targetAllocation = Allocation.createFromBitmap(rs, target,
                Allocation.MipmapControl.MIPMAP_NONE,
                Allocation.USAGE_SCRIPT);
        if(!isClockwise){
            script.forEach_rotate_90_clockwise(targetAllocation, targetAllocation);
        }
        else{
            script.forEach_rotate_270_clockwise(targetAllocation,targetAllocation);
        }
        targetAllocation.copyTo(target);
        script.destroy();
        rs.destroy();
        return target;
    }
}
