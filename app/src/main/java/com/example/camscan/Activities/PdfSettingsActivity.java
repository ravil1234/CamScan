package com.example.camscan.Activities;

import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.camscan.R;
import com.example.camscan.UtilityClass;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PdfSettingsActivity extends AppCompatActivity {

    Switch borderSwitch;
    ImageView light;
    TextView pass,pageSize,orientation;

    String passString,uriString;
    Boolean isBorder;
    int orientInt,pageSizeInt;

    ImageView imgForStamp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_pdf);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Pdf Setting");

        borderSwitch=findViewById(R.id.pdf_setting_item4);
        light=findViewById(R.id.pdf_setting_item4_light);
        pass=findViewById(R.id.pdf_setting_text2);
        pageSize=findViewById(R.id.pdf_setting_text1);
        orientation=findViewById(R.id.pdf_setting_text3);


        fillDefaultSettings();

        borderSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                setPageBorder(b);
                if(b){
                    light.setImageDrawable(getResources().getDrawable(R.drawable.border_on));
                }else{
                    light.setImageDrawable(getResources().getDrawable(R.drawable.border_off));
                }
            }
        });

    }

    private void fillDefaultSettings() {
        SharedPreferences pref=this.getSharedPreferences(UtilityClass.PDF_SETTING, Context.MODE_PRIVATE);
        pageSizeInt =pref.getInt("PDF_PAGE_SIZE",1);
        orientInt=pref.getInt("PDF_PAGE_ORIENTATION",0);
        isBorder=pref.getBoolean("PDF_PAGE_BORDER",false);
        passString=pref.getString("PDF_PAGE_PASSWORD","admin");
        uriString=pref.getString("PDF_STAMP_URI","android.resource://"+getPackageName()+"/drawable/"+R.drawable.stamp);

        if(isBorder){
            light.setImageDrawable(getResources().getDrawable(R.drawable.border_on));

        }else{
            light.setImageDrawable(getResources().getDrawable(R.drawable.border_off));
        }
        borderSwitch.setChecked(isBorder);

        pass.setText(passString);

        if(orientInt==0){
            //port
            orientation.setText("Portrait");
        }else{
            //land
            orientation.setText("Landscape");
        }

        setSizeText(pageSizeInt);

    }

    private void setSizeText(int pageSizeInt) {
        switch (pageSizeInt){
            case 0:{//A5
                pageSize.setText("A5 (5.8×8.3in)");
                break;
            }
            case 1:{//A4
                pageSize.setText("A4 (8.3×11.7in)");
                break;
            }
            case 2:{//A3
                pageSize.setText("A3 (8.4×14in)");
                break;
            }
            case 3:{//Legal
                pageSize.setText("Legal (11×17in)");
                break;
            }
            case 4:{//Tabloid
                pageSize.setText("Tabloid (11.7×16.5in)");
                break;
            }



        }
    }


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
    public void setPageBorder(Boolean bor){
        SharedPreferences.Editor pref= getSharedPreferences(UtilityClass.PDF_SETTING,MODE_PRIVATE).edit();
        pref.putBoolean("PDF_PAGE_BORDER",bor);
        pref.apply();
    }
    public void setPagePassword(String password){
        SharedPreferences.Editor pref= getSharedPreferences(UtilityClass.PDF_SETTING,MODE_PRIVATE).edit();
        pref.putString("PDF_PAGE_PASSWORD",password);
        pref.apply();
    }
    public void setPageStamp(String imgUri){
        SharedPreferences.Editor pref= getSharedPreferences(UtilityClass.PDF_SETTING,MODE_PRIVATE).edit();
        pref.putString("PDF_STAMP_URI",imgUri);
        pref.apply();
    }

    public void onPassClick(View view){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Set New Password");
        View v= LayoutInflater.from(this).inflate(R.layout.fragment_rename,null);
        builder.setView(v);

        EditText newNameView=v.findViewById(R.id.rename_fragment_eview);
        newNameView.setSelectAllOnFocus(true);
        //newNameView.setText(currentDoc1.getDocName());
        newNameView.setText(passString);

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
                    pass.setText(newPass);
                    Toast.makeText(PdfSettingsActivity.this,"New Password Is Set", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(PdfSettingsActivity.this, "Password can't be null", Toast.LENGTH_SHORT).show();
                }
            }
        }).setNegativeButton("Cancel",null);

        builder.create().show();
    }
    public void onPageSizeClicked(View view){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Select Page Size");
        View v= LayoutInflater.from(this).inflate(R.layout.fragment_page_size,null);
        builder.setView(v);

        RadioButton a5=v.findViewById(R.id.frag_psize_a5);
        RadioButton a4=v.findViewById(R.id.frag_psize_a4);
        RadioButton a3=v.findViewById(R.id.frag_psize_a3);
        RadioButton legal=v.findViewById(R.id.frag_psize_legal);
        RadioButton tabloid=v.findViewById(R.id.frag_psize_tabloid);

        TextView saveBtn=v.findViewById(R.id.frag_psize_save);
        int index=pageSizeInt;
        if(index==0){
            a5.setChecked(true);
        }
        else if(index==1){
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
        AlertDialog d=builder.create();
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                setSizeText(radio);
                d.dismiss();

            }
        });


        d.show();
    }
    public void onOrientationClicked(View view){
        AlertDialog.Builder builder=new AlertDialog.Builder(PdfSettingsActivity.this);
        builder.setTitle("Set Orientation");
        View dialog=LayoutInflater.from(PdfSettingsActivity.this).inflate(R.layout.fragment_orientation,null);

        builder.setView(dialog);

        RadioButton portBtn=dialog.findViewById(R.id.frag_orient_port);
        RadioButton landBtn=dialog.findViewById(R.id.frag_orient_land);
        TextView saveBtn=dialog.findViewById(R.id.frag_orient_save);

        int rad=orientInt;
        if(rad==0){
            portBtn.setChecked(true);
        }else{
            landBtn.setChecked(true);
        }

        AlertDialog d=builder.create();
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int radio;
                if(portBtn.isChecked()){
                    radio=0;
                }else{
                    radio=1;
                }
                setPageOrientation(radio);
                if(radio==0){
                    orientation.setText("Portrait");
                }else{
                    orientation.setText("Landscape");
                }
                d.dismiss();
            }
        });

        d.show();
    }
    public void onStampClicked(View view){

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Choose Stamp");
        View v=LayoutInflater.from(this).inflate(R.layout.fragment_pdf_stamp,null);
        builder.setView(v);
        imgForStamp=v.findViewById(R.id.frag_pdf_stamp_image);
        RadioButton def=v.findViewById(R.id.frag_pdf_stamp_def);
        RadioButton gall=v.findViewById(R.id.frag_pdf_stamp_gallery);
        TextView saveBtn=v.findViewById(R.id.frag_pdf_stamp_save);

        Bitmap stamp= null;
        try {
           // Log.e("THIS SETTINGS", "onStampClicked: "+uriString );
            Uri uri=Uri.parse(uriString);
            InputStream is=PdfSettingsActivity.this.getContentResolver().openInputStream(uri);
            stamp = BitmapFactory.decodeStream(is);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if(stamp!=null){
            imgForStamp.setImageBitmap(stamp);
            //.recycle();
        }
        if(uriString.equals("android.resource://"+getPackageName()+"/drawable/"+R.drawable.stamp)){
            def.setChecked(true);
        }else{
            gall.setChecked(true);
        }

        gall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, 110);
                }else{
                    Bitmap stamp= null;
                    try {
                        stamp = BitmapFactory.decodeStream(PdfSettingsActivity.this.getContentResolver()
                                .openInputStream(Uri.parse("android.resource://"+getPackageName()+"/"+R.drawable.stamp)));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    if(stamp!=null){
                        imgForStamp.setImageBitmap(stamp);
                       // stamp.recycle();
                    }

                    setPageStamp("android.resource://"+getPackageName()+"/"+R.drawable.stamp);
                }
            }
        });

        imgForStamp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gall.setChecked(true);
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 110);

            }
        });

        AlertDialog d=builder.create();
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //builder.create().dismiss();
                d.dismiss();
            }
        });

        d.show();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==110){
            if(resultCode==RESULT_OK){
                Uri uri=data.getData();
                Bitmap stamp= null;
                Uri savedStamp=null;
                try {
                    stamp = BitmapFactory.decodeStream(PdfSettingsActivity.this.getContentResolver()
                            .openInputStream(uri));
                    stamp=UtilityClass.resizeImage(stamp,200,50);
                    stamp=makeTransparent(stamp,70);
                    savedStamp=saveStamp(stamp);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                if(savedStamp!=null && imgForStamp!=null){
                    imgForStamp.setImageBitmap(stamp);
                    setPageStamp(savedStamp.toString());
                    uriString=savedStamp.toString();
                    //stamp.recycle();
                }

            }else{
                Toast.makeText(PdfSettingsActivity.this, "FAILED", Toast.LENGTH_SHORT).show();
                setPageStamp("android.resource://"+getPackageName()+"/"+R.drawable.stamp);
            }
        }
    }

    private Uri saveStamp(Bitmap stamp) {
        File f=null;
        File dir;
        String name="STAMP";
        if(Build.VERSION.SDK_INT< Build.VERSION_CODES.Q){

            String path= Environment.getExternalStorageDirectory().getPath()+"/CamScan/"+name+".jpg";
            f=new File(path);
            dir=new File(Environment.getExternalStorageDirectory().getPath()+"/CamScan");
        //          String path2=Environment.getExternalStorageDirectory().getPath()+System.currentTimeMillis()+".jpg";
//            original=new File(path2);
        }else{
            f=new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES),"CamScan/"+name+".jpg");
            dir=new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES),"CamScan");
        }
        if(!dir.exists() && !dir.isDirectory()){
            dir.mkdirs();
        }
        if(f.exists()){
            f.delete();
        }
        try {

            FileOutputStream fos=new FileOutputStream(f,false);
            stamp.compress(Bitmap.CompressFormat.JPEG,100,fos);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Uri.fromFile(f);
    }
    public Bitmap makeTransparent(Bitmap src, int value) {
        int width = src.getWidth();
        int height = src.getHeight();
        Bitmap transBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(transBitmap);
        canvas.drawARGB(0, 0, 0, 0);
        // config paint
        final Paint paint = new Paint();
        paint.setAlpha(value);
        canvas.drawBitmap(src, 0, 0, paint);
        return transBitmap;
    }
}