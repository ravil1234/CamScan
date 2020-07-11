package com.example.camscan.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.camscan.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class BoxActivity extends AppCompatActivity {

    private static final String TAG = "actovity";
    ImageView imageView;
    FloatingActionButton nextBtn;

    MyBoxLayout boundingBox;

    Bitmap image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box);

        initializeViews();
        getSupportActionBar().hide();

     //   byte[] byteArray=getIntent().getByteArrayExtra("image");
//        image=BitmapFactory.decodeByteArray(byteArray,0,byteArray.length);
        if(image==null){
            image=BitmapFactory.decodeResource(getResources(),R.drawable.test_img);
        }

        boundingBox.setBitmap(image);

        adjustBoudningBoxParams();


        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<Point> points=boundingBox.getCorners();
                int flag=0;
                for(Point p :points){
                    if(p.x<0 ||p.y<0){
                        flag=1;
                        break;
                    }
                }
                if(flag==0) {
                    /*
                    // First decode with inJustDecodeBounds=true to check dimensions
                    final BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeResource(getResources(), R.drawable.test_img, options);

                    // Calculate inSampleSize

                    options.inSampleSize = calculateInSampleSize(options, imageView.getWidth(), imageView.getHeight());
                    Log.e(TAG, "onClick: " + imageView.getWidth() + " " + imageView.getHeight());
                    // Decode bitmap with inSampleSize set
                    options.inJustDecodeBounds = false;

                    image = BitmapFactory.decodeResource(BoxActivity.this.getResources(), R.drawable.test_img, options);
                    image = Bitmap.createScaledBitmap(image, imageView.getWidth(), imageView.getHeight(), true);
                    */
                    imageView.setImageBitmap(image);
                    if (image != null) {
                        Log.e("THIS", "onClick: " + "HERE");
                        Bitmap trns = cornerPin(image, points);

                        Bitmap emptyBitmap = Bitmap.createBitmap(trns.getWidth(), trns.getHeight(),
                                trns.getConfig());

                        if(trns.sameAs(emptyBitmap)){
                            Toast.makeText(BoxActivity.this, "Can't Crop", Toast.LENGTH_SHORT).show();
                        }else{

                            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                            imageView.setImageBitmap(trns);

                        }


                    }
                }else{
                    Toast.makeText(BoxActivity.this, "Co-ordinates out of Bounds", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void adjustBoudningBoxParams() {
        //RelativeLayout.LayoutParams params=(RelativeLayout.LayoutParams)boundingBox.getLayoutParams();


    }

    private void initializeViews() {
        imageView=findViewById(R.id.box_img_view);
        nextBtn=findViewById(R.id.box_next_btn);
        boundingBox=findViewById(R.id.box_bounding_box);
    }
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
    public Bitmap cornerPin(Bitmap B,ArrayList<Point> dis){
        int w=B.getWidth();
        int h=B.getHeight();

        int ImgWidth=imageView.getWidth();
        int imgHeight=imageView.getHeight();

        float widthRatio=(float)w/(float)ImgWidth;
        float heightRatio=(float)h/(float) imgHeight;

        int x1=(int)(dis.get(0).x*widthRatio);
        int y1=(int)(dis.get(0).y*heightRatio);

        int x2=(int)(dis.get(1).x*widthRatio);
        int y2=(int)(dis.get(1).y*heightRatio);

        int x3=(int)(dis.get(2).x*widthRatio);
        int y3=(int)(dis.get(2).y*heightRatio);

        int x4=(int)(dis.get(3).x*widthRatio);
        int y4=(int)(dis.get(3).y*heightRatio);

        int minLeft=Math.min(Math.min(x1,x2),Math.min(x3,x4));
        int maxRight=Math.max(Math.max(x1,x2),Math.max(x3,x4));
        int minTop=Math.min(Math.min(y1,y2),Math.min(y3,y4));
        int maxBottom=Math.max(Math.max(y1,y2),Math.max(y3,y4));

        int wid=maxRight-minLeft;
        int hei=maxBottom-minTop;

        float[] src = {
                minLeft, minTop, // Coordinate of top left point
                minLeft, maxBottom, // Coordinate of bottom left point
                maxRight, maxBottom, // Coordinate of bottom right point
                maxRight,minTop  // Coordinate of top right point
        };


        float[] dst = {
                x1,y1,//dis.get(0).x, dis.get(0).y,        // Desired coordinate of top left point
                x4,y4,//dis.get(3).x, dis.get(3).y,        // Desired coordinate of bottom left point
                x3,y3,//dis.get(2).x, dis.get(2).y, // Desired coordinate of bottom right point
                x2,y2//dis.get(1).x, dis.get(1).y  // Desired coordinate of top right point
        };


        Log.e(TAG, "cornerPin: "+dis );
        Bitmap result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        Canvas c = new Canvas(result);
        Matrix m = new Matrix();
        m.setPolyToPoly(dst, 0, src, 0, 4);
        c.setMatrix(m);
        c.drawBitmap(B, 0,0, p);

        Bitmap resized=Bitmap.createBitmap(result,minLeft,minTop,wid,hei);
//        Log.e(TAG, "cornerPin: "+ result.getHeight()+" "+result.getWidth());
        Log.e(TAG, "cornerPin: "+resized.getHeight()+" "+resized.getWidth() );
        return resized;

    }

    public void rotateBitmapClockWise(View view){
        Matrix matrix=new Matrix();
        matrix.preRotate(90);
        image=Bitmap.createBitmap(image,0,0,image.getWidth(),image.getHeight(),matrix,true);

        boundingBox.setBitmap(image);
        imageView.setImageBitmap(image);
    }
    public void rotateBitmapAntiClockWise(View view){
        Matrix matrix=new Matrix();
        matrix.preRotate(-90);
        image=Bitmap.createBitmap(image,0,0,image.getWidth(),image.getHeight(),matrix,true);
        boundingBox.setBitmap(image);
        imageView.setImageBitmap(image);
        boundingBox.resetPoints();
    }
}
