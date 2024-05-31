package com.example.storey.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.example.storey.data.model.StoriesResponse
import com.example.storey.data.repository.MainRepository
import com.example.storey.ui.map.MapsViewModel
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
class MapsViewModelTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var repository: MainRepository

    private lateinit var mapsViewModel: MapsViewModel

    @Before
    @Throws(Exception::class)
    fun setUp() {
        mapsViewModel = MapsViewModel(repository)
    }

    @Test
    fun `successfully Get Stories With Location`() = runTest {
        val storiesResponse = StoriesResponse(arrayListOf(), false, "success")

        val expectedResponse = MutableLiveData<Result<StoriesResponse>>().apply {
            value = Result.Success(storiesResponse)
        }

        Mockito.`when`(repository.getLocationStories()).thenReturn(expectedResponse)

        mapsViewModel.getLocationStories().getOrAwaitValue().let { result ->
            Assert.assertTrue(result is Result.Success)
            Assert.assertFalse(result is Result.Error)

            if (result is Result.Success) {
                Assert.assertNotNull(result.data)
                assertEquals(storiesResponse, result.data)
            }
        }

        Mockito.verify(repository).getLocationStories()
    }

    @Test
    fun `failed To Get Stories With Location`() = runTest {
        val errorResponse = "error"

        val expectedResponse = MutableLiveData<Result<StoriesResponse>>()
        expectedResponse.value = Result.Error(errorResponse)

        Mockito.`when`(repository.getLocationStories()).thenReturn(expectedResponse)

        mapsViewModel.getLocationStories().getOrAwaitValue().let { result ->
            Assert.assertTrue(result is Result.Error)
            Assert.assertFalse(result is Result.Success)

            if (result is Result.Error) {
                Assert.assertNotNull(result.error)
            }
        }

        Mockito.verify(repository).getLocationStories()
    }
}