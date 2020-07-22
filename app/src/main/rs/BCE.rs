#pragma version(1)
#pragma rs_fp_relaxed
#pragma rs java_package_name(com.example.camscan)

static float Cfactor=0.f;
static float Expo=0.f;
static int bri=0;
void setVals(float f,float v,int br){
    Cfactor=f;
    Expo=255.f/(255.f-v);
    bri=br;
}

void Evaluate(const uchar4 *in,uchar4 *out){
    int r;
    int g;
    int b;

    r=clamp((int)(bri+in->r),0,255);
    r=clamp((int)(Cfactor*(r-128)+128),0,255);
    out->r=clamp((int)(Expo*r),0,255);

    g=clamp((int)(bri+in->g),0,255);
    g=clamp((int)(Cfactor*(g-128)+128),0,255);
    out->g=clamp((int)(Expo*g),0,255);

    b=clamp((int)(bri+in->b),0,255);
    b=clamp((int)(Cfactor*(b-128)+128),0,255);
    out->b=clamp((int)(Expo*b),0,255);

}

