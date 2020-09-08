package com.example.camscan.Activities;

import android.animation.LayoutTransition;
import android.app.ActionBar;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.example.camscan.Adapters.InDocMiniAdapter;
import com.example.camscan.Adapters.InDocRecyclerAdapter;
import com.example.camscan.Adapters.NavMenuAdapter;
import com.example.camscan.Callbacks.ItemMoveCallback;
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
import com.example.camscan.RenderScriptJava.rotator;
import com.example.camscan.UtilityClass;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MyDocumentActivity extends AppCompatActivity {

    //VIEWS
    ImageView docIcon;
    //ImageView docRenameBtn;
    //TextView doc_name,doc_pageCount,time_create,last_updated;
    TextView doc_name;
    ImageView doc_about;

    DrawerLayout dl;
    NavigationView nv;
    ListView menuListView;


    ViewPager2 vp2;


    View pageSettingLayout;
    //VIEWS END

    //NAV DRAWER
    ArrayList<NavMenuObject> menuList;
    View aboutUs,shareApp,exitDoc;

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
        setContentView(R.layout.activity_my_document);

        //Debugger.initialize(this);

        ActionBar actionBar = getActionBar();
        actionBar.setTitle("Document");
        initializeViews();

        initializeNavBar();

        initializeVP();

        initializeBottomBar();

        vp2.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        vp2.getLayoutTransition().setDuration(500);



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
                List<MyPicture> lst =getImagesFromDatabase(did);
                if(lst!=null){
                    list.addAll(lst);
                }
                new Thread(new Runnable() {
                    @Override
                    public  void run() {
                        int index=0;
                        for (MyPicture p:list) {
                            if (p.getEditedUri() != null) {
                              //  Log.e("PAGES", "run: "+p.getPosition() );
                                Bitmap b = UtilityClass.populateImage(MyDocumentActivity.this, Uri.parse(p.getEditedUri()),
                                        true, 0, 0);
                                tNails.add(b);
                                if(p.getPosition()==1){
                                    currDoc.setfP_URI(p.getEditedUri());
                                    updateDoc(currDoc);
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(p.getPosition()==1){
                                            updateHeader();
                                        }
                                        miniAdapter.notifyDataSetChanged();
                                    }
                                });
                            }else{
                                //if not edited yet
                                Bitmap original=BitmapFactory.decodeFile(Uri.parse(p.getOriginalUri()).getPath());
                                if(original!=null){
                                    original=cornerPin(original,p.getCoordinates(),p.getS_width(),p.getS_height());
                                    Bitmap emptyBitmap = Bitmap.createBitmap(original.getWidth(), original.getHeight(),
                                            original.getConfig());
                                    if(original.sameAs(emptyBitmap)){
                                        //Toast.makeText(MyDocumentActivity.this, "Can't Crop", Toast.LENGTH_SHORT).show();
                                        Log.e("Thread", "run: "+"Cant Crop"+p.getEditedName() );
                                        index++;
                                        continue;
                                    }
                                    original=getFilteredBitmap(original);
                                    Uri edited=UtilityClass.saveImage(MyDocumentActivity.this,original,Uri.parse(p.getOriginalUri()).getLastPathSegment(),false);
                                    p.setEditedUri(edited.toString());
                                    if(p.getPosition()==1){

                                        if(currDoc.getfP_URI()==null || !currDoc.getfP_URI().equals(p.getEditedUri())){
                                            currDoc.setfP_URI(p.getEditedUri());
                                            updateDoc(currDoc);
                                        }


                                    }

                                    // tNails.set(index,Bitmap.createScaledBitmap(original,100,100,true));
                                    tNails.add(UtilityClass.populateImage(MyDocumentActivity.this,edited,true,0,0));
                                    if (original != null) {
                                        original.recycle();
                                        original = null;
                                    }


                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            //  list.add(p);
                                            updatePic(p);
                                            adapter.notifyDataSetChanged();
                                            miniAdapter.notifyDataSetChanged();
                                            if(p.getPosition()==1){
                                                updateHeader();
                                            }
                                        }
                                    });
                                }else{
                                    Log.e("THIS", "run: "+"LOAD FAILED" );
                                    list.remove(p);
                                }

                            }
                            index++;
                        }
                        isLoading=false;
                        System.gc();
                    }
                }).start();
                adapter.notifyDataSetChanged();

            }else{
                /*from import images create new document*/
                long time=System.currentTimeMillis();
                currDoc=new MyDocument(UtilityClass.appName+UtilityClass.lineSeparator+time%1000000,time,time,null);
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
                        Uri original=UtilityClass.saveImage(MyDocumentActivity.this,currPic,currDoc.getdName()+UtilityClass.lineSeparator+System.currentTimeMillis()%10000,true);
                        p.setDid(currDoc.getDid());
                        p.setOriginalUri(original.toString());
                        p.setCoordinates(null);
                        p.setEditedName(original.getLastPathSegment());
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
                                Bitmap n=UtilityClass.populateImage(MyDocumentActivity.this,edited,true,0,0);
                                tNails.add(n);
                                p.setEditedUri(edited.toString());
                                if(ind==uris.length-1){
                                    isLoading=false;
                                }
                                if(p.getPosition()==1){
                                    if(currDoc.getfP_URI()==null || !currDoc.getfP_URI().equals(p.getEditedUri())){
                                        currDoc.setfP_URI(p.getEditedUri());
                                        updateDoc(currDoc);
                                    }

                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(p.getPosition()==1)
                                            updateHeader();
                                        adapter.notifyDataSetChanged();
                                        miniAdapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        }).start();
                    }
                }
                updateDoc(currDoc);
                isLoading=false;
                System.gc();
            }

        }
        else if(from.equals("BoxActivity")){
            //directly from box activity without any filters applied
            String docString=getIntent().getStringExtra("MyDocument");
            String picString=getIntent().getStringExtra("MyPicture");

            currDoc=UtilityClass.getDocFromJson(docString);
            ArrayList<MyPicture> newPics=UtilityClass.getListOfPics(picString);

            if(currDoc.getDid()==0){
                long i=saveDocInDatabase(currDoc);
                currDoc.setDid((int)i);
            }
            ArrayList<MyPicture> oldPics=getImagesFromDatabase(currDoc.getDid());
            for(MyPicture p:oldPics){
                if(p.getPosition()==1){
                    currDoc.setfP_URI(p.getEditedUri());
                    updateDoc(currDoc);
                    updateHeader();
                }
                Bitmap b=UtilityClass.populateImage(this,Uri.parse(p.getEditedUri()),true,0,0);
                tNails.add(b);
                miniAdapter.notifyDataSetChanged();
            }
            list.addAll(oldPics);

            adapter.notifyDataSetChanged();


            for(MyPicture p:newPics){
                p.setDid(currDoc.getDid());
                addImageInList(p);
            }

            new Thread(new Runnable() {
                @Override
                public synchronized void run() {
                    int index=0;
                    for(MyPicture p:newPics){
                        Bitmap original=BitmapFactory.decodeFile(Uri.parse(p.getOriginalUri()).getPath());
                        if(original!=null){
                            original=cornerPin(original,p.getCoordinates(),p.getS_width(),p.getS_height());
                            Bitmap emptyBitmap = Bitmap.createBitmap(original.getWidth(), original.getHeight(),
                                    original.getConfig());
                            if(original.sameAs(emptyBitmap)){
                                //Toast.makeText(MyDocumentActivity.this, "Can't Crop", Toast.LENGTH_SHORT).show();
                                emptyBitmap.recycle();
                                Log.e("Thread", "run: "+"Cant Crop"+p.getEditedName() );
                                continue;
                            }
                            emptyBitmap.recycle();
                            original=getFilteredBitmap(original);
                            Uri edited=UtilityClass.saveImage(MyDocumentActivity.this,original,Uri.parse(p.getOriginalUri()).getLastPathSegment(),false);
                            p.setEditedUri(edited.toString());
                           // tNails.set(index,Bitmap.createScaledBitmap(original,100,100,true));

                            tNails.add(UtilityClass.populateImage(MyDocumentActivity.this,edited,true,0,0));
                            if (original != null) {
                                original.recycle();
                                original = null;
                            }
                            if(index==newPics.size()-1){
                                //last image
                                isLoading=false;
                            }
                            index++;

                            if(p.getPosition()==1){
                                if(currDoc.getfP_URI()==null || !currDoc.getfP_URI().equals(p.getEditedUri())){
                                    currDoc.setfP_URI(p.getEditedUri());
                                    updateDoc(currDoc);
                                }

                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //  list.add(p);
                                    updatePic(p);
                                    if(p.getPosition()==1)
                                        updateHeader();
                                    adapter.notifyDataSetChanged();
                                    miniAdapter.notifyDataSetChanged();

                                }
                            });
                        }else{
                            Log.e("THIS", "run: "+"LOAD FAILED" );
                            list.remove(p);
                        }


                    }
                    System.gc();
                }
            }).start();


            updateDoc(currDoc);

        }
        else if(from.equals("FilterActivity")){
            String docString=getIntent().getStringExtra("MyDocument");
            String picString=getIntent().getStringExtra("MyPicture");
            currDoc=UtilityClass.getDocFromJson(docString);
            ArrayList<MyPicture> newPics=UtilityClass.getListOfPics(picString);

            //add doc
            if(currDoc.getDid()==0){
                //new doc
                currDoc.setfP_URI(newPics.get(0).getEditedUri());
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
            System.gc();
        }

        //update the mini recycelr view
      //  isLoading=false;
        updateHeader();
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
        doc_about=view.findViewById(R.id.nav_header_about);
        doc_name=view.findViewById(R.id.nav_header_name);

        //others
        shareApp=findViewById(R.id.in_doc_shareApp);
        exitDoc=findViewById(R.id.in_doc_nav_list_exit);
        aboutUs=findViewById(R.id.in_doc_aboutUs);

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

        doc_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //POPUP FRAGMENT WITH DETAILS
                aboutDocClicked();
            }
        });

        TextView tv1=shareApp.findViewById(R.id.item_nav_title);
        tv1.setText("Share App");
        TextView tv2=aboutUs.findViewById(R.id.item_nav_title);
        tv2.setText("About Us");
        TextView tv3=exitDoc.findViewById(R.id.item_nav_title);
        tv3.setText("Close Document");

        ImageView icon_share=shareApp.findViewById(R.id.item_nav_icon);
        icon_share.setImageDrawable(getResources().getDrawable(R.drawable.move_to));
        ImageView icon_about_us=aboutUs.findViewById(R.id.item_nav_icon);
        icon_about_us.setImageDrawable(getResources().getDrawable(R.drawable.about_us));
        ImageView icon_close=exitDoc.findViewById(R.id.item_nav_icon);
        icon_close.setImageDrawable(getResources().getDrawable(R.drawable.cross));

        shareApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onShareAppClicked();
                }
        });
        aboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAboutUsClicked();
            }
        });
        exitDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onExitDocClicked();
            }
        });

        doc_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aboutClicked();
            }
        });

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
        miniAdapter=new InDocMiniAdapter(this, tNails,list, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView num=view.findViewById(R.id.filter_item_name);
                int curr=Integer.parseInt(num.getText().toString())-1;
                vp2.setCurrentItem(curr,true);

               // miniAdapter.notifyItemChanged(curr);

            }
        });

        ItemTouchHelper.Callback callback=new ItemMoveCallback(miniAdapter);
        ItemTouchHelper touchHelper=new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(miniRView);




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

//        CompositePageTransformer cpf=new CompositePageTransformer();
//        cpf.addTransformer(new MarginPageTransformer(40));
//        cpf.addTransformer(new ViewPager2.PageTransformer() {
//            @Override
//            public void transformPage(@NonNull View page, float position) {
//                float r=1-Math.abs(position);
//                page.setScaleY(0.85f+r*0.15f);
//            }
//        });
//
//        vp2.setPageTransformer(cpf);

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
            if(!isLoading){
                switch (i){
                    case 0:{//Import From Gallery
                        importPic();
                        break;
                    }
                    case 1:{//secure pdf
                        if(!isLoading) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(MyDocumentActivity.this);
                            builder.setTitle("Set Password");
                            View dialog = LayoutInflater.from(MyDocumentActivity.this).inflate(R.layout.fragment_rename, null);
                            builder.setCancelable(false);
                            builder.setView(dialog);

                            EditText newNameView = dialog.findViewById(R.id.rename_fragment_eview);
                            newNameView.setSelectAllOnFocus(true);
                            //newNameView.setText(currentDoc1.getDocName());
                            String pass = getPdfPassword();
                            newNameView.setText(pass);


                            newNameView.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View view, MotionEvent motionEvent) {

                                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                                        if (motionEvent.getRawX() >= (newNameView.getRight() - newNameView.getCompoundDrawables()[2].getBounds().width())) {
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
                                    String newPass = newNameView.getText().toString();
                                    if (!newPass.equals("")) {

                                        AlertDialog.Builder builder = new AlertDialog.Builder(MyDocumentActivity.this);
                                        ProgressBar pbar=new ProgressBar(MyDocumentActivity.this,null,android.R.attr.progressBarStyleHorizontal);
                                        pbar.setIndeterminate(false);
                                        pbar.setMax(100);

                                        builder.setView(pbar);
                                        builder.setTitle("Loading");
                                        AlertDialog d = builder.create();
                                        d.setCancelable(false);
                                        d.show();
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Uri pdfUri = savePdf(newPass, true, pbar);
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        d.dismiss();
                                                        if (pdfUri != null) {
                                                            Toast.makeText(MyDocumentActivity.this, "Pdf saved at " + pdfUri, Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(MyDocumentActivity.this, "Pdf Saving Failed", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });


                                            }
                                        }).start();
                                    } else {
                                        Toast.makeText(MyDocumentActivity.this, "Password can't be null", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).setNegativeButton("Cancel", null);
                            builder.create().show();
                        }
                        else{
                            Toast.makeText(MyDocumentActivity.this, "Task In Progress", Toast.LENGTH_SHORT).show();
                        }
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
                    case 7:{//delete doc
                        AlertDialog.Builder builder=new AlertDialog.Builder(MyDocumentActivity.this);
                        builder.setTitle("Do You Really Want to Delete this Document?");
                        builder.setMessage("This will delete all of its images");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteDocument();
                            }
                        }).setNegativeButton("Cancel",null);
                        builder.create().show();
                        break;
                    }

                }
            }else{
                Toast.makeText(MyDocumentActivity.this, "Task In Progress", Toast.LENGTH_SHORT).show();
            }

            dl.closeDrawer(Gravity.LEFT);
            if(i!=4){
                reset();
            }


        }
    }

    public  interface pdfProgress{
        void onUpdate(int perc);
    }


    //LISTENERS END

    //OVERRRIDE
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(abdt.onOptionsItemSelected(item)){
            return true;
        }

        switch(item.getItemId()){
            case R.id.In_doc_menu_edit:{
                if(!isLoading && !isShareOpen && !isImgOpen){
                    expandPager();
                    Toast.makeText(this, "Long Press To Edit", Toast.LENGTH_SHORT).show();
                    isImgOpen=true;
                }else if(isLoading){
                    Toast.makeText(this, "Task In Progress", Toast.LENGTH_SHORT).show();
                }else if(isImgOpen){
                    closePageOption();
                    shareClose();
                    isImgOpen=false;
                }
                break;
            }
            case R.id.In_doc_menu_pdf:{
                if(!isLoading) {
                    convertToPDF();
                }else{
                    Toast.makeText(this, "Task In Progress", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.In_doc_menu_share:{
                if(!isLoading) {
                    share(true);
                }
                else{
                    Toast.makeText(this, "Task In Progress", Toast.LENGTH_SHORT).show();
                }
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
            case UtilityClass.RETAKE_REQ_CODE:{
                if(resultCode==RESULT_OK){
                    //now image is updated
                    int currItem=vp2.getCurrentItem();
                    MyPicture currPic=list.get(currItem);
                    //so that it wont go back
                    currPic.setCoordinates(null);
                    onEditClicked(null);
                }else{
                    Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    //OVERRRIDE END

    //ANIMATE

    //ENIMATE END

    //NAV
    private void populateMenuListItems() {
        menuList.add(new NavMenuObject(false,"Import Image From Gallery",R.drawable.import1,true));
        menuList.add(new NavMenuObject(false,"Secure PDf",R.drawable.pdf_lock,true));
        menuList.add(new NavMenuObject(false,"Pdf Setting",R.drawable.pdf_setting,true));
        //pdf etting
//        menuList.add(new NavMenuObject(false,"Password",0,false));
//        menuList.add(new NavMenuObject(false,"Orientation",0,false));
//        menuList.add(new NavMenuObject(false,"PageSize",0,false));
//        menuList.add(new NavMenuObject(false,"Margin",0,false));
        //
        menuList.add(new NavMenuObject(false,"Email",R.drawable.email,true));
        menuList.add(new NavMenuObject(false,"Share",R.drawable.share_app_black,true));
        //share
//        menuList.add(new NavMenuObject(false,"As Images",0,false));
//        menuList.add(new NavMenuObject(false,"As Long Image",0,false));
//        menuList.add(new NavMenuObject(false,"As PDF",0,false));
        //
        menuList.add(new NavMenuObject(false,"Reverse Order",R.drawable.reverse,true));
        menuList.add(new NavMenuObject(false,"Add more Pages",R.drawable.add_more,true));
        menuList.add(new NavMenuObject(false,"Delete Document",R.drawable.trash,true));

    }
    private boolean noDocLikeThis(String name){
        List<MyDocument> l=MyDatabase.getInstance(this).myDocumentDao().getDocsWithSameName(name);
        if(l!=null){
            if(l.size()==0){
                return true;
            }

            if(l.size()>1){
                return false;
            }
            else {
                if(l.get(0).getDid()==currDoc.getDid()){
                    return true;
                }
                else{
                    return false;
                }
            }
        }
        return true;
    }
    //mini VIew
    private void updateHeader(){
        currDoc=getDocFromDatabase(currDoc.getDid());
        doc_name.setText(currDoc.getdName());
        if(currDoc.getfP_URI()!=null){
            Bitmap b=UtilityClass.populateImage(this,Uri.parse(currDoc.getfP_URI()),true,0,0);
            b=UtilityClass.getRoundedCroppedBitmap(b);
            docIcon.setImageBitmap(b);
        }else{
            if(list!=null && list.size()>0 && list.get(0).getEditedUri()!=null){
                currDoc.setfP_URI(list.get(0).getEditedUri());
            }
        }

    }
    private void aboutClicked(){
        AlertDialog.Builder builder=new AlertDialog.Builder(MyDocumentActivity.this);
        builder.setTitle("Rename Document");
        View dialog=LayoutInflater.from(MyDocumentActivity.this).inflate(R.layout.fragment_rename,null);

        builder.setView(dialog);

        EditText newNameView=dialog.findViewById(R.id.rename_fragment_eview);
        newNameView.setSelectAllOnFocus(true);
        //newNameView.setText(currentDoc1.getDocName());

        newNameView.setText(currDoc.getdName());

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
                String newName=newNameView.getText().toString();
                if(!newName.equals("")){
                    if(noDocLikeThis(newName)){
                        currDoc.setdName(newName);
                        updateDoc(currDoc);
                        updateHeader();
                    }else{
                        Toast.makeText(MyDocumentActivity.this,"A Document With similar name exists",Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(MyDocumentActivity.this, "Name can't be Empty", Toast.LENGTH_SHORT).show();
                }
            }
        }).setNegativeButton("Cancel",null);

        builder.create().show();
    }
    private void aboutDocClicked(){
        AlertDialog.Builder b=new AlertDialog.Builder(this);
        View v= LayoutInflater.from(this).inflate(R.layout.fragment_about_doc,null);
        b.setView(v);
        b.setTitle("Details");
        TextView name,pc,timeCreated,lastModified;
        name=v.findViewById(R.id.frag_about_doc_name);
        pc=v.findViewById(R.id.frag_about_doc_pc);
        timeCreated=v.findViewById(R.id.frag_about_doc_created_at);
        lastModified=v.findViewById(R.id.frag_about_doc_lastModified);

        name.setText(currDoc.getdName());
        int count=MyDatabase.getInstance(MyDocumentActivity.this).myPicDao().getCount(currDoc.getDid());
        pc.setText(String.valueOf(count));
        timeCreated.setText(getDate(currDoc.getTimeCreated()));
        lastModified.setText(getDate(currDoc.getTimeEdited()));

        b.setNegativeButton("Ok",null);

        b.create().show();

    }
    private void onShareAppClicked(){
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
    }
    private void onAboutUsClicked(){
        AlertDialog.Builder builder=new AlertDialog.Builder(MyDocumentActivity.this);
        builder.setTitle("About App");
        builder.setMessage(getResources().getString(R.string.lorem));
        builder.setPositiveButton("OK",null);
        builder.create().show();
    }
    private void onExitDocClicked(){
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
    }
    private String getDate(long timestamp){
        DateFormat d=new SimpleDateFormat("dd/MM/yyyy ");
        return d.format(new Date(timestamp));
    }
    //NAV END

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
        //Toast.makeText(this, "Expanded", Toast.LENGTH_SHORT).show();
//        pageSettingLayout.animate().translationY(-pageSettingLayout.getHeight());

        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.my_doc_anim_slide_up);

        pageSettingLayout.setVisibility(View.VISIBLE);
        pageSettingLayout.startAnimation(slideUp);


        miniCont.setVisibility(View.GONE);
        vp2.setUserInputEnabled(false);

//        pageSettingLayout.setVisibility(View.VISIBLE);

    }

    private void closePageOption() {
        //Toast.makeText(this, "Closed", Toast.LENGTH_SHORT).show();
       // pageSettingLayout.animate().translationY(pageSettingLayout.getHeight());
        Animation slideDown=AnimationUtils.loadAnimation(this,R.anim.my_doc_anim_slide_down);
        pageSettingLayout.startAnimation(slideDown);
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

        if(list.size()>0 && list.get(0).getEditedUri()!=null){
            doc.setfP_URI(list.get(0).getEditedUri());
        }
        MyDatabase db=MyDatabase.getInstance(this);
        MyDocument d=getDocFromDatabase(currDoc.getDid());
        if(doc!=d){
            doc.setTimeEdited(System.currentTimeMillis());
            db.myDocumentDao().updateDoc(doc);

        }
    }
    private void updatePic(MyPicture pic){
        MyDatabase db=MyDatabase.getInstance(this);
        db.myPicDao().updatePic(pic);
    }
    private void deletePicFromDB(MyPicture pic){
        MyDatabase db=MyDatabase.getInstance(MyDocumentActivity.this);
        db.myPicDao().deletePic(pic);
    }
    public void updateListFromMiniAdapter(int i,int j){
        int index=1;
        for(MyPicture p:list){
            p.setPosition(index++);
            updatePic(p);
            adapter.notifyItemMoved(i,j);
        }
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
        if(B!=null){
            B.recycle();
            B=null;
        }
//        Log.e(TAG, "cornerPin: "+ result.getHeight()+" "+result.getWidth());
        //   Log.e(TAG, "cornerPin: "+resized.getHeight()+" "+resized.getWidth() );

        c.setBitmap(null);
        c = null;

        return result;

    }
    //BITMAP OPS END

    //DELETE
    private void deleteDocument(){
        AlertDialog.Builder b=new AlertDialog.Builder(this);
        View v=LayoutInflater.from(this).inflate(R.layout.fragment_progress,null);
        b.setView(v);
        AlertDialog d=b.create();
        d.setCancelable(false);
        d.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                for(MyPicture p:list){
                    Uri u1=Uri.parse(p.getOriginalUri());
                    Uri u2=Uri.parse(p.getEditedUri());

                    try {
                        File f = new File(u1.getPath());
                        if (f.exists()) {
                            f.delete();
                        }
                        f=new File(u2.getPath());
                        if(f.exists()){
                            f.delete();
                        }
                    }catch (NullPointerException e){
                        e.printStackTrace();
                        d.dismiss();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MyDocumentActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    deletePicFromDB(p);
                }
                MyDatabase.getInstance(MyDocumentActivity.this).myDocumentDao().deleteDoc(currDoc);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MyDocumentActivity.this, "Document Deleted", Toast.LENGTH_SHORT).show();
                        d.dismiss();
                        finish();
                    }
                });


            }
        }).start();
    }
    //DELETE END


    //SHARE
    private void share(boolean isFull){
        if(!isShareOpen){
            shareOpen();
        }else{
            shareClose();
            return;
        }
//        if(isFull){
//            //want to send complete document
//            //open share window
//
//        }else{
//            //sharing only a single image
//            //Share as a image
//            //share as a pdf
//            LinearLayout long_box=shareWindow.findViewById(R.id.in_doc_share_frag_long);
//            long_box.setVisibility(View.GONE);
//        }
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
        AlertDialog.Builder builder=new AlertDialog.Builder(MyDocumentActivity.this);
        //View  v=LayoutInflater.from(MyDocumentActivity.this).inflate(R.layout.fragment_progress,null);
        ProgressBar pbar=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        pbar.setIndeterminate(false);
        pbar.setMax(100);

        builder.setView(pbar);
        builder.setTitle("Loading");

        //ProgressBar pb=v.findViewById(R.id.frag_progress_bar);
        AlertDialog d=builder.create();
        d.setCancelable(false);
        d.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Uri savedPdf = savePdf(null, false,pbar);

                if(savedPdf!=null){
                    Intent intentShareFile=new Intent(Intent.ACTION_SEND);
                    intentShareFile.setType("application/pdf");
                    intentShareFile.putExtra(Intent.EXTRA_STREAM, savedPdf);

                    intentShareFile.putExtra(Intent.EXTRA_SUBJECT,
                            "Document from CamScan...");
                    intentShareFile.putExtra(Intent.EXTRA_TEXT, "Document made from CamScan");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            d.dismiss();
                            startActivity(Intent.createChooser(intentShareFile, "Share as Pdf"));
                        }
                    });
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            d.dismiss();
                            Toast.makeText(MyDocumentActivity.this, "Time Out", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        }).start();
        //simple share intent


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
    public void shareAsLongimage(View view) {
        if (list.size() <= 10) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MyDocumentActivity.this);
            View v = LayoutInflater.from(MyDocumentActivity.this).inflate(R.layout.fragment_progress, null);
            builder.setView(v);
            AlertDialog d = builder.create();
            d.setCancelable(false);
            d.show();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    ArrayList<Uri> uris = new ArrayList<>();
                    for (MyPicture p : list) {
                        uris.add(Uri.parse(p.getEditedUri()));
                    }
                    Uri savedLongImage = UtilityClass.saveLongImage(MyDocumentActivity.this, uris, currDoc.getdName());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //simple intent
                            d.dismiss();
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_SEND);
                            //intent.putExtra(Intent.EXTRA_SUBJECT, "Here are some files.");
                            intent.setType("image/jpeg"); /* This example is sharing jpeg images. */

                            intent.putExtra(Intent.EXTRA_STREAM, savedLongImage);
                            startActivity(Intent.createChooser(intent, "Share as LongImage"));

                            shareClose();
                        }
                    });
                }
            }).start();

        }else{
            Toast.makeText(this, "Documents are more than 10", Toast.LENGTH_SHORT).show();
        }
    }

    //PDF
    public Uri savePdf(String pass, boolean isPassSet, ProgressBar pBar){

        if(isPerMissionGranted()){
            ArrayList<Uri> pdfList=new ArrayList<>();
            for(MyPicture p:list){
                pdfList.add(Uri.parse(p.getEditedUri()));
            }

            MyCustomPdf pdf = new MyCustomPdf(this, pdfList, isPassSet);
            Uri savedPdf=pdf.savePdf2(currDoc.getdName(), pass, new pdfProgress() {
                @Override
                public void onUpdate(int perc) {
                    //Log.e("THIS", "onUpdate: "+perc );
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pBar.setProgress(perc);
                            //Log.e("TAG", "run: "+perc );
                        }
                    });

                }
            });
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
            return savePdf(pass,isPassSet,pBar);
        }
    }

    public boolean isPerMissionGranted(){
        if(ContextCompat.checkSelfPermission(MyDocumentActivity.this,"android.permission.WRITE_EXTERNAL_STORAGE")!= PackageManager.PERMISSION_GRANTED){
            return false;
        }
        return true;
    }

    private void convertToPDF(){

        AlertDialog.Builder builder=new AlertDialog.Builder(MyDocumentActivity.this);
        ProgressBar pbar=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        pbar.setIndeterminate(false);
        pbar.setMax(100);

        builder.setView(pbar);
        builder.setTitle("Loading");
        AlertDialog d=builder.create();
        d.setCancelable(false);
        d.show();
        new Thread(new Runnable() {
            @Override
            public void run() {

                Uri pdf_uri=savePdf(null,false,pbar);
                if(pdf_uri!=null){

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            d.dismiss();
                            Toast.makeText(MyDocumentActivity.this, "Pdf Saved at :"+pdf_uri.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

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

    public void addImageWithFilter(@NonNull Uri uri){
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
        String picName=currDoc.dName+UtilityClass.lineSeparator+System.currentTimeMillis()%100000;
        Uri original=UtilityClass.saveImage(MyDocumentActivity.this,img,picName,true);
        p.setOriginalUri(original.toString());
        p.setPosition(list.size()+1);
        p.setEditedName(picName);
        list.add(p);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });

        img=getFilteredBitmap(img);
        Uri edited=UtilityClass.saveImage(MyDocumentActivity.this,img,uri.getLastPathSegment(),false);
        p.setEditedUri(edited.toString());


        long i=addImage(p);

        tNails.add(UtilityClass.populateImage(MyDocumentActivity.this,edited,true,0,0));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                miniAdapter.notifyDataSetChanged();
                adapter.notifyDataSetChanged();
            }
        });

        p.setPid((int)i);
        if(img!=null){
            img.recycle();
            img=null;
        }
    }
    //IMAGE END

    //EMAIL
    private void sendEmail(){

        AlertDialog.Builder builder=new AlertDialog.Builder(MyDocumentActivity.this);
        ProgressBar pbar=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        pbar.setIndeterminate(false);
        pbar.setMax(100);

        builder.setView(pbar);
        builder.setTitle("Loading");
        AlertDialog d=builder.create();
        d.setCancelable(false);
        d.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Uri pdf_uri=savePdf(null,false,pbar);
                if(pdf_uri!=null){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
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
                            d.dismiss();
                            Toast.makeText(MyDocumentActivity.this, "Pdf Failed to Save", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();

    }
    //EMAIL END


    //PAGES
    public void onSwapLeftClicked(View view){
        int itemPos=vp2.getCurrentItem();
        MyPicture currPic=list.get(itemPos);
        if(itemPos!=0){
            MyPicture leftPic=list.get(itemPos-1);
            int currPos=currPic.getPosition();
            currPic.setPosition(leftPic.getPosition());
            leftPic.setPosition(currPos);
            updatePic(leftPic);
            updatePic(currPic);
            list.set(itemPos,leftPic);
            list.set(itemPos-1,currPic);
            adapter.notifyDataSetChanged();
            //swap item pos and itemPos -1 in tNails
            Bitmap tmp=tNails.get(itemPos);
            tNails.set(itemPos,tNails.get(itemPos-1));
            tNails.set(itemPos-1,tmp);
            miniAdapter.notifyDataSetChanged();
            vp2.setCurrentItem(itemPos-1);
        }else{
            Toast.makeText(this, "No Left Image", Toast.LENGTH_SHORT).show();
        }
    }

    public void onRetakeClicked(View view){
        int currItem=vp2.getCurrentItem();
        MyPicture currPic=list.get(currItem);
        //so that it wont go back

        String jsonString=new Gson().toJson(currPic);
        String jsonDoc=new Gson().toJson(currDoc);

        Intent intent=new Intent(MyDocumentActivity.this,SingleCameraActivity.class);
        intent.putExtra("PICTURE_URI",currPic.getOriginalUri());
        startActivityForResult(intent,UtilityClass.RETAKE_REQ_CODE);
    }

    public void onEditClicked(View view){
        int currItem=vp2.getCurrentItem();
        MyPicture currPic=list.get(currItem);
        //so that it wont go back
        ArrayList<MyPicture> pics=new ArrayList<>();
        pics.add(currPic);

        String jsonString=UtilityClass.getStringFromObject(pics);
        String jsonDoc=UtilityClass.getStringFromObject(currDoc);
        if(jsonString!=null && jsonDoc!=null){
            //Send it both back to Bounding Box activity
            Intent intent=new Intent(MyDocumentActivity.this,BoxActivity.class);
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
            updatePic(rightPic);
            updatePic(currPic);
            list.set(itemPos,rightPic);
            list.set(itemPos+1,currPic);
            adapter.notifyItemChanged(itemPos);
            adapter.notifyItemChanged(itemPos+1);
            //swap item pos and itemPos -1 in tNails
            Bitmap tmp=tNails.get(itemPos);
            tNails.set(itemPos,tNails.get(itemPos+1));
            tNails.set(itemPos+1,tmp);
            miniAdapter.notifyDataSetChanged();
            vp2.setCurrentItem(itemPos+1);
        }else{
            Toast.makeText(this, "No Right Image", Toast.LENGTH_SHORT).show();
        }
    }
    public void onDeleteClicked(View view){
        if(list.size()>1) {
            int currItem = vp2.getCurrentItem();
            MyPicture currPic = list.get(currItem);
            String uriEdited = currPic.getEditedUri();
            String uriOriginal = currPic.getOriginalUri();
            list.remove(currItem);
            tNails.remove(currItem);
            miniAdapter.notifyDataSetChanged();
            adapter.notifyItemChanged(currItem);
            resetPositions(currPic.getPosition());
            deletePicFromDB(currPic);
            UtilityClass.deleteFromStorage(Uri.parse(uriEdited));
            UtilityClass.deleteFromStorage(Uri.parse(uriOriginal));
        }else{
            AlertDialog.Builder builder=new AlertDialog.Builder(MyDocumentActivity.this);
            builder.setTitle("Delete");
            builder.setMessage("Deleting this image will delete this document");
            builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    deletePicFromDB(list.get(0));
                    UtilityClass.deleteFromStorage(Uri.parse(list.get(0).getEditedUri()));
                    UtilityClass.deleteFromStorage(Uri.parse(list.get(0).getOriginalUri()));
                    list.clear();
                    MyDatabase db=MyDatabase.getInstance(MyDocumentActivity.this);
                    db.myDocumentDao().deleteDoc(currDoc);
                    finish();
                }
            }).setNegativeButton("Cancel",null);

            builder.create().show();
        }
    }

    private void resetPositions(int deletedPos) {
        for(MyPicture p:list){
            p.setPosition(list.indexOf(p)+1);
            updatePic(p);
        }
    }

    public void onShareClicked(View view){
        int index=vp2.getCurrentItem();
        MyPicture p =list.get(index);
        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        //Log.e("ATG", "onShareClicked: "+p.getEditedUri() );
        intent.putExtra(Intent.EXTRA_STREAM,Uri.parse(p.getEditedUri()));
        startActivity(Intent.createChooser(intent,"Share"));

        closePageOption();
        isImgOpen=false;
    }
    public void onRotateClicked(View view){

        int currPicIndex=vp2.getCurrentItem();
        MyPicture currPic=list.get(currPicIndex);

        AlertDialog.Builder builder=new AlertDialog.Builder(MyDocumentActivity.this);
        View  v=LayoutInflater.from(MyDocumentActivity.this).inflate(R.layout.fragment_progress,null);
        builder.setView(v);
        AlertDialog d=builder.create();
        d.setCancelable(false);
        d.show();



        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap currBitmap=BitmapFactory.decodeFile(Uri.parse(currPic.getEditedUri()).getPath());
                if(currBitmap==null){
                    //load bit map
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            d.dismiss();
                            Toast.makeText(MyDocumentActivity.this, "File NOT EXIST ", Toast.LENGTH_SHORT).show();
                        }
                    });

                }else{


                    rotator r=new rotator(MyDocumentActivity.this);
                    currBitmap=r.rotate(currBitmap,true);

                    tNails.set(currPicIndex,Bitmap.createScaledBitmap(currBitmap,100,100,false));
                    UtilityClass.saveImage(MyDocumentActivity.this,currBitmap,Uri.parse(currPic.getEditedUri()).getLastPathSegment(),false);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            d.dismiss();
                            adapter.notifyItemChanged(currPicIndex);

                            miniAdapter.notifyDataSetChanged();
                        }
                    });


                }

            }
        }).start();

    }
    public void onMoveClicked(View view){
        AlertDialog.Builder builder=new AlertDialog.Builder(MyDocumentActivity.this);
        View view1=LayoutInflater.from(MyDocumentActivity.this).inflate(R.layout.fragment_move_to,null);
        builder.setView(view1);
        builder.setTitle("Move To");

        TextView prev,curr,next;
        SeekBar sBar;
        prev=view1.findViewById(R.id.move_to_prev);
        curr=view1.findViewById(R.id.move_to_curr);
        next=view1.findViewById(R.id.move_to_next);
        sBar=view1.findViewById(R.id.move_to_seekbar);

        //initialize
        int index=vp2.getCurrentItem();
        Point currPoint=new Point();
        curr.setText(String.valueOf(index+1));
        ArrayList<Point> intervals=new ArrayList<>();
        for(int i=0;i<=list.size();i++){
            Point p=new Point(i,i+1);
            if(p.y==index+1){
                p.y+=1;
                currPoint=p;

            }
            if(p.x!=index+1){
                intervals.add(p);
//                Log.e("THA", "onMoveClicked: "+p.x+" "+p.y+" "+list.size() );
            }
        }

        if(currPoint.x==0){
            prev.setText("s");
        }else{
            prev.setText(String.valueOf(currPoint.x));
        }
        if(currPoint.y==intervals.size()+1){
            next.setText("e");
        }else {
            next.setText(String.valueOf(currPoint.y));
        }

        sBar.setMax(intervals.size()-1);
        sBar.setProgress(index);
        sBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Point p=intervals.get(i);
                if(p.x==0){
                    prev.setText("s");
                }else{
                    prev.setText(String.valueOf(p.x));
                }
                if(p.y==intervals.size()+1){
                    next.setText("e");
                }else {
                    next.setText(String.valueOf(p.y));
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //initialize END


        builder.setPositiveButton("Move", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Point p=intervals.get(sBar.getProgress());
                MyPicture pic=list.get(index);
                list.add(p.x,pic);
                Bitmap cur=tNails.get(index);
                tNails.add(p.x,cur);
                if(index>p.x){
                    list.remove(index+1);
                    tNails.remove(index+1);
                }else{
                    list.remove(index);
                    tNails.remove(index);
                }
                adapter.notifyDataSetChanged();
                miniAdapter.notifyDataSetChanged();
                closePageOption();
                isImgOpen=false;
                vp2.setCurrentItem(p.x,true);


                for(MyPicture l:list){
                    l.setPosition(list.indexOf(l)+1);
                    updatePic(l);

                }

            }
        }).setNegativeButton("Cancel",null);
//        AlertDialog d=builder.create();


        builder.create().show();
    }
    //PAGES END

    //COMMON
    private void reset(){
        if(isShareOpen){
            shareClose();
        }
        bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);

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
        intent.putExtra("MyDocument",UtilityClass.getStringFromObject(currDoc));
        intent.putExtra("from","MyDocumentActivity");
        startActivity(intent);
        finish();
    }

    //COMMON END
}


