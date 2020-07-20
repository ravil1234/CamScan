package com.example.camscan.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.camscan.Adapters.InDocMiniAdapter;
import com.example.camscan.Adapters.InDocRecyclerAdapter;
import com.example.camscan.Database.MyDatabase;
import com.example.camscan.MyCustomPdf;
import com.example.camscan.Objects.MyDocument;
import com.example.camscan.Objects.MyPicture;
import com.example.camscan.R;
import com.example.camscan.RenderScriptJava.FlatCorrection;
import com.example.camscan.UtilityClass;
import com.google.android.gms.common.util.JsonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;

public class InDocRecyclerActivity extends AppCompatActivity {

    ViewPager2 vp2;
    View shareWindow;

    ArrayList<MyPicture> list;
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
    ArrayList<MyPicture> currentPics;

    MyDatabase db;
    //FOR DATABASE END


    //to delete
    ArrayList<Point> points;

    //to delete end

    //PAGE SETTING


    View pageSettingLayout;
    //PAGE SETTING END

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
       //isEmailOpen=true;
       // share();


     //   db=MyDatabase.getInstance(this);
        //delete end


        //setting docName
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
        fillArrayList();
        adapter=new InDocRecyclerAdapter(this,list,new MyOnClickListener(),new MyLongClickListener());
        vp2.setAdapter(adapter);

        currentDoc=new MyDocument("MyDoc",0l,0l,6,list.get(0).getEditedUri());

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

    //-----------------------------------------------------------------------------------------

    private void initializeViews() {
        vp2=findViewById(R.id.in_doc_pageViewer);
        shareWindow=findViewById(R.id.in_doc_share_window);
        miniRView=findViewById(R.id.in_doc_mini_rview);
        miniCont=findViewById(R.id.in_doc_mini_r_cont);

        pageSettingLayout=findViewById(R.id.in_doc_page_setting);


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
    //-----------------------------------------------------------------------------------------
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

        list.add(new MyPicture(0,null,"file:///storage/emulated/0/CamScan/.Edited/Something17869.jpg","Image5",5,points));
        list.add(new MyPicture(0,null,"file:///storage/emulated/0/CamScan/.Edited/Something278100.jpg","Image5",5,points));
        list.add(new MyPicture(0,null,"file:///storage/emulated/0/CamScan/.Edited/Something80568.jpg","Image5",5,points));
        list.add(new MyPicture(0,null,"file:///storage/emulated/0/CamScan/.Edited/Something736207.jpg","Image5",5,points));

    }
    //delete end
//-----------------------------------------------------------------------------------------

    //DATABASE FUNCTIONS
    public void getPictures(){

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
                "Something",list.get(list.size()-1).getPosition()+1,points);
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
                        ArrayList<Point> points=new ArrayList<>();
                        int h=blur.getHeight();
                        int w=blur.getWidth();
                        points.add(new Point(0,0));
                        points.add(new Point(w,0));
                        points.add(new Point(w,h));
                        points.add(new Point(0,h));

                        pic.setCoordinates(points);

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
            Uri savedPdf=savePdf();
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
    public Uri savePdf(){
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

            MyCustomPdf pdf = new MyCustomPdf(this, pdfList, false);
            Uri savedPdf=pdf.savePdf2(currentDoc.getdName());
            if (savedPdf!=null) {
                Toast.makeText(this, "SAVED", Toast.LENGTH_SHORT).show();
                return savedPdf;
            } else {
                Toast.makeText(this, "FAILED", Toast.LENGTH_SHORT).show();
                return null;
            }


        }else{
            ActivityCompat.requestPermissions(this,new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"},101);
            return savePdf();
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
    public void setPageSize(int index){
        SharedPreferences.Editor pref= getSharedPreferences(UtilityClass.PDF_SETTING,MODE_PRIVATE).edit();
        pref.putInt("PDF_PAGE_SIZE",index);
        pref.apply();
    }
    public void setPageOrientation(int option){
        SharedPreferences.Editor pref= getSharedPreferences(UtilityClass.PDF_SETTING,MODE_PRIVATE).edit();
        pref.putInt("PDF_PAGE_ORIENTATION",option);
        pref.apply();
    }
    public void setPageMargin(Boolean mar){
        SharedPreferences.Editor pref= getSharedPreferences(UtilityClass.PDF_SETTING,MODE_PRIVATE).edit();
        pref.putBoolean("PDF_PAGE_MARGIN",mar);
        pref.apply();
    }
    public void setPagePassword(String password){
        SharedPreferences.Editor pref= getSharedPreferences(UtilityClass.PDF_SETTING,MODE_PRIVATE).edit();
        pref.putString("PDF_PAGE_PASSWORD",password);
        pref.apply();
    }
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

        String jsonString=new Gson().toJson(currPic);
        String jsonDoc=new Gson().toJson(currentDoc);
        if(jsonString!=null && jsonDoc!=null){
            //Send it both back to Bounding Box activity

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
        UtilityClass.deleteFromStorage(Uri.parse(uriEdited),false);
        UtilityClass.deleteFromStorage(Uri.parse(uriOriginal),true);

    }

    private void resetPositions(int deletedPos) {

    }

    public void onShareClicked(View view){

    }
    public void onRotateClicked(View view){
        int currPicIndex=vp2.getCurrentItem();
        MyPicture currPic=list.get(currPicIndex);
        Bitmap currBitmap=currPic.getImg();
        if(currBitmap==null){
            //load bit map
            //practically not possible to reach here
            Toast.makeText(this, "LAODING FAILED", Toast.LENGTH_SHORT).show();
        }else{
            Matrix m=new Matrix();
            m.preRotate(90);
            Bitmap copy=Bitmap.createBitmap(currBitmap,0,0,currBitmap.getWidth(),currBitmap.getHeight(),m,true);
            currPic.setImg(copy);
            adapter.notifyDataSetChanged();

            UtilityClass.saveImage(this,copy,Uri.parse(currPic.getEditedUri()).getLastPathSegment(),false);
            Log.e("TAG", "onRotateClicked: "+Uri.parse(currPic.getEditedUri()).getLastPathSegment() );
            tNails.clear();
            populateMiniAdapter();

        }

    }
    public void onMoveClicked(View view){

    }
    //PAGE SETTINGS END

    //JSON FUNCTIONS

    //JSON FUNCTIONS END
}
