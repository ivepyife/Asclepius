package com.dicoding.Asclepius.data.source

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.dicoding.Asclepius.data.local.entity.HistoryEntity
import com.dicoding.Asclepius.data.local.room.HistoryDao
import com.dicoding.Asclepius.data.remote.response.ArticlesItem
import com.dicoding.Asclepius.data.remote.retrofit.ApiService
import retrofit2.HttpException
import java.io.File

class MainRepository(
    private val apiService: ApiService,
    private val historyDao: HistoryDao
) {
    fun getNews(): LiveData<Result<List<ArticlesItem>>> = liveData {
        emit(Result.Loading) // Emit loading state
        try {
            val response = apiService.getNews() // Call API to get news
            val articles = response.articles?.filterNotNull()?: emptyList() // Filter out null articles
            emit(Result.Success(articles)) // Emit success with the list of articles
        } catch (e: HttpException) {
            emit(Result.Error(e.message.toString())) // Emit error state
        } catch (e: Exception) {
            emit(Result.Error(e.localizedMessage?: "An unknown error occurred")) // Handle other exceptions
        }
    }

    fun getHistory() : LiveData<List<HistoryEntity>> {
        return historyDao.getHistory()
    }

    suspend fun insertHistory(historyEntity: HistoryEntity) {
        historyDao.insertHistory(historyEntity)
    }

    suspend fun deleteHistory(historyEntity: HistoryEntity) {
        historyDao.deleteHistory(historyEntity)

        val fileData = File(historyEntity.imagePath)
        if (fileData.exists()) fileData.delete()
    }

    companion object {
        @Volatile
        private var instance: MainRepository? = null
        fun getInstance(
            apiService: ApiService,
            historyDao: HistoryDao
        ): MainRepository =
            instance?: synchronized(this) {
                instance?: MainRepository(
                    apiService,
                    historyDao
                )
            }.also { instance = it }
    }
}