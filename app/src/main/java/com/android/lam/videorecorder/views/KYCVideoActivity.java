package com.android.lam.videorecorder.views;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.lam.videorecorder.R;
import com.android.lam.videorecorder.services.PermissionService;

import static com.android.lam.videorecorder.Utils.AppConstants.PERMISSION_REQUEST;
import static com.android.lam.videorecorder.Utils.AppConstants.REQUEST_VIDEO_CAPTURE;

public class KYCVideoActivity extends AppCompatActivity implements PermissionService.PermissionListener {

    PermissionService permissionService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_k_y_c_video);
        if (hasCamera()) {
            permissionService = new PermissionService();
            permissionService.askPermissions(
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
        if (requestCode == REQUEST_VIDEO_CAPTURE && intent != null) {
            Uri videoUri = intent.getData();
        }
    }

    @Override
    public void permissionGranted(String... permissions) {
        dispatchTakeVideoIntent();
    }

    @Override
    public void permissionDenied(String... permissions) {
        permissionService.handleDeniedPermission(this, permissions);
    }

    @Override
    public void shouldShowRequestPermission(String... permission) {
        showAlertDialog(getString(R.string.description_permission, permission[0]), permission);
    }

    @Override
    public void neverAskPermission(String... permission) {
        redirectSettings();
    }

    private void showAlertDialog(String message, String... permission) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.title_required));
        builder.setMessage(message);
        builder.setPositiveButton(getString(android.R.string.ok), (dialog, which) -> {
            permissionService.askPermissions(this, permission);
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void redirectSettings() {
        Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST) {
            permissionService.handlePermissionsResults(permissions, grantResults);
        }
    }

}