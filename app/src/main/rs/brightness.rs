#pragma version(1)
#pragma rs_fp_relaxed
#pragma rs java_package_name(com.example.camscan)

static int val=0;

void setValue(int v){
    val=v;

}

void setBrightness(const uchar4 *in,uchar4 *out){
    out->r=clamp((int)(in->r+val),0,255);
    out->g=clamp((int)(in->g+val),0,255);
    out->b=clamp((int)(in->b+val),0,255);

}