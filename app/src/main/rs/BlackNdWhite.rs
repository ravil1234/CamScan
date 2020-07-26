#pragma version(1)
#pragma rs java_package_name(com.example.camscan)
#pragma rs_fp_full

static float threshold=0.f;

void setThresh(float thresh){
    threshold=thresh;

}

void applyBWFilter(const uchar4 *in, uchar4 *out){

    int maxim=0;
    if(in->r>in->g){
        if(in->r>in->b){
            maxim=in->r;
        }else{
            maxim=in->b;
        }

    }else{
        if(in->g>in->b){
        maxim=in->g;
        }else{
            maxim=in->b;
        }
    }
    float val=maxim/255.f;

    if(val>threshold){
      //  val=1.f;
        out->r=255;
        out->g=255;
        out->b=255;
    }
    else{
        out->r=0;
        out->g=0;
        out->b=0;
    }





}