package com.example.camscan.Activities;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.example.camscan.AdapterClass.FilterGridAdapter;
import com.example.camscan.AdapterClass.GridViewImages;
import com.example.camscan.ObjectClass.FilterObject;
import com.example.camscan.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.ArrayList;
import java.util.List;
public class SettingsActivity extends AppCompatActivity
{
    RelativeLayout relativeLayoutTheme,relativeLayoutPdf,relativeLayoutActivity,relativeLayoutView,
            relativeLayoutFilter,relativeLayoutName;
    BottomSheetDialog bottom_dialog,filter_dialog;
    View bottom_view,filter_view;
    ImageView imageViewOne,imageViewTwo;
    TextView settings_name,textViewOne,textViewTwo;
    RadioButton radioButtonOne,radioButtonTwo;
    RelativeLayout relativeLayoutOne,relativeLayoutTwo;
    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().show();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Settings");
        preferences=getSharedPreferences("SharedPreference",MODE_PRIVATE);
        relativeLayoutTheme=findViewById(R.id.relative_theme);
        relativeLayoutPdf=findViewById(R.id.relative_pdf);
        relativeLayoutActivity=findViewById(R.id.relative_default_activity);
        relativeLayoutView=findViewById(R.id.relative_view_activity);
        relativeLayoutFilter=findViewById(R.id.relative_filter);
        relativeLayoutName=findViewById(R.id.relative_doc_name);
        show_bottom_sheet();
        set_filter_bottom_sheet();
        relative_ClickListener();
    }
    private  void set_filter_bottom_sheet()
    {
        filter_view= getLayoutInflater().inflate(R.layout.bottom_sheet_filter, null);
        filter_dialog = new BottomSheetDialog(this,R.style.BottomSheetDialog);
        filter_dialog.setContentView(filter_view);
        filter_dialog.setCancelable(false);
        filter_dialog.setCanceledOnTouchOutside(true);
        CardView cardView=filter_view.findViewById(R.id.filter_parent_card_view);
        cardView.setBackgroundResource(R.drawable.bottom_sheet_corner_radius);
        ImageView cancel_btn=filter_view.findViewById(R.id.cancel_btn);
        RecyclerView filter_recycler_view=filter_view.findViewById(R.id.recycler_view_filter);
        cancel_btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
               filter_dialog.dismiss();
            }
        });
       List<FilterObject> filter_list=new ArrayList<>();
       filter_list.add(new FilterObject(R.drawable.circular_shape,"Original"));
        filter_list.add(new FilterObject(R.drawable.circular_shape,"Luminous"));
        filter_list.add(new FilterObject(R.drawable.circular_shape,"Corrected"));
        filter_list.add(new FilterObject(R.drawable.circular_shape,"GrayScale"));
        filter_list.add(new FilterObject(R.drawable.circular_shape,"B/W"));
        filter_list.add(new FilterObject(R.drawable.circular_shape,"Inverted"));
        GridLayoutManager mGridLayoutManager = new GridLayoutManager(SettingsActivity.this, 3);
        filter_recycler_view.setLayoutManager(mGridLayoutManager);
        FilterGridAdapter myAdapter=new FilterGridAdapter(SettingsActivity.this,filter_list,
                preferences.getInt("myfilter",0));
        filter_recycler_view.setAdapter(myAdapter);
    }
    private void show_bottom_sheet()
    {
        bottom_view= getLayoutInflater().inflate(R.layout.bottom_sheet_settings, null);
        bottom_dialog = new BottomSheetDialog(this,R.style.BottomSheetDialog);
        bottom_dialog.setContentView(bottom_view);
        bottom_dialog.setCancelable(false);
        bottom_dialog.setCanceledOnTouchOutside(true);
        CardView cardView=bottom_view.findViewById(R.id.parent_card_view);
        cardView.setBackgroundResource(R.drawable.bottom_sheet_corner_radius);
        ImageView cancel_btn=bottom_view.findViewById(R.id.cancel_sheet);
        settings_name=bottom_view.findViewById(R.id.settings_name);
        imageViewOne=bottom_view.findViewById(R.id.imageview1);
        imageViewTwo=bottom_view.findViewById(R.id.imageview2);
        textViewOne=bottom_view.findViewById(R.id.textView1);
        textViewTwo=bottom_view.findViewById(R.id.textView2);
        radioButtonOne=bottom_view.findViewById(R.id.radio_button1);
        radioButtonTwo=bottom_view.findViewById(R.id.radio_button2);
        relativeLayoutOne=bottom_view.findViewById(R.id.relative_layout_first);
        relativeLayoutTwo=bottom_view.findViewById(R.id.relative_layout_second);
        cancel_btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                bottom_dialog.dismiss();
            }
        });
    }
    public  void relative_ClickListener()
    {
        relativeLayoutTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                //show_bottom_theme();
                show_default_theme(view);
            }
        });
        relativeLayoutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                 //show_bottom_view();
                 show_default_view(view);
            }
        });
        relativeLayoutFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
              show_bottom_filter();
            }
        });
        relativeLayoutActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
             //show_bottom_activity();
             show_default_activity(view);
            }
        });
        relativeLayoutPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent i=new Intent(SettingsActivity.this,PdfSettingsActivity.class);
                 startActivity(i);
            }
        });
        relativeLayoutName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
             show_bottom_name();
            }
        });
    }
    public  void show_bottom_theme()
    {
      settings_name.setText("App Theme");
      textViewOne.setText("Dark Theme");
      textViewTwo.setText("Light Theme");
      imageViewOne.setImageResource(R.drawable.dark_theme);
      imageViewTwo.setImageResource(R.drawable.light_theme);
      radioButtonOne.setChecked(false);
      radioButtonTwo.setChecked(false);
      if(preferences.getInt("mytheme",0)==1)
          radioButtonOne.setChecked(true);
      else
          radioButtonTwo.setChecked(true);
      bottom_dialog.show();
        relativeLayoutOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                radioButtonOne.setChecked(true);
                radioButtonTwo.setChecked(false);
                preferences.edit().putInt("mytheme",1).apply();
            }
        });
        relativeLayoutTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                radioButtonOne.setChecked(false);
                radioButtonTwo.setChecked(true);
                preferences.edit().putInt("mytheme",2).apply();
            }
        });
    }
    public  void show_bottom_activity()
    {
        settings_name.setText("Default Activity");
        textViewOne.setText("Home Activity");
        textViewTwo.setText("Camera Activity");
        imageViewOne.setImageResource(R.drawable.dark_theme);
        imageViewTwo.setImageResource(R.drawable.light_theme);
        radioButtonOne.setChecked(false);
        radioButtonTwo.setChecked(false);
        if(preferences.getInt("myactivity",0)==1)
            radioButtonOne.setChecked(true);
        else
            radioButtonTwo.setChecked(true);
        bottom_dialog.show();

        relativeLayoutOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                radioButtonOne.setChecked(true);
                radioButtonTwo.setChecked(false);
                preferences.edit().putInt("myactivity",1).apply();
            }
        });
        relativeLayoutTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                radioButtonOne.setChecked(false);
                radioButtonTwo.setChecked(true);
                preferences.edit().putInt("myactivity",2).apply();
            }
        });
    }
    public  void show_bottom_view()
    {
        settings_name.setText("View Document As");
        textViewOne.setText("List View");
        textViewTwo.setText("Grid View");
        imageViewOne.setImageResource(R.drawable.dark_theme);
        imageViewTwo.setImageResource(R.drawable.light_theme);
        radioButtonOne.setChecked(false);
        radioButtonTwo.setChecked(false);
        if(preferences.getInt("myview",0)==1)
            radioButtonOne.setChecked(true);
        else
            radioButtonTwo.setChecked(true);
        bottom_dialog.show();
        bottom_dialog.show();
        relativeLayoutOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                radioButtonOne.setChecked(true);
                radioButtonTwo.setChecked(false);
                preferences.edit().putInt("myview",1).apply();
            }
        });
        relativeLayoutTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                radioButtonOne.setChecked(false);
                radioButtonTwo.setChecked(true);
                preferences.edit().putInt("myview",2).apply();
            }
        });
    }
    public  void show_bottom_filter()
    {
       filter_dialog.show();
    }
    public  void show_bottom_pdf()
    {

    }
    public  void show_bottom_name()
    {

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case android.R.id.home:
                 finish();
                return true;
        }
        return(super.onOptionsItemSelected(item));
    }
    public void show_default_activity(View view)
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle("Default Activity");
        View dialog= LayoutInflater.from(SettingsActivity.this).inflate(R.layout.dialog_box_settings,null);
        builder.setView(dialog);
        RadioButton portBtn=dialog.findViewById(R.id.frag_orient_port);
        RadioButton landBtn=dialog.findViewById(R.id.frag_orient_land);
        portBtn.setText("Home Activity");
        landBtn.setText("Camera Activity");
        TextView saveBtn=dialog.findViewById(R.id.frag_orient_save);
        int rad=preferences.getInt("myactivity",0);
        if(rad==1)
        {
            portBtn.setChecked(true);
        }else
            {
            landBtn.setChecked(true);
        }
        AlertDialog d=builder.create();
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int radio;
                if(portBtn.isChecked())
                {
                    radio=1;

                }else
                    {
                    radio=2;
                }
                preferences.edit().putInt("myactivity",radio).apply();
                d.dismiss();
            }
        });

        d.show();
    }
    public void show_default_view(View view)
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle("View Document as ");
        View dialog= LayoutInflater.from(SettingsActivity.this).inflate(R.layout.dialog_box_settings,null);
        builder.setView(dialog);
        RadioButton portBtn=dialog.findViewById(R.id.frag_orient_port);
        RadioButton landBtn=dialog.findViewById(R.id.frag_orient_land);
        portBtn.setText("Grid View");
        landBtn.setText("List View");
        TextView saveBtn=dialog.findViewById(R.id.frag_orient_save);
        int rad=preferences.getInt("myview",0);
        if(rad==1)
        {
            portBtn.setChecked(true);
        }else
        {
            landBtn.setChecked(true);
        }
        AlertDialog d=builder.create();
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int radio;
                if(portBtn.isChecked())
                {
                    radio=1;

                }else
                {
                    radio=2;
                }
                preferences.edit().putInt("myview",radio).apply();
                d.dismiss();
            }
        });
        d.show();
    }
    public void show_default_theme(View view)
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle("App Theme ");
        View dialog= LayoutInflater.from(SettingsActivity.this).inflate(R.layout.dialog_box_settings,null);
        builder.setView(dialog);
        RadioButton portBtn=dialog.findViewById(R.id.frag_orient_port);
        RadioButton landBtn=dialog.findViewById(R.id.frag_orient_land);
        portBtn.setText("Dark Mode");
        landBtn.setText("Light Mode");
        TextView saveBtn=dialog.findViewById(R.id.frag_orient_save);
        int rad=preferences.getInt("mytheme",0);
        if(rad==1)
        {
            portBtn.setChecked(true);
        }else
        {
            landBtn.setChecked(true);
        }
        AlertDialog d=builder.create();
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int radio;
                if(portBtn.isChecked())
                {
                    radio=1;

                }else
                {
                    radio=2;
                }
                preferences.edit().putInt("mytheme",radio).apply();
                d.dismiss();
            }
        });
        d.show();
    }
}
