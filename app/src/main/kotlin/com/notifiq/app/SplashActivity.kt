package com.notifiq.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        val videoView = findViewById<VideoView>(R.id.splashVideo)
        val uri = Uri.parse("android.resource://$packageName/${R.raw.splash}")
        videoView.setVideoURI(uri)
        videoView.setOnPreparedListener { it.isLooping = false }
        videoView.setOnCompletionListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        videoView.start()
    }
}