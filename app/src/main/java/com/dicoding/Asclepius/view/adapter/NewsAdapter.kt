package com.dicoding.Asclepius.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.Asclepius.data.remote.response.ArticlesItem
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.NewsItemBinding
import com.squareup.picasso.Picasso

class NewsAdapter : ListAdapter<ArticlesItem, NewsAdapter.ViewHolder>(DIFF_CALLBACK) {

    var listener: OnNewsClickListener? = null

    inner class ViewHolder(private val binding: NewsItemBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(news: ArticlesItem) {
            // Load image with Picasso and use placeholders
            Picasso.get()
                .load(news.urlToImage)
                .placeholder(R.drawable.ic_place_holder) // Placeholder image
                .error(R.drawable.ic_broken_image) // Error image if loading fails
                .into(binding.imgNews)

            // Set the text and accessibility descriptions
            binding.tvTitleNews.text = news.title
            binding.tvNewsDescription.text = news.description
            binding.imgNews.contentDescription = news.title

            // Handle item click using listener for better flexibility
            binding.root.setOnClickListener {
                listener?.onNewsClick(news)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = NewsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val news = getItem(position)
        holder.bind(news)
    }

    // Define a callback interface to handle click events
    interface OnNewsClickListener {
        fun onNewsClick(news: ArticlesItem)
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ArticlesItem>() {
            override fun areItemsTheSame(oldItem: ArticlesItem, newItem: ArticlesItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ArticlesItem, newItem: ArticlesItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}
