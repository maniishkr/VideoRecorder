package com.android.lam.videorecorder.views;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import com.android.lam.videorecorder.R;

import static com.android.lam.videorecorder.Utils.AppConstants.REQUEST_VIDEO_CAPTURE;

public class KYCVideoActivity extends PermissionActivity implements PermissionActivity.PermissionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_k_y_c_video);
        if (hasCamera()) {
            askPermissions(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
            );

        }
    }

    private boolean hasCamera() {
        return (getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA_ANY));
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Uri videoUri = intent.getData();
        if (requestCode == REQUEST_VIDEO_CAPTURE) {
            Log.d("uri ", "" + videoUri);
        }
    }

    @Override
    public void permissionGranted(boolean isGranted, String... permissions) {
        if (isGranted) dispatchTakeVideoIntent();
    }
}