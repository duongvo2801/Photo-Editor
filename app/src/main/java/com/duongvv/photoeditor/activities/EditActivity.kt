package com.duongvv.photoeditor.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import com.dsphotoeditor.sdk.activity.DsPhotoEditorActivity
import com.dsphotoeditor.sdk.utils.DsPhotoEditorConstants
import com.duongvv.photoeditor.MainActivity
import com.duongvv.photoeditor.R
import com.duongvv.photoeditor.databinding.ActivityEditBinding

class EditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit)

        val imageUri = intent.getStringExtra("imageUri")
//        Log.d("OriginalImageUri", imageUri.toString())

        if (imageUri != null) {
            val dsPhotoEditorIntent = Intent(this, DsPhotoEditorActivity::class.java)
            dsPhotoEditorIntent.data = imageUri.toUri()
            dsPhotoEditorIntent.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_OUTPUT_DIRECTORY, "Pictures")

            startActivityForResult(dsPhotoEditorIntent, 100)
            Log.d("ds", dsPhotoEditorIntent.toString())
        } else {
            Toast.makeText(this, "Image URI is null", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100) {
            // check data
            val outputUri: Uri? = data?.data
            Log.d("EditedImageUri", outputUri.toString())
            if (outputUri != null) {
                backHome()
                Toast.makeText(this, "Editing successful", Toast.LENGTH_SHORT).show()
            } else {
                backHome()
                Log.e("Error", "")
            }
        } else {
            Toast.makeText(this, "Editing cancelled or failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun backHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}
