package com.akhijix.themule.ui.breaking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhijix.themule.data.NewsArticle
import com.akhijix.themule.data.NewsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

class BreakingNewsViewModel @Inject constructor(
    private val repository: NewsRepository
) : ViewModel() {

    val breakingNews = repository.getBreakingNews()
        .stateIn(viewModelScope, SharingStarted.Lazily,null)

}