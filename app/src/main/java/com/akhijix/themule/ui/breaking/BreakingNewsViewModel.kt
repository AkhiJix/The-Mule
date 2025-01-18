package com.akhijix.themule.ui.breaking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhijix.themule.data.NewsArticle
import com.akhijix.themule.data.NewsRepository
import com.akhijix.themule.utils.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class BreakingNewsViewModel @Inject constructor(
    private val repository: NewsRepository
) : ViewModel() {

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    private val refreshTriggerChannel = Channel<Unit>()
    private val refreshTrigger = refreshTriggerChannel.receiveAsFlow()

    var pendingScrollToTopAfterRefresh = false

    val breakingNews = refreshTrigger.flatMapLatest {
        repository.getBreakingNews(
            onFetchSuccess = {
                pendingScrollToTopAfterRefresh = true
            },
            onFetchFailed = { t ->
                viewModelScope.launch {
                    eventChannel.send(
                        Event.ShowErrorMessage(
                            t
                        )
                    )
                }
            }
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun onStart() {
        if (breakingNews.value !is Resource.Loading) {
            viewModelScope.launch {
                refreshTriggerChannel.send(Unit)
            }
        }
    }

    fun onManualRefresh() {
        if (breakingNews.value !is Resource.Loading) {
            viewModelScope.launch {
                refreshTriggerChannel.send(Unit)
            }
        }
    }

    sealed class Event {
        data class ShowErrorMessage(val error: Throwable) : Event()
    }
}