#pragma version(1)
#pragma rs_fp_relaxed
#pragma rs java_package_name(com.example.camscan)

static float factor=0.f;

void setFactor(float f){
    factor=f;
}

void setContrast(const uchar4 *in,uchar4 *out){
    out->r=clamp((int)(factor*(in->r-128)+128),0,255);
    out->g=clamp((int)(factor*(in->g-128)+128),0,255);
    out->b=clamp((int)(factor*(in->b-128)+128),0,255);

}