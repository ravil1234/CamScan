package com.example.camscan.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.impl.PreviewConfig;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.camscan.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class CamActivity extends AppCompatActivity {

    PreviewView screenView;
    FloatingActionButton captureBtn;

    ImageCapture imgCap;
    ImageView imageView;


    public static int  RequestCODE=101;
    String [] PermissionsReq=new String[]{"android.permission.CAMERA","android.permission.WRITE_EXTERNAL_STORAGE"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam);

        getSupportActionBar().hide();
        initializeViews();

        if(allPermissionGranted()){
            initializeCamera();
            Log.e("CAMSCAN", "onCreate: Preview started" );
        }else{
            ActivityCompat.requestPermissions(this,PermissionsReq,RequestCODE);
            Log.e("THIS", "onCreate: FALSE" );
        }



        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                capture();
            }
        });

    }

    private boolean allPermissionGranted() {

        for(String perm:PermissionsReq){
            if(ContextCompat.checkSelfPermission(CamActivity.this,perm)!= PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;

    }

    private void initializeCamera() {

        final ListenableFuture<ProcessCameraProvider> pcp= ProcessCameraProvider.getInstance(this);
        pcp.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider camPro=pcp.get();

                    Preview preview=new Preview.Builder().build();

                    CameraSelector selector=new CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build();

                    camPro.unbindAll();

                    preview.setSurfaceProvider(screenView.createSurfaceProvider());

                     imgCap=new ImageCapture.Builder()
//                            .setTargetRotation(screenView.getDisplay().getRotation())
                            .build();

                    Camera camera=camPro.bindToLifecycle(CamActivity.this,selector,imgCap,preview);
                    Log.e("CamActivity", "run: PrevIew Done" );

                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        },ContextCompat.getMainExecutor(CamActivity.this));
/*
        //CameraX.unbindAll();
        //Rational aspectRatio=new Rational(screenView.getWidth(),screenView.getHeight());
        Size screen=new Size(screenView.getWidth(),screenView.getHeight());

        //PreviewConfig pConfig=new PreviewConfig.Builder();
        Preview.Builder prev=new Preview.Builder();
        prev.setTargetAspectRatio(AspectRatio.RATIO_16_9);
        //prev.setTargetResolution(screen);
        prev.build();
*/
    }

    private void capture() {
        //image Captured
//        ImageCapture.OutputFileOptions outputFileOptions=new ImageCapture.OutputFileOptions
//                .Builder(new File()).build();

        imgCap.takePicture(ContextCompat.getMainExecutor(this),new ImageCapture.OnImageCapturedCallback(){
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
             //   super.onCaptureSuccess(image);
                Log.e("Image", "onCaptureSuccess: ImagePlaced" );
                imageView.setImageBitmap(imageProxyToBitmap(image));

            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                super.onError(exception);
                Toast.makeText(CamActivity.this, "ERROR", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void initializeViews() {
        screenView=findViewById(R.id.cam_textture_view);
        captureBtn=findViewById(R.id.cam_capture_btn);
        imageView=findViewById(R.id.cam_imageView);
    }

    private Bitmap imageProxyToBitmap(ImageProxy image) {
        ImageProxy.PlaneProxy planeProxy = image.getPlanes()[0];
        ByteBuffer buffer = planeProxy.getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }


}
