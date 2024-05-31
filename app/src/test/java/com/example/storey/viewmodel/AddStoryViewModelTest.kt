package com.example.storey.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.example.storey.data.model.UploadResponse
import com.example.storey.data.repository.MainRepository
import com.example.storey.ui.addstory.AddStoryViewModel
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
import java.io.File

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AddStoryViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var repository: MainRepository

    private lateinit var addStoryViewModel: AddStoryViewModel

    @Before
    @Throws(Exception::class)
    fun setUp() {
        addStoryViewModel = AddStoryViewModel(repository)
    }

    @Test
    fun `successfully Upload Story`() = runTest {
        val file = File("file")
        val desc = "desc"
        val uploadResponse = UploadResponse(
            error = false,
            message = "success"
        )

        val expectedResponse = MutableLiveData<Result<UploadResponse>>().apply {
            value = Result.Success(uploadResponse)
        }

        Mockito.`when`(
            repository.uploadStory(
                file,
                desc
            )
        ).thenReturn(expectedResponse)

        addStoryViewModel.uploadStory(desc, file)
            .getOrAwaitValue().let { result ->
                Assert.assertTrue(result is Result.Success)
                Assert.assertFalse(result is Result.Error)

                if (result is Result.Success) {
                    Assert.assertNotNull(result.data)
                    assertEquals(uploadResponse, result.data)
                }
            }

        Mockito.verify(repository).uploadStory(
            file,
            desc
        )
    }

    @Test
    fun `failed To Upload Story`() = runTest {
        val file = File("file")
        val desc = "desc"
        val errorResponse = "error"

        val expectedResponse = MutableLiveData<Result<UploadResponse>>().apply {
            value = Result.Error(errorResponse)
        }

        Mockito.`when`(
            repository.uploadStory(
                file,
                desc
            )
        ).thenReturn(expectedResponse)

        addStoryViewModel.uploadStory(desc, file)
            .getOrAwaitValue().let { result ->
                Assert.assertTrue(result is Result.Error)
                Assert.assertFalse(result is Result.Success)

                if (result is Result.Error) {
                    Assert.assertNotNull(result.error)
                }
            }

        Mockito.verify(repository).uploadStory(
            file,
            desc
        )
    }
}