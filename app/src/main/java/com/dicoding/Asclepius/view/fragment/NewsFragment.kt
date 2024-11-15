package com.dicoding.Asclepius.view.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.Asclepius.data.remote.response.ArticlesItem
import com.dicoding.Asclepius.data.source.Result
import com.dicoding.Asclepius.view.adapter.NewsAdapter
import com.dicoding.Asclepius.view.viewmodel.MainViewModel
import com.dicoding.Asclepius.view.viewmodel.ViewModelFactory
import com.dicoding.asclepius.databinding.FragmentNewsBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "NewsFragment"

class NewsFragment : Fragment() {
    private var _binding: FragmentNewsBinding? = null
    private val binding get() = _binding!!

    private val newsAdapter: NewsAdapter by lazy {
        NewsAdapter().apply {
            listener = object : NewsAdapter.OnNewsClickListener {
                override fun onNewsClick(news: ArticlesItem) {
                    news.url?.let { url ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: Binding initialized")
        setupRecyclerView()
        observeNews()

        binding.swipeRefreshLayout.setOnRefreshListener {
            lifecycleScope.launch {
                observeNews()
                delay(1000)
                _binding?.swipeRefreshLayout?.isRefreshing = false
            }
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = newsAdapter
            setHasFixedSize(true)
        }
    }

    private fun observeNews() {
        viewModel.newsResult.observe(viewLifecycleOwner) { result ->
            if (_binding == null) {
                Log.d(TAG, "observeNews: Binding is null, skipping update")
                return@observe
            }

            when (result) {
                is Result.Loading -> {
                    showLoading(true)
                    hideError()
                    _binding?.swipeRefreshLayout?.isRefreshing = true
                }
                is Result.Success -> {
                    showLoading(false)
                    hideError()

                    val filteredNews = result.data.filter { article ->
                        !article.title.isNullOrEmpty() &&
                                !article.description.isNullOrEmpty() &&
                                !article.urlToImage.isNullOrEmpty()
                    }
                    newsAdapter.submitList(filteredNews)
                    _binding?.swipeRefreshLayout?.postDelayed({
                        _binding?.swipeRefreshLayout?.isRefreshing = false
                    }, 1000)
                }
                is Result.Error -> {
                    showLoading(false)
                    showError(result.error)
                    showSnackbar(result.error)
                    _binding?.swipeRefreshLayout?.postDelayed({
                        _binding?.swipeRefreshLayout?.isRefreshing = false
                    }, 1000)
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        _binding?.progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        _binding?.apply {
            errorLayout.visibility = View.VISIBLE
            tvError.text = message
            btnRetry.setOnClickListener {
                viewModel.getNews()
            }
        }
    }

    private fun hideError() {
        _binding?.errorLayout?.visibility = View.GONE
    }

    private fun showSnackbar(message: String) {
        _binding?.let {
            Snackbar.make(
                it.root,
                message,
                Snackbar.LENGTH_LONG
            ).setAction("Retry") {
                viewModel.getNews()
            }.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: Binding set to null")
        _binding = null
    }
}
