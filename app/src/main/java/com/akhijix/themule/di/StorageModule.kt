package com.akhijix.themule.di

import android.app.Application
import androidx.room.Room
import com.akhijix.themule.data.NewsArticleDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

@Module
@InstallIn(SingletonComponent::class)
object StorageModule{

    @Provides
    @Singleton
    fun provideDatabase(app: Application) =
        Room.databaseBuilder(app, NewsArticleDatabase::class.java, "newsArticle_database")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideDao(db : NewsArticleDatabase) = db.newsArticleDao()

    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())
}