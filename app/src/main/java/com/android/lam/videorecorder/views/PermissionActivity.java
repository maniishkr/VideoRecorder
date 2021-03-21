package com.android.lam.videorecorder.views;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.lam.videorecorder.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.android.lam.videorecorder.Utils.AppConstants.PERMISSION_REQUEST;

public abstract class PermissionActivity extends AppCompatActivity {

    private PermissionListener permissionListener;

    public void askPermissions(PermissionListener listener, String... permissions) {
        permissionListener = listener;
        List<String> permissionNeededList = getPermissionNeededList(permissions);
        if (permissionNeededList.size() > 0) {
            ActivityCompat.requestPermissions(this, permissionNeededList.toArray(new String[permissionNeededList.size()]), PERMISSION_REQUEST);
            return;
        }
        permissionListener.permissionGranted(true, permissions);
    }

    private List<String> getPermissionNeededList(String... permissions) {
        List<String> listPermissionNeeded = new ArrayList<>();
        if (permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                    listPermissionNeeded.add(permission);
            }
        }
        return listPermissionNeeded;
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
            handlePermissionsResults(permissions, grantResults);
        }
    }

    private void handlePermissionsResults(String[] permissions, int[] grantResults) {
        HashMap<String, Integer> permissionResult = new HashMap<>();
        int deniedCount = 0;
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                permissionResult.put(permissions[i], grantResults[i]);
                deniedCount++;
            }
        }
        if (deniedCount == 0 && permissionListener != null) {
            permissionListener.permissionGranted(true);
        } else {
            for (Map.Entry<String, Integer> entry : permissionResult.entrySet()) {
                showAskAgainDialog(
                        entry.getKey(),
                        !ActivityCompat.shouldShowRequestPermissionRationale(this, entry.getKey())
                );
                break;
            }
        }
    }

    private void showAskAgainDialog(final String permission, boolean isNeverAsk) {
        switch (permission) {
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                showAlertDialog(
                        getString(R.string.description_storage_permission),
                        permission,
                        isNeverAsk
                );
                break;

            case Manifest.permission.RECORD_AUDIO:
                showAlertDialog(
                        getString(R.string.description_microphone_permission),
                        permission,
                        isNeverAsk
                );
                break;
        }
    }

    private void showAlertDialog(String message, String permission, boolean isNeverAsk) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.title_required));
        builder.setMessage(message);
        builder.setPositiveButton(getString(android.R.string.ok), (dialog, which) -> {
            if (isNeverAsk) redirectSettings();
            else askPermissions(permissionListener, permission);
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public interface PermissionListener {
        void permissionGranted(boolean isGranted, String... permissions);
    }


}
