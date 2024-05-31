package com.example.storey.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.recyclerview.widget.ListUpdateCallback
import com.example.storey.data.model.ListStoryItem
import com.example.storey.data.repository.MainRepository
import com.example.storey.ui.adapter.StoryAdapter
import com.example.storey.ui.main.MainViewModel
import com.example.storey.utils.CoroutineTestRule
import com.example.storey.utils.LiveDataTestUtils.getOrAwaitValue
import com.example.storey.utils.PagedTestDataSource
import com.example.storey.utils.SettingsPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@ExperimentalPagingApi
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    private lateinit var mainViewModel: MainViewModel

    @Mock
    private lateinit var repository: MainRepository

    @Mock
    private lateinit var preferences: SettingsPreferences

    @Before
    @Throws(Exception::class)
    fun setUp() {
        mainViewModel = MainViewModel(repository, preferences)
    }

    @Test
    fun `successfully Get Stories and Not Null`() = runTest {
        val storyList = mutableListOf<ListStoryItem>().apply {
            for (i in 0..10) {
                add(
                    ListStoryItem(
                        id = "id",
                        photoUrl = "photoUrl",
                        createdAt = "createdAt",
                        name = "name",
                        description = "description",
                        lon = 0.0,
                        lat = 0.0
                    )
                )
            }
        }

        val listStory = MutableLiveData<PagingData<ListStoryItem>>().apply {
            value = PagedTestDataSource.snapshot(storyList)
        }

        Mockito.`when`(repository.getStories()).thenReturn(listStory)

        val actualStories =
            mainViewModel.storyList.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = listUpdateCallback,
            mainDispatcher = coroutinesTestRule.testDispatcher,
            workerDispatcher = coroutinesTestRule.testDispatcher
        )

        differ.submitData(actualStories)

        verify(repository).getStories()
        assertNotNull(differ.snapshot())
        assertEquals(storyList.size, differ.snapshot().size)
        assertEquals(storyList[0], differ.snapshot()[0])
    }

    @Test
    fun `failed to Get Stories but Not Null`() = runTest {
        val storyList: MutableList<ListStoryItem> = mutableListOf()
        val listStory = MutableLiveData<PagingData<ListStoryItem>>().apply {
            value = PagedTestDataSource.snapshot(storyList)
        }

        Mockito.`when`(repository.getStories()).thenReturn(listStory)

        val actualStories =
            mainViewModel.storyList.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = listUpdateCallback,
            mainDispatcher = coroutinesTestRule.testDispatcher,
            workerDispatcher = coroutinesTestRule.testDispatcher
        )

        differ.submitData(actualStories)

        verify(repository).getStories()
        assertNotNull(differ.snapshot())
        assertEquals(0, differ.snapshot().size)
    }

    @Test
    fun `successfully User Clear Preferences`() = runTest {
        mainViewModel.clearPreferences()
        verify(preferences).clearPreferences()
    }

    private val listUpdateCallback = object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
    }
}