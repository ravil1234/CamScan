#pragma version(1)
#pragma rs_fp_relaxed
#pragma rs java_package_name(com.example.camscan)



void invert(const uchar4 *in,uchar4 *out){
    out->r=clamp((int)(255-in->r),0,255);
    out->g=clamp((int)(255-in->g),0,255);
    out->b=clamp((int)(255-in->b),0,255);

}