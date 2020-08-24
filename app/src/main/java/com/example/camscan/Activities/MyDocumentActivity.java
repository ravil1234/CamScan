package com.example.camscan.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.example.camscan.Adapters.InDocMiniAdapter;
import com.example.camscan.Adapters.InDocRecyclerAdapter;
import com.example.camscan.Adapters.NavMenuAdapter;
import com.example.camscan.Database.MyDatabase;
import com.example.camscan.MyCustomPdf;
import com.example.camscan.Objects.MyDocument;
import com.example.camscan.Objects.MyPicture;
import com.example.camscan.Objects.NavMenuObject;
import com.example.camscan.R;
import com.example.camscan.RenderScriptJava.BlackAndWhite;
import com.example.camscan.RenderScriptJava.Filter1;
import com.example.camscan.RenderScriptJava.FlatCorrection;
import com.example.camscan.RenderScriptJava.GrayScale;
import com.example.camscan.RenderScriptJava.Inversion;
import com.example.camscan.UtilityClass;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;

import org.spongycastle.pqc.math.ntru.util.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;

public class MyDocumentActivity extends AppCompatActivity {

    //VIEWS
    ImageView docIcon;
    ImageView docRenameBtn;
    TextView doc_name,doc_pageCount,time_create,last_updated;

    DrawerLayout dl;
    NavigationView nv;
    ListView menuListView;


    ViewPager2 vp2;


    View pageSettingLayout;
    //VIEWS END

    //NAV DRAWER
    ArrayList<NavMenuObject> menuList;
    ActionBarDrawerToggle abdt;
    NavMenuAdapter menuAdapter;
    //NAV DRAWER END


    //VP ADAPTER
    ArrayList<MyPicture> list;
    InDocRecyclerAdapter adapter;
    boolean isImgOpen=false;
    boolean isLoading=true;
    //VP ADAPTER END

    //BOTTOM SHEET BEHAVIOUR
    RecyclerView miniRView;
    LinearLayout miniCont;

    BottomSheetBehavior bsb;

    ImageView bottom_draggable;

        //MINI ADAPTER
        ArrayList<Bitmap> tNails;
        InDocMiniAdapter miniAdapter;
        //MINI ADAPTER END

    //BOTTOM SHEET END

    //DATABASE

    MyDocument currDoc;
    //dATABASE END

    //SHARE
    View shareWindow;
    boolean isShareOpen=false;
    //SHARE END

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_doc_recycler);


        initializeViews();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initializeNavBar();
        initializeBottomBar();
        initializeVP();

    }

    private void fillImages() {
        String from=getIntent().getStringExtra("from");
        if(from==null){
            Toast.makeText(this,"Can't Load Image",Toast.LENGTH_SHORT).show();
            return;
        }
        if(from.equals("HomeScreenActivity")){
            //from Main Activity
            int did=getIntent().getIntExtra("did",-1);
            if(did!=-1){
                //from on item click
                currDoc=getDocFromDatabase(did);
                list=getImagesFromDatabase(did);
            }else{
                //from import images

                //create new document

                currDoc=new MyDocument(UtilityClass.getUniqueDocName(),System.currentTimeMillis(),System.currentTimeMillis(),0,null);
                long newDid=saveDocInDatabase(currDoc);
                currDoc.setDid((int)newDid);
                String[] uris=getIntent().getStringArrayExtra("uris");
                //copy them in private folder
                Bitmap currPic=null;
                ArrayList<MyPicture> newPics=new ArrayList<>();
                int index=1;
                for(String u:uris){
                    currPic=null;
                    currPic= BitmapFactory.decodeFile(Uri.parse(u).getPath());

                    if(currPic!=null){
                        final MyPicture p=new MyPicture();
                        Uri original=UtilityClass.saveImage(MyDocumentActivity.this,currPic,"CSImport",true);
                        p.setDid(currDoc.getDid());
                        p.setOriginalUri(original.toString());
                        p.setCoordinates(null);
                        p.setEditedName(original.getLastPathSegment());
                        p.setDid(0);
                        p.setPid(0);
                        p.setPosition(index++);
                        addImageInList(p);

                        //now apply filter

                        final Bitmap finalCopy=currPic.copy(currPic.getConfig(),false);
                        final int ind=index-1;
                        new Thread(new Runnable() {
                            @Override
                            public synchronized void run() {
                                Bitmap filtered=getFilteredBitmap(finalCopy);
                                Uri edited=UtilityClass.saveImage(MyDocumentActivity.this,filtered,Uri.parse(p.getOriginalUri()).getLastPathSegment(),false);
                                p.setEditedUri(edited.toString());
                                if(ind==uris.length-1){
                                    isLoading=false;
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        }).start();
                    }
                }
                currDoc.setpCount(list.size());
                updateDoc(currDoc);

            }

        }
        else if(from.equals("BoxActivity")){
            //directly from box activity without any filters applied
            String docString=getIntent().getStringExtra("MyDocument");
            String picString=getIntent().getStringExtra("MyPicture");
            String[] dimss=getIntent().getStringExtra("Dimensions").split(" ");
            currDoc=UtilityClass.getDocFromJson(docString);
            ArrayList<MyPicture> newPics=UtilityClass.getListOfPics(picString);

            if(currDoc.getDid()==0){
                long i=saveDocInDatabase(currDoc);
                currDoc.setDid((int)i);
            }

            list.addAll(getImagesFromDatabase(currDoc.getDid()));
            adapter.notifyDataSetChanged();

            int w=Integer.valueOf(dimss[0]);
            int h=Integer.valueOf(dimss[1]);


            for(MyPicture p:newPics){
                addImageInList(p);

            }

            new Thread(new Runnable() {
                @Override
                public synchronized void run() {
                    int index=0;
                    for(MyPicture p:newPics){
                        Bitmap original=BitmapFactory.decodeFile(Uri.parse(p.getOriginalUri()).getPath());
                        if(original!=null){
                            original=cornerPin(original,p.getCoordinates(),w,h);
                            Bitmap emptyBitmap = Bitmap.createBitmap(original.getWidth(), original.getHeight(),
                                    original.getConfig());
                            if(original.sameAs(emptyBitmap)){
                                //Toast.makeText(MyDocumentActivity.this, "Can't Crop", Toast.LENGTH_SHORT).show();
                                Log.e("Thread", "run: "+"Cant Crop"+p.getEditedName() );
                                continue;
                            }
                            original=getFilteredBitmap(original);
                            Uri edited=UtilityClass.saveImage(MyDocumentActivity.this,original,Uri.parse(p.getOriginalUri()).getLastPathSegment(),false);
                            p.setEditedUri(edited.toString());
                           // tNails.set(index,Bitmap.createScaledBitmap(original,100,100,true));
                            p.setDid(currDoc.getDid());
                            tNails.add(UtilityClass.populateImage(MyDocumentActivity.this,edited,true,0,0));
                            if (original != null) {
                                original.recycle();
                                original = null;
                            }
                            if(index>=newPics.size()){
                                isLoading=false;
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //  list.add(p);
                                    updatePic(p);
                                    adapter.notifyDataSetChanged();
                                    miniAdapter.notifyDataSetChanged();

                                }
                            });
                        }else{
                            Log.e("THIS", "run: "+"LOAD FAILED" );
                        }


                    }
                }
            }).start();
/*
            for(MyPicture p:newPics){
                Bitmap original=BitmapFactory.decodeFile(Uri.parse(p.getOriginalUri()).getPath());
                if(original!=null){
                    original=cornerPin(original,p.getCoordinates(),w,h);
                    Bitmap emptyBitmap = Bitmap.createBitmap(original.getWidth(), original.getHeight(),
                            original.getConfig());
                    if(original.sameAs(emptyBitmap)){
                        Toast.makeText(this, "Can't Crop", Toast.LENGTH_SHORT).show();
                        continue;
                    }
                    Bitmap finalOriginal = original;
                    addImageInList(p);
                    Thread t=new Thread(new Runnable() {
                        @Override
                        public synchronized void run() {
                            Bitmap filtered=getFilteredBitmap(finalOriginal);
                            Uri edited=UtilityClass.saveImage(MyDocumentActivity.this,filtered,Uri.parse(p.getOriginalUri()).getLastPathSegment(),false);
                            p.setEditedUri(edited.toString());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                  //  list.add(p);
                                    finalOriginal.recycle();
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        }
                    });
                    t.start();
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }else{
                    Log.e("THIS", "fillImages: "+"Image Load ERROR 212" );
                    newPics.remove(p);
                }
            }
*/
            currDoc.setpCount(currDoc.getpCount()+newPics.size());
            updateDoc(currDoc);

        }
        else if(from.equals("FilterActivity")){
            String docString=getIntent().getStringExtra("MyDocument");
            String picString=getIntent().getStringExtra("MyPicture");
            currDoc=UtilityClass.getDocFromJson(docString);
            ArrayList<MyPicture> newPics=UtilityClass.getListOfPics(picString);

            //add doc
            if(currDoc.getDid()==0){
                Log.e("FILTER OPTION", "fillImages: "+"did 0" );
                long i=saveDocInDatabase(currDoc);
                currDoc.setDid((int)i);
                newPics.get(0).setDid((int)i);
            }

            //only one image
            long id=addImage(newPics.get(0));
            newPics=getImagesFromDatabase(currDoc.getDid());
            for(MyPicture p:newPics){
                tNails.add(UtilityClass.populateImage(MyDocumentActivity.this,Uri.parse(p.getEditedUri()),true,0,0));
                miniAdapter.notifyDataSetChanged();
            }
            isLoading=false;
            list.addAll(newPics);
            adapter.notifyDataSetChanged();
        }

        //update the mini recycelr view

    }

    private void initializeViews() {
        vp2=findViewById(R.id.in_doc_pageViewer);
        shareWindow=findViewById(R.id.in_doc_share_window);
        miniRView=findViewById(R.id.in_doc_mini_rview);
        miniCont=findViewById(R.id.in_doc_mini_r_cont);

        pageSettingLayout=findViewById(R.id.in_doc_page_setting);

        menuListView=findViewById(R.id.in_doc_nav_list);
        dl=findViewById(R.id.in_doc_drawer);
        nv=findViewById(R.id.in_doc_nav);
        bottom_draggable=findViewById(R.id.in_doc_recyclerDraggable);

        //header
        View view=findViewById(R.id.in_doc_nav_header);
        docIcon=view.findViewById(R.id.nav_header_image);
        docRenameBtn=view.findViewById(R.id.nav_header_rename);
        doc_name=view.findViewById(R.id.nav_header_name);
        doc_pageCount=view.findViewById(R.id.nav_header_pageCount);
        time_create=view.findViewById(R.id.nav_header_createTime);
        last_updated=view.findViewById(R.id.nav_header_lastUpdateTime);

    }

    private void initializeNavBar(){
        abdt = new ActionBarDrawerToggle(this, dl,R.string.open, R.string.close);

        dl.addDrawerListener(abdt);
        abdt.syncState();

        //MENU
        menuList=new ArrayList<>();


        populateMenuListItems();
        menuAdapter=new NavMenuAdapter(this,menuList);
        menuListView.setAdapter(menuAdapter);
        menuListView.setOnItemClickListener(new MyMenuOnClickListener());
        //MENU END

        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

    }
    private void initializeBottomBar(){
        //MINI ADAPTER
        tNails=new ArrayList<>();
        miniAdapter=new InDocMiniAdapter(this, tNails, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView num=view.findViewById(R.id.filter_item_name);
                int curr=Integer.parseInt(num.getText().toString())-1;
                vp2.setCurrentItem(curr,true);

               // miniAdapter.notifyItemChanged(curr);

            }
        });
        miniRView.setAdapter(miniAdapter);
        miniRView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));

        View bs=findViewById(R.id.in_doc_bottom_sheet);
        bsb=BottomSheetBehavior.from(bs);

        bsb.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState==BottomSheetBehavior.STATE_HIDDEN){
                    bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        bottom_draggable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isImgOpen){
                    isImgOpen=false;
                    bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }else{
                    isImgOpen=true;
                    bsb.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });



        //MINI ADAPTER END
    }
    private void initializeVP(){
        //VP
        list=new ArrayList<>();
        adapter=new InDocRecyclerAdapter(this,list,new MyOnClickListener(),new MyLongClickListener());
        vp2.setAdapter(adapter);


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

        vp2.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                vp2.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int w=vp2.getWidth();
                int h=vp2.getHeight();
                adapter.getDimensions(w,h);


                fillImages();

//                MyPicture p=new MyPicture(0,null,"file:///storage/emulated/0/CamScan/.Edited/930233.jpg","SOMETHING",1,null);
//                list.add(p);
//                adapter.notifyDataSetChanged();
//                Bitmap b= UtilityClass.populateImage(MyDocumentActivity.this,Uri.parse(p.getEditedUri()),true,0,0);
//                tNails.add(b);
//                miniAdapter.notifyDataSetChanged();

            }
        });

        //VP END
    }


    //LISTENERS
    private class MyLongClickListener implements View.OnLongClickListener{

        @Override
        public boolean onLongClick(View view) {
            if(!isImgOpen){
                isImgOpen=true;
                expandPager();
            }

            reset();
            return true;
        }
    }

    private class MyOnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            if(isImgOpen){
                isImgOpen=false;
                closePageOption();
            }

            reset();

        }
    }


    private class MyMenuOnClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            switch (i){
                case 0:{//EDIT page
                        if(!isLoading && !isShareOpen && !isImgOpen){
                            expandPager();
                            isImgOpen=true;
                        }
                    break;
                }
                case 1:{//secure pdf
                    AlertDialog.Builder builder=new AlertDialog.Builder(MyDocumentActivity.this);
                    builder.setTitle("Set Password");
                    View dialog=LayoutInflater.from(MyDocumentActivity.this).inflate(R.layout.fragment_rename,null);

                    builder.setView(dialog);

                    EditText newNameView=dialog.findViewById(R.id.rename_fragment_eview);
                    newNameView.setSelectAllOnFocus(true);
                    //newNameView.setText(currentDoc1.getDocName());
                    String pass=getPdfPassword();
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
                                Toast.makeText(MyDocumentActivity.this,"Pdf saved at "+pdfUri, Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(MyDocumentActivity.this, "Password can't be null", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).setNegativeButton("Cancel",null);

                    builder.create().show();

                    break;
                }
                case 2:{//pdf setting
                    Intent intent=new Intent(MyDocumentActivity.this,PdfSettingsActivity.class);
                    startActivity(intent);
                    break;
                }
                case 3:{//email
                        sendEmail();
                    break;
                }
                case 4:{//share
                        share(true);
                    break;
                }

                case 5:{//Reverse Items
                    reverseList();
                    break;
                }
                case 6:{//Add more Pages
                    addMorePages();
                    break;
                }
                case 7:{//About us
                    AlertDialog.Builder builder=new AlertDialog.Builder(MyDocumentActivity.this);
                    builder.setTitle("About App");
                    builder.setMessage(getResources().getString(R.string.lorem));
                    builder.setPositiveButton("OK",null);
                    builder.create().show();
                    break;
                }
                case 8:{//Share app
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
                case 9:{//Exit
                    AlertDialog.Builder builder=new AlertDialog.Builder(MyDocumentActivity.this);
                    builder.setTitle("Close This Document?");
//                    builder.setMessage("Close This Document");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    }).setNegativeButton("No",null);
                    builder.create().show();
                    break;
                }



            }

            dl.closeDrawer(Gravity.LEFT);
            reset();

        }
    }
    //LISTENERS END

    //OVERRRIDE
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(abdt.onOptionsItemSelected(item)){
            return true;
        }

        switch(item.getItemId()){
            case R.id.In_doc_menu_import:{
                importPic();
                break;
            }
            case R.id.In_doc_menu_pdf:{
                convertToPDF();
                break;
            }
            case R.id.In_doc_menu_share:{
                share(true);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.in_doc_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case UtilityClass.IMPORT_REQ_CODE: {
                if (resultCode == RESULT_OK) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (data.getClipData() != null) {

                                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                                    Uri imgUri = data.getClipData().getItemAt(i).getUri();
                                    addImageWithFilter(imgUri);
                                }

                            } else if (data.getData() != null) {
                                Uri imgUri = data.getData();

                                addImageWithFilter(imgUri);


                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.e("TAG", "onActivityResult: Image can't be clipped");
                                    }
                                });
                            }
                        }
                    }).start();


                } else {
                    Toast.makeText(this, "Image can't be imported", Toast.LENGTH_SHORT).show();
                }

                break;
            }
        }
    }

    //OVERRRIDE END

    //NAV
    private void populateMenuListItems() {
        menuList.add(new NavMenuObject(false,"Edit Page",R.drawable.ic_picture_as_pdf_black_24dp,true));
        menuList.add(new NavMenuObject(false,"Secure PDf",R.drawable.ic_picture_as_pdf_black_24dp,true));
        menuList.add(new NavMenuObject(false,"Pdf Setting",R.drawable.ic_picture_as_pdf_black_24dp,true));
        //pdf etting
//        menuList.add(new NavMenuObject(false,"Password",0,false));
//        menuList.add(new NavMenuObject(false,"Orientation",0,false));
//        menuList.add(new NavMenuObject(false,"PageSize",0,false));
//        menuList.add(new NavMenuObject(false,"Margin",0,false));
        //
        menuList.add(new NavMenuObject(false,"Email",R.drawable.ic_picture_as_pdf_black_24dp,true));
        menuList.add(new NavMenuObject(false,"Share",R.drawable.ic_picture_as_pdf_black_24dp,true));
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
        menuList.add(new NavMenuObject(false,"Close Document",R.drawable.ic_picture_as_pdf_black_24dp,true));

    }
    //NAV END

    //mini VIew
    private void addIntoMiniAdapter(){
        for(MyPicture p:list){
            updateMiniAdapter(p);
        }
    }
    private void updateMiniAdapter(MyPicture p){
        Uri uri=Uri.parse(p.getOriginalUri());
        if(p.getEditedUri()!=null){
            uri=Uri.parse(p.getEditedUri());
        }
        if(uri==null){
            uri=Uri.parse(p.getOriginalUri());
        }
        Bitmap Tn=UtilityClass.populateImage(this,uri,true,0,0);
        tNails.add(Tn);
        miniAdapter.notifyDataSetChanged();
    }
    //mini VIEW END

    //VP
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
    private MyPicture addImageInList(MyPicture p){
        //add in database
        long id=addImage(p);
        p.setPid((int)id);
        list.add(p);
        adapter.notifyDataSetChanged();
        return p;
    }
    //vP END

    //DATABASE
    private MyDocument getDocFromDatabase(int did){
        MyDatabase db=MyDatabase.getInstance(this);
        return db.myDocumentDao().getDocumentWithId(did);
    }
    private ArrayList<MyPicture> getImagesFromDatabase(int did){
        MyDatabase db=MyDatabase.getInstance(this);
        return new ArrayList<>(db.myPicDao().getDocPics(did));
    }
    private long addImage(MyPicture p){
        MyDatabase db=MyDatabase.getInstance(this);
        return db.myPicDao().insertIntoDoc(p);
    }
    private long saveDocInDatabase(MyDocument d){
        MyDatabase db=MyDatabase.getInstance(this);
        return db.myDocumentDao().insertNewDoc(d);
    }
    private void updateDoc(MyDocument doc){
        MyDatabase db=MyDatabase.getInstance(this);
        db.myDocumentDao().updateDoc(doc);
    }
    private void updatePic(MyPicture pic){
        MyDatabase db=MyDatabase.getInstance(this);
        db.myPicDao().updatePic(pic);
    }
    //DATABASE END

    //BITMAP OPS
    private int getDefaultBitmapFilter(){
        return 2;
    }
    private Bitmap getFilteredBitmap(Bitmap bitmap){
        Bitmap result=null;
        switch (getDefaultBitmapFilter()){
            case 0:{//original
                result=bitmap;

                break;
            }
            case 1:{//luminious
                Filter1 fil=new Filter1(this);
                result=fil.filter(100,bitmap);
                // result=applyExposure(100,transformed);
                fil.cleanUp();
                break;
            }
            case 2:{//flat correction
                FlatCorrection corr=new FlatCorrection(this);
                result=corr.flatCorr(bitmap);
                corr.clear();
                break;
            }
            case 3:{//grayscale
                GrayScale gs=new GrayScale();
                result=gs.toGrayscale(bitmap);

                break;
            }
            case 4:{//bnw
                BlackAndWhite bnw=new BlackAndWhite(this);
                result=bnw.toBnwRender(bitmap);
                bnw.clear();
                break;
            }
            case 5:{//inverted
                Inversion inv=new Inversion(this);
                result=inv.setInversion(bitmap);
                inv.clear();
                break;
            }
        }
        return result;
    }

    public Bitmap cornerPin(Bitmap B,ArrayList<Point> dis,int ImgWidth,int imgHeight){
        //Width= width of the boxAcivity
        //Height=height of the boxActivity

        int w=B.getWidth();
        int h=B.getHeight();


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


//        Log.e(TAG, "cornerPin: "+dis );
        Bitmap result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        Canvas c = new Canvas(result);
        Matrix m = new Matrix();
        m.setPolyToPoly(dst, 0, src, 0, 4);
        c.setMatrix(m);
        c.drawBitmap(B, 0,0, p);

        result=Bitmap.createBitmap(result,minLeft,minTop,wid,hei);
//        Log.e(TAG, "cornerPin: "+ result.getHeight()+" "+result.getWidth());
        //   Log.e(TAG, "cornerPin: "+resized.getHeight()+" "+resized.getWidth() );

        c.setBitmap(null);
        c = null;

        return result;

    }
    //BITMAP OPS END

    //SHARE
    private void share(boolean isFull){
        if(!isShareOpen){
            shareOpen();
        }else{
            shareClose();
            return;
        }
        if(isFull){
            //want to send complete document
            //open share window

        }else{
            //sharing only a single image
            //Share as a image
            //share as a pdf
            LinearLayout long_box=shareWindow.findViewById(R.id.in_doc_share_frag_long);
            long_box.setVisibility(View.GONE);
        }
    }
    private void shareOpen(){
        if(!isShareOpen){
            isShareOpen=true;
            shareWindow.setVisibility(View.VISIBLE);

        }
    }
    private void shareClose(){
        if(isShareOpen){
            isShareOpen=false;
            shareWindow.setVisibility(View.GONE);
            LinearLayout long_box=shareWindow.findViewById(R.id.in_doc_share_frag_long);
            long_box.setVisibility(View.VISIBLE);
        }
    }

    public void shareAsPdf(View view){
        if(currDoc.getPdf_uri()!=null){
            File f=new File(Uri.parse(currDoc.getPdf_uri()).getPath());
            if(f.exists()){
                f.delete();
            }
        }
        Uri savedPdf=savePdf(null,false);

        //simple share intent
        if(savedPdf!=null){
                Intent intentShareFile=new Intent(Intent.ACTION_SEND);
                intentShareFile.setType("application/pdf");
                intentShareFile.putExtra(Intent.EXTRA_STREAM, savedPdf);

                intentShareFile.putExtra(Intent.EXTRA_SUBJECT,
                        "Document from CamScan...");
                intentShareFile.putExtra(Intent.EXTRA_TEXT, "Document made from CamScan");

                startActivity(Intent.createChooser(intentShareFile, "Share as Pdf"));
            }

        shareClose();

    }
    public void shareAsImages(View view){
        ArrayList<Uri> uris=new ArrayList<>();
        for(MyPicture p:list){
            uris.add(Uri.parse(p.getEditedUri()));
        }

            //simple share intent
        if(!uris.isEmpty()){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            intent.putExtra(Intent.EXTRA_SUBJECT, "Here are some files.");
            intent.setType("image/jpeg"); /* This example is sharing jpeg images. */

            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            startActivity(Intent.createChooser(intent,"Send Via"));
            }


        shareClose();
    }
    public void shareAsLongimage(View view){

        AlertDialog.Builder builder=new AlertDialog.Builder(MyDocumentActivity.this);
        View  v=LayoutInflater.from(MyDocumentActivity.this).inflate(R.layout.fragment_progress,null);
        builder.setView(v);
        AlertDialog d=builder.create();
        d.show();
        stopInteraction();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Uri> uris=new ArrayList<>();
                for(MyPicture p:list){
                    uris.add(Uri.parse(p.getEditedUri()));
                }
                Uri savedLongImage=UtilityClass.saveLongImage(MyDocumentActivity.this,uris,currDoc.getdName());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //simple intent
                        resumeInteraction();
                        d.dismiss();
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        //intent.putExtra(Intent.EXTRA_SUBJECT, "Here are some files.");
                        intent.setType("image/jpeg"); /* This example is sharing jpeg images. */

                        intent.putExtra(Intent.EXTRA_STREAM, savedLongImage);
                        startActivity(Intent.createChooser(intent,"Share as LongImage"));

                        shareClose();
                    }
                });
            }
        }).start();


    }

    //PDF
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
            Uri savedPdf=pdf.savePdf2(currDoc.getdName(),pass);
            if (savedPdf!=null) {
                //Toast.makeText(this, "SAVED", Toast.LENGTH_SHORT).show();
                Log.e("PDF MY DOCACTIVITY", "savePdf: "+"PDF SAVED" );
                return savedPdf;
            } else {
                Log.e("PDF MY DOCACTIVITY", "savePdf: "+"PDF FAILED" );
                return null;
            }


        }else{
            ActivityCompat.requestPermissions(this,new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"},101);
            return savePdf(pass,isPassSet);
        }
    }

    public boolean isPerMissionGranted(){
        if(ContextCompat.checkSelfPermission(MyDocumentActivity.this,"android.permission.WRITE_EXTERNAL_STORAGE")!= PackageManager.PERMISSION_GRANTED){
            return false;
        }
        return true;
    }

    private void convertToPDF(){
        if(currDoc.getPdf_uri()!=null){
            File f=new File(Uri.parse(currDoc.getPdf_uri()).getPath());
            if(f.exists()){
                f.delete();
            }
        }
        AlertDialog.Builder builder=new AlertDialog.Builder(MyDocumentActivity.this);
        View  v=LayoutInflater.from(MyDocumentActivity.this).inflate(R.layout.fragment_progress,null);
        builder.setView(v);
        AlertDialog d=builder.create();
        d.show();
        stopInteraction();
        new Thread(new Runnable() {
            @Override
            public void run() {

                Uri pdf_uri=savePdf(null,false);
                if(pdf_uri!=null){
                    currDoc.setPdf_uri(pdf_uri.toString());
                    updateDoc(currDoc);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            resumeInteraction();
                            d.dismiss();
                            Toast.makeText(MyDocumentActivity.this, "Pdf Saved at :"+pdf_uri.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            resumeInteraction();
                            d.dismiss();
                            Toast.makeText(MyDocumentActivity.this, "Pdf Failed to Save", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        }).start();
    }

    private String getPdfPassword(){
        SharedPreferences pref=this.getSharedPreferences(UtilityClass.PDF_SETTING, Context.MODE_PRIVATE);

        return pref.getString("PDF_PAGE_PASSWORD","admin");
    }

    //PDF END
    //SHARE END

    //IMPORT IMAGE
    public void importPic(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), UtilityClass.IMPORT_REQ_CODE);
    }

    public void addImageWithFilter(Uri uri){
        InputStream is=null;

        try{
            is=this.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Bitmap img=null;
        if(is!=null){

            img=BitmapFactory.decodeStream(is);
        }
        if(img==null){
            Log.e("THIS", "addImageWithFilter: "+"FAILED TO IMPORT" );
            return;
        }
        MyPicture p=new MyPicture();
        p.setDid(currDoc.getDid());
        p.setCoordinates(null);
        Uri original=UtilityClass.saveImage(MyDocumentActivity.this,img,uri.getLastPathSegment(),true);
        p.setOriginalUri(original.toString());
        img=getFilteredBitmap(img);
        Uri edited=UtilityClass.saveImage(MyDocumentActivity.this,img,uri.getLastPathSegment(),false);
        p.setEditedUri(edited.toString());
        p.setPosition(list.size()+1);
        p.setEditedName(String.valueOf(list.size()+1));

        long i=addImage(p);
        list.add(p);
        tNails.add(UtilityClass.populateImage(MyDocumentActivity.this,edited,true,0,0));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                miniAdapter.notifyDataSetChanged();
                adapter.notifyDataSetChanged();
            }
        });

        p.setPid((int)i);
    }
    //IMAGE END

    //EMAIL
    private void sendEmail(){
        if(currDoc.getPdf_uri()!=null){
            File f=new File(Uri.parse(currDoc.getPdf_uri()).getPath());
            if(f.exists()){
                f.delete();
            }
        }
        AlertDialog.Builder builder=new AlertDialog.Builder(MyDocumentActivity.this);
        View  v=LayoutInflater.from(MyDocumentActivity.this).inflate(R.layout.fragment_progress,null);
        builder.setView(v);
        AlertDialog d=builder.create();
        d.show();
        stopInteraction();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Uri pdf_uri=savePdf(null,false);
                if(pdf_uri!=null){
                    currDoc.setPdf_uri(pdf_uri.toString());
                    updateDoc(currDoc);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            resumeInteraction();
                            d.dismiss();

                            Intent emailIntent = new Intent(Intent.ACTION_SEND);
                            // set the type to 'email'
                            emailIntent .setType("vnd.android.cursor.dir/email");

                            emailIntent .putExtra(Intent.EXTRA_STREAM, pdf_uri);
                            // the mail subject
                            emailIntent .putExtra(Intent.EXTRA_SUBJECT, "Document By CamScan");
                            startActivity(Intent.createChooser(emailIntent , "Send email..."));
                          }
                    });
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            resumeInteraction();
                            d.dismiss();
                            Toast.makeText(MyDocumentActivity.this, "Pdf Failed to Save", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();

    }
    //EMAIL END

    //COMMON
    private void reset(){
        if(isShareOpen){
            shareClose();
        }
        bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);

    }
    public void stopInteraction(){
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
    public void resumeInteraction(){
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
    public void reverseList(){
        Collections.reverse(list);
        adapter.notifyDataSetChanged();
        int index=0;
        for (MyPicture p:list){
            p.setPosition(index+1);
            index++;
            updatePic(p);
        }
        Collections.reverse(tNails);
        miniAdapter.notifyDataSetChanged();
    }

    private void addMorePages() {
        //goto camera activity
        Intent intent=new Intent(MyDocumentActivity.this,MainActivity.class);
        intent.putExtra(UtilityClass.getStringFromObject(currDoc),"MyDocument");
        intent.putExtra("from","InDocRecyclerActivity");
        startActivity(intent);
    }

    //COMMON END


}


