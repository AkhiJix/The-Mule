package com.akhijix.themule.ui.search

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.isEmpty
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.paging.LoadType
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhijix.themule.R
import com.akhijix.themule.databinding.FragmentSearchNewsBinding
import com.akhijix.themule.utils.exhaustive
import com.akhijix.themule.utils.onQueryTextSubmit
import com.akhijix.themule.utils.showIfOrInvisible
import com.akhijix.themule.utils.showSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchNewsFragment : Fragment(R.layout.fragment_search_news) {

    private val viewModel: SearchNewsViewModel by viewModels()
    private lateinit var newsArticleAdapter: NewsArticlePagingAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentSearchNewsBinding.bind(view)
        newsArticleAdapter = NewsArticlePagingAdapter(
            onItemClick = { article ->
                val uri = Uri.parse(article.url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                try{ requireActivity().startActivity(intent) }
                catch (exception : ActivityNotFoundException){
                    Toast.makeText(context,"No Browser Installed!", Toast.LENGTH_SHORT).show()
                }
            },
            onBookmarkClick = { article ->
                viewModel.onBookmarkClick(article)
            }
        )

        binding.apply {
            searchRecycler.apply {
                adapter = newsArticleAdapter.withLoadStateFooter(
                    NewsArticleLoadStateAdapter(newsArticleAdapter::retry)
                )
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                itemAnimator?.changeDuration = 0
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.searchResults.collectLatest { data ->
                        newsArticleAdapter.submitData(data)
                    }
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.hasCurrentQuery.collect { hasCurrentQuery ->
                        searchTextInstructions.isVisible = !hasCurrentQuery
                        searchSwipeRefresh.isEnabled = hasCurrentQuery

                        if (!hasCurrentQuery) {
                            searchRecycler.isVisible = false
                        }
                    }
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    newsArticleAdapter.loadStateFlow
                        .distinctUntilChangedBy { it.source.refresh }
                        .filter { it.source.refresh is LoadState.NotLoading }
                        .collect {
                            if (viewModel.pendingScrollToTopAfterNewQuery) {
                                searchRecycler.scrollToPosition(0)
                                viewModel.pendingScrollToTopAfterNewQuery = false
                            }
                            if (viewModel.pendingScrollToTopAfterRefresh && it.mediator?.refresh is LoadState.NotLoading) {
                                searchRecycler.scrollToPosition(0)
                                viewModel.pendingScrollToTopAfterRefresh = false
                            }
                        }
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    newsArticleAdapter.loadStateFlow
                        .collect { loadState ->
                            when (val refresh = loadState.mediator?.refresh) {
                                is LoadState.Loading -> {
                                    searchErrorText.isVisible = false
                                    searchBtnRetry.isVisible = false
                                    searchSwipeRefresh.isRefreshing = true
                                    searchTextNoResults.isVisible = false
                                    searchRecycler.showIfOrInvisible {
                                        !viewModel.newQueryInProgress && newsArticleAdapter.itemCount > 0
                                    }

                                    viewModel.refreshInProgress = true
                                    viewModel.pendingScrollToTopAfterRefresh = true
                                }

                                is LoadState.NotLoading -> {
                                    searchErrorText.isVisible = false
                                    searchBtnRetry.isVisible = false
                                    searchSwipeRefresh.isRefreshing = true
                                    searchRecycler.isVisible = newsArticleAdapter.itemCount > 0

                                    val noResults =
                                        newsArticleAdapter.itemCount < 1 && loadState.append.endOfPaginationReached && loadState.source.append.endOfPaginationReached
                                    searchTextNoResults.isVisible = noResults
                                    viewModel.refreshInProgress = false
                                    viewModel.newQueryInProgress = false
                                }

                                is LoadState.Error -> {
                                    searchSwipeRefresh.isRefreshing = false
                                    searchTextNoResults.isVisible = false
                                    searchRecycler.isVisible = newsArticleAdapter.itemCount > 0

                                    val noCachedResults =
                                        newsArticleAdapter.itemCount < 1 && loadState.source.append.endOfPaginationReached

                                    searchErrorText.isVisible = noCachedResults
                                    searchBtnRetry.isVisible = noCachedResults

                                    val errorMessage = getString(
                                        R.string.could_not_load_search_results,
                                        refresh.error.localizedMessage
                                            ?: getString(R.string.unknown_error_occurred)
                                    )
                                    searchErrorText.text = errorMessage
                                    if (viewModel.refreshInProgress) {
                                        showSnackbar(errorMessage)
                                    }
                                    viewModel.refreshInProgress = false
                                    viewModel.newQueryInProgress = false
                                    viewModel.pendingScrollToTopAfterRefresh = false
                                }

                                else -> return@collect //?
                            }
                        }
                }
            }
            searchSwipeRefresh.setOnRefreshListener {
                newsArticleAdapter.refresh()
            }

            searchSwipeRefresh.setOnClickListener {
                newsArticleAdapter.retry()
            }

        }
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search_news, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView

        searchView.onQueryTextSubmit { query ->
            viewModel.onSearchQuerySubmit(query)
            searchView.clearFocus()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.action_refresh -> {
                newsArticleAdapter.refresh()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
}