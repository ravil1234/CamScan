package com.example.camscan.Activities;
import  androidx.appcompat.app.AppCompatActivity;
import  android.graphics.Bitmap;
import  android.graphics.BitmapFactory;
import  android.os.Bundle;
import android.util.Log;
import  android.widget.ImageView;
import com.example.camscan.ObjectClass.BitmapObject;
import com.example.camscan.R;
import com.squareup.picasso.Picasso;

public class CapturedImageActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_captured_image);
        ImageView imageView =findViewById(R.id.imageView);
//        Bundle extras = getIntent().getExtras();
//        assert extras != null;
//        byte[] byteArray = extras.getByteArray("captured_image");
//        Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray != null ? byteArray.length : 0);

//        if(BitmapObject.bitmap_image!=null)
//        {
//            imageView.setImageBitmap(BitmapObject.bitmap_image);
//            Log.d("resolution",BitmapObject.bitmap_image.getHeight()+" * "+BitmapObject.bitmap_image.getWidth());
//        }
        Log.d("image_uri","-> "+getIntent().getStringExtra("uri"));
        Bitmap myBitmap = BitmapFactory.decodeFile(getIntent().getStringExtra("uri"));
        imageView.setImageBitmap(myBitmap);
      // Picasso.with(this).load(getIntent().getStringExtra("uri")).into(imageView);
          BitmapObject.bitmap_image=null;
    }
}
