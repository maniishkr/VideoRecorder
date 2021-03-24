package com.android.lam.videorecorder.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.TextureView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.android.lam.videorecorder.R;
import com.android.lam.videorecorder.services.PermissionService;


public class VideoRecorderActivity extends AppCompatActivity implements PermissionService.VideoRecorderListener {

    private PermissionService permissionService;
    private TextureView viewFinder;
    private ImageButton captureButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_recorder);
        /*viewFinder = findViewById(R.id.view_finder);
        captureButton = findViewById(R.id.capture_button);*/
        permissionService = new PermissionService();
        permissionService.askVideoRecorderPermission(this);

    }

    @Override
    public void permissionGranted(String... permissions) {
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        navController.navigate(R.id.videoFragment);
    }

    @Override
    public void permissionDenied(String... permissions) {

    }

    @Override
    public void shouldShowRational(String... permissions) {

    }

    @Override
    public void neverAsk(String... permission) {

    }

    @Override
    public void getVideoUri(Uri uri) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        permissionService.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionService.onRequestPermissionResult(this, requestCode, permissions, grantResults);
    }

   /* @SuppressLint("RestrictedApi")
    private void startVideo() {
        PreviewConfig previewConfig = new PreviewConfig.Builder().build();
        // Build the viewfinder use case
        Preview preview = new Preview(previewConfig);

        // Create a configuration object for the video use case
        VideoCaptureConfig videoCaptureConfig = new VideoCaptureConfig.Builder().build();
        videoCapture = new VideoCapture(videoCaptureConfig);

        preview.setOnPreviewOutputUpdateListener(output ->
                viewFinder.setSurfaceTexture(output.getSurfaceTexture())
        );

        // Bind use cases to lifecycle
        CameraX.bindToLifecycle(this, preview, videoCapture);
    }

    @SuppressLint({"ClickableViewAccessibility", "RestrictedApi"})
    private void setTouchListeners() {
        File file = new File(getExternalMediaDirs()[0],"video.mp4");
        captureButton.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                captureButton.setBackgroundColor(Color.GREEN);
                videoCapture.startRecording(file, new VideoCapture.OnVideoSavedListener() {
                    @Override
                    public void onVideoSaved(File file) {
                        Log.d("asdf","file is "+ file);
                    }

                    @Override
                    public void onError(VideoCapture.UseCaseError useCaseError, String message, @Nullable Throwable cause) {
                        cause.printStackTrace();
                    }
                });
            } else if( event.getAction() == MotionEvent.ACTION_UP){
                captureButton.setBackgroundColor(Color.RED);
                videoCapture.stopRecording();
                Log.d("asdf", "Video File stopped");
            }
            return false;
        });
    }*/
}