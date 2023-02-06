package golany.imagespickertest.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AlphaAnimation
import androidx.activity.addCallback
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import golany.imagespickertest.databinding.ActivityCamImagesPickerBinding
import golany.imagespickertest.viewmodel.CamImagesPickerViewModel
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import androidx.activity.viewModels
import golany.imagespickertest.R
import golany.imagespickertest.builder.CamImagePicker
import golany.imagespickertest.common.Const
import golany.imagespickertest.common.Sound
import golany.imagespickertest.common.Toast
import golany.imagespickertest.common.toast

internal class CamImagesPickerActivity : AppCompatActivity() {

    private val binding by lazy { ActivityCamImagesPickerBinding.inflate(layoutInflater) }

    val viewModel: CamImagesPickerViewModel by viewModels { CamImagesPickerViewModel.Factory(builder) }

    val builder by lazy {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.getParcelableExtra(Const.EXTRA_BUILDER, CamImagePicker.Builder::class.java) ?: CamImagePicker.Builder()
        else
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<CamImagePicker.Builder>(Const.EXTRA_BUILDER) ?: CamImagePicker.Builder()
    }

    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>

    private var camera: Camera? = null

    private val imageCapture = ImageCapture.Builder().build()

    private val sound = Sound()

    private fun initOrientation(){
        this.requestedOrientation =
            if(builder.orientationFix) {
                if(builder.orientationVertical) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                else ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }else {
                ActivityInfo.SCREEN_ORIENTATION_SENSOR
            }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            binding.apply {
                activity = this@CamImagesPickerActivity
                viewModel = this@CamImagesPickerActivity.viewModel
                lifecycleOwner = this@CamImagesPickerActivity
            }.root
        )

        Toast.setApplication(this.application)

        isLensFacingBack = builder.isLensFacingBack

        cameraProviderFuture = ProcessCameraProvider.getInstance(this).apply {
            addListener(Runnable {
                val cameraProvider = cameraProviderFuture.get()
                val lensFacing = if(builder.isLensFacingBack) CameraSelector.LENS_FACING_BACK else CameraSelector.LENS_FACING_FRONT
                camera = bindPreview(cameraProvider = cameraProvider, lensFacing = lensFacing).apply {
                    cameraControl.enableTorch(viewModel.isTorchOn.value ?: false)
                }


                initOrientation()
            }, ContextCompat.getMainExecutor(this@CamImagesPickerActivity))
        }

        viewModel.isTorchOn.observe(this){
            binding.btnFlash.setImageResource(
                if(it) R.drawable.ic_flashlight_on else R.drawable.ic_flashlight_off
            )
            camera?.cameraControl?.enableTorch(it)
        }

        this.onBackPressedDispatcher.addCallback {
            viewModel.clearImages()
            setResult(Activity.RESULT_CANCELED)
            this@CamImagesPickerActivity.finish()
        }
    }

    private var isLensFacingBack = true
    private fun bindPreview(
        cameraProvider : ProcessCameraProvider,
        previewView: PreviewView = binding.camView,
        imageCapture: ImageCapture = this.imageCapture,
        lensFacing: Int? = null
    ): Camera {
        val preview : Preview = Preview.Builder()
            .build()

        val mLensFacing = lensFacing ?: if(this.isLensFacingBack) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK

        val cameraSelector : CameraSelector = CameraSelector.Builder()
            .requireLensFacing(mLensFacing)
            .build()

        isLensFacingBack = mLensFacing == CameraSelector.LENS_FACING_BACK

        preview.setSurfaceProvider(previewView.surfaceProvider)

        cameraProvider.unbindAll()
        return cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, imageCapture, preview)
    }

    /**
     *  set color statusBar, navigationBar
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if(hasFocus){
            window.statusBarColor = Color.BLACK
            window.navigationBarColor = Color.BLACK
        }
    }

    private var isCapturing = false
    fun cameraCaptured(){
        if(viewModel.checkMaxCount(builder.maxCount)) {
            if(isCapturing) return
            else isCapturing = true

            val file = File(builder.filePath ?: this.cacheDir, builder.fileName(viewModel.imagesSize.value ?: 0))
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()

            imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(error: ImageCaptureException) {
                        isCapturing = false
                    }

                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        isCapturing = false
                        outputFileResults.savedUri?.let {
                            sound.playShutter()
                            binding.camView.startAnimation(
                                AlphaAnimation(1f, 0f).apply { duration = 50 }
                            )
                            viewModel.addImage(it)
                        }
                    }
                }
            )
        }else{
            toast(
                builder.textOverMax?.replace("%d", builder.maxCount.toString()) ?:
                getString(R.string.toast_over_max, builder.maxCount)
            )
        }
    }

    fun cameraSwitch(){
        bindPreview(cameraProviderFuture.get())
    }

    fun torchSwitch(){
        viewModel.isTorchOn.value = !(viewModel.isTorchOn.value ?: false)
    }

    fun confirmClick(){
        if(!viewModel.checkMinCount(builder.minCount)){
            toast(
                builder.textUnderMin?.replace("%d", builder.minCount.toString()) ?:
                getString(R.string.toast_under_min, builder.minCount)
            )
        }else {
            val data = Intent().apply {
                putParcelableArrayListExtra(
                    Const.EXTRA_SELECTED_URIS, viewModel.images.value?.let { it1 -> ArrayList(it1) }
                )
            }
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }

}