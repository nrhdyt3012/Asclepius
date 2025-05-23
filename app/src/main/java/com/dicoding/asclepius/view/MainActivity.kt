package com.dicoding.asclepius.view

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import com.dicoding.asclepius.viewmodel.MainViewModel
import com.yalantis.ucrop.UCrop
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.io.File

class MainActivity : AppCompatActivity(), ImageClassifierHelper.ClassifierListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var imageClassifierHelper: ImageClassifierHelper
    private val mainViewModel: MainViewModel by viewModels()
    private var croppedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageClassifierHelper = ImageClassifierHelper(
            threshold = 0.1f,
            maxResults = 3,
            context = this,
            classifierListener = this
        )

        mainViewModel.imageUri.observe(this, Observer { uri ->
            uri?.let {
                binding.previewImageView.setImageURI(it)
                croppedImageUri = it
            }
        })

        binding.galleryButton.setOnClickListener {
            launcherIntentGallery.launch("image/*")
        }

        binding.analyzeButton.setOnClickListener {
            croppedImageUri?.let {
                analyzeImage(it)
            } ?: Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
        }
    }

    private val launcherIntentGallery =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                startCrop(it)
                mainViewModel.setImageUri(it)
            }
        }

    private fun startCrop(uri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "croppedImage.jpg"))
        val uCrop = UCrop.of(uri, destinationUri)
        uCrop.withAspectRatio(1f, 1f)
        uCrop.withMaxResultSize(224, 224)
        uCrop.start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            croppedImageUri = UCrop.getOutput(data!!)
            croppedImageUri?.let {
                mainViewModel.setImageUri(it)
            }
        }
    }

    private fun analyzeImage(uri: Uri) {
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        imageClassifierHelper.classifyStaticImage(bitmap)
    }

    override fun onResults(results: List<Classifications>?) {
        val topResult = results!![0]
        val resultText = topResult.categories[0].label
        val confidenceScore = topResult.categories[0].score



        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra("IMAGE_URI", croppedImageUri.toString())
            putExtra("RESULT_TEXT", resultText)
            putExtra("CONFIDENCE_SCORE", confidenceScore)
        }
        startActivity(intent)
    }



    override fun onError(error: String) {
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra("ERROR_MESSAGE", error)
        }
        startActivity(intent)
    }
}
