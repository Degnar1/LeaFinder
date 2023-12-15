package pl.degnar.leafinder

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.GridView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_OPEN_GALLERY = 2
    private val REQUEST_PERMISSION = 3
    private val PREFS_NAME = "MyPrefsFile"

    private lateinit var gridView: GridView
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var capturedImagePaths: ArrayList<String>
    private lateinit var sharedPreferences: SharedPreferences

    private var selectedImageIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        capturedImagePaths = ArrayList()
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val captureButton: Button = findViewById(R.id.captureButton)
        val galleryButton: Button = findViewById(R.id.galleryButton)
        gridView = findViewById(R.id.gridView)
        imageAdapter = ImageAdapter(this, capturedImagePaths)
        gridView.adapter = imageAdapter

        loadSavedImagePaths()

        gridView = findViewById(R.id.gridView)
        imageAdapter = ImageAdapter(this, capturedImagePaths)
        gridView.adapter = imageAdapter


        gridView.setOnItemLongClickListener { _, _, position, _ ->
            // Obsługa przytrzymania na elemencie w gridview
            selectedImageIndex = position
            showDeleteConfirmationDialog()
            true
        }

        captureButton.setOnClickListener {
            checkPermissionAndCapture()
        }

        galleryButton.setOnClickListener {
            openGallery()
        }
    }

    private fun showDeleteConfirmationDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Delete Image")
        alertDialogBuilder.setMessage("Are you sure you want to delete this image?")
        alertDialogBuilder.setPositiveButton("Delete") { _, _ ->
            deleteSelectedImage()
        }
        alertDialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        alertDialogBuilder.create().show()
    }

    private fun deleteSelectedImage() {
        if (selectedImageIndex != -1 && selectedImageIndex < capturedImagePaths.size) {
            capturedImagePaths.removeAt(selectedImageIndex)
            saveImagePaths()
            imageAdapter.notifyDataSetChanged()
            selectedImageIndex = -1
        }
    }

    private fun checkPermissionAndCapture() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_PERMISSION
            )
        } else {
            captureImage()
        }
    }

    private fun captureImage() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if (data != null) {
                val selectedImageUri = data.data
                if (selectedImageUri != null) {
                    val imagePath = getImagePath(selectedImageUri)
                    if (imagePath != null) {
                        capturedImagePaths.add(imagePath)
                        saveImagePaths()
                        imageAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this, "Error retrieving image from gallery", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Selected image URI is null", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, REQUEST_OPEN_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_OPEN_GALLERY -> {
                if (resultCode == RESULT_OK && data != null) {
                    val selectedImageUri = data.data
                    if (selectedImageUri != null) {
                        try {
                            val inputStream = contentResolver.openInputStream(selectedImageUri)
                            val bitmap = inputStream?.let { BitmapFactory.decodeStream(it) }
                            inputStream?.close()

                            if (bitmap != null) {
                                val imageUri = saveImageToGallery(bitmap)
                                if (imageUri != null) {
                                    capturedImagePaths.add(imageUri.toString())
                                    saveImagePaths()
                                    imageAdapter.notifyDataSetChanged()
                                } else {
                                    Toast.makeText(this, "Error saving image to gallery", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this, "Error decoding image from gallery", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                            Toast.makeText(this, "Error reading image from gallery", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Selected image URI is null", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            REQUEST_IMAGE_CAPTURE -> {
                if (resultCode == RESULT_OK && data != null) {
                    val imageBitmap = data.extras?.get("data") as Bitmap?
                    if (imageBitmap != null) {
                        val imageUri = saveImageToGallery(imageBitmap)
                        if (imageUri != null) {
                            capturedImagePaths.add(imageUri.toString())
                            saveImagePaths()
                            imageAdapter.notifyDataSetChanged()
                        } else {
                            Toast.makeText(this, "Error saving image to gallery", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Error capturing image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun getImagePath(uri: Uri): String? {
        var imagePath: String? = null
        val cursor = contentResolver.query(uri, null, null, null, null)

        cursor?.use {
            if (it.moveToFirst()) {
                val idColumnIndex = it.getColumnIndex(MediaStore.Images.Media._ID)
                val id = it.getLong(idColumnIndex)
                val columnPathIndex = it.getColumnIndex(MediaStore.Images.Media.DATA)

                if (columnPathIndex != -1) {
                    imagePath = it.getString(columnPathIndex)
                } else {
                    // In some cases, the path is not available in the DATA column
                    val imageUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())
                    val projection = arrayOf(MediaStore.Images.Media.DATA)
                    val secondCursor = contentResolver.query(imageUri, projection, null, null, null)

                    secondCursor?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                            imagePath = cursor.getString(columnIndex)
                        }
                    }
                }
            }
        }

        return imagePath
    }

    private fun saveImageToGallery(bitmap: Bitmap): Uri? {
        val contentResolver = contentResolver
        val imageFileName = "IMG_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(
            Date()
        )
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        return try {
            contentResolver.openOutputStream(imageUri!!)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            imageUri
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun saveImagePaths() {
        val editor = sharedPreferences.edit()
        editor.putStringSet("imagePaths", capturedImagePaths.toSet())
        editor.apply()
    }


    private fun loadSavedImagePaths() {
        val savedImagePathsSet = sharedPreferences.getStringSet("imagePaths", null)
        if (savedImagePathsSet != null) {
            capturedImagePaths.clear()  // Clear existing paths before adding saved paths
            capturedImagePaths.addAll(savedImagePathsSet)
            imageAdapter.notifyDataSetChanged()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureImage()
            } else {
                Toast.makeText(
                    this,
                    "Camera permission is required to capture images",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
