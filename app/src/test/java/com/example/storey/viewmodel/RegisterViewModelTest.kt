package com.example.storey.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.example.storey.data.model.RegisterResponse
import com.example.storey.data.repository.AuthRepository
import com.example.storey.ui.auth.loginregister.viewmodels.RegisterViewModel
import com.example.storey.utils.CoroutineTestRule
import com.example.storey.utils.LiveDataTestUtils.getOrAwaitValue
import com.example.storey.utils.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class RegisterViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutineTestRule = CoroutineTestRule()

    @Mock
    private lateinit var repository: AuthRepository

    private lateinit var registerViewModel: RegisterViewModel

    @Before
    fun setUp() {
        registerViewModel = RegisterViewModel(repository)
    }

    @Test
    fun `successfully Registered User`() = runTest {
        val name = "name"
        val email = "email@email.com"
        val password = "password"
        val registerResponse = RegisterResponse(false, "success")

        val expectedResponse = MutableLiveData<Result<RegisterResponse>>().apply {
            value = Result.Success(registerResponse)
        }

        Mockito.`when`(repository.register(name, email, password))
            .thenReturn(expectedResponse)

        registerViewModel.register(name, email, password).getOrAwaitValue().let { result ->
            Assert.assertTrue(result is Result.Success)
            Assert.assertFalse(result is Result.Error)

            if (result is Result.Success) {
                Assert.assertNotNull(result.data)
                assertEquals(registerResponse, result.data)
            }
        }

        Mockito.verify(repository).register(name, email, password)
    }

    @Test
    fun `failed To Register User`() = runTest {
        val name = "name"
        val email = "email@email.com"
        val password = "password"
        val errorResponse = "error"

        val expectedResponse = MutableLiveData<Result<RegisterResponse>>().apply {
            value = Result.Error(errorResponse)
        }

        Mockito.`when`(repository.register(name, email, password))
            .thenReturn(expectedResponse)

        registerViewModel.register(name, email, password).getOrAwaitValue().let { result ->
            Assert.assertTrue(result is Result.Error)
            Assert.assertFalse(result is Result.Success)

            if (result is Result.Error) {
                Assert.assertNotNull(result.error)
            }
        }

        Mockito.verify(repository).register(name, email, password)
    }
}