package golany.imagespickertest.builder

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import com.gun0912.tedonactivityresult.TedOnActivityResult
import golany.imagespickertest.ui.CamImagesPickerActivity
import golany.imagespickertest.common.Const
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

class CamImagePicker {

    companion object {
        @JvmStatic
        fun with(context: Context) = Builder(context)
    }


    @Parcelize
    class Builder(
        @IgnoredOnParcel
        private val context: Context? = null,
        var showNowCount: Boolean = true,
        var showMinMaxCount: Boolean = true,
        internal var minCount: Int = 0,
        internal var maxCount: Int = Int.MAX_VALUE,
        internal var selectedImages: List<Uri> = listOf()
    ) : Parcelable {

        fun showNowCount(show: Boolean): Builder = apply {
            this.showNowCount = show
        }

        fun showMinMaxCount(show: Boolean): Builder = apply{
            this.showMinMaxCount = show
        }

        fun min(minCount: Int): Builder = apply {
            this.minCount = minCount
        }

        fun max(maxCount: Int): Builder = apply {
            this.maxCount = maxCount
        }

        fun getMinMaxText(): String = "$minCount ~ $maxCount"

        fun selectedImages(images: List<Uri>): Builder = apply{
            this.selectedImages = images
        }

        fun startGetImages(action: (List<Uri>) -> Unit) {

            TedOnActivityResult.with(context)
                .setIntent(
                    Intent(
                        context,
                        CamImagesPickerActivity::class.java
                    ).apply { putExtra(Const.EXTRA_BUILDER, this@Builder) }
                )
                .setListener { resultCode, data ->
                    if (resultCode == Activity.RESULT_OK) {
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            data.getParcelableArrayListExtra(Const.EXTRA_SELECTED_URIS, Uri::class.java)
                        }else {
                            @Suppress("DEPRECATION")
                            data.getParcelableArrayListExtra<Uri>(Const.EXTRA_SELECTED_URIS)
                        }
                            ?.let { action(it) }
                    } else if (resultCode == Activity.RESULT_CANCELED) {

                    }
                }
                .startActivityForResult()

        }

    }

}