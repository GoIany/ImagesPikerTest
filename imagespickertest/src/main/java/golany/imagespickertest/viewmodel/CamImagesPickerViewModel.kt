package golany.imagespickertest.viewmodel

import android.net.Uri
import androidx.lifecycle.*
import golany.imagespickertest.builder.CamImagePicker
import golany.imagespickertest.extenstion.deleteFile

internal class CamImagesPickerViewModel(builder: CamImagePicker.Builder): ViewModel() {

    var images = MutableLiveData(builder.selectedImages.toMutableList())

    val imagesSize = Transformations.map(images){ it.size }

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
        (images.value?.size ?: 0) < (max ?: 0)

    class Factory(private val builder: CamImagePicker.Builder): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CamImagesPickerViewModel(builder) as T
        }
    }

}