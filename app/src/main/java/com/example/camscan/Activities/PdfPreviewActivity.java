package com.example.camscan.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.camscan.Adapters.PdfPreviewAdapter;
import com.example.camscan.Callbacks.ItemMoveCallback;
import com.example.camscan.Database.MyDatabase;
import com.example.camscan.MyCustomPdf;
import com.example.camscan.Objects.MyDocument;
import com.example.camscan.Objects.MyPicture;
import com.example.camscan.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class PdfPreviewActivity extends AppCompatActivity {

    ArrayList<Uri> imgUris;
   // ArrayList<MyPicture> list;
    MyDocument currDoc;
    RecyclerView rView;
    PdfPreviewAdapter adapter;
    int did;
    boolean isChanged=false;


    BottomNavigationView bnv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_preview);

        getSupportActionBar().setTitle("Pdf Preview");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        did=getIntent().getIntExtra("DocDID",-1);
        initializeViews();
        imgUris=new ArrayList<>();

        fillList();
        adapter=new PdfPreviewAdapter(PdfPreviewActivity.this,imgUris);
        rView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        rView.setAdapter(adapter);


        ItemTouchHelper.Callback callback=new ItemMoveCallback(adapter);
        ItemTouchHelper touchHelper=new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(rView);
        bnv.setOnNavigationItemSelectedListener(new MyNavListener());

    }

    private void fillList() {
        if(did!=-1){
            MyDatabase db=MyDatabase.getInstance(this);
            currDoc=db.myDocumentDao().getDocumentWithId(did);
            List<MyPicture> pics=db.myPicDao().getDocPics(did);
            if(pics!=null){
                //list.addAll(pics);
                for(MyPicture p:pics){
                    if(p.getEditedUri()!=null){
                        imgUris.add(Uri.parse(p.getEditedUri()));
                    }else{
                        imgUris.add(Uri.parse(p.getOriginalUri()));
                    }
                }
            }else{
                Log.e("THIS", "fillList: "+"No Pics Found" );
            }
        }else{
            Log.e("THIS", "fillList: "+"Failed" );
        }
    }

    private void initializeViews() {
        rView=findViewById(R.id.pdf_preview_rView);
        bnv=findViewById(R.id.pdf_preview_bnv);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isChanged){
            isChanged=false;
            adapter.getDefaultSettings();

            rView.setAdapter(null);
            rView.setLayoutManager(null);
            rView.setAdapter(adapter);
            rView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void shareAsPdf(){
        AlertDialog.Builder builder=new AlertDialog.Builder(PdfPreviewActivity.this);
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
                            finish();
                        }
                    });
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            d.dismiss();
                            Toast.makeText(PdfPreviewActivity.this, "Time Out", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }

            }
        }).start();
        //simple share intent


    }
    public Uri savePdf(String pass, boolean isPassSet, ProgressBar pBar){

        if(isPerMissionGranted()){


            MyCustomPdf pdf = new MyCustomPdf(this, imgUris, isPassSet);
            Uri savedPdf=pdf.savePdf2(currDoc.getDname(), pass, new MyDocumentActivity.pdfProgress() {
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
        if(ContextCompat.checkSelfPermission(PdfPreviewActivity.this,"android.permission.WRITE_EXTERNAL_STORAGE")!= PackageManager.PERMISSION_GRANTED){
            return false;
        }
        return true;
    }

    private void save(){
        AlertDialog.Builder builder=new AlertDialog.Builder(PdfPreviewActivity.this);
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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            d.dismiss();
                            Toast.makeText(PdfPreviewActivity.this, "Pdf is saved at "+savedPdf.toString(), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            d.dismiss();
                            Toast.makeText(PdfPreviewActivity.this, "Time Out", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }

            }
        }).start();
        //simple share intent

    }
    private class MyNavListener implements BottomNavigationView.OnNavigationItemSelectedListener{

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()){
                case R.id.action_pdf_preview_setting:{
                    Intent intent =new Intent(PdfPreviewActivity.this,PdfSettingsActivity.class);
                    startActivity(intent);
                    isChanged=true;
                    break;
                }
                case R.id.action_pdf_preview_share:{
                    shareAsPdf();
                    break;
                }
                case R.id.action_pdf_preview_save:{
                    save();
                    break;
                }
            }

            return true;
        }
    }
}

