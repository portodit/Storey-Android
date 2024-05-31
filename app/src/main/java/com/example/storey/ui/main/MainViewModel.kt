package com.example.storey.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.storey.data.model.ListStoryItem
import com.example.storey.data.repository.MainRepository
import com.example.storey.utils.SettingsPreferences
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: MainRepository,
    private val settingsPreferences: SettingsPreferences
) : ViewModel() {
    val storyList: LiveData<PagingData<ListStoryItem>> by lazy {
        repository.getStories().cachedIn(viewModelScope)
    }

    fun clearPreferences() {
        viewModelScope.launch {
            settingsPreferences.clearPreferences()
        }
    }
}