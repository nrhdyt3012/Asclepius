package com.dicoding.asclepius.view

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.asclepius.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUri = intent.getStringExtra("IMAGE_URI")?.let { Uri.parse(it) }
        val resultText = intent.getStringExtra("RESULT_TEXT")
        val confidenceScore = intent.getFloatExtra("CONFIDENCE_SCORE",0f)
        val errorMessage = intent.getStringExtra("ERROR_MESSAGE")
        @SuppressLint("DefaultLocale")
        fun Float.formatToString(): String {
            return String.format("%.2f%%", this * 100)
        }

        if (errorMessage != null) {
            binding.resultText.text = errorMessage
        } else {
            binding.resultImage.setImageURI(imageUri)
            binding.resultText.text = "$resultText ${confidenceScore.formatToString()}"
        }
    }
}
