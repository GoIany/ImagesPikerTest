package golany.imagespickertest

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AlphaAnimation
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.google.common.util.concurrent.ListenableFuture
import golany.imagespickertest.databinding.ActivityCamImagesPickerBinding
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class CamImagesPickerActivity : AppCompatActivity() {

    private val binding by lazy { ActivityCamImagesPickerBinding.inflate(layoutInflater) }

    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>

    var images = MutableLiveData(mutableListOf<Uri>())

    private val imageCapture = ImageCapture.Builder().build()

    private val sound = Sound()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.apply { activity = this@CamImagesPickerActivity }.root)

        cameraProviderFuture = ProcessCameraProvider.getInstance(this).apply {
            addListener(Runnable {
                val cameraProvider = cameraProviderFuture.get()
                bindPreview(binding.camView, cameraProvider, imageCapture)
            }, ContextCompat.getMainExecutor(this@CamImagesPickerActivity))
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

    fun cameraCaptured(){
        val file = File(this.cacheDir, "${UUID.randomUUID()}")
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(error: ImageCaptureException)
                {

                }
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    outputFileResults.savedUri?.let {
                        sound.playShutter()
                        binding.camView.startAnimation(
                            AlphaAnimation(1f,0f).apply { duration = 50 }
                        )
                        images.value = images.value?.apply { add(it) }?.toMutableList()
                    }
                }
            }
        )
    }

    fun confirmClick(){
        val data = Intent().apply {
            putParcelableArrayListExtra(
                Const.EXTRA_SELECTED_URIS, images.value?.let { it1 -> ArrayList(it1) }
            )
        }
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }

}