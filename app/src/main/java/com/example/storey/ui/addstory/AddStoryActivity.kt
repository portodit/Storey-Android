package com.example.storey.ui.addstory

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.location.Geocoder
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.example.storey.databinding.ActivityAddStoryBinding
import com.example.storey.ui.factory.ViewModelFactory
import com.example.storey.ui.main.MainActivity
import com.example.storey.utils.Result
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class AddStoryActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var binding: ActivityAddStoryBinding
    private val addStoryViewModel by viewModels<AddStoryViewModel> {
        ViewModelFactory(this)
    }

    private lateinit var imagePathLocation: String

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                addStoryViewModel.imageFile.postValue(uriToFileConverter(it))
            }
        }

    val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val file = File(imagePathLocation)
            file.let { image ->
                val bitmap = BitmapFactory.decodeFile(image.path)
                addStoryViewModel.imageFile.postValue(rotateCompressImage(bitmap, file))
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    getMyLastLocation()
                }

                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    getMyLastLocation()
                }

                else -> {
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        observeViewModel()
        setListeners()
    }

    private fun observeViewModel() {
        addStoryViewModel.apply {
            imageFile.observe(this@AddStoryActivity) {
                binding.ivStory.setImageBitmap(BitmapFactory.decodeFile(it.path))

            }
            isLoading.observe(this@AddStoryActivity, ::showLoading)
            errorMessage.observe(this@AddStoryActivity, ::showToast)
            areBothCoordinatesFilled.observe(this@AddStoryActivity) {
                showTvLocation(it)

                latitude.value?.let { lat ->
                    longitude.value?.let { lon ->
                        try {
                            @Suppress("DEPRECATION") val address =
                                Geocoder(this@AddStoryActivity).getFromLocation(lat, lon, 1)
                                    ?.firstOrNull()
                                    ?.getAddressLine(0)
                            binding.tvLocation.setText(address ?: "")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun setListeners() {
        with(binding) {
            toolbar.setNavigationOnClickListener { finish() }
            btnCamera.setOnClickListener {
                if (checkCameraPermission()) {
                    if (checkImagePermission()) {
                        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

                        val photoFile = File.createTempFile(
                            SimpleDateFormat(
                                "dd-MMM-yyyy",
                                Locale.US
                            ).format(System.currentTimeMillis()),
                            ".jpg",
                            storageDir
                        ).also {
                            imagePathLocation = it.absolutePath
                        }
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        intent.resolveActivity(packageManager)
                        intent.putExtra(
                            MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(
                                this@AddStoryActivity,
                                packageName,
                                photoFile
                            )
                        )
                        cameraLauncher.launch(intent)
                    } else {
                        ActivityCompat.requestPermissions(
                            this@AddStoryActivity,
                            REQUIRED_CAMERA_PERMISSION,
                            REQUEST_CODE_PERMISSIONS
                        )
                    }
                } else {
                    requestCameraPermission()
                }
            }

            btnGallery.setOnClickListener {
                galleryLauncher.launch("image/*")
            }

            switchLocation.setOnCheckedChangeListener { compoundButton, _ ->
                if (compoundButton.isChecked) {
                    getMyLastLocation()
                } else {
                    addStoryViewModel.apply {
                        latitude.postValue(null)
                        longitude.postValue(null)
                    }
                }
            }

            btnUpload.setOnClickListener {
                when {
                    addStoryViewModel.imageFile.value == null -> {
                        showToast("Invalid Image")
                    }

                    binding.edDesc.text.isNullOrEmpty() -> {
                        showToast("Invalid Description")
                    }

                    else -> {
                        addStoryViewModel.uploadStory(
                            binding.edDesc.text.toString(),
                            reduceFileSize(addStoryViewModel.imageFile.value!!)
                        ).observe(this@AddStoryActivity) { result ->
                            when (result) {
                                is Result.Loading -> addStoryViewModel.isLoading.postValue(true)
                                is Result.Success -> {
                                    addStoryViewModel.isLoading.postValue(false)
                                    showToast(result.data.message.toString())
                                    val iMain =
                                        Intent(this@AddStoryActivity, MainActivity::class.java)
                                    finishAffinity()
                                    startActivity(iMain)
                                }

                                is Result.Error -> {
                                    addStoryViewModel.isLoading.postValue(false)
                                    addStoryViewModel.errorMessage.postValue(result.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun rotateCompressImage(bitmap: Bitmap, file: File): File {
        val exif = ExifInterface(file.path)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        val rotationAngle = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

        val matrix = Matrix().apply { setRotate(rotationAngle) }
        val rotatedBitmap = Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )

        val outputStream = FileOutputStream(file)
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.close()

        return file
    }

    private fun uriToFileConverter(uri: Uri): File {
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        val tempFile = File.createTempFile(
            SimpleDateFormat("dd-MMM-yyyy", Locale.US).format(System.currentTimeMillis()),
            ".jpg",
            storageDir
        ).also { imagePathLocation = it.absolutePath }

        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        return tempFile
    }

    private fun checkCameraPermission() = REQUIRED_CAMERA_PERMISSION.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            REQUIRED_CAMERA_PERMISSION,
            REQUEST_CODE_PERMISSIONS
        )
    }

    private fun reduceFileSize(file: File): File {
        val maxFileSize = 1000000
        val bitmap = BitmapFactory.decodeFile(file.path)
        var compressQuality = 100
        var outputStream: FileOutputStream? = null

        try {
            while (compressQuality > 0) {
                val bmpStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
                val bmpPicByteArray = bmpStream.toByteArray()

                if (bmpPicByteArray.size <= maxFileSize) {
                    outputStream = FileOutputStream(file)
                    outputStream.write(bmpPicByteArray)
                    outputStream.flush()
                    break
                }

                compressQuality -= 5
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            outputStream?.close()
        }

        return file
    }

    private fun checkImagePermission() = REQUIRED_CAMERA_PERMISSION.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun getMyLastLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    addStoryViewModel.apply {
                        latitude.postValue(it.latitude)
                        longitude.postValue(it.longitude)
                    }
                } ?: run {
                    showToast("Location is not found. Try Again")
                }
            }
        } else {
            requestLocationPermissions()
        }
    }

    private fun checkPermission(permission: String) =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED


    private fun requestLocationPermissions() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            progressbar.isVisible = isLoading
            btnUpload.isVisible = !isLoading
            btnGallery.isEnabled = !isLoading
            btnCamera.isEnabled = !isLoading
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showTvLocation(isLocationFilled: Boolean) {
        binding.tvLocationLayout.isVisible = isLocationFilled
    }

    companion object {
        private val REQUIRED_CAMERA_PERMISSION = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 100
    }
}