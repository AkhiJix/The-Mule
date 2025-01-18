package com.akhijix.themule.shared

import androidx.recyclerview.widget.RecyclerView
import com.akhijix.themule.R
import com.akhijix.themule.data.NewsArticle
import com.akhijix.themule.databinding.ItemNewsArticleBinding
import com.bumptech.glide.Glide

class NewsArticleViewHolder(
    private val binding : ItemNewsArticleBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(article: NewsArticle){
        binding.apply {
            Glide.with(itemView)
                .load(article.thumbnailUrl)
                .error(R.drawable.img_placeholder)
                .into(imageView)

            newsArticleTitle.text = article.title ?: ""

            imageBookmark.setImageResource(
                when{
                    article.isBookmarked -> R.drawable.ic_bookmark_selected
                    else -> R.drawable.ic_bookmark_unselected
                }
            )
        }
    }

}