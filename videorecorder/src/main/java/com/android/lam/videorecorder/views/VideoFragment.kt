package com.android.lam.videorecorder.views

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.android.lam.videorecorder.R
import com.android.lam.videorecorder.Utils.MainExecutor
import java.io.File
import java.lang.Math.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [VideoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@SuppressLint("RestrictedApi")
class VideoFragment : Fragment() {

    companion object {
        private const val TAG = "CameraXDemo"

        const val KEY_GRID = "sPrefGridVideo"

        private const val RATIO_4_3_VALUE = 4.0 / 3.0 // aspect ratio 4x3
        private const val RATIO_16_9_VALUE = 16.0 / 9.0 // aspect ratio 16x9
    }

    protected val outputDirectory: String by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "${Environment.DIRECTORY_DCIM}/CameraXDemo/"
        } else {
            "${requireContext().getExternalFilesDir(Environment.DIRECTORY_DCIM)?.path}/CameraXDemo/"
        }
    }

    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var videoCapture: VideoCapture? = null

    private var displayId = -1
    private var toggleCamera = false

    private lateinit var viewFinder: PreviewView
    private lateinit var btnRecordVideo: ImageButton
    private lateinit var btnGallery: ImageButton
    private lateinit var btnSwitchCamera: ImageButton
    private lateinit var btnGrid: ImageButton
    private lateinit var btnFlash: ImageButton

    // Selector showing which camera is selected (front or back)
    private var lensFacing = CameraSelector.DEFAULT_BACK_CAMERA
    private val displayManager by lazy { requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager }

    // Selector showing which flash mode is selected (on, off or auto)


    // Selector showing is grid enabled or not
    private var hasGrid = false

    // Selector showing is flash enabled or not
    private var isTorchOn = false

    // Selector showing is recording currently active
    private var isRecording = false
    private val animateRecord by lazy {
        ObjectAnimator.ofFloat(btnRecordVideo, View.ALPHA, 1f, 0.5f).apply {
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
        }
    }


    /**
     * A display listener for orientation changes that do not trigger a configuration
     * change, for example if we choose to override config change in manifest or for 180-degree
     * orientation changes.
     */
    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit

        @SuppressLint("UnsafeExperimentalUsageError")
        override fun onDisplayChanged(displayId: Int) = view?.let { view ->
            if (displayId == this@VideoFragment.displayId) {
                preview?.targetRotation = view.display.rotation
                videoCapture?.setTargetRotation(view.display.rotation)
            }
        } ?: Unit
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(com.android.lam.videorecorder.R.layout.fragment_video, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)

        displayManager.registerDisplayListener(displayListener, null)

        run {
            viewFinder.addOnAttachStateChangeListener(object :
                    View.OnAttachStateChangeListener {
                override fun onViewDetachedFromWindow(v: View) =
                        displayManager.registerDisplayListener(displayListener, null)

                override fun onViewAttachedToWindow(v: View) =
                        displayManager.unregisterDisplayListener(displayListener)
            })
            btnRecordVideo.setOnClickListener { recordVideo() }
            btnSwitchCamera.setOnClickListener { toggleCamera() }
            btnFlash.setOnClickListener { toggleFlash() }
        }
    }


    /**
     * Create some initial states
     * */
    private fun initViews(view: View) {
        btnRecordVideo = view.findViewById(R.id.btnRecordVideo)
        btnGallery = view.findViewById(R.id.btnGallery)
        btnSwitchCamera = view.findViewById(R.id.btnSwitchCamera)
        btnFlash = view.findViewById(R.id.btnFlash)
        btnGrid = view.findViewById(R.id.btnGrid)
        viewFinder = view.findViewById(R.id.viewFinder)
        adjustInsets()
    }

    /**
     * This methods adds all necessary margins to some views based on window insets and screen orientation
     * */
    private fun adjustInsets() {

    }

    /**
     * Change the facing of camera
     *  toggleButton() function is an Extension function made to animate button rotation
     * */
    private fun toggleCamera() {
        toggleCamera = !toggleCamera
        lensFacing = if (toggleCamera) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }

        startCamera()
    }

    /**
     * Unbinds all the lifecycles from CameraX, then creates new with new parameters
     * */
    private fun startCamera() {
        // This is the Texture View where the camera will be rendered
        val viewFinder = viewFinder

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(Runnable {
            cameraProvider = cameraProviderFuture.get()

            // The display information
            val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
            // The ratio for the output image and preview
            val aspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
            // The display rotation
            val rotation = viewFinder.display.rotation

            val localCameraProvider = cameraProvider
                    ?: throw IllegalStateException("Camera initialization failed.")

            // The Configuration of camera preview
            preview = Preview.Builder()
                    .setTargetAspectRatio(aspectRatio) // set the camera aspect ratio
                    .setTargetRotation(rotation) // set the camera rotation
                    .build()

            val videoCaptureConfig = VideoCapture.DEFAULT_CONFIG.config // default config for video capture
            // The Configuration of video capture
            videoCapture = VideoCapture.Builder
                    .fromConfig(videoCaptureConfig)
                    .build()

            localCameraProvider.unbindAll() // unbind the use-cases before rebinding them

            try {
                // Bind all use cases to the camera with lifecycle
                camera = localCameraProvider.bindToLifecycle(
                        viewLifecycleOwner, // current lifecycle owner
                        lensFacing, // either front or back facing
                        preview, // camera preview use case
                        videoCapture, // video capture use case
                )

                // Attach the viewfinder's surface provider to preview use case
                preview?.setSurfaceProvider(viewFinder.surfaceProvider)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to bind use cases", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    /**
     *  Detecting the most suitable aspect ratio for current dimensions
     *
     *  @param width - preview width
     *  @param height - preview height
     *  @return suitable aspect ratio
     */
    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    /**
     * Navigate to PreviewFragment
     * */

    private fun recordVideo() {
        val localVideoCapture = videoCapture
                ?: throw IllegalStateException("Camera initialization failed.")

        // Options fot the output video file
        val outputOptions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis())
                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                put(MediaStore.MediaColumns.RELATIVE_PATH, outputDirectory)
            }

            requireContext().contentResolver.run {
                val contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

                VideoCapture.OutputFileOptions.Builder(this, contentUri, contentValues)
            }
        } else {
            File(outputDirectory).mkdirs()
            val file = File("$outputDirectory/${System.currentTimeMillis()}.mp4")

            VideoCapture.OutputFileOptions.Builder(file)
        }.build()

        if (!isRecording) {
            animateRecord.start()
            localVideoCapture.startRecording(
                    outputOptions, // the options needed for the final video
                    MainExecutor(), // the executor, on which the task will run
                    object : VideoCapture.OnVideoSavedCallback { // the callback after recording a video
                        override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
                            // Create small preview
                            outputFileResults.savedUri
                                    ?.let { uri ->
                                        Log.d(TAG, "Video saved in $uri")
                                    }
                        }

                        override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
                            // This function is called if there is an error during recording process
                            animateRecord.cancel()
                            val msg = "Video capture failed: $message"
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                            Log.e(TAG, msg)
                            cause?.printStackTrace()
                        }
                    })
        } else {
            animateRecord.cancel()
            localVideoCapture.stopRecording()
        }
        isRecording = !isRecording
    }

    /**
     * Turns on or off the grid on the screen
     * */

    /**
     * Turns on or off the flashlight
     * */
    private fun toggleFlash() {

        /*  flashMode = !flashMode
          if(flashMode)
          flashMode = if (flashMode) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
          camera?.cameraControl?.enableTorch(flag)*/
    }


    override fun onStop() {
        super.onStop()
        camera?.cameraControl?.enableTorch(false)
    }
}