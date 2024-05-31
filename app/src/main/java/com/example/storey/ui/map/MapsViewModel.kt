package com.example.storey.ui.map

import androidx.lifecycle.ViewModel
import com.example.storey.data.repository.MainRepository

class MapsViewModel(
    private val repository: MainRepository
) : ViewModel() {
    fun getLocationStories() = repository.getLocationStories()
}