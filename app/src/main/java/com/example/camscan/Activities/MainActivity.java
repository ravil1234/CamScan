package com.example.camscan.Activities;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.budiyev.android.codescanner.AutoFocusMode;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.budiyev.android.codescanner.ScanMode;
import com.example.camscan.R;
import com.google.zxing.Result;
public class MainActivity extends AppCompatActivity
{
    private CodeScanner mCodeScanner;
    private CodeScannerView scannerView;
    private CameraManager mCameraManager;
    private String mCameraId;
    ImageView flashlight;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    boolean isflash_on;
    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        scannerView = findViewById(R.id.scanner_view);
        flashlight=findViewById(R.id.flash_light);
        isflash_on=false;
        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setCamera(CodeScanner.CAMERA_BACK );// or CAMERA_FRONT or specific camera id
        mCodeScanner.setFormats(CodeScanner.ALL_FORMATS);  // list of type BarcodeFormat,
        mCodeScanner.setAutoFocusEnabled(true);
        mCodeScanner.setScanMode(ScanMode.SINGLE );
        mCodeScanner.setTouchFocusEnabled(true);
        final boolean isFlashAvailable = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);

        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            assert mCameraManager != null;
            mCameraId = mCameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
         flashlight.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view)
             {
                 if(isFlashAvailable) {
                     if (isflash_on)
                         isflash_on = false;
                     else
                         isflash_on = true;
                     switchFlashLight(isflash_on);
                 }
                 else
                 {
                     Toast.makeText(MainActivity.this, "Flash is not available o this device",
                             Toast.LENGTH_LONG).show();
                 }
             }
         });
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String message = "result :\n" + result.getText();
                       Log.d("message",message);
                    }
                });
            }
        });

        checkCameraPermission();
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        checkCameraPermission();
    }
    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();

    }
    public  void checkCameraPermission()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
            }
            else
            {
                mCodeScanner.startPreview();
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                mCodeScanner.startPreview();
            } else
                {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
                checkCameraPermission();
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void switchFlashLight(boolean status) {
        try {
            mCameraManager.setTorchMode(mCameraId, status);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}
