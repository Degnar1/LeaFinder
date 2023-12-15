package pl.degnar.leafinder

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import java.util.ArrayList

class ImageAdapter(private val context: Context, private val imagePaths: ArrayList<String>) : BaseAdapter() {

    override fun getCount(): Int {
        return imagePaths.size
    }

    override fun getItem(position: Int): Any {
        return imagePaths[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val viewHolder: ViewHolder
        val view: View

        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.grid_item_layout, null) as LinearLayout

            val imageView: ImageView = view.findViewById(R.id.gridImageView)
            val button: Button = view.findViewById(R.id.connectButton)

            viewHolder = ViewHolder(imageView, button)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val targetWidth = 200 // Wprowadź żądaną szerokość
        val targetHeight = 200 // Wprowadź żądaną wysokość
        viewHolder.imageView.layoutParams.width = targetWidth
        viewHolder.imageView.layoutParams.height = targetHeight
        viewHolder.imageView.requestLayout()

        // Ustawienie obrazu w ImageView
        val imagePath = imagePaths[position]
        // Tutaj można użyć dowolnej biblioteki do ładowania obrazów, np. Glide lub Picasso
        // W tym przypadku zakładamy, że imagePath zawiera lokalny ścieżkę do pliku obrazu
        viewHolder.imageView.setImageURI(Uri.parse(imagePath))

        // Ustawienie przycisku pod obrazem
        viewHolder.button.setOnClickListener {
            // Tutaj dodaj logikę, która ma zostać wykonana po naciśnięciu przycisku
            // Może to być np. otwarcie nowej aktywności, wywołanie jakiejś funkcji itp.
            // Wartość position pozwala zidentyfikować, który przycisk został naciśnięty
            // np. można uzyskać ścieżkę do obrazu za pomocą imagePaths[position]
            // i wykonać odpowiednie działania
            // Na razie przykładowa wiadomość do wyświetlenia
            Toast.makeText(context, "Button clicked for image at position $position", Toast.LENGTH_SHORT).show()
        }
        viewHolder.imageView.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        viewHolder.imageView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        viewHolder.imageView.requestLayout()
        viewHolder.imageView.layoutParams.height = 120.dpToPx(parent?.context) // Ustaw stałą wysokość

        return view
    }
    private fun Int.dpToPx(context: Context?): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return (this * scale).toInt()
    }
    private class ViewHolder(val imageView: ImageView, val button: Button)

}
