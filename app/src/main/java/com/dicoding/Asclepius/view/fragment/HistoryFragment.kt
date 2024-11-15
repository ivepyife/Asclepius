package com.dicoding.Asclepius.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.Asclepius.data.local.entity.HistoryEntity
import com.dicoding.Asclepius.view.adapter.HistoryAdapter
import com.dicoding.Asclepius.view.viewmodel.MainViewModel
import com.dicoding.Asclepius.view.viewmodel.ViewModelFactory
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.FragmentHistoryBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.io.File

class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    private val historyAdapter: HistoryAdapter by lazy {
        HistoryAdapter().apply {
            onDeleteClick = { history ->
                showDeleteConfirmationDialog(history)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeHistoryData()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
            setHasFixedSize(true)
        }
    }

    private fun observeHistoryData() {
        viewModel.getHistory().observe(viewLifecycleOwner) { historyList ->
            if (historyList.isNotEmpty()) {
                historyAdapter.submitList(historyList)
                showEmptyState(false)
            } else {
                showEmptyState(true)
            }
        }
    }

    private fun showEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.apply {
                recyclerView.visibility = View.GONE
                emptyStateLayout.visibility = View.VISIBLE
            }
        } else {
            binding.apply {
                recyclerView.visibility = View.VISIBLE
                emptyStateLayout.visibility = View.GONE
            }
        }
    }

    private fun showDeleteConfirmationDialog(history: HistoryEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_confirmation_title))
            .setMessage(getString(R.string.delete_confirmation_message))
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                deleteHistory(history)
            }
            .show()
    }

    private fun deleteHistory(history: HistoryEntity) {
        viewModel.deleteHistory(history)
        // Delete the image file
        val imageFile = File(history.imagePath)
        if (imageFile.exists()) {
            imageFile.delete()
        }
        showSnackbar(getString(R.string.history_deleted))
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(
            binding.root,
            message,
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}