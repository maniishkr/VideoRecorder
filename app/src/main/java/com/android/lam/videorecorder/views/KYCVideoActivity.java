package com.android.lam.videorecorder.views;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.camera.core.VideoCapture;
import androidx.camera.core.VideoCaptureConfig;

import com.android.lam.videorecorder.R;
import com.android.lam.videorecorder.Utils.Utils;
import com.android.lam.videorecorder.services.PermissionService;

import java.io.File;


public class KYCVideoActivity extends AppCompatActivity implements PermissionService.VideoRecorderListener {

    private PermissionService permissionService;
    private TextureView textureView;
    private ImageButton captureButton, stopButton;
    private VideoCapture videoCapture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_k_y_c_video);
        initViews();
        if (Utils.hasCamera(getApplicationContext())) {
            permissionService = new PermissionService();
            permissionService.startVideoRecorder(this);
        }
    }

    @SuppressLint({"RestrictedApi", "ClickableViewAccessibility"})
    private void setTouchListener() {
        File file = new File(Environment.getExternalStorageDirectory() + "/" + System.currentTimeMillis() + ".mp4");
        captureButton = findViewById(R.id.capture_button);

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoCapture.startRecording(file, new VideoCapture.OnVideoSavedListener() {
                    @Override
                    public void onVideoSaved(File file) {

                    }

                    @Override
                    public void onError(VideoCapture.UseCaseError useCaseError, String message, @Nullable Throwable cause) {

                    }
                });
            }
        });

        stopButton.setOnClickListener(v -> videoCapture.stopRecording());
    }

    @SuppressLint("RestrictedApi")
    private void initConfiguration() {


        Rational aspectRatio = new Rational(textureView.getWidth(), textureView.getHeight());
        Size screen = new Size(textureView.getWidth(), textureView.getHeight());
        PreviewConfig pConfig = new PreviewConfig.Builder().setTargetAspectRatio(aspectRatio).setTargetResolution(screen).build();
        Preview preview = new Preview(pConfig);
        preview.setOnPreviewOutputUpdateListener(output -> {
            textureView.setSurfaceTexture(output.getSurfaceTexture());
        });

        VideoCaptureConfig videoCaptureConfig = new VideoCaptureConfig.Builder()
                .setLensFacing(CameraX.LensFacing.FRONT)
                .setTargetAspectRatio(new Rational(16, 9))
                .build();

        videoCapture = new VideoCapture(videoCaptureConfig);
        setTouchListener();
    }

    private void initViews() {
        textureView = findViewById(R.id.view_finder);
        stopButton = findViewById(R.id.stop_button);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (permissionService != null)
            permissionService.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void getVideoUri(Uri uri) {
        Log.d("asdf", "video uri is " + uri);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionService != null)
            permissionService.onRequestPermissionResult(this, requestCode, permissions, grantResults);
    }

}