package com.speed.freehuddisplay

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.*
import kotlin.math.roundToInt

class SpeedViewModel : ViewModel() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private val _speed = MutableLiveData<Int>()
    val speed: LiveData<Int> = _speed

    private val _unit = MutableLiveData<String>()
    val unit: LiveData<String> = _unit

    private val _accuracy = MutableLiveData<Int>()
    val accuracy: LiveData<Int> = _accuracy

    private var isKmh = true

    init {
        _speed.value = 0
        _unit.value = "km/h"
        _accuracy.value = 0
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 1000
        ).apply {
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(false)
            setMinUpdateIntervalMillis(500)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    updateSpeed(location)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun updateSpeed(location: Location) {
        val speedMps = if (location.hasSpeed()) location.speed else 0f
        val accuracy = if (location.hasAccuracy()) location.accuracy.roundToInt() else 0

        val displaySpeed = if (isKmh) {
            (speedMps * 3.6).roundToInt() // Convert m/s to km/h
        } else {
            (speedMps * 2.237).roundToInt() // Convert m/s to mph
        }

        _speed.value = displaySpeed
        _accuracy.value = accuracy
    }

    fun toggleUnit() {
        isKmh = !isKmh
        _unit.value = if (isKmh) "km/h" else "mph"

        // Recalculate speed with new unit
        val currentSpeed = _speed.value ?: 0
        if (isKmh) {
            // Was mph, now km/h
            _speed.value = (currentSpeed * 1.609).roundToInt()
        } else {
            // Was km/h, now mph
            _speed.value = (currentSpeed * 0.621).roundToInt()
        }
    }


    fun stopLocationUpdates() {
        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}