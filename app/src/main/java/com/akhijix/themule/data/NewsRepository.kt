package com.akhijix.themule.data

import androidx.room.withTransaction
import coil3.network.HttpException
import com.akhijix.themule.network.NewsApi
import com.akhijix.themule.utils.Resource
import com.akhijix.themule.utils.networkBoundResource
import kotlinx.coroutines.flow.Flow
import okio.IOException
import javax.inject.Inject

class NewsRepository @Inject constructor(
    private val newsApi: NewsApi,
    private val newsArticleDatabase: NewsArticleDatabase
) {
    private val newsArticleDao = newsArticleDatabase.newsArticleDao()

//    suspend fun getBreakingNews(): List<NewsArticle> {
//        val response = newsApi.getBreakingNews()
//        val serverBreakingNewsArticles = response.articles
//        val breakingNewsArticles = serverBreakingNewsArticles.map { serverBreakingNewsArticle ->
//            NewsArticle(
//                title = serverBreakingNewsArticle.title,
//                url = serverBreakingNewsArticle.url,
//                thumbnailUrl = serverBreakingNewsArticle.urlToImage
//            )
//        }
//        return breakingNewsArticles
//    }


    fun getBreakingNews(
        onFetchSuccess: () -> Unit,
        onFetchFailed: (Throwable) -> Unit
    ): Flow<Resource<List<NewsArticle>>> =
        networkBoundResource(
            query = {
                newsArticleDao.getAllBreakingNewsArticles()
            },
            fetch = {
                val response = newsApi.getBreakingNews()
                response.articles
            },
            saveFetchResult = { serverBreakingNewsArticles ->
                val breakingNewsArticles =
                    serverBreakingNewsArticles.map { serverBreakingNewsArticle ->
                        NewsArticle(
                            title = serverBreakingNewsArticle.title,
                            url = serverBreakingNewsArticle.url,
                            thumbnailUrl = serverBreakingNewsArticle.urlToImage,
                            isBookmarked = false
                        )
                    }

                val breakingNews = breakingNewsArticles.map { article ->
                    BreakingNews(article.url)
                }

                newsArticleDatabase.withTransaction {
                    newsArticleDao.deleteAllBreakingNews()
                    newsArticleDao.insertArticles(breakingNewsArticles)
                    newsArticleDao.insertBreakingNews(breakingNews)
                }
            },
            onFetchSuccess = onFetchSuccess,
            onFetchFailed = { t ->
                if( t !is HttpException && t !is IOException){
                    throw t
                }
                onFetchFailed(t)
            }
        )
}