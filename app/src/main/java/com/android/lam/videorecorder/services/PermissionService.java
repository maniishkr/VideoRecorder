package com.android.lam.videorecorder.services;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.android.lam.videorecorder.Utils.AppConstants.PERMISSION_REQUEST;

public class PermissionService {

    private PermissionListener permissionListener;

    public void askPermissions(Activity activity, String... permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            permissionListener.permissionGranted(permissions);
            return;
        }
        permissionListener = (PermissionListener) activity;
        List<String> permissionNeededList = getPermissionNeededList(activity, permissions);
        if (permissionNeededList.size() > 0) {
            ActivityCompat.requestPermissions(activity, permissionNeededList.toArray(new String[permissionNeededList.size()]), PERMISSION_REQUEST);
            return;
        }
        permissionListener.permissionGranted(permissions);
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


    public void handlePermissionsResults(String[] permissions, int[] grantResults) {
        HashMap<String, Integer> permissionResult = new HashMap<>();
        int deniedCount = 0;
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                permissionResult.put(permissions[i], grantResults[i]);
                deniedCount++;
            }
        }
        if (deniedCount == 0 && permissionListener != null) {
            permissionListener.permissionGranted(permissions);
        } else {
            permissionListener.permissionDenied(permissionResult.keySet().toArray(new String[0]));
        }
    }

    public void handleDeniedPermission(Activity activity, String... permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission))
                permissionListener.shouldShowRequestPermission(permissions);
            else permissionListener.neverAskPermission(permissions);
            break;
        }
    }

    public interface PermissionListener {
        void permissionGranted(String... permissions);

        void permissionDenied(String... permissions);

        void shouldShowRequestPermission(String... permission);

        void neverAskPermission(String... permission);
    }
}
