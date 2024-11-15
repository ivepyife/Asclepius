package com.dicoding.Asclepius.view.activity

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.Asclepius.data.local.entity.HistoryEntity
import com.dicoding.Asclepius.helper.ImageClassifierHelper
import com.dicoding.Asclepius.view.viewmodel.MainViewModel
import com.dicoding.Asclepius.view.viewmodel.ViewModelFactory
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityResultBinding
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var imageClassifierHelper: ImageClassifierHelper

    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve the image URI from the intent
        val imageUri = intent.getStringExtra(EXTRA_IMAGE_URI)?.let { Uri.parse(it) }

        imageUri?.also {
            Log.d("Image URI", "showImage: $it")
            binding.resultImage.setImageURI(it)
            analyzeImage(it)
        } ?: showToast("Image URI is null.")
    }

    private fun analyzeImage(uriImage: Uri) {
        // Initialize the image classifier helper
        imageClassifierHelper = ImageClassifierHelper(
            context = this,
            classifierListener = object : ImageClassifierHelper.ClassifierListener {
                override fun onError(error: String) {
                    showToast(error)
                }

                override fun onResults(results: List<Classifications>, inferenceTime: Long) {
                    if (results.isNotEmpty() && results[0].categories.isNotEmpty()) {
                        val sortedCategories = results[0].categories.sortedByDescending { it.score }
                        val predictedLabel = sortedCategories[0].label
                        val confidenceScore = NumberFormat.getPercentInstance()
                            .format(sortedCategories[0].score)
                            .trim()

                        binding.resultText.text = getString(R.string.after_analyze, predictedLabel, confidenceScore)

                        binding.btnSave.setOnClickListener {
                            saveResultToHistory(uriImage, predictedLabel, confidenceScore)
                        }
                    } else {
                        showToast("No results found.")
                    }
                }
            }
        )
        // Start the classification process
        imageClassifierHelper.classifyStaticImage(uriImage)
    }

    private fun saveResultToHistory(uriImage: Uri, predictedLabel: String, confidenceScore: String) {
        val history = HistoryEntity(
            result = getString(R.string.result_history, predictedLabel),
            confidenceScore = getString(R.string.conf_score_history, confidenceScore),
            imagePath = uriToFile(uriImage).toString()
        )
        viewModel.insertHistory(history)
        showToast("Result is saved to History")
        finish()
    }

    private fun uriToFile(imageUri: Uri): String? {
        val fileName = "img_${System.currentTimeMillis()}.jpg"
        val destinationFile = File(this.filesDir, fileName)
        return try {
            this.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (inputStream.read(buffer).also { length = it } != -1) {
                        outputStream.write(buffer, 0, length)
                    }
                    outputStream.flush()
                    destinationFile.absolutePath
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
    }
}
