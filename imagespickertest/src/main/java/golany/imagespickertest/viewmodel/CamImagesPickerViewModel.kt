package golany.imagespickertest.viewmodel

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import golany.imagespickertest.extenstion.deleteFile

internal class CamImagesPickerViewModel: ViewModel() {

    var images = MutableLiveData(mutableListOf<Uri>())

    fun addImage(uri: Uri){
        images.value = images.value?.apply { add(uri) }?.toMutableList()
    }

    fun clearImages(){
        images.value?.forEach { it.deleteFile() }
        images.value = mutableListOf()
    }

}