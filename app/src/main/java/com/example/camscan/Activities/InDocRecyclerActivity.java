package com.example.camscan.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.DialogCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.camscan.Adapters.InDocMiniAdapter;
import com.example.camscan.Adapters.InDocRecyclerAdapter;
import com.example.camscan.MyCustomPdf;
import com.example.camscan.Objects.DatabaseObject;
import com.example.camscan.R;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.Permission;
import java.util.ArrayList;

public class InDocRecyclerActivity extends AppCompatActivity {

    ViewPager2 vp2;
    View shareWindow;

    ArrayList<DatabaseObject> list;
    InDocRecyclerAdapter adapter;

    Boolean isPageOpen=false;
    Boolean isShareOpen=false;

    ArrayList<Bitmap> tNails;
    InDocMiniAdapter miniAdapter;
    RecyclerView miniRView;
    RelativeLayout miniCont;


    //to delete
    ArrayList<Point> points;
    DatabaseObject currentDoc=new DatabaseObject();
    //to delete end
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_doc_recycler);


        initializeViews();


        //delete
        points=new ArrayList<>();
        Point p=new Point(0,0);
        points.add(p);
        points.add(p);
        points.add(p);
        points.add(p);
       // rename();
     //   share();

        //delete end


        //setting docName
        updateDocName();

        list=new ArrayList<>();
        fillArrayList();
        adapter=new InDocRecyclerAdapter(this,list,new MyOnClickListener(),new MyLongClickListener());
        vp2.setAdapter(adapter);


        tNails=new ArrayList<>();
        miniAdapter=new InDocMiniAdapter(this, tNails, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView num=view.findViewById(R.id.filter_item_name);
                int curr=Integer.parseInt(num.getText().toString())-1;
                vp2.setCurrentItem(curr,true);
                miniAdapter.setSelected(curr);
                miniAdapter.notifyDataSetChanged();

            }
        });
        populateMiniAdapter();
        miniRView.setAdapter(miniAdapter);
        miniRView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));


        vp2.setClipToPadding(false);
        vp2.setClipChildren(false);
        vp2.setOffscreenPageLimit(3);
        vp2.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);

        CompositePageTransformer cpf=new CompositePageTransformer();
        cpf.addTransformer(new MarginPageTransformer(40));
        cpf.addTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float r=1-Math.abs(position);
                page.setScaleY(0.85f+r*0.15f);
            }
        });



        vp2.setPageTransformer(cpf);


    }



    private void initializeViews() {
        vp2=findViewById(R.id.in_doc_pageViewer);
        shareWindow=findViewById(R.id.in_doc_share_window);
        miniRView=findViewById(R.id.in_doc_mini_rview);
        miniCont=findViewById(R.id.in_doc_mini_r_cont);
    }


    //ON PAGE FUNCTIONS

    private void expandPager() {
        Toast.makeText(this, "Expanded", Toast.LENGTH_SHORT).show();

        miniCont.setVisibility(View.GONE);
        vp2.setUserInputEnabled(false);


    }

    private void closePageOption() {
        Toast.makeText(this, "Closed", Toast.LENGTH_SHORT).show();

        miniCont.setVisibility(View.VISIBLE);
        vp2.setUserInputEnabled(true);
    }

    private class MyLongClickListener implements View.OnLongClickListener{


        @Override
        public boolean onLongClick(View view) {
            if(!isPageOpen && !isShareOpen){
                expandPager();
                isPageOpen=true;
            }
            return true;
        }
    }

    private class MyOnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            if(!isShareOpen){
                if(isPageOpen){
                    closePageOption();
                    isPageOpen=false;
                }
            }else{
                closeShareWindow();
                isShareOpen=false;
            }

        }
    }


    //ON PAGE FUNCTIONS END

    //MINI RECYCLER VIEW
    private void populateMiniAdapter() {
        for(DatabaseObject i:list){
            Uri img=Uri.parse(i.getEditedUri());
            InputStream is=null;
            try{
                is=this.getContentResolver().openInputStream(img);
            }catch (FileNotFoundException e){
                e.printStackTrace();
            }
            if(is!=null){
                //Log.e("LOL", "populateMiniAdapter: "+"HEHR" );
                Bitmap im= BitmapFactory.decodeStream(is);
                Bitmap resized=Bitmap.createScaledBitmap(im,100,100,true);
                tNails.add(resized);
                miniAdapter.notifyDataSetChanged();
            }
        }
        }
    //MINI RECYCLER VIEW END



    //delete
    private void fillArrayList() {
        Uri path = Uri.parse("android.resource://com.example.camscan/" + R.drawable.img1);
        list.add(new DatabaseObject(null,null,path.toString(),"Image1",points,0,0));
         path = Uri.parse("android.resource://com.example.camscan/" + R.drawable.img2);
        list.add(new DatabaseObject(null,null,path.toString(),"Image2",points,0,0));
         path = Uri.parse("android.resource://com.example.camscan/" + R.drawable.img3);
        list.add(new DatabaseObject(null,null,path.toString(),"Image3",points,0,0));
         path = Uri.parse("android.resource://com.example.camscan/" + R.drawable.img4);
        list.add(new DatabaseObject(null,null,path.toString(),"Image4",points,0,0));
         path = Uri.parse("android.resource://com.example.camscan/" + R.drawable.img_5);
        list.add(new DatabaseObject(null,null,path.toString(),"Image5",points,0,0));
         path = Uri.parse("android.resource://com.example.camscan/" + R.drawable.test_img);
        list.add(new DatabaseObject(null,null,path.toString(),"Image6",points,0,0));
    }
    //delete end





    //RENAMING
    private void setDocName(String dName){
        currentDoc.setDocName(dName);
        //update database
        updateDocName();

    }
    private void updateDocName(){
     //   getActionBar().setTitle(currentDoc.getDocName());
            getSupportActionBar().setTitle("SOMETHINg");
    }

    private void rename(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        View view= LayoutInflater.from(this).inflate(R.layout.fragment_rename,null);
        //builder.setTitle("RENAME");
        builder.setView(view);
        EditText newNameView=view.findViewById(R.id.rename_fragment_eview);
        newNameView.setSelectAllOnFocus(true);
        //newNameView.setText(currentDoc.getDocName());
        newNameView.setText("DOC NAME");

        newNameView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(motionEvent.getRawX() >= (newNameView.getRight() - newNameView.getCompoundDrawables()[2].getBounds().width())) {
                        // your action here
                        newNameView.setText("");
                        return true;
                    }
                }
                return false;
            }
        });


        builder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String newName=newNameView.getText().toString();
                if(newName.equals("")){
                    Toast.makeText(InDocRecyclerActivity.this, "Name Field is Empty", Toast.LENGTH_SHORT).show();
                }else{
                    setDocName(newName);
                }

            }
        }).setNegativeButton("Cancel",null);

        builder.create();
        builder.show();

    }

    //RENAMING END

    //SHARE
    public void share(){
        if(isShareOpen){
            isShareOpen=false;
            closeShareWindow();
        }else{
            isShareOpen=true;
            openShareWindow();
        }
    }

    private void openShareWindow() {
        shareWindow.setVisibility(View.VISIBLE);
    }
    private void closeShareWindow(){
        shareWindow.setVisibility(View.GONE);

    }
    //SHARE END

    //FAB FUNCTIONS
    public void onFabClicked(View view){
        if(isPerMissionGranted()) {

            if (tNails.size() != 0) {
                tNails.add(BitmapFactory.decodeResource(getResources(),R.drawable.back));
                tNails.add(BitmapFactory.decodeResource(getResources(),R.drawable.test_img));
                tNails.add(BitmapFactory.decodeResource(getResources(),R.drawable.img3));

                MyCustomPdf pdf = new MyCustomPdf(this, tNails, false);
                if (pdf.savePdf2("anas")) {
                    Toast.makeText(this, "SAVED", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "FAILED", Toast.LENGTH_SHORT).show();
                }
            }
        }else{
            ActivityCompat.requestPermissions(this,new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"},101);
        }
    }

    public boolean isPerMissionGranted(){
        if(ContextCompat.checkSelfPermission(InDocRecyclerActivity.this,"android.permission.WRITE_EXTERNAL_STORAGE")!= PackageManager.PERMISSION_GRANTED){
            return false;
        }
        return true;
    }
    //FAB FUNCTIONS END
}
