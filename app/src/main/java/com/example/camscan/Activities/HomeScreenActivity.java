package com.example.camscan.Activities;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.camera.core.ImageCapture;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.camscan.AdapterClass.GridViewImages;
import com.example.camscan.AdapterClass.ListViewImages;
import com.example.camscan.Database.MyDatabase;
import com.example.camscan.ObjectClass.GridViewImagesList;
import com.example.camscan.Objects.MyDocument;
import com.example.camscan.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class HomeScreenActivity extends AppCompatActivity {

    RecyclerView recyclerViewlayout;
    List<GridViewImagesList> gridViewImagesListList;
    ImageView list_grid_view;
    boolean grid_view=true;
    GridViewImages myAdapter1;
    ListViewImages myAdapter;
    boolean long_click_enabled;
    RelativeLayout relativeLayoutTop,sort_by_layout,no_result_layout,search_bar_layout;
    FloatingActionButton floatingActionButton;
    ArrayList<Integer> selected_position;
    BottomNavigationView bottomNavigationView;
    ImageView settings,more_option,cross_button;
    EditText search_bar;
    TextView sort_by_text;
    SharedPreferences mypreference;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        getSupportActionBar().hide();
        gridViewImagesListList=new ArrayList<>();
        selected_position=new ArrayList<>();
        mypreference=getSharedPreferences("SharedPreference",MODE_PRIVATE);
        recyclerViewlayout=findViewById(R.id.recycler_view_layout);
        list_grid_view=findViewById(R.id.list_grid_view);
        relativeLayoutTop=findViewById(R.id.relative_layout_top);
        floatingActionButton=findViewById(R.id.floating_button);
        bottomNavigationView=findViewById(R.id.bottom_navigation);
        settings=findViewById(R.id.settings);
        more_option=findViewById(R.id.more_option);
        search_bar=findViewById(R.id.edit_text_search);
        cross_button=findViewById(R.id.cross_button);
        sort_by_layout=findViewById(R.id.relative_layout2);
        sort_by_text=findViewById(R.id.sort_by);
        no_result_layout=findViewById(R.id.no_result_layout);
        search_bar_layout=findViewById(R.id.relative_layout);
        long_click_enabled=false;
        if(mypreference.getInt("myview",0)==1)
           grid_view=false;
          else
            grid_view=true;
         set_default_adapter();
         set_image_list();
         settings.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent i=new Intent(HomeScreenActivity.this,SettingsActivity.class);
                  startActivity(i);
             }
         });
         more_option.setOnClickListener(new View.OnClickListener()
         {
             @Override
             public void onClick(View view) {
                 showPopupMenu(view,true,R.style.MyPopupStyle);
             }
         });
        search_bar.addTextChangedListener(SearchTextWatcher);
        cross_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                cross_button.setVisibility(View.GONE);
                search_bar.setText("");
              //  search_bar.setFocusable(false);
                set_file_list("");
            }
        });
        search_bar_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_bar.setFocusable(true);
                search_bar.setFocusableInTouchMode(true);
            }
        });
        sort_by_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
              showPopupMenuSortBy(view,false,R.style.MyPopupStyle);
            }
        });
         list_grid_view.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view)
             {
                 if(grid_view)
                 {
                  list_grid_view.setImageResource(R.drawable.grid_view);
                  grid_view=false;
                     list_view();
                 }
                 else
                 {
                     list_grid_view.setImageResource(R.drawable.list_view);
                     grid_view=true;
                     grid_view();
                 }
             }
         });
         onTouch_Listener();
         BottomNavigationBar();
    }
    private TextWatcher SearchTextWatcher = new TextWatcher()
    {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {

        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
            String text=search_bar.getText().toString().trim();
            if(text.length()>0)
            {
                set_file_list(text);
             cross_button.setVisibility(View.VISIBLE);
            }
            else
            {
                set_file_list(text);
                cross_button.setVisibility(View.GONE);
            }
        }
        @Override
        public void afterTextChanged(Editable s) {
        }
    };
    public  void set_default_adapter()
    {
        GridLayoutManager mGridLayoutManager = new GridLayoutManager(HomeScreenActivity.this, 3);
        recyclerViewlayout.setLayoutManager(mGridLayoutManager);
        myAdapter1 = new  GridViewImages(HomeScreenActivity.this, gridViewImagesListList,new MyClickListener());
        recyclerViewlayout.setAdapter(myAdapter1);

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        recyclerViewlayout.setLayoutManager(linearLayoutManager);
        myAdapter = new  ListViewImages(HomeScreenActivity.this, gridViewImagesListList,new MyClickListener());
        recyclerViewlayout.setAdapter(myAdapter);
    }
    public void onTouch_Listener()
    {
        recyclerViewlayout.addOnItemTouchListener(new RecyclerTouchListener(this,
                recyclerViewlayout, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                //Values are passing to activity & to fragment as well
                if(!long_click_enabled)
                {
                    //Toast.makeText(HomeScreenActivity.this, "Single Click on position :" + position,
                      //      Toast.LENGTH_SHORT).show();
                    GridViewImagesList cur = gridViewImagesListList.get(recyclerViewlayout.getChildLayoutPosition(view));
                    Intent intent = new Intent(HomeScreenActivity.this, MyDocumentActivity.class);
                    intent.putExtra("from", "HomeScreenActivity");
                    intent.putExtra("did", cur.getDid());
                    startActivity(intent);
                }
            }
            @Override
            public void onLongClick(View view, int position)
            {
                if(!long_click_enabled)
                {
                    show_action_bar();
                    relativeLayoutTop.setVisibility(View.GONE);
                    long_click_enabled = true;
//                    Toast.makeText(HomeScreenActivity.this, "Long press on position :" + position,
//                            Toast.LENGTH_LONG).show();
                    GridViewImagesList list = gridViewImagesListList.get(position);
                    ///  list.setCheckbox_visibility(true);
                    list.setIschecked(true);
                    gridViewImagesListList.set(position, list);
                    for (int i = 0; i < gridViewImagesListList.size(); i++) {
                        list = gridViewImagesListList.get(i);
                        list.setCheckbox_visibility(true);
                        gridViewImagesListList.set(i, list);
                    }
                    myAdapter1.notifyDataSetChanged();
                    myAdapter.notifyDataSetChanged();
                }
            }
        }));
    }
    public void show_action_bar()
    {
        getSupportActionBar().show();
        bottomNavigationView.setVisibility(View.VISIBLE);
        floatingActionButton.setVisibility(View.GONE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("1 Selected");
    }
    public  void hide_action_bar()
    {
        getSupportActionBar().hide();
        selected_position=new ArrayList<>();
        floatingActionButton.setVisibility(View.VISIBLE);
        bottomNavigationView.setVisibility(View.GONE);
        long_click_enabled=false;
        relativeLayoutTop.setVisibility(View.VISIBLE);
        GridViewImagesList list;
        for (int i = 0; i < gridViewImagesListList.size(); i++) {
            list = gridViewImagesListList.get(i);
            list.setIschecked(false);
            list.setCheckbox_visibility(false);
            gridViewImagesListList.set(i, list);
        }
        myAdapter1.notifyDataSetChanged();
        myAdapter.notifyDataSetChanged();
    }
    public void change_list(boolean b)
    {
        GridViewImagesList list;
        for (int i = 0; i < gridViewImagesListList.size(); i++) {
            list = gridViewImagesListList.get(i);
            list.setIschecked(b);
            gridViewImagesListList.set(i, list);
        }
        myAdapter1.notifyDataSetChanged();
        myAdapter.notifyDataSetChanged();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.action_bar_homescreen, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case android.R.id.home:
                hide_action_bar();
                return true;
            case R.id.select_all:
                selected_position=new ArrayList<>();
                if(item.getTitle().equals("SELECT ALL"))
                {
                    change_list(true);
                    item.setTitle("DESELECT ALL");
                    Objects.requireNonNull(getSupportActionBar()).setTitle("All Selected");
                    for(int i=0;i<gridViewImagesListList.size();i++)
                    {
                        selected_position.add(i);
                    }
                }
                else
                {
                    Objects.requireNonNull(getSupportActionBar()).setTitle(" ");
                    change_list(false);
                    item.setTitle("SELECT ALL");
                }
                return true;
        }
        return(super.onOptionsItemSelected(item));
    }
    public void grid_view()
    {
        GridLayoutManager mGridLayoutManager = new GridLayoutManager(HomeScreenActivity.this, 3);
        recyclerViewlayout.setLayoutManager(mGridLayoutManager);
         myAdapter1 = new  GridViewImages(HomeScreenActivity.this, gridViewImagesListList,new MyClickListener());
        recyclerViewlayout.setAdapter(myAdapter1);
    }
    public void list_view()
    {
        recyclerViewlayout.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        recyclerViewlayout.setLayoutManager(linearLayoutManager);
        myAdapter = new  ListViewImages(HomeScreenActivity.this, gridViewImagesListList,new MyClickListener());
        recyclerViewlayout.setAdapter(myAdapter);
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
           Log.d("HomeScreen_Uri",fp_uri+"uri");
           long date_edited=myDocument.getTimeEdited();
           gridViewImagesListList.add(new GridViewImagesList(name,did,fp_uri+"",dateformatter(date),
                   pcount,dateformatter(date_edited),false,false));
       }
       grid_view();
       list_view();
    }
    public  void set_file_list(String name)
    {
        List<GridViewImagesList> lists=new ArrayList<>();
        if(!name.equals("")) {
            for (GridViewImagesList myDocument : gridViewImagesListList) {
                if (myDocument.getName().toLowerCase().contains(name.toLowerCase())) {
                    lists.add(myDocument);
                }
            }
        }
        else
        {
            lists=gridViewImagesListList;
        }
        if(lists.size()>0)
            no_result_layout.setVisibility(View.GONE);
        else
            no_result_layout.setVisibility(View.VISIBLE);
        GridLayoutManager mGridLayoutManager = new GridLayoutManager(HomeScreenActivity.this, 3);
        recyclerViewlayout.setLayoutManager(mGridLayoutManager);
        myAdapter1 = new  GridViewImages(HomeScreenActivity.this, lists,new MyClickListener());
        recyclerViewlayout.setAdapter(myAdapter1);
        recyclerViewlayout.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        recyclerViewlayout.setLayoutManager(linearLayoutManager);
        myAdapter = new  ListViewImages(HomeScreenActivity.this,lists,new MyClickListener());
        recyclerViewlayout.setAdapter(myAdapter);
    }
    public  String dateformatter(long timestamp)
    {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return  formatter.format(new Date(timestamp));
    }

    public void gotoCamera(View view)
    {
        Intent intent=new Intent(HomeScreenActivity.this,CameraXActivity.class);
        startActivity(intent);
    }
    public class MyClickListener implements View.OnClickListener
    {
        @SuppressLint("UseValueOf")
        @Override
        public void onClick(View view)
        {
            GridViewImagesList cur=gridViewImagesListList.get(recyclerViewlayout.getChildLayoutPosition(view));
            int position=recyclerViewlayout.getChildLayoutPosition(view);
            if(cur.Ischecked())
            {
                selected_position.remove(new Integer(position));
                cur.setIschecked(false);
            }
            else
            {
                selected_position.add(position);
                cur.setIschecked(true);
            }
            Objects.requireNonNull(getSupportActionBar()).setTitle(selected_position.size()+" Selected");
        }
    }

    public static interface ClickListener
    {
        public void onClick(View view,int position);
        public void onLongClick(View view,int position);
    }
    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{

        private ClickListener clicklistener;
        private GestureDetector gestureDetector;

        public RecyclerTouchListener(Context context, final RecyclerView recycleView, final ClickListener clicklistener){

            this.clicklistener=clicklistener;
            gestureDetector=new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child=recycleView.findChildViewUnder(e.getX(),e.getY());
                    if(child!=null && clicklistener!=null){
                        clicklistener.onLongClick(child,recycleView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child=rv.findChildViewUnder(e.getX(),e.getY());
            if(child!=null && clicklistener!=null && gestureDetector.onTouchEvent(e)){
                clicklistener.onClick(child,rv.getChildAdapterPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e)
        {

        }
        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept)
        {
        }
    }
    private void BottomNavigationBar()
    {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                View view = findViewById(R.id.bottom_navigation);
                switch (item.getItemId())
                {
                    case R.id.action_share:
                        break;
                    case R.id.action_lock:
                        break;
                    case R.id.action_tag:
                        break;
                    case R.id.action_delete:
                      break;
                    case R.id.action_more:
                        break;
                }
                return true;
            }
        });
    }
    private void showPopupMenu(View anchor, boolean isWithIcons, int style) {
        //init the wrapper with style
        Context wrapper = new ContextThemeWrapper(this, style);
        PopupMenu popup = new PopupMenu(wrapper, anchor);
        if (isWithIcons) {
            try {
                Field[] fields = popup.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if ("mPopup".equals(field.getName())) {
                        field.setAccessible(true);
                        Object menuPopupHelper = field.get(popup);
                        assert menuPopupHelper != null;
                        Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                        Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                        setForceIcons.invoke(menuPopupHelper, true);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        popup.getMenuInflater().inflate(R.menu.more_option_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.contact_us:
                        break;
                    case R.id.terms_condition:
                        break;
                    case R.id.about_us:
                        break;
                }
                return true;
            }
        });
        popup.show();
    }
    private void showPopupMenuSortBy(View anchor, boolean isWithIcons, int style) {
        //init the wrapper with style
        Context wrapper = new ContextThemeWrapper(this, style);
        PopupMenu popup = new PopupMenu(wrapper, anchor);
        if (isWithIcons) {
            try {
                Field[] fields = popup.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if ("mPopup".equals(field.getName())) {
                        field.setAccessible(true);
                        Object menuPopupHelper = field.get(popup);
                        assert menuPopupHelper != null;
                        Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                        Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                        setForceIcons.invoke(menuPopupHelper, true);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        popup.getMenuInflater().inflate(R.menu.sort_by_popup_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.sort_name:
                        sort_by_text.setText("Sort By Name");
                        break;
                    case R.id.sort_date:
                        sort_by_text.setText("Sort By Date");
                        break;
                }
                return true;
            }
        });
        popup.show();
    }
}
