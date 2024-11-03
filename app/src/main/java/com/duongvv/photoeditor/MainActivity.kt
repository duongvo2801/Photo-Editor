package com.duongvv.photoeditor

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.duongvv.photoeditor.activities.EditActivity
import com.duongvv.photoeditor.activities.TempActivity
import com.duongvv.photoeditor.adapter.ImageAdapter
import com.duongvv.photoeditor.databinding.ActivityMainBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var imageAdapter: ImageAdapter
    private var imagePaths = mutableListOf<Any>()
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAndRequestPermissions()

        loadImagesToRecyclerView()

        // Track the change of the photo gallery
        contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserver
        )

        binding.fab.setOnClickListener {
            openCamera()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            loadImagesToRecyclerView()
        }

        // status bar
        window.statusBarColor = ContextCompat.getColor(this, R.color.color_main)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        try {
            if (data != null) {
                imageUri = data.data

                val intent = Intent(this, EditActivity::class.java)
                intent.putExtra("imageUri", imageUri.toString())
                startActivity(intent)

            }
        } catch (e: Exception) {
            Log.e("onActivityResult", "Error handling result", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        contentResolver.unregisterContentObserver(contentObserver)
    }

    private val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)
            loadImagesToRecyclerView()
        }
    }

    private fun loadImagesToRecyclerView() {
        binding.swipeRefreshLayout.isRefreshing = true

        imagePaths.clear()
        imagePaths.addAll(getAllImagesFromGallery())
//        Log.d("Image paths", "Image paths: $imagePaths")

        if (binding.recyclerView.layoutManager == null) {
            val gridLayoutManager = GridLayoutManager(this, 4)
            gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (imageAdapter.getItemViewType(position) == 1) 4 else 1
                }
            }
            binding.recyclerView.layoutManager = gridLayoutManager
        }

        imageAdapter = ImageAdapter(imagePaths) { _ ->
            ImagePicker.with(this)
                .crop()
                .start()

        }

        binding.recyclerView.adapter = imageAdapter
        imageAdapter.notifyDataSetChanged()

        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun getAllImagesFromGallery(): List<Any> {
        val imageList = mutableListOf<Any>()
        val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_ADDED)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val cursor = contentResolver.query(uri, projection, null, null, sortOrder)
        var currentDay = ""

        val dateFormatCurrentYear = SimpleDateFormat("dd MMMM", Locale("vi"))
        val dateFormatOtherYears = SimpleDateFormat("dd MMMM yyyy", Locale("vi"))
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        val calendar = Calendar.getInstance()
        val today = dateFormatCurrentYear.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = dateFormatCurrentYear.format(calendar.time)

        cursor?.use {
            val dataColumnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val dateColumnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

            while (it.moveToNext()) {
                val imagePath = it.getString(dataColumnIndex)
                val dateAdded = it.getLong(dateColumnIndex) * 1000
                val imageDate = Date(dateAdded)
                val imageCalendar = Calendar.getInstance().apply { time = imageDate }
                val imageYear = imageCalendar.get(Calendar.YEAR)

                val dayImage = if (imageYear == currentYear) {
                    dateFormatCurrentYear.format(imageDate)
                } else {
                    dateFormatOtherYears.format(imageDate)
                }

                val displayDay = when (dayImage) {
                    today -> "Hôm nay"
                    yesterday -> "Hôm qua"
                    else -> dayImage
                }

                if (displayDay != currentDay) {
                    imageList.add(displayDay)
                    currentDay = displayDay
                }
                imageList.add(imagePath)
            }
        }
        return imageList
    }

    private fun openCamera() {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Photo-Editor")
        }

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        }

        cameraLauncher.launch(intent)
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, "Save gallery!", Toast.LENGTH_SHORT).show()
            loadImagesToRecyclerView()
        } else {
            imageUri?.let { contentResolver.delete(it, null, null) }
        }
    }

    private fun checkAndRequestPermissions() {
        val requiredPermissions = mutableListOf(
            Manifest.permission.CAMERA,
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
                Manifest.permission.READ_MEDIA_IMAGES
            else
                Manifest.permission.READ_EXTERNAL_STORAGE
        )

        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), CAMERA_PERMISSION_REQUEST_CODE)
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                loadImagesToRecyclerView()
                Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, TempActivity::class.java)
                startActivity(intent)
                Toast.makeText(this, "Access denied. Please grant permission to use", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }
}
