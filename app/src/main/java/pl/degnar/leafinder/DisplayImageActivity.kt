// DisplayImageActivity.kt

package pl.degnar.leafinder

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class DisplayImageActivity : AppCompatActivity() {

    companion object {
        const val IMAGE_PATH_EXTRA = "image_path_extra"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_image)

        val imagePath = intent.getStringExtra(IMAGE_PATH_EXTRA)
        if (imagePath != null) {
            val imageView: ImageView = findViewById(R.id.imageView)
            val bitmap = BitmapFactory.decodeFile(imagePath)
            imageView.setImageBitmap(bitmap)
        }
    }
}
