package com.example.storey.ui.addstory

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.storey.data.repository.MainRepository
import java.io.File

class AddStoryViewModel(private val repository: MainRepository) : ViewModel() {
    val imageFile = MutableLiveData<File>()
    val isLoading = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val latitude = MutableLiveData<Double?>(null)
    val longitude = MutableLiveData<Double?>(null)

    val areBothCoordinatesFilled = MediatorLiveData<Boolean>().apply {
        fun update() {
            val lat = latitude.value
            val lon = longitude.value
            value = lat != null && lon != null
        }

        addSource(latitude) {
            update()
        }

        addSource(longitude) {
            update()
        }
    }


    fun uploadStory(
        description: String,
        file: File,
    ) = repository.uploadStory(
        file,
        description,
        if (latitude.value == null) null else latitude.value.toString(),
        if (longitude.value == null) null else longitude.value.toString(),
    )
}