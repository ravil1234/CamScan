#pragma version(1)
#pragma rs java_package_name(com.example.camscan)
#pragma rs_fp_full

static float bright=0.f;
void setBright(float v){
    bright=255.f/(255.f-v);
}

void exposure(const uchar4 *in,uchar4 *out){
    out->r=clamp((int)(bright*in->r),0,255);
    out->g=clamp((int)(bright*in->g),0,255);
    out->b=clamp((int)(bright*in->b),0,255);

}