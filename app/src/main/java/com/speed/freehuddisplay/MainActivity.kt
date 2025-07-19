package com.speed.freehuddisplay

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.speed.freehuddisplay.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var speedViewModel: SpeedViewModel

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted.
                startLocationUpdates()
            }

            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.
                startLocationUpdates()
            }

            else -> {
                // No location access granted.
                binding.speedText.text = "Location permission required"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep screen on and set full brightness
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.attributes.screenBrightness = 1.0f

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide system UI for fullscreen experience
        hideSystemUI()

        speedViewModel = ViewModelProvider(this)[SpeedViewModel::class.java]

        setupObservers()
        checkLocationPermissions()

        // Click listener to toggle between units
        binding.speedText.setOnClickListener {
            speedViewModel.toggleUnit()
        }

        binding.speedText.setOnLongClickListener {
            exitApp()
            true
        }

        binding.root.setOnClickListener {
            exitApp()
            true
        }
    }

    private fun setupObservers() {
        speedViewModel.speed.observe(this) { speed ->
            binding.speedText.text = speed.toString()
        }

        speedViewModel.unit.observe(this) { unit ->
            binding.unitText.text = unit
        }

        speedViewModel.accuracy.observe(this) { accuracy ->
            binding.accuracyText.text = "±${accuracy}m"
        }
    }

    private fun checkLocationPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                startLocationUpdates()
            }

            else -> {
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun startLocationUpdates() {
        speedViewModel.startLocationUpdates(this)
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
    }

    override fun onDestroy() {
        super.onDestroy()
        speedViewModel.stopLocationUpdates()
    }

    private fun exitApp() {
        speedViewModel.stopLocationUpdates()
        finish()
    }
}