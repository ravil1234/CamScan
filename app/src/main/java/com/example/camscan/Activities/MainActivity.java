package com.example.camscan.Activities;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import com.example.camscan.Adapters.InDocRecyclerAdapter;
import com.example.camscan.Database.MyDatabase;
import com.example.camscan.Objects.MyDocument;
import com.example.camscan.Objects.MyPicture;
import com.example.camscan.R;
import com.example.camscan.UtilityClass;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

      //  Bitmap image= BitmapFactory.decodeResource(getResources(),R.drawable.test_img);
       // ByteArrayOutputStream bstream=new ByteArrayOutputStream();
       // image.compress(Bitmap.CompressFormat.PNG,100,bstream);
        //byte[] byteArray=bstream.toByteArray();

        Intent intent=new Intent(MainActivity.this, BoxActivity.class);
        //intent.putExtra("image",byteArray);
        ArrayList<MyPicture> list=new ArrayList<>();
        list.add(new MyPicture(0,"file:///storage/emulated/0/CamScan/.original/159514406622666226.jpg",null,"01",1,null));
//        list.add(new MyPicture(0,"file:///storage/emulated/0/CamScan/.original/159514406650066500.jpg",null,"02",2,null));
//        list.add(new MyPicture(0,"file:///storage/emulated/0/CamScan/.original/1595087242244242244.jpg",null,"03",3,null));
//        list.add(new MyPicture(0,"file:///storage/emulated/0/CamScan/.original/1595133729167729167.jpg",null,"04",4,null));
        String myPics=UtilityClass.getStringFromObject(list);
        String myDoc= UtilityClass.getStringFromObject(new MyDocument("NAME",System.currentTimeMillis(),0,1,null));
        Log.e("THIS", "onCreate: "+myDoc );
        Log.e("THIS", "onCreate: "+myPics );

        intent.putExtra("MyPicture",myPics);
        intent.putExtra("MyDocument",myDoc);
        startActivity(intent);

    }
}
