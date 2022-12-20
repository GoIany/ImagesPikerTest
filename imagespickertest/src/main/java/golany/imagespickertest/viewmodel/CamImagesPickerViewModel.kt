package golany.imagespickertest.viewmodel

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import golany.imagespickertest.extenstion.deleteFile

internal class CamImagesPickerViewModel: ViewModel() {

    var images = MutableLiveData(mutableListOf<Uri>())

    fun addImage(uri: Uri){
        images.value = images.value?.apply { add(0, uri) }?.toMutableList()
    }

    fun clearImages(){
        images.value?.forEach { it.deleteFile() }
        images.value = mutableListOf()
    }

    fun checkMinCount(min: Int?): Boolean =
        (images.value?.size ?: 0) >= (min ?: 0)

    fun checkMaxCount(max: Int?): Boolean =
        (images.value?.size ?: 0) <= (max ?: 0)

}