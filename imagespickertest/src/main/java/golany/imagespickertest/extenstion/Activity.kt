package golany.imagespickertest.extenstion

import android.app.Activity
import android.content.Intent
import android.util.Log


fun Activity.onActivityResult(requestCode: Int, resultCode: Int, data: Intent){
    Log.d("xorm","requestCode: $requestCode, rsultCode: $resultCode, data: $data")
}