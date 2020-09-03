#pragma version(1)
#pragma rs java_package_name(com.example.camscan)
#pragma rs_fp_relaxed


// Use two global counters
static int totalSumR = 0;
static int totalSumG = 0;
static int totalSumB = 0;
static int counter = 0;

// One kernel just sums up the channel red value and increments
// the global counter by 1 for each pixel
void __attribute__((kernel)) addChannel(uchar4 in){

 rsAtomicAdd(&totalSumR, in.r);
 rsAtomicAdd(&totalSumG, in.g);
 rsAtomicAdd(&totalSumB, in.b);
 rsAtomicInc(&counter);

}

// This kernel places, inside the output allocation, the average
float __attribute__((kernel)) getTotalSum(int x){
    return (float)totalSumR/(float)counter;
}
float __attribute__((kernel)) getTotalSum2(int x){
    return (float)totalSumG/(float)counter;
}
float __attribute__((kernel)) getTotalSum3(int x){
    return (float)totalSumB/(float)counter;
}

void resetCounters(){

    totalSumR = 0;
    totalSumG = 0;
    totalSumB = 0;
    counter = 0;

}