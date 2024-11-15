package com.dicoding.Asclepius.view.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.Asclepius.data.local.entity.HistoryEntity
import com.dicoding.Asclepius.data.remote.response.ArticlesItem
import com.dicoding.Asclepius.data.source.MainRepository
import com.dicoding.Asclepius.data.source.Result
import kotlinx.coroutines.launch

class MainViewModel(private val mainRepository: MainRepository): ViewModel() {
    private val _currentImgUri = MutableLiveData<Uri?>()
    val currentImgUri: LiveData<Uri?> get() = _currentImgUri

    val newsResult: LiveData<Result<List<ArticlesItem>>> = mainRepository.getNews()

    fun setCurrentImage(uri: Uri?) {
        _currentImgUri.value = uri
    }

    fun getNews() = mainRepository.getNews()

    fun getHistory() = mainRepository.getHistory()

    fun deleteHistory(historyEntity: HistoryEntity) = viewModelScope.launch {
        mainRepository.deleteHistory(historyEntity)
    }

    fun insertHistory(historyEntity: HistoryEntity) = viewModelScope.launch {
        mainRepository.insertHistory(historyEntity)
    }
}