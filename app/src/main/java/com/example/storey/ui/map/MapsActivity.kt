package com.example.storey.ui.map

import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.os.Bundle
import android.os.StrictMode
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.storey.R
import com.example.storey.data.model.ListStoryItem
import com.example.storey.databinding.ActivityMapsBinding
import com.example.storey.databinding.ItemMapTooltipBinding
import com.example.storey.ui.detail.DetailActivity
import com.example.storey.ui.factory.ViewModelFactory
import com.example.storey.utils.Result
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.InfoWindowAdapter {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private val viewModel by viewModels<MapsViewModel> {
        ViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        with(mMap) {
            moveCamera(CameraUpdateFactory.newLatLng(LatLng(-6.200000, 106.816666)))
            mapType = GoogleMap.MAP_TYPE_TERRAIN

            uiSettings.apply {
                isZoomControlsEnabled = true
                isIndoorLevelPickerEnabled = true
                isCompassEnabled = true
                isMapToolbarEnabled = true
            }

            setOnInfoWindowClickListener {
                val iDetail = Intent(this@MapsActivity, DetailActivity::class.java)
                iDetail.putExtra(DetailActivity.EXTRA_STORY, it.tag as ListStoryItem)
                startActivity(iDetail)
            }

            setInfoWindowAdapter(this@MapsActivity)
        }

        observeViewModel()
        setListeners()
    }

    private fun observeViewModel() {
        viewModel.getLocationStories()
            .observe(this) { result ->
                when (result) {
                    is Result.Success -> {
                        result.data.listStory.forEach { story ->
                            mMap.addMarker(
                                MarkerOptions().position(
                                    LatLng(
                                        story.lat,
                                        story.lon
                                    )
                                )
                            )?.tag = story
                        }
                    }

                    is Result.Loading -> {}
                    is Result.Error -> {}
                }
            }
    }

    private fun setListeners() {
        binding.toolbar.apply {
            setNavigationOnClickListener { finish() }
            inflateMenu(R.menu.map_menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.map_style_normal -> {
                        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                    }

                    R.id.map_style_satellite -> {
                        mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    }

                    R.id.map_style_terrain -> {
                        mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                    }

                    R.id.map_style_hybrid -> {
                        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                    }

                    else -> {
                        super.onOptionsItemSelected(it)
                    }
                }
                return@setOnMenuItemClickListener true
            }
        }
    }

    override fun getInfoContents(p0: Marker) = null

    override fun getInfoWindow(p0: Marker): View {
        val itemMapTooltipBinding =
            ItemMapTooltipBinding.inflate(LayoutInflater.from(this@MapsActivity))
        val story = p0.tag as ListStoryItem

        with(itemMapTooltipBinding) {
            ivItemPhoto.setImageBitmap(urlBitmapConverter(story.photoUrl))
            tvItemName.text = story.name

            try {
                @Suppress("DEPRECATION") val address =
                    Geocoder(this@MapsActivity).getFromLocation(story.lat, story.lon, 1)
                        ?.firstOrNull()
                        ?.getAddressLine(0)
                tvLocation.text = address ?: ""
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return itemMapTooltipBinding.root
    }

    private fun urlBitmapConverter(urlString: String) = try {
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())

        val url = URL(urlString)
        val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
        connection.doInput = true
        connection.connect()

        val input: InputStream = connection.inputStream
        BitmapFactory.decodeStream(input)
    } catch (e: IOException) {
        BitmapFactory.decodeResource(this.resources, R.drawable.ic_image)
    }
}