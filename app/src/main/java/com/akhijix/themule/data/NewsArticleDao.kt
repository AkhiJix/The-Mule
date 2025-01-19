package com.akhijix.themule.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsArticleDao {

    @Query("SELECT * FROM breaking_news_table INNER JOIN news_article_table ON articleUrl = url ")
    fun getAllBreakingNewsArticles(): Flow<List<NewsArticle>>

    @Query("SELECT * FROM search_results_table INNER JOIN news_article_table ON articleUrl = url WHERE searchQuery = :query ORDER BY queryPosition ")
    fun getSearchResultArticlesPaged(query: String) : PagingSource<Int, NewsArticle>

    @Query("SELECT * FROM news_article_table WHERE isBookmarked = 1")
    fun getAllBookmarkedArticles() : Flow<List<NewsArticle>>

    @Query("SELECT MAX(queryPosition) FROM search_results_table WHERE searchQuery = :searchQuery")
    suspend fun getLastQueryPosition(searchQuery: String) : Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<NewsArticle>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBreakingNews(breakingNews: List<BreakingNews>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchResults(searchResults: List<SearchResult>)

    @Update
    suspend fun updateArticle(article: NewsArticle)

    @Query("UPDATE news_article_table SET isBookmarked = 0")
    suspend fun resetAllBookmarks()

    @Query("DELETE FROM search_results_table WHERE searchQuery = :query")
    suspend fun deleteSearchResultsForQuery(query: String)

    @Query("DELETE FROM breaking_news_table")
    suspend fun deleteAllBreakingNews()

    @Query("DELETE FROM news_article_table WHERE updatedAt < :timestampInMillis AND isBookmarked = 0")
    suspend fun deleteNonBookmarkedArticlesOlderThan(timestampInMillis: Long)
}