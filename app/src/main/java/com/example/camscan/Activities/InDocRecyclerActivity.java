package com.example.camscan.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Database;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.camscan.Adapters.InDocMiniAdapter;
import com.example.camscan.Adapters.InDocRecyclerAdapter;
import com.example.camscan.Adapters.NavMenuAdapter;
import com.example.camscan.Database.MyDatabase;
import com.example.camscan.MyCustomPdf;
import com.example.camscan.Objects.MyDocument;
import com.example.camscan.Objects.MyPicture;
import com.example.camscan.Objects.NavMenuObject;
import com.example.camscan.R;
import com.example.camscan.RenderScriptJava.FlatCorrection;
import com.example.camscan.UtilityClass;
import com.google.android.gms.common.util.JsonUtils;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;
import org.spongycastle.pqc.math.ntru.util.Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InDocRecyclerActivity extends AppCompatActivity {

    ViewPager2 vp2;
    View shareWindow;

    InDocRecyclerAdapter adapter;

    Boolean isPageOpen=false;
    Boolean isShareOpen=false;
    Boolean isEmailOpen=false;


    ArrayList<Bitmap> tNails;
    InDocMiniAdapter miniAdapter;
    RecyclerView miniRView;
    RelativeLayout miniCont;

    //FOR DATABASE
    MyDocument currentDoc;
    ArrayList<MyPicture> list;
    //  ArrayList<MyPicture> currentPics;

    MyDatabase db;
    //FOR DATABASE END


    //to delete
    ArrayList<Point> points;

    //to delete end

    //PAGE SETTING

    //NAV DRAWER
    ArrayList<NavMenuObject> menuList;
    DrawerLayout dl;
    ActionBarDrawerToggle abdt;
    NavigationView nv;
    NavMenuAdapter menuAdapter;
    ListView menuListView;
    boolean isPdfSettingOpen=false;
    boolean isShareSettingOpen=false;

    //NAV DRAWER END
    View pageSettingLayout;
    //PAGE SETTING END

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_doc_recycler);


        initializeViews();


        db=MyDatabase.getInstance(this);
        //delete

        points=new ArrayList<>();
        Point p=new Point(0,0);
        points.add(p);
        points.add(p);
        points.add(p);
        points.add(p);
       // rename();
       //isEmailOpen=true;
       // share();


     //   db=MyDatabase.getInstance(this);
        //delete end


        //setting docName

        //NAV DRAWER


        abdt = new ActionBarDrawerToggle(this, dl,R.string.open, R.string.close);

        dl.addDrawerListener(abdt);
        abdt.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        menuList=new ArrayList<>();
        populateMenuListItems();
        menuAdapter=new NavMenuAdapter(this,menuList);
        menuListView.setAdapter(menuAdapter);
        menuListView.setOnItemClickListener(new MyMenuOnClickListener());
        //NAV DRAWER END

        updateDocName();


        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        list=new ArrayList<>();

        adapter=new InDocRecyclerAdapter(this,list,new MyOnClickListener(),new MyLongClickListener());
        vp2.setAdapter(adapter);
        fillArrayList();
//        currentDoc=new MyDocument("MyDoc",0l,0l,6,list.get(0).getEditedUri());

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

    //NAV DRAWER
    private void populateMenuListItems() {
        menuList.add(new NavMenuObject(false,"Edit Page",R.drawable.ic_picture_as_pdf_black_24dp,true));
        menuList.add(new NavMenuObject(false,"Secure PDf",R.drawable.ic_picture_as_pdf_black_24dp,true));
        menuList.add(new NavMenuObject(true,"Pdf Setting",R.drawable.ic_picture_as_pdf_black_24dp,true));
        //pdf etting
//        menuList.add(new NavMenuObject(false,"Password",0,false));
//        menuList.add(new NavMenuObject(false,"Orientation",0,false));
//        menuList.add(new NavMenuObject(false,"PageSize",0,false));
//        menuList.add(new NavMenuObject(false,"Margin",0,false));
        //
        menuList.add(new NavMenuObject(false,"Email",R.drawable.ic_picture_as_pdf_black_24dp,true));
        menuList.add(new NavMenuObject(true,"Share",R.drawable.ic_picture_as_pdf_black_24dp,true));
        //share
//        menuList.add(new NavMenuObject(false,"As Images",0,false));
//        menuList.add(new NavMenuObject(false,"As Long Image",0,false));
//        menuList.add(new NavMenuObject(false,"As PDF",0,false));
        //
        menuList.add(new NavMenuObject(false,"Reverse Order",R.drawable.ic_picture_as_pdf_black_24dp,true));
        menuList.add(new NavMenuObject(false,"Add more Pages",R.drawable.ic_picture_as_pdf_black_24dp,true));
        //---------------------
        menuList.add(new NavMenuObject(false,"About Us",R.drawable.ic_picture_as_pdf_black_24dp,true));
        menuList.add(new NavMenuObject(false,"Share App",R.drawable.ic_picture_as_pdf_black_24dp,true));
        menuList.add(new NavMenuObject(false,"Exit",R.drawable.ic_picture_as_pdf_black_24dp,true));

    }
    public void openPdfSettings(){
        menuList.get(3).setVisible(true);
        menuList.get(4).setVisible(true);
        menuList.get(5).setVisible(true);
        menuList.get(6).setVisible(true);
        menuAdapter.notifyDataSetChanged();
    }
    public void closePdfSettings(){
        menuList.get(3).setVisible(false);
        menuList.get(4).setVisible(false);
        menuList.get(5).setVisible(false);
        menuList.get(6).setVisible(false);
        menuAdapter.notifyDataSetChanged();
    }
    public void openShareMenuItem(){
        menuList.get(9).setVisible(true);
        menuList.get(10).setVisible(true);
        menuList.get(11).setVisible(true);
        menuAdapter.notifyDataSetChanged();
    }
    public void closeShareMenuItem(){
        menuList.get(9).setVisible(false);
        menuList.get(10).setVisible(false);
        menuList.get(11).setVisible(false);
        menuAdapter.notifyDataSetChanged();
    }
    public void resetDrawer(){

        closePdfSettings();
        closeShareMenuItem();
        isPdfSettingOpen=false;
        isShareSettingOpen=false;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(abdt.onOptionsItemSelected(item)){
            resetDrawer();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class MyMenuOnClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            switch (i){
                case 0:{//EDIT page
                    if(!isPageOpen && !isShareOpen){
                        expandPager();
                        isPageOpen=true;
                    }

                    break;
                }
                case 1:{//secure pdf
                    AlertDialog.Builder builder=new AlertDialog.Builder(InDocRecyclerActivity.this);
                    builder.setTitle("Set Password");
                    View dialog=LayoutInflater.from(InDocRecyclerActivity.this).inflate(R.layout.fragment_rename,null);

                    builder.setView(dialog);

                    EditText newNameView=dialog.findViewById(R.id.rename_fragment_eview);
                    newNameView.setSelectAllOnFocus(true);
                    //newNameView.setText(currentDoc1.getDocName());
                    String pass=getPagePassword();
                    newNameView.setText(pass);

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

                    builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String newPass=newNameView.getText().toString();
                            if(!newPass.equals("")){
                                Uri pdfUri=savePdf(newPass,true);
                                Toast.makeText(InDocRecyclerActivity.this,"Pdf saved at "+pdfUri, Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(InDocRecyclerActivity.this, "Password can't be null", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).setNegativeButton("Cancel",null);

                    builder.create().show();

                    break;
                }
                case 2:{//pdf setting
//                        if(!isPdfSettingOpen){
//                            isPdfSettingOpen=true;
//                            openPdfSettings();
//                            ImageView ex=view.findViewById(R.id.item_nav_expand);
//                            ex.setImageDrawable(getDrawable(android.R.drawable.arrow_up_float));
//                        }else{
//                            isPdfSettingOpen=false;
//                            closePdfSettings();
//                            ImageView ex=view.findViewById(R.id.item_nav_expand);
//                            ex.setImageDrawable(getDrawable(android.R.drawable.arrow_down_float));
//                        }
                    Intent intent=new Intent(InDocRecyclerActivity.this,PdfSettingsActivity.class);
                    startActivity(intent);
                    break;
                }
                /*
                case 3:{//password
                    AlertDialog.Builder builder=new AlertDialog.Builder(InDocRecyclerActivity.this);
                    builder.setTitle("Save Default Password");
                    View dialog=LayoutInflater.from(InDocRecyclerActivity.this).inflate(R.layout.fragment_rename,null);

                    builder.setView(dialog);

                    EditText newNameView=dialog.findViewById(R.id.rename_fragment_eview);
                    newNameView.setSelectAllOnFocus(true);
                    //newNameView.setText(currentDoc1.getDocName());
                    String pass=getPagePassword();
                    newNameView.setText(pass);

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

                    builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String newPass=newNameView.getText().toString();
                            if(!newPass.equals("")){
                                setPagePassword(newPass);
                                Toast.makeText(InDocRecyclerActivity.this,"New Password Is Set", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(InDocRecyclerActivity.this, "Password can't be null", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).setNegativeButton("Cancel",null);

                    builder.create().show();
                    break;
                }
                case 4:{//orientation
                    AlertDialog.Builder builder=new AlertDialog.Builder(InDocRecyclerActivity.this);
                    builder.setTitle("Set Orientation");
                    View dialog=LayoutInflater.from(InDocRecyclerActivity.this).inflate(R.layout.fragment_orientation,null);

                    builder.setView(dialog);

                    RadioButton portBtn=dialog.findViewById(R.id.frag_orient_port);
                    RadioButton landBtn=dialog.findViewById(R.id.frag_orient_land);

                    int rad=getPageOrientation();
                    if(rad==0){
                        portBtn.setChecked(true);
                    }else{
                        landBtn.setChecked(true);
                    }


                    builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            int radio;
                            if(portBtn.isChecked()){
                                radio=0;
                            }else{
                                radio=1;
                            }
                            setPageOrientation(radio);
                        }
                    }).setNegativeButton("Cancel",null);

                    builder.create().show();
                    break;
                }
                case 5:{//Margin

                    boolean isMargin=getPageMargin();
                    if(isMargin){
                        Toast.makeText(InDocRecyclerActivity.this, "Margin is Off", Toast.LENGTH_SHORT).show();
                        setPageMargin(false);
                    }else{
                        Toast.makeText(InDocRecyclerActivity.this, "Margin is On", Toast.LENGTH_SHORT).show();
                        setPageMargin(true);
                    }
                    break;
                }
                case 6:{//pageSize
                    AlertDialog.Builder builder=new AlertDialog.Builder(InDocRecyclerActivity.this);
                    builder.setTitle("Set Orientation");
                    View dialog=LayoutInflater.from(InDocRecyclerActivity.this).inflate(R.layout.fragment_page_size,null);

                    builder.setView(dialog);

                    RadioButton a5=dialog.findViewById(R.id.frag_psize_a5);
                    RadioButton a4=dialog.findViewById(R.id.frag_psize_a4);
                    RadioButton a3=dialog.findViewById(R.id.frag_psize_a3);
                    RadioButton legal=dialog.findViewById(R.id.frag_psize_legal);
                    RadioButton tabloid=dialog.findViewById(R.id.frag_psize_tabloid);

                    int index=getPageSize();
                    if(index==0){
                        a5.setChecked(true);
                    }else if(index==1){
                        a4.setChecked(true);
                    }
                    else if(index==2){
                        a3.setChecked(true);
                    }
                    else if(index==3){
                        legal.setChecked(true);
                    }
                    else if(index==4){
                        tabloid.setChecked(true);
                    }



                    builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            int radio=1;
                            if(a5.isChecked()){
                                radio=0;
                            }else if(a4.isChecked()){
                                radio=1;
                            }
                            else if(a3.isChecked()){
                                radio=2;
                            }
                            else if(legal.isChecked()){
                                radio=3;
                            }
                            else if(tabloid.isChecked()){
                                radio=4;
                            }
                            setPageSize(radio);
                        }
                    }).setNegativeButton("Cancel",null);

                    builder.create().show();
                    break;
                }
                */
                case 3:{//email
                    email();
                    break;
                }
                case 4:{//share
                        if(!isShareSettingOpen){
                            isShareSettingOpen=true;
                            openShareMenuItem();
                            ImageView ex=view.findViewById(R.id.item_nav_expand);
                            ex.setImageDrawable(getDrawable(android.R.drawable.arrow_up_float));
                        }else{
                            isShareSettingOpen=false;
                            closeShareMenuItem();
                            ImageView ex=view.findViewById(R.id.item_nav_expand);
                            ex.setImageDrawable(getDrawable(android.R.drawable.arrow_down_float));
                        }

                    break;
                }
//                case 9:{//as image
//                    shareAsImages(view);
//                    break;
//                }
//                case 10:{//as long image
//                    shareAsLongimage(view);
//                    break;
//                }
                case 5:{//as pdf
                    shareAsPdf(view);
                    break;
                }
                case 6:{//Reverse Items
                    reverseList();
                    break;
                }
                case 7:{//Add more Pages
                    addMorePages();
                    break;
                }
                case 8:{//About us
                    AlertDialog.Builder builder=new AlertDialog.Builder(InDocRecyclerActivity.this);
                    builder.setTitle("About App");
                    builder.setMessage(getResources().getString(R.string.lorem));
                    builder.setPositiveButton("OK",null);
                    builder.create().show();
                    break;
                }
                case 9:{//Share app
                    Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                    /*This will be the actual content you wish you share.*/
                    String shareBody = "Hey! This is the awesome app I found which will help you to manage your documents in one place. Make sure to Check is out.!";
                    /*The type of the content is text, obviously.*/
                    intent.setType("text/plain");
                    /*Applying information Subject and Body.*/
                    intent.putExtra(android.content.Intent.EXTRA_SUBJECT,"Cam Scan Available in PlayStore!!!");
                    intent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                    /*Fire!*/
                    startActivity(Intent.createChooser(intent, "Share Via"));
                    break;
                }
                case 10:{//Exit
                    AlertDialog.Builder builder=new AlertDialog.Builder(InDocRecyclerActivity.this);
                    builder.setTitle("Exit?");
                    builder.setMessage("Are you sure you want to exit");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finishAffinity();
                        }
                    }).setNegativeButton("No",null);
                    builder.create().show();
                    break;
                }


            }

            dl.closeDrawer(Gravity.LEFT);

        }
    }

    //NAV DRAWER END

    //-----------------------------------------------------------------------------------------
    private void initializeViews() {
        vp2=findViewById(R.id.in_doc_pageViewer);
        shareWindow=findViewById(R.id.in_doc_share_window);
        miniRView=findViewById(R.id.in_doc_mini_rview);
        miniCont=findViewById(R.id.in_doc_mini_r_cont);

        pageSettingLayout=findViewById(R.id.in_doc_page_setting);

        menuListView=findViewById(R.id.in_doc_nav_list);
        dl=findViewById(R.id.in_doc_drawer);
        nv=findViewById(R.id.in_doc_nav);

    }


    //-----------------------------------------------------------------------------------------

    //ON PAGE FUNCTIONS
    private void expandPager() {
        Toast.makeText(this, "Expanded", Toast.LENGTH_SHORT).show();

        miniCont.setVisibility(View.GONE);
        vp2.setUserInputEnabled(false);

        pageSettingLayout.setVisibility(View.VISIBLE);

    }

    private void closePageOption() {
        Toast.makeText(this, "Closed", Toast.LENGTH_SHORT).show();

        miniCont.setVisibility(View.VISIBLE);
        vp2.setUserInputEnabled(true);
        pageSettingLayout.setVisibility(View.GONE);
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
                isEmailOpen=false;
            }

        }
    }
    //ON PAGE FUNCTIONS END


    //MINI RECYCLER VIEW
    private void populateMiniAdapter() {
        for(MyPicture i:list){
            Bitmap resized=UtilityClass.populateImage(this,Uri.parse(i.getEditedUri()),true,0,0);
            tNails.add(resized);
            miniAdapter.notifyDataSetChanged();
        }
        }

    private void addIntoMiniAdapter(Bitmap img){
        Bitmap resized=Bitmap.createScaledBitmap(img,100,100,true);
        tNails.add(resized);
        miniAdapter.notifyDataSetChanged();
    }


    //MINI RECYCLER VIEW END


    //-----------------------------------------------------------------------------------------

    //delete
    private void fillArrayList() {

        String from=getIntent().getStringExtra("from");
        if(from.equals("BoxActivity") || from.equals("FilterActivity")){
            //came from box activity with multiple images
            String myPics=getIntent().getStringExtra("MyPicture");
            String myDoc=getIntent().getStringExtra("MyDocument");
            Log.e("THIS", "fillArrayList: "+myPics );
            Log.e("THIS", "fillArrayList: "+myDoc );
            ArrayList<MyPicture> newList=UtilityClass.getListOfPics(myPics);
            currentDoc= UtilityClass.getDocFromJson(myDoc);

            saveDocInDatabase();
           savePicsIntoDatabase(newList);
            //Pick pics from database
            ArrayList<MyPicture> ps=getPicturesFromDatabase();
            list.addAll(ps);
            adapter.notifyDataSetChanged();
        }
        else if(from.equals("HomeActivity")){

            String myPics=getIntent().getStringExtra("MyPicture");
            String myDoc=getIntent().getStringExtra("MyDocument");
            ArrayList<MyPicture> newList=UtilityClass.getListOfPics(myPics);
            currentDoc= UtilityClass.getDocFromJson(myDoc);

            if(newList.size()!=0){
                list.addAll(newList);
                adapter.notifyDataSetChanged();
            }
        }


//        currentDoc=new MyDocument("DOC",123456,123456,4,"file:///storage/emulated/0/CamScan/.Edited/Something17869.jpg");
//        list.add(new MyPicture(0,"file:///storage/emulated/0/CamScan/.Edited/Something17869.jpg","file:///storage/emulated/0/CamScan/.Edited/Something17869.jpg","Image5",5,points));
//        list.add(new MyPicture(0,"file:///storage/emulated/0/CamScan/.Edited/Something278100.jpg","file:///storage/emulated/0/CamScan/.Edited/Something278100.jpg","Image5",5,points));
//        list.add(new MyPicture(0,"file:///storage/emulated/0/CamScan/.Edited/Something80568.jpg","file:///storage/emulated/0/CamScan/.Edited/Something80568.jpg","Image5",5,points));
//        list.add(new MyPicture(0,"file:///storage/emulated/0/CamScan/.Edited/Something736207.jpg","file:///storage/emulated/0/CamScan/.Edited/Something736207.jpg","Image5",5,points));

    }
    //delete end

    //-----------------------------------------------------------------------------------------

    //DATABASE FUNCTIONS
    public ArrayList<MyPicture> getPicturesFromDatabase()
    {int did=currentDoc.getDid();
        ArrayList<MyPicture> lst=new ArrayList<>();
        List<MyPicture> l=db.myPicDao().getDocPics(did);
        if(l!=null){
            lst.addAll(l);
        }
        return lst;
    }
    public void updatePictureInfo(){

    }
    public void saveImportedIntoDatabase(MyPicture pic){
        currentDoc.setpCount(currentDoc.getpCount()+1);

        //Update doc and insert into pictures
    }
    private void updateReverseDatabase() {}

    private void updatePositionInDatabase(MyPicture currPic) {
    }

    private void saveDocInDatabase(){
        long id=db.myDocumentDao().insertNewDoc(currentDoc);
        currentDoc.setDid((int)id);
    }
    private void savePicsIntoDatabase(ArrayList<MyPicture> l) {
        for(MyPicture p:l){
            p.setDid(currentDoc.getDid());
        }

        db.myPicDao().InsertMultiplePics(l);
        }

    private void deleteFromDatabase(int pid) {
    }
    // DATABASE FUNCTIONS END
//-----------------------------------------------------------------------------------------
    //IMPORT FROM GALLERY

    public void importPic(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), UtilityClass.IMPORT_REQ_CODE);
    }

    public void addImage(Uri imgUri){

        MyPicture pic=new MyPicture(currentDoc.getDid(),imgUri.toString(),null,
                "Something",list.get(list.size()-1).getPosition()+1,null);
        applyFilter(pic);
    }

    public void applyFilter(MyPicture pic){
        Bitmap img=null;
        InputStream is=null;

        try{
            is=this.getContentResolver().openInputStream(Uri.parse(pic.getOriginalUri()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if(is!=null){
            img=BitmapFactory.decodeStream(is);
            //copyThe original img to my personal folder
            Uri original= UtilityClass.saveImage(this,img,System.currentTimeMillis()+"",true);
            pic.setOriginalUri(original.toString());
            applyFlatCorrection(img,pic);
        }


    }
    private void applyFlatCorrection(Bitmap cropped,MyPicture pic) {
        System.gc();
        Thread t=new Thread(new Runnable() {
            @Override
            public void run() {
                FlatCorrection fc=new FlatCorrection(InDocRecyclerActivity.this);
                Bitmap blur=fc.flatCorr(cropped.copy(cropped.getConfig(),false));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        ArrayList<Point> points=new ArrayList<>();
//                        int h=blur.getHeight();
//                        int w=blur.getWidth();
//

                        Uri saved= UtilityClass.saveImage(InDocRecyclerActivity.this,blur,pic.getEditedName(),false);
                   //     Log.e("TAG", "run: "+saved.toString() );
                        pic.setEditedUri(saved.toString());
                        saveImportedIntoDatabase(pic);
                        list.add(pic);
                        addIntoMiniAdapter(blur);

                        adapter.notifyDataSetChanged();
                        Log.e("TOS", "run: "+pic.getEditedUri() );

                    }
                });
                fc.clear();

            }
        });
        t.start();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case UtilityClass.IMPORT_REQ_CODE:{
                if(resultCode==RESULT_OK){
                    if(data.getClipData()!=null){
                        for(int i=0;i<data.getClipData().getItemCount();i++){
                            Uri imgUri=data.getClipData().getItemAt(i).getUri();
                            addImage(imgUri);

                        }
                    }else if(data.getData()!=null) {
                        Uri imgUri=data.getData();
                        addImage(imgUri);
                    }
                    else
                    {
                        Log.e("TAG", "onActivityResult: Image can't be clipped" );
                    }
                }else{
                    Toast.makeText(this, "Image can't be imported", Toast.LENGTH_SHORT).show();
                }

                break;
            }
        }

    }
    //IMPORT FROM GALLERY END
    //-----------------------------------------------------------------------------------------

    //RENAMING
    private void setDocName(String dName){
        currentDoc.setdName(dName);
        //update database
        updateDocName();

    }
    private void updateDocName(){
     //   getActionBar().setTitle(currentDoc1.getDocName());
            getSupportActionBar().setTitle("SOMETHINg");
    }
    private void rename(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        View view= LayoutInflater.from(this).inflate(R.layout.fragment_rename,null);
        //builder.setTitle("RENAME");
        builder.setView(view);
        EditText newNameView=view.findViewById(R.id.rename_fragment_eview);
        newNameView.setSelectAllOnFocus(true);
        //newNameView.setText(currentDoc1.getDocName());
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

//-----------------------------------------------------------------------------------------

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
    public void shareAsPdf(View view){
        if(isEmailOpen){
            //email intent
            isEmailOpen=false;
            Uri savedPdf=savePdf(null,false);
            if(savedPdf!=null){
                callIntentForEmail(savedPdf,1,null);
            }

        }else{
            //simple share intent

        }
        isShareOpen=false;
        closeShareWindow();

    }
    public void shareAsImages(View view){
        if(isEmailOpen){
            //email intent
            isEmailOpen=false;
            if(list!=null){
                ArrayList<Uri> uris=new ArrayList<>();
                for(MyPicture p:list){
                    uris.add(Uri.parse(p.getEditedUri()));
                }
                callIntentForEmail(null,3,uris);
            }
        }else{
            //simple share intent

        }
        isShareOpen=false;
        closeShareWindow();
    }
    public void shareAsLongimage(View view){
        if(isEmailOpen){
            //email intent
            isEmailOpen=false;
            ArrayList<Uri> uris=new ArrayList<>();
            for(MyPicture p:list){
                uris.add(Uri.parse(p.getEditedUri()));
            }
            Uri savedLongImage=UtilityClass.saveLongImage(this,uris,currentDoc.getdName());
            if(savedLongImage!=null){
                Toast.makeText(this, "SAVED", Toast.LENGTH_SHORT).show();
                callIntentForEmail(savedLongImage,2,null);
            }
        }
        else{
            //simple intent
        }
        isShareOpen=false;
        closeShareWindow();
    }
    //SHARE END
//-----------------------------------------------------------------------------------------
    //FAB FUNCTIONS
    public void onFabClicked(View view){
        importPic();

     //   MyDocument doc=new MyDocument("DOC NAME",54612l,26454876l,5,list.get(0).getEditedUri());


      //  db.myDocumentDao().insertNewDoc(doc);

     //   List<MyDocument> docs=db.myDocumentDao().getAllDocs();
    //    for(MyDocument d:docs){
     //       Log.e("THIS", "onFabClicked: "+d.getdName()+" "+d.getDid()+" "+d.getpCount() );
      //  }
    }

    //FAB FUNCTIONS END

//-----------------------------------------------------------------------------------------

    //PDF FEATURE
  /*
    public void savePDF(){
        if(isPerMissionGranted()) {
            if (tNails.size() != 0) {
                tNails.add(BitmapFactory.decodeResource(getResources(),R.drawable.back));
                tNails.add(BitmapFactory.decodeResource(getResources(),R.drawable.test_img));
                tNails.add(BitmapFactory.decodeResource(getResources(),R.drawable.img3));

                MyCustomPdf pdf = new MyCustomPdf(this, tNails, false);
                Uri savedPdf=pdf.savePdf2("anas");
                if (savedPdf!=null) {
                    Toast.makeText(this, "SAVED", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "FAILED", Toast.LENGTH_SHORT).show();
                }
            }
        }else{
            ActivityCompat.requestPermissions(this,new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"},101);
        }
    }
*/
    public Uri savePdf(String pass,boolean isPassSet){
        if(isPerMissionGranted()){
            ArrayList<Bitmap> pdfList=new ArrayList<>();
            for(MyPicture p:list){
                Uri current=Uri.parse(p.getEditedUri());
                InputStream is=null;
                Bitmap img=null;
                try{

                    is=getContentResolver().openInputStream(current);
                    img=BitmapFactory.decodeStream(is);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                if(img!=null){
                    pdfList.add(img);
                }

            }

            MyCustomPdf pdf = new MyCustomPdf(this, pdfList, isPassSet);
            Uri savedPdf=pdf.savePdf2(currentDoc.getdName(),pass);
            if (savedPdf!=null) {
                Toast.makeText(this, "SAVED", Toast.LENGTH_SHORT).show();
                return savedPdf;
            } else {
                Toast.makeText(this, "FAILED", Toast.LENGTH_SHORT).show();
                return null;
            }


        }else{
            ActivityCompat.requestPermissions(this,new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"},101);
            return savePdf(pass,isPassSet);
        }
    }

    public boolean isPerMissionGranted(){
        if(ContextCompat.checkSelfPermission(InDocRecyclerActivity.this,"android.permission.WRITE_EXTERNAL_STORAGE")!= PackageManager.PERMISSION_GRANTED){
            return false;
        }
        return true;
    }
    //PDF FEATURE END

    //EMAIL FEATURE
    public void email(){
        share();
        isEmailOpen=true;
    }

    private void callIntentForEmail(Uri savedFile, int mode,ArrayList<Uri> uris) {
        //mode 1->email 2->longImage 3-> multiple Images
        Intent emailIntent;
        if(mode!=3){
             emailIntent = new Intent(Intent.ACTION_SEND);
        }else{
             emailIntent=new Intent(Intent.ACTION_SEND_MULTIPLE);
        }

// set the type to 'email'
        emailIntent .setType("vnd.android.cursor.dir/email");

// the attachment
        if(mode!=3){
            emailIntent .putExtra(Intent.EXTRA_STREAM, savedFile);
        }else{
            emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,uris);
        }

// the mail subject
        emailIntent .putExtra(Intent.EXTRA_SUBJECT, "Document Shared By CamScan");
        startActivity(Intent.createChooser(emailIntent , "Send email..."));
    }
    //EMAIL FEATURE END

    //REVERSE FEATURE
    public void reverseList(){
        Collections.reverse(list);
        adapter.notifyDataSetChanged();
        tNails.clear();
        populateMiniAdapter();
        updateReverseDatabase();
    }

    //REVERSE FEATURE END

    //PDF SETTINGS

    public int getPageSize(){
        SharedPreferences pref= getSharedPreferences(UtilityClass.PDF_SETTING,MODE_PRIVATE);
        int pageSize=pref.getInt("PDF_PAGE_SIZE",1);
        return pageSize;
    }
    public int getPageOrientation(){
        SharedPreferences pref= getSharedPreferences(UtilityClass.PDF_SETTING,MODE_PRIVATE);
        int pageOrient=pref.getInt("PDF_PAGE_ORIENTATION",0);
        return pageOrient;
    }
    public boolean getPageMargin(){
        SharedPreferences pref= getSharedPreferences(UtilityClass.PDF_SETTING,MODE_PRIVATE);
        boolean isPageMargin=pref.getBoolean("PDF_PAGE_MARGIN",false);
        return isPageMargin;
    }
    public String getPagePassword(){
        SharedPreferences pref= getSharedPreferences(UtilityClass.PDF_SETTING,MODE_PRIVATE);
        String pass=pref.getString("PDF_PAGE_PASSWORD","admin");
        return pass;
    }

    //PDF SETTINGS END

    //PAGE SETTINGS
    public void onSwapLeftClicked(View view){
        int itemPos=vp2.getCurrentItem();
        MyPicture currPic=list.get(itemPos);
        if(itemPos!=0){
            MyPicture leftPic=list.get(itemPos-1);
            int currPos=currPic.getPosition();
            currPic.setPosition(leftPic.getPosition());
            leftPic.setPosition(currPos);
            updatePositionInDatabase(leftPic);
            updatePositionInDatabase(currPic);
            list.set(itemPos,leftPic);
            list.set(itemPos-1,currPic);
            adapter.notifyDataSetChanged();
            tNails.clear();
            populateMiniAdapter();
            vp2.setCurrentItem(itemPos-1);
        }
    }

    public void onRetakeClicked(View view){
        int currItem=vp2.getCurrentItem();
        MyPicture currPic=list.get(currItem);
        //so that it wont go back
        currPic.setImg(null);

        String jsonString=new Gson().toJson(currPic);
        String jsonDoc=new Gson().toJson(currentDoc);
        if(jsonString!=null && jsonDoc!=null){
            //Send it both back to cam activity

        }
    }

    public void onEditClicked(View view){
        int currItem=vp2.getCurrentItem();
        MyPicture currPic=list.get(currItem);
        //so that it wont go back
        currPic.setImg(null);
        ArrayList<MyPicture> pics=new ArrayList<>();
        pics.add(currPic);

        String jsonString=UtilityClass.getStringFromObject(pics);
        String jsonDoc=UtilityClass.getStringFromObject(currentDoc);
        if(jsonString!=null && jsonDoc!=null){
            //Send it both back to Bounding Box activity
            Intent intent=new Intent(InDocRecyclerActivity.this,BoxActivity.class);
            intent.putExtra("MyPicture",jsonString);
            intent.putExtra("MyDocument",jsonDoc);
            startActivity(intent);
            finish();
        }
    }

    public void onSwapRightClicked(View view){
        int itemPos=vp2.getCurrentItem();
        MyPicture currPic=list.get(itemPos);
        if(itemPos<list.size()-1){
            MyPicture rightPic=list.get(itemPos+1);
            int currPos=currPic.getPosition();
            currPic.setPosition(rightPic.getPosition());
            rightPic.setPosition(currPos);
            updatePositionInDatabase(rightPic);
            updatePositionInDatabase(currPic);
            list.set(itemPos,rightPic);
            list.set(itemPos+1,currPic);
            adapter.notifyDataSetChanged();
            tNails.clear();
            populateMiniAdapter();
            vp2.setCurrentItem(itemPos+1);
        }
    }
    public void onDeleteClicked(View view){
        int currItem=vp2.getCurrentItem();
        MyPicture currPic=list.get(currItem);
        String uriEdited=currPic.getEditedUri();
        String uriOriginal=currPic.getOriginalUri();
        list.remove(currItem);
        tNails.clear();
        populateMiniAdapter();
        adapter.notifyDataSetChanged();
        resetPositions(currPic.getPosition());
        deleteFromDatabase(currPic.getPid());
        UtilityClass.deleteFromStorage(Uri.parse(uriEdited));
        UtilityClass.deleteFromStorage(Uri.parse(uriOriginal));

    }

    private void resetPositions(int deletedPos) {

    }

    public void onShareClicked(View view){

    }
    public void onRotateClicked(View view){
        int currPicIndex=vp2.getCurrentItem();
        MyPicture currPic=list.get(currPicIndex);
        Bitmap currBitmap=null;
        try{
            InputStream bais=this.getContentResolver().openInputStream(Uri.parse(currPic.getEditedUri()));
            currBitmap=BitmapFactory.decodeStream(bais);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        if(currBitmap==null){
            //load bit map
            Toast.makeText(this, "File NOT EXIST ", Toast.LENGTH_SHORT).show();
        }else{
            Matrix m=new Matrix();
            m.preRotate(90);
            Bitmap copy=Bitmap.createBitmap(currBitmap,0,0,currBitmap.getWidth(),currBitmap.getHeight(),m,true);

            currBitmap.recycle();

            UtilityClass.saveImage(this,copy,Uri.parse(currPic.getEditedUri()).getLastPathSegment(),false);
            int width=vp2.getWidth();
            int height=vp2.getHeight();
            Bitmap resize=UtilityClass.populateImage(this,Uri.parse(currPic.getEditedUri()),false,width,height);
            currPic.setImg(resize);
//            adapter.viewWidth=0;
            adapter.notifyDataSetChanged();


            //   Log.e("TAG", "onRotateClicked: "+Uri.parse(currPic.getEditedUri()).getLastPathSegment() );
            tNails.clear();
            populateMiniAdapter();

        }

    }
    public void onMoveClicked(View view){

    }
    //PAGE SETTINGS END

    //EXTRA

    private void addMorePages() {
        //goto camera activity
    }
    //EXTRA END
}
