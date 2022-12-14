package golany.imagespickertest.builder

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.gun0912.tedonactivityresult.TedOnActivityResult
import golany.imagespickertest.CamImagesPickerActivity
import golany.imagespickertest.Const

class CamImagePicker {

    companion object {
        @JvmStatic
        fun with(context: Context) = Builder(context)
    }


    class Builder(private val context: Context) : CamImagePickerBaseBuilder() {

        fun startGetImages(action: (List<Uri>) -> Unit){

            TedOnActivityResult.with(context)
                .setIntent(
                    Intent(context, CamImagesPickerActivity::class.java).apply{ putExtra(Const.EXTRA_BUILDER, this@Builder) }
                )
                .setListener { resultCode, data ->
                    if(resultCode == Activity.RESULT_OK){
                        data.getParcelableArrayListExtra<Uri>(Const.EXTRA_SELECTED_URIS)?.let { action(it) }
                    }else if(resultCode == Activity.RESULT_CANCELED){

                    }
                }
                .startActivityForResult()

        }

    }

}