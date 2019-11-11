package com.stharzun.placepicker

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.button_get).setOnClickListener {
            startActivityForResult(Intent(this, PlacePicker::class.java), 99)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 99) {
            if (resultCode == Activity.RESULT_OK) {
                val lat = data?.getStringExtra(PlacePicker.Latitude)
                val lng = data?.getStringExtra(PlacePicker.Longitude)
                findViewById<TextView>(R.id.return_result).text = "$lat \n $lng"
            }
        }
    }
}
