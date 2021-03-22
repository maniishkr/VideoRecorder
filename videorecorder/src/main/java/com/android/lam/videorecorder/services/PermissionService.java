package com.android.lam.videorecorder.services;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.android.lam.videorecorder.R;
import com.android.lam.videorecorder.Utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class PermissionService {

    public static final int REQUEST_VIDEO_CAPTURE = 1;
    public static int PERMISSION_REQUEST = 100;
    private final String[] permissions = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO};
    private VideoRecorderListener videoRecorderListener;

    public void startVideoRecorder(Activity activity) {
        askPermissions(activity, permissions);
    }


    private void askPermissions(Activity activity, String... permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Utils.dispatchTakeVideoIntent(activity);
            return;
        }
        videoRecorderListener = (VideoRecorderListener) activity;
        List<String> permissionNeededList = getPermissionNeededList(activity, permissions);
        if (permissionNeededList.size() > 0) {
            ActivityCompat.requestPermissions(activity, permissionNeededList.toArray(new String[permissionNeededList.size()]), PERMISSION_REQUEST);
            return;
        }
        Utils.dispatchTakeVideoIntent(activity);
    }

    private List<String> getPermissionNeededList(Activity activity, String... permissions) {
        List<String> listPermissionNeeded = new ArrayList<>();
        if (permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED)
                    listPermissionNeeded.add(permission);
            }
        }
        return listPermissionNeeded;
    }


    private void handlePermissionsResults(Activity activity, String[] permissions, int[] grantResults) {
        HashMap<String, Integer> permissionResult = new HashMap<>();
        int deniedCount = 0;
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                permissionResult.put(permissions[i], grantResults[i]);
                deniedCount++;
            }
        }
        if (videoRecorderListener != null) {
            if (deniedCount == 0) {
                Utils.dispatchTakeVideoIntent(activity);
            } else {
                handleDeniedPermission(activity, permissionResult.keySet().toArray(new String[0]));
            }
        }
    }

    private void handleDeniedPermission(Activity activity, String... permissions) {
        for (String permission : permissions) {
            showAlertDialog(
                    activity,
                    activity.getString(R.string.title_required),
                    activity.getString(R.string.description_permission, permission.substring(permission.lastIndexOf(".") + 1)),
                    !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission),
                    permissions);
            break;
        }
    }

    private void redirectSettings(Activity activity) {
        Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.setData(Uri.parse("package:" + activity.getApplicationContext().getPackageName()));
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(i);
    }

    private void showAlertDialog(Activity activity, String title, String message, boolean isNeverAsk, String... permission) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(activity.getString(android.R.string.ok), (dialog, which) -> {
            if (isNeverAsk) redirectSettings(activity);
            else askPermissions(activity, permission);
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void onRequestPermissionResult(Activity activity, int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != PERMISSION_REQUEST) return;
        handlePermissionsResults(activity, permissions, grantResults);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_VIDEO_CAPTURE && intent != null) {
            videoRecorderListener.getVideoUri(intent.getData());
            return;
        }
        videoRecorderListener.getVideoUri(null);
    }

    public interface VideoRecorderListener {
        void getVideoUri(Uri uri);
    }
}
