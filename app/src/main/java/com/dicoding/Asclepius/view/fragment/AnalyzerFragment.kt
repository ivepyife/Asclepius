package com.dicoding.Asclepius.view.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dicoding.Asclepius.view.activity.ResultActivity
import com.dicoding.Asclepius.view.viewmodel.MainViewModel
import com.dicoding.Asclepius.view.viewmodel.ViewModelFactory
import com.dicoding.asclepius.databinding.FragmentAnalyzerBinding
import com.yalantis.ucrop.UCrop
import java.io.File

class AnalyzerFragment : Fragment() {

    private var _binding: FragmentAnalyzerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyzerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe changes to the image URI
        viewModel.currentImgUri.observe(viewLifecycleOwner) { uri ->
            uri?.let {
                showImage(it)  // Show cropped image in preview
            }
        }
        // Set up click listeners
        binding.galleryButton.setOnClickListener {
            startGallery()
        }
        binding.analyzeButton.setOnClickListener {
            viewModel.currentImgUri.value?.let { uri ->
                moveToResult(uri)
            } ?: showToast("Please select an image first.")
        }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            launchUcrop(uri)  // Launch UCrop with the selected image
        } else {
            Log.d("Photo Picker", "No media selected")
            showToast("No image selected.")
        }
    }

    private val launcherUCrop = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let {
                val croppedUri = UCrop.getOutput(it)
                croppedUri?.let { uri ->
                    viewModel.setCurrentImage(uri)
                }
            }
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            UCrop.getError(result.data!!)?.let { error ->
                showToast(error.message.toString())
            }
        }
    }

    private fun launchUcrop(uri: Uri) {
        val destinationUri = Uri.fromFile(File.createTempFile("cropped_", ".jpg", requireActivity().cacheDir))
        val uCropIntent = UCrop.of(uri, destinationUri)
            .getIntent(requireActivity())
        launcherUCrop.launch(uCropIntent)
    }

    private fun showImage(uri: Uri) {
        Log.d("Image URI", "showImage: $uri")
        binding.previewImageView.setImageURI(uri)
    }

    private fun moveToResult(uri: Uri) {
        val intent = Intent(requireActivity(), ResultActivity::class.java).apply {
            putExtra(ResultActivity.EXTRA_IMAGE_URI, uri.toString())
        }
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
