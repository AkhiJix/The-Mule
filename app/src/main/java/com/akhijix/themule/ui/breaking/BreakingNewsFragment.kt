package com.akhijix.themule.ui.breaking

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
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
import com.akhijix.themule.utils.exhaustive
import com.akhijix.themule.utils.showSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BreakingNewsFragment : Fragment(R.layout.fragment_breaking_news) {

    private val viewModel: BreakingNewsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentBreakingNewsBinding.bind(view)
        val newsArticleAdapter = NewsArticleListAdapter(
            onItemClick = { article ->
                val uri = Uri.parse(article.url)
                val intent = Intent(Intent.ACTION_VIEW,uri)
                requireActivity().startActivity(intent)
            },
            onBookmarkClick = {article ->
                viewModel.onBookmarkClick(article)
            }
        )

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
                        newsArticleAdapter.submitList(result.data) {
                            if (viewModel.pendingScrollToTopAfterRefresh) {
                                breakingRecycler.scrollToPosition(0)
                                viewModel.pendingScrollToTopAfterRefresh = false
                            }
                        }
                    }
                }
            }

            breakingSwipeRefresh.setOnRefreshListener {
                viewModel.onManualRefresh()
            }

            breakingBtnRetry.setOnClickListener {
                viewModel.onManualRefresh()
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.events.collect { event ->
                        when (event) {
                            is BreakingNewsViewModel.Event.ShowErrorMessage ->
                                showSnackbar(
                                    getString(
                                        R.string.could_not_refresh,
                                        event.error.localizedMessage
                                            ?: getString(R.string.unknown_error_occurred)
                                    )
                                )
                        }.exhaustive
                    }
                }
            }
        }
        setHasOptionsMenu(true)
    }

    override fun onStart() {
        super.onStart()
        viewModel.onStart()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_breaking_news, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.breaking_news_refresh -> {
                viewModel.onManualRefresh()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
}