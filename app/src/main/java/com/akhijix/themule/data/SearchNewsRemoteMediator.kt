package com.akhijix.themule.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.akhijix.themule.network.NewsApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.io.IOException

private const val NEWS_STARTING_PAGE_INDEX = 1

@OptIn(ExperimentalPagingApi::class)
class SearchNewsRemoteMediator(
    private val searchQuery: String,
    private val newsApi: NewsApi,
    private val newsArticleDatabase: NewsArticleDatabase,
    private val refreshOnInit : Boolean
) : RemoteMediator<Int, NewsArticle>() {

    private val newsArticleDao = newsArticleDatabase.newsArticleDao()
    private val searchQueryRemoteKeyDao = newsArticleDatabase.searchQueryRemoteKeyDao()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, NewsArticle>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> NEWS_STARTING_PAGE_INDEX
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> searchQueryRemoteKeyDao.getRemoteKey(searchQuery).nextPageKey
        }

        try {
            val response = newsApi.searchNews(searchQuery, page, state.config.pageSize)
            val serverSearchResults = response.articles
            val bookmarkedArticles = newsArticleDao.getAllBookmarkedArticles().first()
            val searchResultArticles = serverSearchResults.map { serverSearchResultArticle ->
                val isBookmarked = bookmarkedArticles.any { bookmarkedArticle ->
                    bookmarkedArticle.url == serverSearchResultArticle.url
                }
                NewsArticle(
                    title = serverSearchResultArticle.title,
                    url = serverSearchResultArticle.url,
                    thumbnailUrl = serverSearchResultArticle.urlToImage,
                    isBookmarked = isBookmarked
                )
            }

            newsArticleDatabase.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    newsArticleDao.deleteSearchResultsForQuery(searchQuery)
                }

                val lastQueryPosition = newsArticleDao.getLastQueryPosition(searchQuery) ?: 0
                var queryPosition = lastQueryPosition + 1

                val searchResults = searchResultArticles.map { article ->
                    SearchResult(searchQuery,article.url,queryPosition++)
                }

                val nextPageKey = page + 1

                newsArticleDao.insertArticles(searchResultArticles)
                newsArticleDao.insertSearchResults(searchResults)
                searchQueryRemoteKeyDao.insertRemoteKey(
                    SearchQueryRemoteKey(searchQuery,nextPageKey)
                )
            }
            return MediatorResult.Success(endOfPaginationReached = serverSearchResults.isEmpty())
        } catch (exception : IOException) {
            return MediatorResult.Error(exception)
        } catch (exception : HttpException) {
            return MediatorResult.Error(exception)
        }
    }

    override suspend fun initialize(): InitializeAction {
        return if(refreshOnInit) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
        else{
            InitializeAction.SKIP_INITIAL_REFRESH
        }
    }

}