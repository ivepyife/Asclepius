package com.dicoding.Asclepius.di

import android.content.Context
import com.dicoding.Asclepius.data.local.room.HistoryDatabase
import com.dicoding.Asclepius.data.remote.retrofit.ApiConfig
import com.dicoding.Asclepius.data.source.MainRepository

object Injection {
    fun provideRepository(context : Context) : MainRepository {
        val apiService  = ApiConfig.getApiService()
        val db = HistoryDatabase.getInstance(context)
        val dao = db.HistoryDao()
        return MainRepository.getInstance(apiService, dao)
    }
}