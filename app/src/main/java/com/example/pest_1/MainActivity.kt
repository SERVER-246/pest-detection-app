package com.example.pest_1

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.pest_1.data.model.ModelCatalog
import com.example.pest_1.data.model.ModelRepository
import com.example.pest_1.databinding.ActivityMainBinding
import com.example.pest_1.domain.model.PredictionResult
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var modelManager: OnnxModelManager
    private lateinit var modelRepository: ModelRepository
    private var currentBitmap: Bitmap? = null

    // Use ModelCatalog instead of hardcoded lists
    private val models = ModelCatalog.models
    private val modelLabels = models.map { it.displayName }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { loadImageFromUri(it) }
        }
    }

    private val takePhotoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as? Bitmap
            bitmap?.let {
                currentBitmap = it
                binding.imageView.setImageBitmap(it)
                binding.imagePlaceholder.visibility = View.GONE
                enableClassifyButton()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        modelManager = OnnxModelManager(this)
        modelRepository = ModelRepository(this)
        setupUI()
        checkPermissions()
    }

    private fun setupUI() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, modelLabels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.modelSpinner.adapter = adapter

        // Default to the first bundled model (MobileNet V2)
        val defaultIndex = models.indexOfFirst { it.isDefault }.takeIf { it >= 0 } ?: 0
        binding.modelSpinner.setSelection(defaultIndex)

        binding.uploadButton.setOnClickListener { pickImage() }
        binding.cameraButton.setOnClickListener { takePhoto() }
        binding.classifyButton.setOnClickListener { classifyImage() }
    }

    private fun checkPermissions() {
        val permissions = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun takePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePhotoLauncher.launch(intent)
        } else {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
            checkPermissions()
        }
    }

    private fun loadImageFromUri(uri: Uri) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            currentBitmap = bitmap
            binding.imageView.setImageBitmap(bitmap)
            binding.imagePlaceholder.visibility = View.GONE
            enableClassifyButton()
            Toast.makeText(this, "Image loaded successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to load image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun enableClassifyButton() {
        binding.classifyButton.isEnabled = true
        binding.classifyButton.alpha = 1f
    }

    private fun classifyImage() {
        val bitmap = currentBitmap
        if (bitmap == null) {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedModelIndex = binding.modelSpinner.selectedItemPosition
        if (selectedModelIndex < 0 || selectedModelIndex >= models.size) {
            Toast.makeText(this, "Please select a valid model", Toast.LENGTH_SHORT).show()
            return
        }

        val modelInfo = models[selectedModelIndex]

        // Show loading state
        binding.loadingProgressBar.visibility = View.VISIBLE
        binding.classifyButton.isEnabled = false
        binding.resultText.text = "Loading model: ${modelInfo.displayName}\nPlease wait..."

        lifecycleScope.launch {
            try {
                // Ensure model is available (download if needed)
                val modelPathResult = modelRepository.ensureModelAvailable(modelInfo)

                if (modelPathResult.isFailure) {
                    val error = modelPathResult.exceptionOrNull()?.message ?: "Unknown error"
                    binding.resultText.text = "❌ MODEL ERROR\n$error"
                    binding.loadingProgressBar.visibility = View.GONE
                    binding.classifyButton.isEnabled = true

                    // Offer to download if not available
                    if (error.contains("not downloaded")) {
                        showDownloadDialog(modelInfo)
                    }
                    return@launch
                }

                val modelPath = modelPathResult.getOrNull()!!

                // Load the model
                binding.resultText.text = "Loading model...\n${modelInfo.displayName}"
                val modelLoaded = modelManager.loadModel(modelPath)

                if (!modelLoaded) {
                    binding.resultText.text =
                        "❌ FAILED TO LOAD MODEL\nModel: ${modelInfo.id}\nPlease try again or select another model."
                    binding.loadingProgressBar.visibility = View.GONE
                    binding.classifyButton.isEnabled = true
                    return@launch
                }

                // Classify image
                binding.resultText.text = "Analyzing image..."
                val result = modelManager.classifyImage(bitmap)
                binding.loadingProgressBar.visibility = View.GONE
                binding.classifyButton.isEnabled = true

                if (result != null) {
                    displayResults(result, modelInfo)
                } else {
                    binding.resultText.text = "❌ CLASSIFICATION FAILED\nUnable to process image.\nPlease try again."
                }
            } catch (e: Exception) {
                e.printStackTrace()
                binding.loadingProgressBar.visibility = View.GONE
                binding.classifyButton.isEnabled = true
                binding.resultText.text = "❌ ERROR: ${e.message}"
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showDownloadDialog(modelInfo: com.example.pest_1.data.model.ModelInfo) {
        AlertDialog.Builder(this)
            .setTitle("Download Model")
            .setMessage("Model '${modelInfo.displayName}' is not available.\n\n" +
                    "Size: ${modelInfo.sizeInMB} MB\n" +
                    "Would you like to download it?")
            .setPositiveButton("Download") { _, _ ->
                downloadModel(modelInfo)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun downloadModel(modelInfo: com.example.pest_1.data.model.ModelInfo) {
        // Show download progress
        binding.loadingProgressBar.visibility = View.VISIBLE
        binding.resultText.text = "Downloading ${modelInfo.displayName}...\nSize: ${modelInfo.sizeInMB} MB"

        lifecycleScope.launch {
            val result = modelRepository.ensureModelAvailable(modelInfo)
            binding.loadingProgressBar.visibility = View.GONE

            if (result.isSuccess) {
                Toast.makeText(this@MainActivity, "Download complete!", Toast.LENGTH_SHORT).show()
                binding.resultText.text = "✓ Model downloaded successfully!\nYou can now use it for classification."
            } else {
                val error = result.exceptionOrNull()?.message ?: "Unknown error"
                Toast.makeText(this@MainActivity, "Download failed: $error", Toast.LENGTH_LONG).show()
                binding.resultText.text = "❌ Download failed\n$error"
            }
        }
    }

    private fun displayResults(result: PredictionResult, modelInfo: com.example.pest_1.data.model.ModelInfo) {
        val sb = StringBuilder()
        val header = if (modelInfo.isEnsemble) "ENSEMBLE RESULT" else "RESULT"

        sb.appendLine("=== $header ===")
        sb.appendLine("Model: ${modelInfo.displayName}")
        sb.appendLine("Inference: ${result.inferenceTimeMs}ms")
        sb.appendLine()
        sb.appendLine("Top: ${result.className}")
        sb.appendLine("Confidence: ${String.format("%.2f", result.confidence)}%")
        sb.appendLine()
        sb.appendLine("All predictions:")
        result.allPredictions.forEachIndexed { idx, p ->
            sb.appendLine("${idx + 1}. ${p.className} - ${String.format("%.2f", p.confidence)}%")
        }

        if (!result.meetsThreshold) {
            sb.appendLine()
            sb.appendLine("⚠️ Low confidence (< 80%)")
            sb.appendLine("Consider using a better image")
        }

        binding.resultText.text = sb.toString()
    }

    private fun showLowConfidenceWarning(result: PredictionResult, modelName: String) {
        val message = "Low confidence: ${String.format("%.2f", result.confidence)}% (required 80%)"
        binding.resultText.text = message

        AlertDialog.Builder(this)
            .setTitle("Low Confidence")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setNegativeButton("Choose Image") { _, _ -> pickImage() }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        modelManager.release()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissions required for full functionality", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }
}
