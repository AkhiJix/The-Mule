package com.akhijix.themule.ui.breaking

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.akhijix.themule.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BreakingNewsFragment : Fragment(R.layout.fragment_breaking_news){

    private val viewModel : BreakingNewsViewModel by viewModels()
}