package golany.imagespickertest

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

class CamImagesPickerActivity : AppCompatActivity() {

    val binding by lazy { ActivityCamImagesPickerBinding.inflate(layoutInflater) }

    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>

    var images = MutableLiveData(mutableListOf<Uri>())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.apply { activity = this@CamImagesPickerActivity }.root)

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        val imageCapture = ImageCapture.Builder()
            .build()

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(binding.camView, cameraProvider, imageCapture)
        }, ContextCompat.getMainExecutor(this))

        //region 캡쳐버튼 클릭
        binding.btnCameraCapture.setOnClickListener {

            val file = File(this.cacheDir, "${UUID.randomUUID()}")
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()

            imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(error: ImageCaptureException)
                    {

                    }
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        outputFileResults.savedUri?.let {
                            images.value = images.value?.apply { add(it) }?.toMutableList()
                        }
                    }
                }
            )

        }
        //endregion

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

}