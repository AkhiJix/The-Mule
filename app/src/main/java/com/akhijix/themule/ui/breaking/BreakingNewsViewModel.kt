package com.akhijix.themule.ui.breaking

import androidx.lifecycle.ViewModel
import com.akhijix.themule.data.NewsRepository
import javax.inject.Inject

class BreakingNewsViewModel @Inject constructor(
    private val repository: NewsRepository
) : ViewModel() {

}