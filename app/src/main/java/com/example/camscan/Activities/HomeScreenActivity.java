package com.example.camscan.Activities;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.example.camscan.AdapterClass.GridViewImages;
import com.example.camscan.AdapterClass.ListViewImages;
import com.example.camscan.ObjectClass.GridViewImagesList;
import com.example.camscan.R;
import java.util.ArrayList;
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

        for (int i=1;i<=15;i++)
        {
            gridViewImagesListList.add(new GridViewImagesList("",i+"- 07-2020"));
        }
        GridLayoutManager mGridLayoutManager = new GridLayoutManager(HomeScreenActivity.this, 3);
        recyclerViewlayout.setLayoutManager(mGridLayoutManager);
        GridViewImages myAdapter = new  GridViewImages(HomeScreenActivity.this, gridViewImagesListList);
        recyclerViewlayout.setAdapter(myAdapter);
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
            ListViewImages myAdapter = new  ListViewImages(HomeScreenActivity.this, gridViewImagesListList);
            recyclerViewlayout.setAdapter(myAdapter);
            return(true);
        case R.id.grid:
            //add the function to perform here
            GridLayoutManager mGridLayoutManager = new GridLayoutManager(HomeScreenActivity.this, 3);
            recyclerViewlayout.setLayoutManager(mGridLayoutManager);
            GridViewImages myAdapter1 = new  GridViewImages(HomeScreenActivity.this, gridViewImagesListList);
            recyclerViewlayout.setAdapter(myAdapter1);
            return(true);
     }
        return(super.onOptionsItemSelected(item));
    }
}
