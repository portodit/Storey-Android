package com.example.storey.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.example.storey.data.model.LoginResponse
import com.example.storey.data.model.LoginResult
import com.example.storey.data.repository.AuthRepository
import com.example.storey.ui.auth.loginregister.viewmodels.LoginViewModel
import com.example.storey.utils.CoroutineTestRule
import com.example.storey.utils.LiveDataTestUtils.getOrAwaitValue
import com.example.storey.utils.Result
import com.example.storey.utils.SettingsPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner


@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class LoginViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutineTestRule = CoroutineTestRule()

    @Mock
    private lateinit var repository: AuthRepository

    @Mock
    private lateinit var preferences: SettingsPreferences

    private lateinit var loginViewModel: LoginViewModel

    @Before
    fun setUp() {
        loginViewModel = LoginViewModel(repository, preferences)
    }

    @Test
    fun `successfully Logged In User`() = runTest {
        val email = "email@email.com"
        val password = "password"
        val loginResponse = LoginResponse(
            loginResult = LoginResult(
                name = "name",
                userId = "id",
                token = "token"
            ),
            error = false,
            message = "success"
        )

        val expectedResponse = MutableLiveData<Result<LoginResponse>>().apply {
            value = Result.Success(loginResponse)
        }

        Mockito.`when`(repository.login(email, password))
            .thenReturn(expectedResponse)

        val result = loginViewModel.loginUser(email, password).getOrAwaitValue()

        assertTrue(result is Result.Success)
        assertFalse(result is Result.Error)

        if (result is Result.Success) {
            assertNotNull(result.data)
            assertEquals(loginResponse, result.data)
        }

        Mockito.verify(repository).login(email, password)
    }

    @Test
    fun `failed To Log In User`() = runTest {
        val email = "email@email.com"
        val password = "password"

        val expectedResponse = MutableLiveData<Result<LoginResponse>>().apply {
            value = Result.Error("error")
        }

        Mockito.`when`(repository.login(email, password))
            .thenReturn(expectedResponse)

        val result = loginViewModel.loginUser(email, password).getOrAwaitValue()

        assertTrue(result is Result.Error)
        assertFalse(result is Result.Success)

        if (result is Result.Error) {
            assertNotNull(result.error)
        }

        Mockito.verify(repository).login(email, password)
    }

    @Test
    fun `successfully Save User Token`() = runTest {
        val token = "token"
        loginViewModel.saveToken(token)
        Mockito.verify(preferences).saveToken(token)
    }

    @Test
    fun `successfully Get User Token`() = runTest {
        val dummyToken = "token"
        val expectedResult = flowOf(dummyToken)

        Mockito.`when`(preferences.getToken()).thenReturn(expectedResult)
        val actualResult = loginViewModel.getToken().getOrAwaitValue()

        assertEquals(dummyToken, actualResult)
        Mockito.verify(preferences).getToken()
    }
}
