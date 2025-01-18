package com.akhijix.themule.ui.breaking

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhijix.themule.R
import com.akhijix.themule.data.NewsArticle
import com.akhijix.themule.databinding.FragmentBreakingNewsBinding
import com.akhijix.themule.shared.NewsArticleListAdapter
import com.akhijix.themule.utils.Resource
import com.akhijix.themule.utils.Resource.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BreakingNewsFragment : Fragment(R.layout.fragment_breaking_news) {

    private val viewModel: BreakingNewsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentBreakingNewsBinding.bind(view)
        val newsArticleAdapter = NewsArticleListAdapter()

        binding.apply {
            breakingRecycler.apply {
                adapter = newsArticleAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.breakingNews.collect {
                        val result = it ?: return@collect

                        breakingSwipeRefresh.isRefreshing = result is Resource.Loading
                        breakingRecycler.isVisible = !result.data.isNullOrEmpty()
                        breakingErrorText.isVisible =
                            result.error != null && result.data.isNullOrEmpty()
                        breakingBtnRetry.isVisible =
                            result.error != null && result.data.isNullOrEmpty()
                        breakingErrorText.text = getString(
                            R.string.could_not_refresh,
                            result.error?.localizedMessage
                                ?: getString(R.string.unknown_error_occurred)
                        )
                        newsArticleAdapter.submitList(result.data)

                    }
                }
            }
        }
    }
}