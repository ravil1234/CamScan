package com.example.camscan.Activities;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
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
import com.example.camscan.ObjectClass.BitmapObject;
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
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.camscan.UtilityClass;
import com.google.common.util.concurrent.ListenableFuture;
import com.squareup.picasso.Picasso;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
public class SingleCamActivity extends AppCompatActivity {

    private Executor executor = Executors.newSingleThreadExecutor();
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private int flashMode;
    PreviewView mPreviewView;
    View captureImage;
    TextView tick_img;
    View view;
    MotionEvent motionEvent;
    ImageView flashmode_btn;
    ImageView gallery,single_mode_img,batch_mode_img,last_img,focus_camera,scan_qr_code;;
    //   ArrayList<MyPicture > myPictureList;
    RelativeLayout show_grid_view,relativeLayoutCameraX,relativeLayoutScanQr;
    LinearLayout line_horizontal,line_vertical;
    String uri="";
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camerax);
        getSupportActionBar().hide();
        preferences=getSharedPreferences("SharedPreference",MODE_PRIVATE);
        mPreviewView = findViewById(R.id.previewView);
        captureImage = findViewById(R.id.captureImage);
        flashmode_btn = findViewById(R.id.flash_mode);
        gallery = findViewById(R.id.gallery);
        single_mode_img=findViewById(R.id.single_mode);
        batch_mode_img=findViewById(R.id.batch_mode);
        tick_img=findViewById(R.id.tick_img);
        last_img=findViewById(R.id.last_img);
        show_grid_view=findViewById(R.id.show_hide_grid);
        line_horizontal=findViewById(R.id.line_horizontal);
        line_vertical=findViewById(R.id.line_vertical);
        focus_camera=findViewById(R.id.focus_camera);
        scan_qr_code=findViewById(R.id.scan_qr_code);
        flashMode = ImageCapture.FLASH_MODE_AUTO;
        if(preferences.contains("flash_mode"))
        {
            flashMode=preferences.getInt("flash_mode",0);
        }
        tick_img.setVisibility(View.GONE);
        batch_mode_img.setVisibility(View.GONE);
        single_mode_img.setVisibility(View.GONE);
        gallery.setVisibility(View.GONE);
        last_img.setVisibility(View.INVISIBLE);
        scan_qr_code.setVisibility(View.GONE);

        uri=getIntent().getStringExtra("PICTURE_URI");
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
        if (allPermissionsGranted()) {
            startCamera(); //start camera if permission has been granted by user
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
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
                .build();

        preview.setSurfaceProvider(mPreviewView.createSurfaceProvider());
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageAnalysis, imageCapture);
        captureImage.setOnClickListener(v -> {
            File my_file=new File(uri);
            ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(my_file).build();
            imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run()
                        {
                            call_save_list(my_file.getPath());
                            Toast.makeText(SingleCamActivity.this, "Image Saved successfully", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(SingleCamActivity.this, "Flash Auto !", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.flash_on:
                        flashMode=ImageCapture.FLASH_MODE_ON;
                        flashmode_btn.setImageResource(R.drawable.flash);
                        preferences.edit().putInt("flash_mode",flashMode).apply();
                        Toast.makeText(SingleCamActivity.this, "Flash On !", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.flash_off:
                        flashMode=ImageCapture.FLASH_MODE_OFF;
                        flashmode_btn.setImageResource(R.drawable.flash_off);
                        preferences.edit().putInt("flash_mode",flashMode).apply();
                        Toast.makeText(SingleCamActivity.this, "Flash Off !", Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });
        popup.show();
    }
    private  void call_save_list(String uri)
    {
        Intent returnIntent = new Intent();
        //    returnIntent.putExtra("PICTURE_URI",uri);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
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
}