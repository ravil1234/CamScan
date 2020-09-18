package com.example.camscan.Activities;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.budiyev.android.codescanner.ScanMode;
import com.example.camscan.R;
import com.google.zxing.Result;

import java.net.MalformedURLException;
import java.net.URL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
public class QRCodeScanActivity  extends AppCompatActivity
{
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    CodeScanner mCodeScanner;
    CodeScannerView scannerView;
    private int REQUEST_CODE_PERMISSIONS = 1001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_scan);
        getSupportActionBar().hide();
        scan_qr_code();
    }
    public void scan_qr_code()
    {
        scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setCamera(CodeScanner.CAMERA_BACK );// or CAMERA_FRONT or specific camera id
        mCodeScanner.setFormats(CodeScanner.ALL_FORMATS);  // list of type BarcodeFormat,
        mCodeScanner.setAutoFocusEnabled(false);
        mCodeScanner.setScanMode(ScanMode.SINGLE );
        mCodeScanner.setTouchFocusEnabled(true);
        mCodeScanner.setFlashEnabled(false);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String message = "result :\n" + result.getText();
                        Log.d("message",message);
                        String url=result.getText();

                        if(isValid(url))
                        {
                            show_hyperlink(url);
                        }
                        else
                        {
                            show_text(url);
                        }
//                        try
//                        {
//                            Intent i=new Intent("android.intent.action.MAIN");
//                            i.setComponent(ComponentName.unflattenFromString("com.android.chrome/com.android.chrome.Main"));
//                            i.addCategory("android.intent.category.LAUNCHER");
//                            i.setData(Uri.parse(url));
//                            startActivity(i);
//                        }
//                        catch(ActivityNotFoundException e)
//                        {
//                            Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse(url));
//                            startActivity(i);
//                        }
                    }
                });
            }
        });

        if (allPermissionsGranted())
        {
            mCodeScanner.startPreview();
        }
        else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }
    public void show_hyperlink(String result)
    {
        androidx.appcompat.app.AlertDialog.Builder builder=new
                androidx.appcompat.app.AlertDialog.Builder(QRCodeScanActivity.this);
        View dialog= LayoutInflater.from(QRCodeScanActivity.this).inflate(R.layout.dialog_box_hyperlink,null);
        builder.setView(dialog);
        TextView hyperlink=dialog.findViewById(R.id.hyperlink);
        ProgressBar progressBar=dialog.findViewById(R.id.progress_bar);
        hyperlink.setVisibility(View.VISIBLE);
        hyperlink.setMovementMethod(LinkMovementMethod.getInstance());
        hyperlink.setText(result);
        androidx.appcompat.app.AlertDialog d=builder.create();
        d.show();
        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run()
            {
                progressBar.setVisibility(View.GONE);
            }
        },1000);
        mCodeScanner.startPreview();
    }
    public void show_text(String result)
    {
        androidx.appcompat.app.AlertDialog.Builder builder=new
                androidx.appcompat.app.AlertDialog.Builder(QRCodeScanActivity.this);
        View dialog= LayoutInflater.from(QRCodeScanActivity.this).inflate(R.layout.dialog_box_hyperlink,null);
        builder.setView(dialog);
        TextView text_result=dialog.findViewById(R.id.text_result);
        ProgressBar progressBar=dialog.findViewById(R.id.progress_bar);
        text_result.setVisibility(View.VISIBLE);
        text_result.setText(result);
        androidx.appcompat.app.AlertDialog d=builder.create();
        d.show();
        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run()
            {
                progressBar.setVisibility(View.GONE);
            }
        },1000);
        mCodeScanner.startPreview();
    }
    public void gotoCamera(View view)
    {
        mCodeScanner.stopPreview();
        mCodeScanner.releaseResources();
       Intent intent=new Intent(QRCodeScanActivity.this,CameraXActivity.class);
       startActivity(intent);
       finish();
    }
    @Override
    public void onBackPressed()
    {
        mCodeScanner.stopPreview();
        mCodeScanner.releaseResources();
        Intent intent=new Intent(QRCodeScanActivity.this,CameraXActivity.class);
        startActivity(intent);
        finish();
    }
    private boolean isValid(String urlString) {
        try {
            URL url = new URL(urlString);
            return URLUtil.isValidUrl(String.valueOf(url))&& Patterns.WEB_URL.matcher(String.valueOf(url)).matches();
        } catch (MalformedURLException e)
        {

        }
        return false;
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
                mCodeScanner.startPreview();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

