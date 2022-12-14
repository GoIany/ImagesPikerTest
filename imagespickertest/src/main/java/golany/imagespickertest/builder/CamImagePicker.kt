package golany.imagespickertest.builder

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import golany.imagespickertest.CamImagesPickerActivity

class CamImagePicker {

    companion object {
        @JvmStatic
        fun with(context: Context) = Builder(context)
    }


    class Builder(private val context: Context) : CamImagePickerBaseBuilder() {

        var imagesRecv: (List<Uri>) -> Unit = {}

        fun startGetImages(action: (List<Uri>) -> Unit){
            imagesRecv = action

            (context as Activity).startActivityForResult(
                Intent(context, CamImagesPickerActivity::class.java).apply{
                    putExtra("BUILDER", this@Builder)
                }, 1
            )
        }

    }

}