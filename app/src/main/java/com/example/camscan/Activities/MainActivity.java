package com.example.camscan.Activities;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.util.TimeUnit;
import android.net.Uri;
import android.os.Bundle;

import com.example.camscan.ObjectClass.BitmapObject;
import com.example.camscan.R;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
public class MainActivity extends AppCompatActivity {

    private Executor executor = Executors.newSingleThreadExecutor();
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private int flashMode;
    PreviewView mPreviewView;
    ImageView captureImage;
    TextView flashmode_btn;
    View view;
    MotionEvent motionEvent;
    CardView gallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPreviewView = findViewById(R.id.previewView);
        captureImage = findViewById(R.id.captureImg);
        flashmode_btn = findViewById(R.id.flash_mode);
        gallery = findViewById(R.id.gallery);
        flashMode = ImageCapture.FLASH_MODE_OFF;

        flashmode_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flashMode == 0) {
                    flashMode = ImageCapture.FLASH_MODE_OFF;
                } else
                    flashMode = ImageCapture.FLASH_MODE_ON;
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
        if (allPermissionsGranted()) {
            startCamera(); //start camera if permission has been granted by user
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
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
                    // No errors need to be handled for this Future.
                    // This should never be reached.
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

        mPreviewView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //show dialog here
                view = v;
                motionEvent = event;
//                MeteringPointFactory meteringPointFactory=new SurfaceOrientedMeteringPointFactory(mPreviewView.getWidth(),mPreviewView.getHeight());
//                MeteringPoint autofocus=meteringPointFactory.createPoint(event.getX(),event.getY());
//                camera.getCameraControl().startFocusAndMetering(FocusMeteringAction.Builder(autofocus))

                return false;
            }
        });
        captureImage.setOnClickListener(v -> {
            SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
            File file = new File(getBatchDirectoryName(), mDateFormat.format(new Date()) + ".jpg");
            String path = getBatchDirectoryName() + mDateFormat.format(new Date()) + ".jpg";
            ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
            imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Image Saved successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, CapturedImageActivity.class);
                            intent.putExtra("uri", file.getPath());
                            startActivity(intent);

                        }
                    });
                }

                @Override
                public void onError(@NonNull ImageCaptureException error) {
                    error.printStackTrace();
                }
            });
//            imageCapture.takePicture(executor, new  ImageCapture.OnImageCapturedCallback() {
//                @Override
//                public void onCaptureSuccess(@NonNull ImageProxy image)
//                {
////                    super.onCaptureSuccess(image);
//                    String destFolder = getCacheDir().getAbsolutePath();
//                    String path=destFolder+ "/image1.png";
//                    try {
//                        FileOutputStream out = new FileOutputStream(destFolder + "/image1.png");
//                        getBitmap(image).compress(Bitmap.CompressFormat.PNG,100,out);
//                       // BitmapObject.bitmap_image=getBitmap(image);
//                        Intent intent=new Intent(MainActivity.this,CapturedImageActivity.class);
//                        intent.putExtra("uri",path);
//                        startActivity(intent);
//                        image.close();
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    }
////                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
////                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
////                    byte[] byteArray = stream.toByteArray();
//
//                }
//                @Override
//                public void onError(@NonNull ImageCaptureException error) {
//                    error.printStackTrace();
//                }
//            });
        });
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK)
            {
                if (data.getClipData() != null)
                {
                    int count = data.getClipData().getItemCount(); //evaluate the count before the for loop --- otherwise, the count is evaluated every loop.
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        Log.d("image_uri_all",imageUri+" ->");
                        //do something with the image (save it to some directory or whatever you need to do with it here)
                    }
                } else if (data.getData() != null) {
                    String imagePath = data.getData().getPath();
                    //do something with the image (save it to some directory or whatever you need to do with it here)
                }
                Intent i=new Intent(MainActivity.this,CapturedImageActivity.class);
                 startActivity(i);
            }
        }
    }
}
