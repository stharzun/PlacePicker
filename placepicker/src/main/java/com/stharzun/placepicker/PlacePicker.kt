package com.stharzun.placepicker

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import java.io.IOException

class PlacePicker : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        const val Latitude = "com.stharzun.placepicker.latitude"
        const val Longitude = "com.stharzun.placepicker.longitude"
    }

    private lateinit var mMap: GoogleMap
    private var onCameraIdleListener: GoogleMap.OnCameraIdleListener? = null
    private lateinit var selectThisLocation: TextView
    private lateinit var markerPosition: TextView
    private var sLat = ""
    private var sLng = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_picker)

        selectThisLocation = findViewById(R.id.select_this_location)
        markerPosition = findViewById(R.id.marker_position)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        configureCameraIdle()

        selectThisLocation.setOnClickListener {
            val returnIntent = Intent()
            returnIntent.putExtra(Latitude, sLat)
            returnIntent.putExtra(Longitude, sLng)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
    }

    private fun configureCameraIdle() {
        onCameraIdleListener = GoogleMap.OnCameraIdleListener {
            val latLng = mMap.cameraPosition.target
            markerPosition.text = getTitle(latLng.latitude, latLng.longitude)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnCameraIdleListener(onCameraIdleListener)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        LocationServices.getFusedLocationProviderClient(this).flushLocations()
        LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnCompleteListener(
            this
        ) { task ->
            if (task.isSuccessful) {
                val loc = LatLng(task.result?.latitude!!, task.result?.longitude!!)
                mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        loc,
                        15.0f
                    )
                )
            }
        }
    }

    private fun getTitle(lat: Double, long: Double): String {
        sLat = lat.toString()
        sLng = long.toString()

        var sLoc: String
        sLoc = "$lat , $long"
        val geoCoder = Geocoder(this)
        try {
            val addressList = geoCoder.getFromLocation(lat, long, 1)
            if (addressList != null && addressList.size > 0) {
                val locality = addressList[0].getAddressLine(0)
                val country = addressList[0].countryName
                if (locality.isNotEmpty() && country.isNotEmpty())
                    sLoc = "$sLoc \n $locality"
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return sLoc
    }

    override fun onBackPressed() {
        val returnIntent = Intent()
        setResult(Activity.RESULT_CANCELED, returnIntent)
        finish()
    }

}
