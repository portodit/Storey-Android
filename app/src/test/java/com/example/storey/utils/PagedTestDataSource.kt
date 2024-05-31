package com.example.storey.utils

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.storey.data.model.ListStoryItem

class PagedTestDataSource : PagingSource<Int, LiveData<List<ListStoryItem>>>() {
    override fun getRefreshKey(state: PagingState<Int, LiveData<List<ListStoryItem>>>): Int {
        return 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LiveData<List<ListStoryItem>>> {
        return LoadResult.Page(emptyList(), 0, 1)
    }

    companion object {
        fun snapshot(story: List<ListStoryItem>): PagingData<ListStoryItem> {
            return PagingData.from(story)
        }
    }
}