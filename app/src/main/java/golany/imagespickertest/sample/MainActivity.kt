package golany.imagespickertest.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import golany.imagespickertest.builder.CamImagePicker

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.btn_start_images).setOnClickListener {
            CamImagePicker.with(this).startGetImages {  }
        }
    }
}