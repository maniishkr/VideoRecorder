package com.android.lam.videorecorder.views.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.MediaStore;

import com.android.lam.videorecorder.views.services.PermissionService;

public class Utils {

    public static void dispatchTakeVideoIntent(Activity activity) {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        takeVideoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        takeVideoIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        takeVideoIntent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        if (takeVideoIntent.resolveActivity(activity.getPackageManager()) != null) {
            takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
            takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            activity.startActivityForResult(takeVideoIntent, PermissionService.REQUEST_VIDEO_CAPTURE);

        }

    }

    public static boolean hasCamera(Context context) {
        return (context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA_ANY));
    }
}
