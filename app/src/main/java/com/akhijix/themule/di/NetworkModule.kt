package com.akhijix.themule.di

import com.akhijix.themule.network.NewsApi
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Singleton
@Provides
fun provideNewsApi(): Retrofit {
    return Retrofit.Builder()
        .baseUrl(NewsApi.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

@Provides
@Singleton
fun provideNewsApi(retrofit: Retrofit): NewsApi =
    retrofit.create(NewsApi::class.java)

//@Singleton
//@Provides
//fun provideNewsRepository(newsApi: NewsApi): NewsRepository = NewsRepositoryImpl(newsApi)