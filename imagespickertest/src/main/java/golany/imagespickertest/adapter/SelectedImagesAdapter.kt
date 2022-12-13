package golany.imagespickertest.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import golany.imagespickertest.databinding.ItemSelectedImageBinding

class SelectedImagesRecyclerAdapter(): RecyclerView.Adapter<SelectedImagesRecyclerAdapter.ViewHolder>() {

    var images = MutableLiveData(mutableListOf<Uri>())

    inner class ViewHolder(var binding: ItemSelectedImageBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(uri: Uri?){
            Glide.with(binding.root)
                .load(uri)
                .transform(CenterCrop())
                .into(binding.imageView)

            binding.btnCancel.setOnClickListener {
                images.value = images.value?.apply { remove(uri) }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemSelectedImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ).apply {}
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(images.value?.get(position))
    }

    override fun getItemCount(): Int = images.value?.size ?: 0

}

@BindingAdapter("images")
fun RecyclerView?.setImages(
    images: MutableLiveData<MutableList<Uri>>?
){
    val adapter = SelectedImagesRecyclerAdapter()
    this?.adapter = adapter

    if (images != null) {
        (this?.adapter as? SelectedImagesRecyclerAdapter)?.images = images
    }
    this?.findViewTreeLifecycleOwner()?.let {
        images?.observe(it) { adapter.notifyDataSetChanged() }
    }
}