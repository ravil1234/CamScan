package com.example.camscan.Activities;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.icu.util.TimeUnit;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.budiyev.android.codescanner.ScanMode;
import com.example.camscan.Objects.MyDocument;
import com.example.camscan.Objects.MyPicture;
import com.example.camscan.R;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraX;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.core.SurfaceOrientedMeteringPointFactory;
import androidx.camera.extensions.HdrImageCaptureExtender;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.camera.view.TextureViewMeteringPointFactory;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
//import com.example.camscan.UtilityClass;
import com.example.camscan.UtilityClass;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.zxing.Result;
import com.squareup.picasso.Picasso;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
public class CameraXActivity extends AppCompatActivity {

    private Executor executor = Executors.newSingleThreadExecutor();
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private int flashMode;
    PreviewView mPreviewView;
    View captureImage,view,batch_mode_true,single_mode_true;
    TextView tick_img;
    MotionEvent motionEvent;
    MyDocument savedDoc;
    ImageView flashmode_btn;
    ImageView gallery,single_mode_img,batch_mode_img,last_img,focus_camera,scan_qr_code;
    boolean single_mode;
    boolean isNew=true;
    RelativeLayout show_grid_view,relativeLayoutCameraX,relativeLayoutScanQr;
    LinearLayout line_horizontal,line_vertical;
    ArrayList<MyPicture > myPictureList;
    SharedPreferences preferences;
    String currDocName;
    int picCount=0;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camerax);
        getSupportActionBar().hide();
        preferences=getSharedPreferences("SharedPreference",MODE_PRIVATE);
        long time=System.currentTimeMillis()%1000000;
        currDocName=UtilityClass.appName+UtilityClass.lineSeparator+time;
        relativeLayoutCameraX=findViewById(R.id.relative_layout_camerax);
        relativeLayoutScanQr=findViewById(R.id.relative_layout_scanqrcode);
        mPreviewView = findViewById(R.id.previewView);
        captureImage = findViewById(R.id.captureImage);
        flashmode_btn = findViewById(R.id.flash_mode);
        gallery = findViewById(R.id.gallery);
        single_mode_img=findViewById(R.id.single_mode);
        batch_mode_img=findViewById(R.id.batch_mode);
        batch_mode_true=findViewById(R.id.batch_mode_true);
        single_mode_true=findViewById(R.id.single_mode_true);
        tick_img=findViewById(R.id.tick_img);
        last_img=findViewById(R.id.last_img);
        show_grid_view=findViewById(R.id.show_hide_grid);
        line_horizontal=findViewById(R.id.line_horizontal);
        line_vertical=findViewById(R.id.line_vertical);
        focus_camera=findViewById(R.id.focus_camera);
        scan_qr_code=findViewById(R.id.scan_qr_code);
        flashMode = ImageCapture.FLASH_MODE_AUTO;
        myPictureList=new ArrayList<>();
        single_mode=true;
        single_mode_true.setVisibility(View.VISIBLE);
        last_img.setVisibility(View.INVISIBLE);
        if(preferences.contains("flash_mode"))
        {
            flashMode=preferences.getInt("flash_mode",0);
        }
        if(preferences.contains("grid"))
        {
            if(preferences.getInt("grid",0)==1)
            {
                line_vertical.setVisibility(View.VISIBLE);
                line_horizontal.setVisibility(View.VISIBLE);
                focus_camera.setVisibility(View.VISIBLE);
            }
        }
        flashmode_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Dialog box;
                showPopupMenu(view,true,R.style.MyPopupOtherStyle);
            }
        });
        show_grid_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if(line_horizontal.getVisibility()==View.GONE)
                {
                    line_horizontal.setVisibility(View.VISIBLE);
                    line_vertical.setVisibility(View.VISIBLE);
                    focus_camera.setVisibility(View.VISIBLE);
                    preferences.edit().putInt("grid",1).apply();
                }
                else
                {
                    line_horizontal.setVisibility(View.GONE);
                    line_vertical.setVisibility(View.GONE);
                    focus_camera.setVisibility(View.GONE);
                    preferences.edit().putInt("grid",-1).apply();
                }
            }
        });
        scan_qr_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent i=new Intent(CameraXActivity.this,QRCodeScanActivity.class);
                startActivity(i);
                finish();
            }
        });
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
            }
        });
        tick_img.setVisibility(View.INVISIBLE);
        batch_mode_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                single_mode=false;
                single_mode_true.setVisibility(View.GONE);
                batch_mode_true.setVisibility(View.VISIBLE);
                Toast.makeText(CameraXActivity.this,"Batch Mode On",Toast.LENGTH_LONG).show();
            }
        });
        single_mode_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                single_mode=true;
                single_mode_true.setVisibility(View.VISIBLE);
                batch_mode_true.setVisibility(View.GONE);
                Toast.makeText(CameraXActivity.this,"Single Mode On",Toast.LENGTH_LONG).show();
            }
        });

        String from=getIntent().getStringExtra("from");
        if(from!=null)
        {
            if(from.equals("InDocRecyclerActivity")){
                //came from recycelr activity from more pages
                isNew=false;
                String doc=getIntent().getStringExtra("MyDocument");
                savedDoc=UtilityClass.getDocFromJson(doc);
            }
        }
        if (allPermissionsGranted())
        {
            startCamera(); //start camera if permission has been granted by user
        }
        else
            {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
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
        popup.getMenuInflater().inflate(R.menu.flash_popup_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.flash_auto:
                        flashMode=ImageCapture.FLASH_MODE_AUTO;
                        flashmode_btn.setImageResource(R.drawable.automatic_flash);
                        preferences.edit().putInt("flash_mode",flashMode).apply();
                        Toast.makeText(CameraXActivity.this, "Flash Auto !", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.flash_on:
                        flashMode=ImageCapture.FLASH_MODE_ON;
                        flashmode_btn.setImageResource(R.drawable.flash);
                        preferences.edit().putInt("flash_mode",flashMode).apply();
                        Toast.makeText(CameraXActivity.this, "Flash On !", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.flash_off:
                        flashMode=ImageCapture.FLASH_MODE_OFF;
                        flashmode_btn.setImageResource(R.drawable.flash_off);
                        preferences.edit().putInt("flash_mode",flashMode).apply();
                        Toast.makeText(CameraXActivity.this, "Flash Off !", Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });
        popup.show();
    }
    @Override
    protected void onResume()
    {
        super.onResume();
    }
    private void startCamera() {

        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {

                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);
                } catch (ExecutionException | InterruptedException e) {
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .build();

        ImageCapture.Builder builder = new ImageCapture.Builder();

        //Vendor-Extensions (The CameraX extensions dependency in build.gradle)
        HdrImageCaptureExtender hdrImageCaptureExtender = HdrImageCaptureExtender.create(builder);

        // Query if extension is available (optional).
        if (hdrImageCaptureExtender.isExtensionAvailable(cameraSelector)) {
            // Enable the extension if available.
            hdrImageCaptureExtender.enableExtension(cameraSelector);
        }

        final ImageCapture imageCapture = builder
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setFlashMode(flashMode)
//                .setTargetAspectRatio(screenAspectRatio)
//                .setTargetResolution(screenSize)
                .build();
        preview.setSurfaceProvider(mPreviewView.createSurfaceProvider());
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageAnalysis, imageCapture);
        captureImage.setOnClickListener(v -> {
            File my_file=saveimagefile();
            ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(my_file).build();
            imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run()
                        {
                            // Todo savelist
                            call_save_list(my_file.getPath());
                            Toast.makeText(CameraXActivity.this, "Image Saved successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                @Override
                public void onError(@NonNull ImageCaptureException error) {
                    error.printStackTrace();
                }
            });
        });
    }
    private  void call_save_list(String uri)
    {

        picCount++;
        MyPicture p=new MyPicture(0,uri,null,currDocName+UtilityClass.lineSeparator+System.currentTimeMillis()%100000,
                picCount,null,0,0);
        myPictureList.add(p);

        MyDocument document=null;
        long time=System.currentTimeMillis();
        if(isNew){
            document=new MyDocument(currDocName,
                    time,time,null);
        }else{
            document=savedDoc;
            document.setTimeEdited(System.currentTimeMillis());
            p.setDid(savedDoc.getDid());
        }

        //String mydoc=UtilityClass.getStringFromObject(document);
        if(single_mode)
        {
            String mypic= UtilityClass.getStringFromObject(myPictureList);
            String mydoc=UtilityClass.getStringFromObject(document);
            Intent intent = new Intent(CameraXActivity.this, BoxActivity.class);
            intent.putExtra("MyPicture",mypic);
            intent.putExtra("MyDocument",mydoc);
            startActivity(intent);
            isNew=true;
            finish();
        }
        if(myPictureList.size()>0)
            tick_img.setVisibility(View.VISIBLE);
        gallery.setVisibility(View.GONE);
        last_img.setVisibility(View.VISIBLE);
        Picasso.with(CameraXActivity.this).load(uri).into(last_img);
        batch_mode_img.setVisibility(View.GONE);
        single_mode_img.setVisibility(View.GONE);
        MyDocument finalDocument = document;
        tick_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                String mypic= UtilityClass.getStringFromObject(myPictureList);
                String mydoc=UtilityClass.getStringFromObject(finalDocument);
                Intent i=new Intent(CameraXActivity.this,BoxActivity.class);
                i.putExtra("MyPicture",mypic);
                i.putExtra("MyDocument",mydoc);
                startActivity(i);
                isNew=true;
                finish();
            }
        });
    }
    private File  saveimagefile()
    {
        File dir;
        File f=null;
        if(Build.VERSION.SDK_INT< Build.VERSION_CODES.Q){
            String path= Environment.getExternalStorageDirectory().getPath()+"/CamScan/.Original/"+System.currentTimeMillis()+".jpg";
            dir=new File(Environment.getExternalStorageDirectory().getPath()+"/CamScan/.Original");
            f=new File(path);
        }
        else{
            f=new File(CameraXActivity.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES),"CamScan/.Original/"+System.currentTimeMillis()+".jpg");
            dir=new File(CameraXActivity.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES),"CamScan/.Original");
        }
        if(!dir.exists() && !dir.isDirectory()){
            dir.mkdirs();
        }
        return  f;
    }
    private Bitmap getBitmap(ImageProxy image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        buffer.rewind();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        byte[] clonedBytes = bytes.clone();
        return BitmapFactory.decodeByteArray(clonedBytes, 0, clonedBytes.length);
    }
    public String getBatchDirectoryName() {

        String app_folder_path = "";
        app_folder_path = Environment.getExternalStorageDirectory().toString() + "/images";
        File dir = new File(app_folder_path);
        if (!dir.exists() && !dir.mkdirs()) {

        }

        return app_folder_path;
    }
    private boolean allPermissionsGranted() {

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void getPics(final ArrayList<Uri> uris){
        AlertDialog.Builder builder=new AlertDialog.Builder(CameraXActivity.this);
        View v= LayoutInflater.from(this).inflate(R.layout.fragment_progress,null);
        builder.setView(v);
        AlertDialog d=builder.create();
        d.setCancelable(false);
        d.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<MyPicture> imported=new ArrayList<>();
                for(Uri u:uris){
                    MyPicture p=new MyPicture();

                    InputStream is=null;

                    try{
                        is=CameraXActivity.this.getContentResolver().openInputStream(u);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    Bitmap b=null;
                    if(is!=null){

                        b=BitmapFactory.decodeStream(is);
                    }
                    if(b==null){
                        Log.e("THIS", "FAILED TO IMPORT in Main Activity" );
                        d.dismiss();
                        return;
                    }

                    String picName=currDocName+UtilityClass.lineSeparator+System.currentTimeMillis()%100000;
                    Uri orig=UtilityClass.saveImage(CameraXActivity.this,b,picName,true);
                    if(isNew){
                        long time=System.currentTimeMillis();
                        savedDoc=new MyDocument(currDocName,time,time,null);
                    }
                    p.setDid(savedDoc.getDid());
                    p.setEditedName(picName);
                    p.setOriginalUri(orig.toString());
                    p.setCoordinates(null);
                    picCount++;
                    p.setPosition(picCount);
                    imported.add(p);

                    if(b!=null){
                        b.recycle();
                        b=null;
                    }
                }
                // return imported;

                String myPic=UtilityClass.getStringFromObject(imported);
                String myDocument=UtilityClass.getStringFromObject(savedDoc);
                Intent i=new Intent(CameraXActivity.this,BoxActivity.class);
                i.putExtra("MyPicture",myPic);
                i.putExtra("MyDocument",myDocument);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        d.dismiss();
                        startActivity(i);
                        finish();
                    }
                });


            }
        }).start();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                ArrayList<Uri> uris=new ArrayList<>();
                if (data.getClipData() != null)
                {
                    int count = data.getClipData().getItemCount(); //evaluate the count before the for loop --- otherwise, the count is evaluated every loop.
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        uris.add(imageUri);
                        //do something with the image (save it to some directory or whatever you need to do with it here)
                    }
                }
                else if (data.getData() != null) {
                    Uri imagePath = data.getData();
                    uris.add(imagePath);
                    //do something with the image (save it to some directory or whatever you need to do with it here)
                }else{
                    Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show();
                    return;
                }
                getPics(uris);

            }
        }
    }
}
