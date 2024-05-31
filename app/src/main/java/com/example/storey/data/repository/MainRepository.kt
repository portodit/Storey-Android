package com.example.storey.data.repository

import androidx.lifecycle.liveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.liveData
import com.example.storey.data.local.StoryDatabase
import com.example.storey.data.remote.ApiService
import com.example.storey.data.remote.StoryRemoteMediator
import com.example.storey.utils.Result
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class MainRepository(
    private val apiService: ApiService,
    private val database: StoryDatabase,
) {
    @OptIn(ExperimentalPagingApi::class)
    fun getStories() = Pager(
        config = PagingConfig(
            5
        ),
        remoteMediator = StoryRemoteMediator(database, apiService),
        pagingSourceFactory = { database.storyDao().getStories() }
    ).liveData

    fun getStories(size: Int) = liveData {
        emit(Result.Loading)
        try {
            val storyResponse = apiService.getStories(size = size)
            emit(Result.Success(storyResponse))
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }

    fun getLocationStories() = liveData {
        emit(Result.Loading)
        try {
            val storyResponse = apiService.getLocationStories()
            emit(Result.Success(storyResponse))
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }

    fun uploadStory(
        image: File,
        desc: String,
        latitude: String? = null,
        longitude: String? = null
    ) = liveData {
        emit(Result.Loading)
        try {
            val imageMultipart = MultipartBody.Part.createFormData(
                "photo",
                image.name,
                image.asRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            val descMultipart = desc.toRequestBody("text/plain".toMediaType())

            val uploadResponse = apiService.uploadStory(
                imageMultipart,
                descMultipart,
                latitude?.toRequestBody("text/plain".toMediaType()),
                longitude?.toRequestBody("text/plain".toMediaType())
            )
            emit(Result.Success(uploadResponse))
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }
}