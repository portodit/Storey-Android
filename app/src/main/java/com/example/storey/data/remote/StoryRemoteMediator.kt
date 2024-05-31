package com.example.storey.data.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.storey.data.local.StoryDatabase
import com.example.storey.data.model.ListStoryItem
import com.example.storey.data.model.RemoteKeys

@OptIn(ExperimentalPagingApi::class)
class StoryRemoteMediator(
    private val database: StoryDatabase,
    private val apiService: ApiService,
) : RemoteMediator<Int, ListStoryItem>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ListStoryItem>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> INITIAL_PAGE_INDEX
            LoadType.PREPEND -> getRemoteKeyForFirstItem(state)
            LoadType.APPEND -> getRemoteKeyForLastItem(state)
            else -> return MediatorResult.Success(endOfPaginationReached = false)
        }

        val responseData = try {
            page?.let {
                apiService.getStories(page = it, size = state.config.pageSize).listStory
            }
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }

        database.apply {
            withTransaction {
                page?.let {
                    if (loadType == LoadType.REFRESH) {
                        remoteKeysDao().deleteRemoteKeys()
                        storyDao().deleteStories()
                    }

                    val keys = responseData?.map {
                        RemoteKeys(
                            id = it.id,
                            previousKey = if (page == INITIAL_PAGE_INDEX) null else (page - 1),
                            nextKey = if (responseData.isEmpty()) null else (page + 1)
                        )
                    }

                    remoteKeysDao().insertRemoteKeys(keys ?: listOf())
                    storyDao().insertStories(responseData ?: listOf())
                }
            }
        }

        return MediatorResult.Success(endOfPaginationReached = responseData?.isEmpty() == true)
    }

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, ListStoryItem>) =
        state.pages.firstOrNull { it.data.isNotEmpty() }
            ?.data?.firstOrNull()
            ?.let { database.remoteKeysDao().getRemoteKeys(it.id) }?.previousKey


    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, ListStoryItem>) =
        state.pages.lastOrNull { it.data.isNotEmpty() }
            ?.data?.lastOrNull()
            ?.let { database.remoteKeysDao().getRemoteKeys(it.id) }?.nextKey

    private companion object {
        private const val INITIAL_PAGE_INDEX = 1
    }
}
