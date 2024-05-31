package com.example.storey.ui.detail

import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.storey.data.model.ListStoryItem
import com.example.storey.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            toolbar.setNavigationOnClickListener { finish() }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(EXTRA_STORY)
            } else {
                intent.getParcelableExtra(EXTRA_STORY, ListStoryItem::class.java)
            }?.let {
                Glide.with(root)
                    .load(it.photoUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(ivStory)

                tvUsernameStory.text = it.name
                tvDesc.text = it.description

                tvLocation.isVisible = it.lon != 0.0 && it.lat != 0.0
                if (it.lon != 0.0 && it.lat != 0.0) {
                    try {
                        @Suppress("DEPRECATION") val address =
                            Geocoder(this@DetailActivity).getFromLocation(it.lat, it.lon, 1)
                                ?.firstOrNull()
                                ?.getAddressLine(0)
                        tvLocation.text = address ?: ""
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_STORY = "extra_story"
    }
}