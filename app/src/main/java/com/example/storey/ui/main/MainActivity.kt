package com.example.storey.ui.main

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storey.R
import com.example.storey.databinding.ActivityMainBinding
import com.example.storey.ui.adapter.LoadingStateAdapter
import com.example.storey.ui.adapter.StoryAdapter
import com.example.storey.ui.addstory.AddStoryActivity
import com.example.storey.ui.auth.AuthActivity
import com.example.storey.ui.detail.DetailActivity
import com.example.storey.ui.factory.ViewModelFactory
import com.example.storey.ui.map.MapsActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel by viewModels<MainViewModel> {
        ViewModelFactory(this)
    }

    private val storyAdapter = StoryAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeViewModel()
        setListeners()
        setView()
    }

    private fun observeViewModel() {
        mainViewModel.apply {
            storyList.observe(this@MainActivity) {
                storyAdapter.submitData(lifecycle, it)
            }
        }
    }

    private fun setListeners() {
        binding.apply {
            toolbar.apply {
                inflateMenu(R.menu.main_menu)
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.menu_map -> {
                            startActivity(Intent(this@MainActivity, MapsActivity::class.java))
                        }

                        R.id.menu_change_language -> {
                            startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                        }

                        R.id.menu_logout -> {
                            val iLogin = Intent(this@MainActivity, AuthActivity::class.java)
                            mainViewModel.clearPreferences()
                            finishAffinity()
                            startActivity(iLogin)
                        }
                    }

                    return@setOnMenuItemClickListener true
                }
            }

            btnAdd.setOnClickListener {
                startActivity(Intent(this@MainActivity, AddStoryActivity::class.java))
            }
        }
    }

    private fun setView() {
        storyAdapter.onStoryClick = { story ->
            val iDetail = Intent(this@MainActivity, DetailActivity::class.java)
            iDetail.putExtra(DetailActivity.EXTRA_STORY, story)
            startActivity(iDetail)
        }

        binding.rvStory.apply {
            adapter = storyAdapter.withLoadStateFooter(
                footer = LoadingStateAdapter {
                    storyAdapter.retry()
                }
            )
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }
}