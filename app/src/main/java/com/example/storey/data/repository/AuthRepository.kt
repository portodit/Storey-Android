package com.example.storey.data.repository

import androidx.lifecycle.liveData
import com.example.storey.utils.Result
import com.example.storey.data.remote.ApiService

class AuthRepository(
    private val apiService: ApiService
) {
    fun login(email: String, password: String) = liveData {
        emit(Result.Loading)
        try {
            val loginResponse = apiService.login(email, password)
            emit(Result.Success(loginResponse))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Result.Error(e.message.toString()))
        }
    }

    fun register(
        name: String,
        email: String,
        password: String,
    ) = liveData {
        emit(Result.Loading)
        try {
            val registerResponse = apiService.register(name, email, password)
            emit(Result.Success(registerResponse))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Result.Error(e.message.toString()))
        }
    }
}