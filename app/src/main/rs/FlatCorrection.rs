#pragma version(1)
#pragma rs java_package_name(com.example.camscan)
#pragma rs_fp_full

static float rM=0.f;
static float gM=0.f;
static float bM=0.f;


void setMean(float red,float green,float blue){
    rM=red;
    gM=green;
    bM=blue;
}

void applyFilter(const uchar4 *in, uchar4 *out){

    out->r=clamp((int)((out->r * rM)/in->r),0,255);
    out->g=clamp((int)((out->g * gM)/in->g),0,255);
    out->b=clamp((int)((out->b * bM)/in->b),0,255);

}