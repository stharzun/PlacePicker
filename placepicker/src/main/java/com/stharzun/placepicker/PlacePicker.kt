package com.stharzun.placepicker

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import java.io.IOException
import java.lang.Exception

class PlacePicker : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        const val Latitude = "com.stharzun.placepicker.latitude"
        const val Longitude = "com.stharzun.placepicker.longitude"
        const val MY_PERMISSIONS_REQUEST_LOCATION = 99

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
        markerPosition.visibility = View.GONE

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

        findViewById<ImageView>(R.id.back).setOnClickListener {
            onBackPressed()
        }
    }

    private fun configureCameraIdle() {
        onCameraIdleListener = GoogleMap.OnCameraIdleListener {
            val latLng = mMap.cameraPosition.target
            markerPosition.visibility = View.VISIBLE
            markerPosition.text = getTitle(latLng.latitude, latLng.longitude)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnCameraIdleListener(onCameraIdleListener)

        //Initialize Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                moveToCurrentLocation()
            } else {
                checkLocationPermission()
            }
        } else {
            moveToCurrentLocation()
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                AlertDialog.Builder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app need the Location permission, please accept to use location functionality")
                    .setPositiveButton("OK") { _, _ ->
                        //Prompt the user once explanation has been shown
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            MY_PERMISSIONS_REQUEST_LOCATION
                        )
                    }
                    .create()
                    .show()
            } else {
                //No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_LOCATION
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

    private fun moveToCurrentLocation() {
        LocationServices.getFusedLocationProviderClient(this).flushLocations()
        LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnCompleteListener(
            this
        ) { task ->
            if (task.isSuccessful) {
                try {
                    val loc = LatLng(task.result?.latitude!!, task.result?.longitude!!)
                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(loc, 18.0f)
                    )
                }catch (e: Exception){
                    e.printStackTrace()
                    //setting default lat long to Kathmandu
                    val loc = LatLng( 27.700769, 85.300140)
                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(loc, 18.0f)
                    )
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        moveToCurrentLocation()
                    }
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


}
