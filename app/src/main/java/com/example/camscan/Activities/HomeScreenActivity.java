package com.example.camscan.Activities;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.example.camscan.AdapterClass.GridViewImages;
import com.example.camscan.AdapterClass.ListViewImages;
import com.example.camscan.Database.MyDatabase;
import com.example.camscan.ObjectClass.GridViewImagesList;
import com.example.camscan.Objects.MyDocument;
import com.example.camscan.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
public class HomeScreenActivity extends AppCompatActivity {

    RecyclerView recyclerViewlayout;
    List<GridViewImagesList> gridViewImagesListList;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        gridViewImagesListList=new ArrayList<>();
        recyclerViewlayout=findViewById(R.id.recycler_view_layout);
         set_image_list();
    }
    private void set_image_list()
    {
       List<MyDocument> myDocuments= MyDatabase.getInstance(this).myDocumentDao().getAllDocs();
       for(MyDocument myDocument:myDocuments)
       {

            int did=myDocument.getDid();
            String name=myDocument.getdName();
            long date=myDocument.getTimeCreated();
            int pcount=myDocument.getpCount();
            String fp_uri=myDocument.getfP_URI();
           long date_edited=myDocument.getTimeEdited();
           gridViewImagesListList.add(new GridViewImagesList(did,fp_uri,dateformatter(date),pcount,dateformatter(date_edited)));
       }
        GridLayoutManager mGridLayoutManager = new GridLayoutManager(HomeScreenActivity.this, 3);
        recyclerViewlayout.setLayoutManager(mGridLayoutManager);
        GridViewImages myAdapter = new  GridViewImages(HomeScreenActivity.this, gridViewImagesListList,new MyClickListener());
        recyclerViewlayout.setAdapter(myAdapter);

    }
    public  String dateformatter(long timestamp)
    {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return  formatter.format(new Date(timestamp));
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_bar_homescreen, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
        case R.id.list:
            //add the function to perform here
            recyclerViewlayout.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
            recyclerViewlayout.setLayoutManager(linearLayoutManager);
            ListViewImages myAdapter = new  ListViewImages(HomeScreenActivity.this, gridViewImagesListList,new MyClickListener());
            recyclerViewlayout.setAdapter(myAdapter);
            return(true);
        case R.id.grid:
            //add the function to perform here
            GridLayoutManager mGridLayoutManager = new GridLayoutManager(HomeScreenActivity.this, 3);
            recyclerViewlayout.setLayoutManager(mGridLayoutManager);
            GridViewImages myAdapter1 = new  GridViewImages(HomeScreenActivity.this, gridViewImagesListList,new MyClickListener());
            recyclerViewlayout.setAdapter(myAdapter1);
            return(true);
     }
        return(super.onOptionsItemSelected(item));
    }
    public void gotoCamera(View view){
        Intent intent=new Intent(HomeScreenActivity.this,MainActivity.class);
        startActivity(intent);
    }

    public class MyClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            GridViewImagesList cur=gridViewImagesListList.get(recyclerViewlayout.getChildLayoutPosition(view));
            Intent intent=new Intent(HomeScreenActivity.this,MyDocumentActivity.class);
            intent.putExtra("from","HomeScreenActivity");
            intent.putExtra("did",cur.getDid());
            startActivity(intent);
        }
    }
}
