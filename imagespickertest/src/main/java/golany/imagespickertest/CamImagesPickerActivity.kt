package golany.imagespickertest

import android.app.Activity
import android.content.Intent
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
import golany.imagespickertest.builder.CamImagePicker
import golany.imagespickertest.common.Toast
import golany.imagespickertest.common.toast

internal class CamImagesPickerActivity : AppCompatActivity() {

    private val binding by lazy { ActivityCamImagesPickerBinding.inflate(layoutInflater) }

    val viewModel: CamImagesPickerViewModel by viewModels { CamImagesPickerViewModel.Factory(builder) }

    val builder by lazy {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.getParcelableExtra(Const.EXTRA_BUILDER, CamImagePicker.Builder::class.java) ?: CamImagePicker.Builder()
        else
            intent.getParcelableExtra<CamImagePicker.Builder>(Const.EXTRA_BUILDER) ?: CamImagePicker.Builder()
    }

    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>

    private val imageCapture = ImageCapture.Builder().build()

    private val sound = Sound()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            binding.apply {
                activity = this@CamImagesPickerActivity
                viewModel = this@CamImagesPickerActivity.viewModel
            }.root
        )

        Toast.setApplication(this.application)

        cameraProviderFuture = ProcessCameraProvider.getInstance(this).apply {
            addListener(Runnable {
                val cameraProvider = cameraProviderFuture.get()
                bindPreview(binding.camView, cameraProvider, imageCapture)
            }, ContextCompat.getMainExecutor(this@CamImagesPickerActivity))
        }

        this.onBackPressedDispatcher.addCallback {
            viewModel.clearImages()
            setResult(Activity.RESULT_CANCELED)
            this@CamImagesPickerActivity.finish()
        }
    }

    fun bindPreview(previewView: PreviewView, cameraProvider : ProcessCameraProvider, imageCapture: ImageCapture): Camera {
        val preview : Preview = Preview.Builder()
            .build()

        val cameraSelector : CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        preview.setSurfaceProvider(previewView.surfaceProvider)

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

    fun cameraCaptured(){
        if(viewModel.checkMaxCount(builder.maxCount)) {
            val file = File(this.cacheDir, "${UUID.randomUUID()}")
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()

            imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(error: ImageCaptureException) {

                    }

                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
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
            toast(getString(R.string.toast_over_max, builder.maxCount))
        }
    }

    fun confirmClick(){
        if(!viewModel.checkMinCount(builder.minCount)){
            toast(getString(R.string.toast_under_min, builder.minCount))
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