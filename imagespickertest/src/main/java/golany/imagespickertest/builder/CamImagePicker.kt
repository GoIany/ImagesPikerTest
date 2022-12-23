package golany.imagespickertest.builder

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import com.gun0912.tedonactivityresult.TedOnActivityResult
import golany.imagespickertest.CamImagesPickerActivity
import golany.imagespickertest.Const
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

class CamImagePicker {

    companion object {
        @JvmStatic
        fun with(context: Context) = Builder(context) as Builder
    }


    @Parcelize
    class Builder(
        @IgnoredOnParcel
        private val context: Context? = null,
        var showNowCount: Boolean = true,
        var showMinMaxCount: Boolean = true,
        internal var minCount: Int = 0,
        internal var maxCount: Int = Int.MAX_VALUE
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
                        data.getParcelableArrayListExtra<Uri>(Const.EXTRA_SELECTED_URIS)
                            ?.let { action(it) }
                    } else if (resultCode == Activity.RESULT_CANCELED) {

                    }
                }
                .startActivityForResult()

        }

    }

}