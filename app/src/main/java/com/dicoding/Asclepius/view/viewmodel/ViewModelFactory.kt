package com.dicoding.Asclepius.view.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.Asclepius.data.source.MainRepository
import com.dicoding.Asclepius.di.Injection

class ViewModelFactory(private val mainRepository: MainRepository): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(mainRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        @Volatile
        private var instance: ViewModelFactory? = null

        fun getInstance(context: Context): ViewModelFactory =
            instance?: synchronized(this) {
                instance?: ViewModelFactory(Injection.provideRepository(context))
            }.also { instance = it }
    }
}
