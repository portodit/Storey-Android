package com.example.storey.ui.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.lifecycle.asFlow
import com.bumptech.glide.Glide
import com.example.storey.R
import com.example.storey.data.local.StoryDatabase
import com.example.storey.data.model.ListStoryItem
import com.example.storey.data.remote.ApiConfig
import com.example.storey.data.repository.MainRepository
import com.example.storey.ui.detail.DetailActivity
import com.example.storey.utils.Result
import com.example.storey.utils.SettingsPreferences
import com.example.storey.utils.dataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutionException

internal class StackRemoteViewsFactory(private val mContext: Context) :
    RemoteViewsService.RemoteViewsFactory {

    private lateinit var apiRepository: MainRepository
    private val listStoryItem = ArrayList<ListStoryItem>()

    override fun onCreate() {
        val preferences = SettingsPreferences.getInstance(mContext.dataStore)
        val storyDatabase = StoryDatabase.getInstance(mContext)
        apiRepository = MainRepository(ApiConfig.getApiService(runBlocking {
            preferences.getToken().first()
        }), storyDatabase)
    }

    override fun onDataSetChanged() {
        runBlocking(Dispatchers.IO) {
            try {
                apiRepository.getStories(50).asFlow().collect {
                    withContext(Dispatchers.Main) {
                        when (it) {
                            is Result.Loading -> {}

                            is Result.Success -> {
                                listStoryItem.clear()
                                listStoryItem.addAll(it.data.listStory.take(5))
                            }

                            is Result.Error -> {}
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {}

    override fun getCount(): Int = listStoryItem.size

    override fun getViewAt(position: Int): RemoteViews {
        val remoteViews = RemoteViews(mContext.packageName, R.layout.image_item)
        if (listStoryItem.isNotEmpty()) {
            val urlImg = listStoryItem[position].photoUrl
            try {
                val bitmap = Glide.with(mContext).asBitmap().load(urlImg).submit().get()
                remoteViews.setImageViewBitmap(R.id.imageView, bitmap)
            } catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        val fillInIntent = Intent()
        fillInIntent.putExtra(DetailActivity.EXTRA_STORY, listStoryItem[position])
        remoteViews.setOnClickFillInIntent(R.id.imageView, fillInIntent)
        return remoteViews
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(i: Int): Long = 0

    override fun hasStableIds(): Boolean = false
}
