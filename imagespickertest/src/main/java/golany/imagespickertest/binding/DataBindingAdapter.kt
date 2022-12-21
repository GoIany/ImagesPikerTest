package golany.imagespickertest.binding

import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.findViewTreeLifecycleOwner


@BindingAdapter("liveData")
fun <T> TextView.setLiveData(item: LiveData<T>?){
    this.findViewTreeLifecycleOwner()?.let {
        item?.observe(it) {
            this.setText(it.toString())
        }
    }
}