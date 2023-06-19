package com.example.caliAiTranier.model

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.caliAiTrainer.R


class TargetActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_target)

        imageView = findViewById(R.id.imageView)

        val poseResultUri = intent.getParcelableExtra<Uri>("poseResultUri")
        poseResultUri?.let {
            imageView.setImageURI(it)
        }
    }
}

