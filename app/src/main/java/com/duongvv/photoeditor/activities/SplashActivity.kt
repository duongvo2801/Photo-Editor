package com.duongvv.photoeditor.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.duongvv.photoeditor.R
import android.os.Handler
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.duongvv.photoeditor.MainActivity
import com.duongvv.photoeditor.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)

        Glide.with(this)
            .load(R.drawable.splash_image)
            .into(binding.splashImage)

        Handler().postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3000)

        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
    }
}